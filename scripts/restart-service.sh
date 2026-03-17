#!/bin/bash
# Restart specific service in frontend development environment

SERVICE=$1

if [ -z "$SERVICE" ]; then
    echo "Usage: ./scripts/restart-service.sh <service>"
    echo ""
    echo "Available services:"
    echo "  postgres  - PostgreSQL database"
    echo "  keycloak  - Keycloak authentication server"
    echo "  backend   - Spring Boot backend API"
    echo ""
    echo "Example: ./scripts/restart-service.sh backend"
    exit 1
fi

echo "🔄 Restarting $SERVICE..."
docker-compose -f docker-compose-frontend.yml restart $SERVICE

if [ $? -eq 0 ]; then
    echo "✅ $SERVICE restarted successfully"
    echo ""
    echo "View logs with: docker-compose -f docker-compose-frontend.yml logs -f $SERVICE"
else
    echo "❌ Failed to restart $SERVICE"
    exit 1
fi
