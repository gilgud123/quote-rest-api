#!/bin/bash
# Start frontend development environment with Docker services

echo "🚀 Starting Quote REST API - Frontend Development Environment"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Start all services with frontend-specific Docker Compose
echo "📦 Starting PostgreSQL, Keycloak, and Backend services..."
docker-compose -f docker-compose-frontend.yml up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
echo "   This may take 60-90 seconds for Keycloak initialization..."

# Wait for backend health check
max_attempts=36
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if docker inspect --format='{{.State.Health.Status}}' quote-backend-frontend 2>/dev/null | grep -q "healthy"; then
        break
    fi
    echo "   ⏳ Waiting for backend to be healthy... ($attempt/$max_attempts)"
    sleep 5
    attempt=$((attempt + 1))
done

if [ $attempt -eq $max_attempts ]; then
    echo "⚠️  Backend did not become healthy within timeout"
    echo "   Check logs with: docker-compose -f docker-compose-frontend.yml logs backend"
fi

echo "✅ Services are ready!"
echo ""
echo "📊 Service Status:"
docker-compose -f docker-compose-frontend.yml ps
echo ""

# Start Angular development server
echo "🎨 Preparing Angular frontend..."
cd frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing npm dependencies (first time setup)..."
    npm install
fi

echo ""
echo "✅ Development environment is ready!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 Access Points:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   Frontend:  http://localhost:4200"
echo "   Backend:   http://localhost:8080"
echo "   Swagger:   http://localhost:8080/swagger-ui.html"
echo "   Keycloak:  http://localhost:9090 (admin/admin)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "👤 Test Users:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   frontend-user  / password  (USER role)"
echo "   frontend-admin / password  (ADMIN role)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 Starting Angular dev server..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   Press Ctrl+C to stop the frontend"
echo "   Use './scripts/stop-frontend-dev.sh' to stop backend services"
echo ""

# Start Angular dev server (foreground)
npm start

# Cleanup message (shown after Ctrl+C)
echo ""
echo "🛑 Frontend stopped. Backend services still running."
echo "   To stop all services: ./scripts/stop-frontend-dev.sh"
