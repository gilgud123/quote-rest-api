#!/bin/bash
# Script to build and run the application with Docker Compose

echo "? Starting Quote REST API with Docker Compose..."
echo ""

# Build and start services
docker-compose up --build -d

echo ""
echo "? Services starting..."
echo ""
echo "Waiting for services to be healthy..."

# Wait for PostgreSQL
echo -n "Waiting for PostgreSQL."
until docker-compose exec -T postgres pg_isready -U quoteuser -d quotedb > /dev/null 2>&1
do
    echo -n "."
    sleep 2
done
echo " ? Ready!"

# Wait for application
echo -n "Waiting for Application."
until curl -s http://localhost:8080/actuator/health > /dev/null 2>&1
do
    echo -n "."
    sleep 3
done
echo " ? Ready!"

echo ""
echo "? Quote REST API is running!"
echo ""
echo "? Endpoints:"
echo "  • API: http://localhost:8080/api/v1"
echo "  • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "  • API Docs: http://localhost:8080/api-docs"
echo "  • Health: http://localhost:8080/actuator/health"
echo ""
echo "? Database:"
echo "  • Host: localhost"
echo "  • Port: 5432"
echo "  • Database: quotedb"
echo "  • Username: quoteuser"
echo "  • Password: quotepass"
echo ""
echo "? Useful Commands:"
echo "  • View logs: docker-compose logs -f"
echo "  • View app logs: docker-compose logs -f app"
echo "  • View DB logs: docker-compose logs -f postgres"
echo "  • Stop: docker-compose down"
echo "  • Stop and remove data: docker-compose down -v"
echo ""
