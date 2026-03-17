# Test Database Setup

## Overview

The backend integration tests use a manually started PostgreSQL Docker container instead of Testcontainers for better compatibility with Windows Docker Desktop.

## Prerequisites

- Docker Desktop running
- PostgreSQL port 5434 available (not in use)

## Starting the Test Database

### Windows (PowerShell)

```powershell
.\scripts\start-test-db.ps1
```

### Linux/macOS (Bash)

```bash
./scripts/start-test-db.sh
```

The script will:
1. Check if the container already exists
2. Start or create a PostgreSQL 15 container named `quote-test-db`
3. Wait for PostgreSQL to be ready
4. Display connection information

### Connection Details

- **Host:** localhost
- **Port:** 5434
- **Database:** testdb
- **Username:** testuser
- **Password:** testpass
- **JDBC URL:** `jdbc:postgresql://localhost:5434/testdb`

## Stopping the Test Database

### Windows (PowerShell)

```powershell
.\scripts\stop-test-db.ps1
```

### Linux/macOS (Bash)

```bash
./scripts/stop-test-db.sh
```

## Manual Container Management

### Start existing container
```bash
docker start quote-test-db
```

### Stop container
```bash
docker stop quote-test-db
```

### Remove container
```bash
docker rm -f quote-test-db
```

### Check container status
```bash
docker ps -a --filter name=quote-test-db
```

## Running Tests

Once the test database is running, execute tests with:

```bash
cd backend
mvn test
```

Or run from the project root:

```bash
mvn test -pl backend
```

## Test Configuration

The test database configuration is set in `backend/src/test/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/testdb
    driver-class-name: org.postgresql.Driver
    username: testuser
    password: testpass
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## Troubleshooting

### Port Already in Use

If port 5434 is already in use, you can either:

1. Stop the conflicting service
2. Change the port mapping in the start script and update `application.yml`

### Container Won't Start

Check Docker Desktop is running:
```bash
docker ps
```

### Tests Fail with Connection Error

Ensure the test database is running:
```bash
docker ps --filter name=quote-test-db
```

Verify PostgreSQL is ready:
```bash
docker exec quote-test-db pg_isready -U testuser -d testdb
```

## Notes

- The database schema is created automatically by Hibernate with `ddl-auto: create-drop`
- Each test class cleans up data in `@AfterEach` methods
- The container uses `postgres:15-alpine` for a smaller footprint
- Data does not persist between test runs (schema is dropped after each test class)
