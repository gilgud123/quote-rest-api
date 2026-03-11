#!/bin/bash
# Wait for services to be ready before running tests
# Usage: ./wait-for-services.sh [service1] [service2] ...

set -e

TIMEOUT=120
INTERVAL=5

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

wait_for_url() {
    local url=$1
    local service_name=$2
    local elapsed=0
    
    echo -e "${YELLOW}Waiting for ${service_name} at ${url}...${NC}"
    
    while [ $elapsed -lt $TIMEOUT ]; do
        if curl -s -f -o /dev/null "${url}"; then
            echo -e "${GREEN}✓ ${service_name} is ready${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}  Waiting... (${elapsed}s/${TIMEOUT}s)${NC}"
        sleep $INTERVAL
        elapsed=$((elapsed + INTERVAL))
    done
    
    echo -e "${RED}✗ ${service_name} failed to become ready within ${TIMEOUT}s${NC}"
    return 1
}

wait_for_postgres() {
    local elapsed=0
    
    echo -e "${YELLOW}Waiting for PostgreSQL...${NC}"
    
    while [ $elapsed -lt $TIMEOUT ]; do
        if docker exec quote-postgres pg_isready -U quoteuser -d quotedb > /dev/null 2>&1; then
            echo -e "${GREEN}✓ PostgreSQL is ready${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}  Waiting... (${elapsed}s/${TIMEOUT}s)${NC}"
        sleep $INTERVAL
        elapsed=$((elapsed + INTERVAL))
    done
    
    echo -e "${RED}✗ PostgreSQL failed to become ready within ${TIMEOUT}s${NC}"
    return 1
}

# Main execution
echo -e "${YELLOW}=== Waiting for Services ===${NC}\n"

FAILED=0

# Check each requested service
for service in "$@"; do
    case $service in
        postgres)
            wait_for_postgres || FAILED=1
            ;;
        app)
            wait_for_url "http://localhost:8080/actuator/health" "Spring Boot App" || FAILED=1
            ;;
        keycloak)
            wait_for_url "http://localhost:8081/health/live" "Keycloak" || FAILED=1
            ;;
        jenkins)
            wait_for_url "http://localhost:8090" "Jenkins" || FAILED=1
            ;;
        *)
            echo -e "${RED}Unknown service: ${service}${NC}"
            echo "Available services: postgres, app, keycloak, jenkins"
            FAILED=1
            ;;
    esac
done

echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}=== All services are ready! ===${NC}"
    exit 0
else
    echo -e "${RED}=== Some services failed to start ===${NC}"
    exit 1
fi
