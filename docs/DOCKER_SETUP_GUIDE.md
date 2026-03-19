# Docker Setup Guide - Quote REST API

## Overview

Run the Quote REST API and PostgreSQL database together using Docker Compose.

---

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

---

## Quick Start

### Windows (PowerShell)

```powershell
cd "C:\Users\Katya de Vries\IdeaProjects\quote-rest-api"
.\scripts\start-docker.ps1
```

### Linux/Mac (Bash)

```bash
cd /path/to/quote-rest-api
chmod +x scripts/start-docker.sh
./scripts/start-docker.sh
```

### Or manually:

```bash
docker-compose up --build -d
```

---

## What Gets Created

### Docker Services

#### 1. postgres

- **Image:** postgres:16-alpine
- **Container:** quote-postgres
- **Port:** 5432:5432
- **Database:** quotedb
- **User:** quoteuser / quotepass
- **Volume:** postgres-data (persistent storage)
- **Health Check:** pg_isready every 10s

#### 2. app

- **Image:** Built from Dockerfile
- **Container:** quote-rest-api
- **Port:** 8080:8080
- **Profile:** docker
- **Depends on:** postgres (waits for DB health check)
- **Health Check:** wget /actuator/health every 30s

### Docker Network

- **Name:** quote-network
- **Type:** bridge
- Allows services to communicate

### Docker Volume

- **Name:** quote-postgres-data
- **Purpose:** Persists PostgreSQL data across container restarts

---

## Dockerfile Explained

### Multi-Stage Build

**Stage 1: Build** (maven:3.9-eclipse-temurin-17)
1. Copy pom.xml
2. Download dependencies (cached layer)
3. Copy source code
4. Build JAR with Maven (skips tests for faster build)

**Stage 2: Runtime** (eclipse-temurin:17-jre-alpine)
1. Copy JAR from build stage
2. Create non-root user (security)
3. Configure health check
4. Set JVM memory limits
5. Run application

**Benefits:**
- ? Smaller final image (JRE only, no build tools)
- ? Faster builds (dependency caching)
- ? Secure (runs as non-root user)
- ? Optimized memory (container-aware JVM)

---

## docker-compose.yml Features

### Service Configuration

```yaml
services:
  postgres:
    - PostgreSQL 16 Alpine
    - Persistent volume
    - Health checks
    - Auto-restart
  
  app:
    - Built from Dockerfile
    - Depends on postgres health
    - Docker profile active
    - Health checks
    - Auto-restart
```

### Networking

- Bridge network for service isolation
- Services communicate by service name
- App connects to postgres via `postgres:5432`

### Volume Persistence

- Database data persists across restarts
- Named volume: `quote-postgres-data`

### Health Checks

- **PostgreSQL:** pg_isready command
- **Application:** /actuator/health endpoint
- Automatic container restart on failure

---

## Docker Commands

### Start Services

```bash
docker-compose up -d
```

### Build and Start (force rebuild)

```bash
docker-compose up --build -d
```

### Stop Services

```bash
docker-compose down
```

### Stop and Remove Data

```bash
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Application only
docker-compose logs -f app

# Database only
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100
```

### Check Status

```bash
docker-compose ps
```

### Restart Services

```bash
docker-compose restart

# Restart specific service
docker-compose restart app
```

### Execute Commands in Containers

```bash
# Access PostgreSQL
docker-compose exec postgres psql -U quoteuser -d quotedb

# Access application shell
docker-compose exec app sh
```

---

## Accessing the Application

After running `docker-compose up -d`:

### API Endpoints

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **Health Check:** http://localhost:8080/actuator/health
- **Authors API:** http://localhost:8080/api/v1/authors
- **Quotes API:** http://localhost:8080/api/v1/quotes

### Database Access

```bash
# Using psql from host (if psql installed)
psql -h localhost -p 5432 -U quoteuser -d quotedb

# Using Docker
docker-compose exec postgres psql -U quoteuser -d quotedb
```

---

## Testing the Deployment

### 1. Check Services Are Running

```bash
docker-compose ps
```

Should show both services as "Up" and "healthy"

### 2. Check Application Health

```bash
curl http://localhost:8080/actuator/health
```

Should return: `{"status":"UP"}`

### 3. Test API Endpoints

```bash
# Get all authors
curl http://localhost:8080/api/v1/authors

# Search for Socrates
curl "http://localhost:8080/api/v1/authors/search?name=Socrates"

# Get all quotes
curl http://localhost:8080/api/v1/quotes
```

### 4. Verify Database

```bash
docker-compose exec postgres psql -U quoteuser -d quotedb -c "SELECT COUNT(*) FROM authors;"
docker-compose exec postgres psql -U quoteuser -d quotedb -c "SELECT COUNT(*) FROM quotes;"
```

---

## Environment Configuration

### Default Configuration

Uses values from `application-docker.yml`:
- Database host: `postgres`
- Database port: `5432`
- Database name: `quotedb`
- Username: `quoteuser`
- Password: `quotepass`

### Override Configuration

Create `.env` file (copy from `.env.example`):

```bash
cp .env .env
```

Edit values as needed:

```env
POSTGRES_DB=quotedb
POSTGRES_USER=quoteuser
POSTGRES_PASSWORD=your-secure-password
```

---

## Troubleshooting

### Issue: Port 5432 already in use

**Solution:** Stop local PostgreSQL or change port in docker-compose.yml:

```yaml
ports:
  - "5433:5432"  # Map to different host port
```

### Issue: Port 8080 already in use

**Solution:** Change port in docker-compose.yml:

```yaml
ports:
  - "8081:8080"  # Map to different host port
```

### Issue: Application fails to start

**Solution:** Check logs:

```bash
docker-compose logs app
```

### Issue: Database connection failed

**Solution:**
1. Check postgres is healthy: `docker-compose ps`
2. Check postgres logs: `docker-compose logs postgres`
3. Verify credentials in application-docker.yml

### Issue: Schema not loaded

**Solution:** Check application logs for SQL execution:

```bash
docker-compose logs app | grep -i "schema.sql"
```

### Issue: Need to reset database

**Solution:** Remove volumes and restart:

```bash
docker-compose down -v
docker-compose up -d
```

---

## Development Workflow

### Make Code Changes

1. Edit Java code
2. Rebuild and restart:

```bash
docker-compose up --build -d app
```

### View Live Logs

```bash
docker-compose logs -f app
```

### Connect to Database

```bash
docker-compose exec postgres psql -U quoteuser -d quotedb
```

### Run SQL Commands

```bash
docker-compose exec postgres psql -U quoteuser -d quotedb -c "SELECT * FROM authors;"
```

---

## Production Considerations

### For Production Deployment:

1. **Use environment variables** for sensitive data:

```yaml
environment:
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
```

2. **Use secrets management** (Docker Swarm, Kubernetes)

3. **Configure resource limits**:

```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 1G
    reservations:
      cpus: '0.5'
      memory: 512M
```

4. **Use production profile**:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: prod
```

5. **Configure logging** to external volume

6. **Set up monitoring** (Prometheus, Grafana)

7. **Use SSL/TLS** for database connections

8. **Regular backups** of PostgreSQL volume

---

## Files Created

```
Project Root:
??? Dockerfile                   ? Multi-stage build
??? docker-compose.yml           ? Service orchestration
??? .dockerignore               ? Build context optimization
??? .env.example                ? Environment template

scripts/:
??? start-docker.sh             ? Start script (Linux/Mac)
??? start-docker.ps1            ? Start script (Windows)
??? stop-docker.sh              ? Stop script (Linux/Mac)
??? stop-docker.ps1             ? Stop script (Windows)

DOCKER_SETUP_GUIDE.md           ? This guide
```

---

## Quick Reference

### Start

```bash
docker-compose up -d
```

### Stop

```bash
docker-compose down
```

### Rebuild

```bash
docker-compose up --build -d
```

### Logs

```bash
docker-compose logs -f
```

### Status

```bash
docker-compose ps
```

### Clean Everything

```bash
docker-compose down -v
docker system prune -a
```

---

## ? Step 4: COMPLETE

Docker configuration is ready! You can now run the entire application stack with a single command. ?
