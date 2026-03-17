#!/bin/bash
# Script to start PostgreSQL test database for integration tests

echo "Starting PostgreSQL test database..."

# Check if container already exists
if docker ps -a --format '{{.Names}}' | grep -q '^quote-test-db$'; then
    echo "Container 'quote-test-db' already exists. Starting it..."
    docker start quote-test-db
else
    echo "Creating new PostgreSQL test container..."
    docker run -d --name quote-test-db \
      -e POSTGRES_DB=testdb \
      -e POSTGRES_USER=testuser \
      -e POSTGRES_PASSWORD=testpass \
      -p 5434:5432 \
      postgres:15-alpine
fi

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 3

# Check if PostgreSQL is accepting connections
until docker exec quote-test-db pg_isready -U testuser -d testdb > /dev/null 2>&1; do
  echo "Waiting for PostgreSQL..."
  sleep 1
done

echo "✓ PostgreSQL test database is ready!"
echo "  Connection: jdbc:postgresql://localhost:5434/testdb"
echo "  Username: testuser"
echo "  Password: testpass"
echo ""
echo "To stop the database, run: docker stop quote-test-db"
echo "To remove the database, run: docker rm -f quote-test-db"
