#!/bin/bash
# Bash script to run all tests with database setup

set -e

SKIP_FRONTEND=false
BACKEND_ONLY=false
FRONTEND_ONLY=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-frontend)
            SKIP_FRONTEND=true
            shift
            ;;
        --backend-only)
            BACKEND_ONLY=true
            shift
            ;;
        --frontend-only)
            FRONTEND_ONLY=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--backend-only|--frontend-only|--skip-frontend]"
            exit 1
            ;;
    esac
done

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  Quote REST API - Test Runner"
echo "═══════════════════════════════════════════════════════"
echo ""

# Get project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Function to check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        echo "✗ Docker is not running. Please start Docker and try again."
        exit 1
    fi
}

# Function to start test database
start_test_database() {
    echo "Starting PostgreSQL test database..."
    
    # Check if container already exists
    if docker ps -a --format "{{.Names}}" | grep -q "^quote-test-db$"; then
        echo "Container 'quote-test-db' already exists. Starting it..."
        docker start quote-test-db >/dev/null
    else
        echo "Creating new PostgreSQL test container..."
        docker run -d --name quote-test-db \
          -e POSTGRES_DB=testdb \
          -e POSTGRES_USER=testuser \
          -e POSTGRES_PASSWORD=testpass \
          -p 5434:5432 \
          postgres:15-alpine >/dev/null
    fi
    
    # Wait for PostgreSQL to be ready
    echo "Waiting for PostgreSQL to be ready..."
    sleep 3
    
    MAX_ATTEMPTS=30
    ATTEMPT=0
    READY=false
    
    while [ "$READY" = false ] && [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        ATTEMPT=$((ATTEMPT + 1))
        if docker exec quote-test-db pg_isready -U testuser -d testdb >/dev/null 2>&1; then
            READY=true
        else
            sleep 1
        fi
    done
    
    if [ "$READY" = true ]; then
        echo "✓ PostgreSQL test database is ready"
        return 0
    else
        echo "✗ Failed to start PostgreSQL test database"
        return 1
    fi
}

# Check Docker is running
check_docker

# Start test database (unless frontend-only)
if [ "$FRONTEND_ONLY" = false ]; then
    if ! start_test_database; then
        exit 1
    fi
    echo ""
fi

# Run backend tests
if [ "$FRONTEND_ONLY" = false ]; then
    echo "───────────────────────────────────────────────────────"
    echo "  Running Backend Tests"
    echo "───────────────────────────────────────────────────────"
    echo ""
    
    cd "$PROJECT_ROOT"
    
    if [ "$BACKEND_ONLY" = true ]; then
        mvn clean test -pl backend
    else
        mvn clean test
    fi
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ Backend tests completed successfully"
    else
        echo ""
        echo "✗ Backend tests failed"
        exit 1
    fi
fi

# Run frontend tests
if [ "$BACKEND_ONLY" = false ] && [ "$SKIP_FRONTEND" = false ]; then
    echo ""
    echo "───────────────────────────────────────────────────────"
    echo "  Running Frontend Tests"
    echo "───────────────────────────────────────────────────────"
    echo ""
    
    cd "$PROJECT_ROOT/frontend"
    
    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        echo "Installing npm dependencies..."
        npm install
        if [ $? -ne 0 ]; then
            echo "✗ npm install failed"
            exit 1
        fi
    fi
    
    npm run test:ci
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ Frontend tests completed successfully"
    else
        echo ""
        echo "✗ Frontend tests failed"
        exit 1
    fi
fi

# Cleanup test database
if [ "$FRONTEND_ONLY" = false ]; then
    echo ""
    echo "───────────────────────────────────────────────────────"
    echo "  Cleaning Up Test Database"
    echo "───────────────────────────────────────────────────────"
    echo ""
    
    echo "Stopping and removing test database container..."
    
    # Stop and remove container with volumes
    docker stop quote-test-db >/dev/null 2>&1
    docker rm -v quote-test-db >/dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        echo "✓ Test database cleaned up successfully"
    else
        echo "⚠ Warning: Failed to cleanup test database (may not exist)"
    fi
fi

# Summary
echo ""
echo "═══════════════════════════════════════════════════════"
echo "  All Tests Completed Successfully! ✓"
echo "═══════════════════════════════════════════════════════"
echo ""

if [ "$FRONTEND_ONLY" = false ]; then
    echo "Backend Coverage Report:"
    echo "  file://$PROJECT_ROOT/backend/target/site/jacoco/index.html"
fi

if [ "$BACKEND_ONLY" = false ]; then
    echo "Frontend Coverage Report:"
    echo "  file://$PROJECT_ROOT/frontend/coverage/quote-frontend/index.html"
fi

echo ""
