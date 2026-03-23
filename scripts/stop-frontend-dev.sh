#!/bin/bash
# Stop frontend development environment

echo "🛑 Stopping Quote REST API - Frontend Development Environment"
echo ""

# Stop Docker services
echo "📦 Stopping all services..."
docker-compose -f docker-compose-frontend.yml down

echo ""
echo "✅ All services stopped!"
echo ""
echo "💡 Useful commands:"
echo "   Remove volumes (clean database): docker-compose -f docker-compose-frontend.yml down -v"
echo "   View stopped containers:         docker ps -a"
echo "   Remove all stopped containers:   docker container prune"
