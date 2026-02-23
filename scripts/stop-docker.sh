#!/bin/bash
echo "  docker-compose down -v"
echo "To remove all data volumes:"
echo ""
echo "? Services stopped!"
echo ""

docker-compose down
echo "? Stopping Quote REST API services..."

# Script to stop Docker Compose services
