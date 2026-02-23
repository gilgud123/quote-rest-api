# Database initialization script for PostgreSQL (Windows PowerShell)
# Run this script to create the database and user

Write-Host "`nCreating PostgreSQL database and user for Quote REST API..." -ForegroundColor Cyan

# PostgreSQL connection details
$POSTGRES_HOST = if ($env:POSTGRES_HOST) { $env:POSTGRES_HOST } else { "localhost" }
$POSTGRES_PORT = if ($env:POSTGRES_PORT) { $env:POSTGRES_PORT } else { "5432" }
$POSTGRES_ADMIN_USER = if ($env:POSTGRES_ADMIN_USER) { $env:POSTGRES_ADMIN_USER } else { "postgres" }
$POSTGRES_ADMIN_PASSWORD = if ($env:POSTGRES_ADMIN_PASSWORD) { $env:POSTGRES_ADMIN_PASSWORD } else { "postgres" }

# Database and user details
$DB_NAME = "quotedb"
$DB_USER = "quoteuser"
$DB_PASSWORD = "quotepass"

Write-Host "Connecting to PostgreSQL at ${POSTGRES_HOST}:${POSTGRES_PORT}..." -ForegroundColor Yellow

# Set password environment variable for psql
$env:PGPASSWORD = $POSTGRES_ADMIN_PASSWORD

# Create database
Write-Host "`nCreating database '$DB_NAME'..." -ForegroundColor Yellow
$result = & psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -c "CREATE DATABASE $DB_NAME;" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "? Database '$DB_NAME' created successfully" -ForegroundColor Green
} else {
    Write-Host "??  Database '$DB_NAME' may already exist" -ForegroundColor Yellow
}

# Create user
Write-Host "Creating user '$DB_USER'..." -ForegroundColor Yellow
$result = & psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';" 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "? User '$DB_USER' created successfully" -ForegroundColor Green
} else {
    Write-Host "??  User '$DB_USER' may already exist" -ForegroundColor Yellow
}

# Grant privileges
Write-Host "Granting privileges..." -ForegroundColor Yellow
& psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;" 2>&1 | Out-Null
Write-Host "? Privileges granted to user '$DB_USER'" -ForegroundColor Green

# Grant schema privileges (PostgreSQL 15+)
& psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "GRANT ALL ON SCHEMA public TO $DB_USER;" 2>&1 | Out-Null
& psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO $DB_USER;" 2>&1 | Out-Null
& psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;" 2>&1 | Out-Null
& psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $DB_USER;" 2>&1 | Out-Null
& psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_ADMIN_USER -d $DB_NAME -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $DB_USER;" 2>&1 | Out-Null

Write-Host "`n? Database setup complete!" -ForegroundColor Green
Write-Host "`nDatabase Details:" -ForegroundColor Cyan
Write-Host "  Host: $POSTGRES_HOST"
Write-Host "  Port: $POSTGRES_PORT"
Write-Host "  Database: $DB_NAME"
Write-Host "  Username: $DB_USER"
Write-Host "  Password: $DB_PASSWORD"
Write-Host "`nConnection URL:" -ForegroundColor Cyan
Write-Host "  jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${DB_NAME}"
Write-Host "`nTo connect with psql:" -ForegroundColor Cyan
Write-Host "  `$env:PGPASSWORD='$DB_PASSWORD'; psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $DB_USER -d $DB_NAME"

# Clear password from environment
Remove-Item Env:\PGPASSWORD
