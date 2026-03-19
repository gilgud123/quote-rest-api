#!/bin/bash
# Script to stop PostgreSQL test database

echo "Stopping PostgreSQL test database..."
docker stop quote-test-db

echo "Test database stopped."
echo "To start it again, run: ./start-test-db.sh"
echo "To remove it completely, run: docker rm quote-test-db"
