#!/bin/bash
# Database initialization script for PostgreSQL
# Run this script to create the database and user

echo "Creating PostgreSQL database and user for Quote REST API..."

# PostgreSQL connection details
POSTGRES_HOST=${POSTGRES_HOST:-localhost}
POSTGRES_PORT=${POSTGRES_PORT:-5432}
POSTGRES_ADMIN_USER=${POSTGRES_ADMIN_USER:-postgres}
POSTGRES_ADMIN_PASSWORD=${POSTGRES_ADMIN_PASSWORD:-postgres}

# Database and user details
DB_NAME=quotedb
DB_USER=quoteuser
DB_PASSWORD=quotepass

echo "Connecting to PostgreSQL at $POSTGRES_HOST:$POSTGRES_PORT..."

# Create database
PGPASSWORD=$POSTGRES_ADMIN_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -c "CREATE DATABASE $DB_NAME;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "? Database '$DB_NAME' created successfully"
else
    echo "??  Database '$DB_NAME' may already exist"
fi

# Create user
PGPASSWORD=$POSTGRES_ADMIN_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "? User '$DB_USER' created successfully"
else
    echo "??  User '$DB_USER' may already exist"
fi

# Grant privileges
PGPASSWORD=$POSTGRES_ADMIN_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;" 2>/dev/null
echo "? Privileges granted to user '$DB_USER'"

# Grant schema privileges (PostgreSQL 15+)
PGPASSWORD=$POSTGRES_ADMIN_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "GRANT ALL ON SCHEMA public TO $DB_USER;" 2>/dev/null
PGPASSWORD=$POSTGRES_ADMIN_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO $DB_USER;" 2>/dev/null
PGPASSWORD=$POSTGRES_ADMIN_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;" 2>/dev/null

echo ""
echo "? Database setup complete!"
echo ""
echo "Database Details:"
echo "  Host: $POSTGRES_HOST"
echo "  Port: $POSTGRES_PORT"
echo "  Database: $DB_NAME"
echo "  Username: $DB_USER"
echo "  Password: $DB_PASSWORD"
echo ""
echo "Connection URL:"
echo "  jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$DB_NAME"
echo ""
echo "To connect with psql:"
echo "  PGPASSWORD=$DB_PASSWORD psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $DB_USER -d $DB_NAME"
