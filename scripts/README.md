# Development Scripts

Helper scripts for frontend development workflow.

## Quick Start

### Windows (PowerShell)

```powershell
# Start all backend services and frontend dev server
.\scripts\start-frontend-dev.ps1

# Stop all services
.\scripts\stop-frontend-dev.ps1

# View logs
.\scripts\logs-frontend-dev.ps1

# Restart a service
.\scripts\restart-service.ps1 -Service backend
```

### Linux/Mac (Bash)

```bash
# Start all backend services and frontend dev server
./scripts/start-frontend-dev.sh

# Stop all services
./scripts/stop-frontend-dev.sh

# View logs
./scripts/logs-frontend-dev.sh

# Restart a service
./scripts/restart-service.sh backend
```

## Available Scripts

### Start Development Environment

**Windows:** `start-frontend-dev.ps1`  
**Linux/Mac:** `start-frontend-dev.sh`

Starts the complete development environment:
- ✅ Checks Docker is running
- ✅ Starts PostgreSQL, Keycloak, and Backend (Docker)
- ✅ Waits for services to be healthy
- ✅ Installs npm dependencies (first time)
- ✅ Starts Angular dev server
- ✅ Displays access points and test credentials

**What it starts:**
- PostgreSQL on port 5433
- Keycloak on port 9090
- Backend API on port 8080
- Frontend dev server on port 4200

### Stop Development Environment

**Windows:** `stop-frontend-dev.ps1`  
**Linux/Mac:** `stop-frontend-dev.sh`

Stops all Docker services cleanly.

**Options:**
```bash
# Stop services but keep volumes (data persists)
./scripts/stop-frontend-dev.sh

# Stop services and remove volumes (clean slate)
docker-compose -f docker-compose-frontend.yml down -v
```

### View Logs

**Windows:** `logs-frontend-dev.ps1`  
**Linux/Mac:** `logs-frontend-dev.sh`

Follows logs from all services in real-time.

**Tips:**
```bash
# View logs for specific service
docker-compose -f docker-compose-frontend.yml logs -f backend

# View last 50 lines
docker-compose -f docker-compose-frontend.yml logs --tail=50

# View logs without following
docker-compose -f docker-compose-frontend.yml logs
```

### Restart Service

**Windows:** `restart-service.ps1 -Service <name>`  
**Linux/Mac:** `restart-service.sh <name>`

Restarts a specific service without affecting others.

**Available services:**
- `postgres` - PostgreSQL database
- `keycloak` - Keycloak authentication server
- `backend` - Spring Boot backend API

**Example:**
```bash
# After making backend code changes
./scripts/restart-service.sh backend
```

## Access Points

Once started, services are available at:

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:4200 | (use test users below) |
| Backend API | http://localhost:8080/api/v1 | (JWT from Keycloak) |
| Swagger UI | http://localhost:8080/swagger-ui.html | (JWT from Keycloak) |
| Keycloak Admin | http://localhost:9090 | admin / admin |

## Test Users

Pre-configured in Keycloak realm:

| Username | Password | Roles | Description |
|----------|----------|-------|-------------|
| frontend-user | password | USER | Regular user |
| frontend-admin | password | ADMIN, USER | Admin user |

## Troubleshooting

### Services not starting

```bash
# Check Docker is running
docker info

# Check port conflicts
netstat -an | findstr "4200 5433 8080 9090"  # Windows
lsof -i :4200,5433,8080,9090                # Linux/Mac

# View service status
docker-compose -f docker-compose-frontend.yml ps

# View detailed logs
docker-compose -f docker-compose-frontend.yml logs
```

### Backend unhealthy

```bash
# Check backend logs
docker-compose -f docker-compose-frontend.yml logs backend

# Common issues:
# - Keycloak not ready (wait longer)
# - Database connection failed (check postgres logs)
# - Port 8080 already in use
```

### Frontend not connecting

```bash
# Check proxy configuration
cat frontend/proxy.conf.json

# Verify backend is accessible
curl http://localhost:8080/api/v1/authors

# Check browser console for CORS errors
```

### Clean restart

```bash
# Stop everything and remove volumes
docker-compose -f docker-compose-frontend.yml down -v

# Remove node_modules and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## Development Workflow

### 1. Daily Development

```bash
# Morning: Start everything
./scripts/start-frontend-dev.sh

# Work on frontend (hot reload automatic)
# Edit files in frontend/src/

# Work on backend (restart after changes)
# Edit files in backend/src/
./scripts/restart-service.sh backend

# Evening: Stop services
./scripts/stop-frontend-dev.sh
```

### 2. Backend Changes

```bash
# Make changes to backend code
vim backend/src/main/java/...

# Rebuild and restart backend
docker-compose -f docker-compose-frontend.yml restart backend

# Or rebuild the image if needed
docker-compose -f docker-compose-frontend.yml up -d --build backend
```

### 3. Keycloak Configuration

```bash
# Edit realm configuration
vim keycloak/realm-quote-frontend.json

# Restart Keycloak to apply changes
./scripts/restart-service.sh keycloak
```

### 4. Database Changes

```bash
# Access database directly
docker exec -it quote-postgres-frontend psql -U quoteuser -d quotedb

# Run schema updates
docker exec -i quote-postgres-frontend psql -U quoteuser -d quotedb < backend/src/main/resources/schema.sql

# Fresh database (caution: deletes data)
docker-compose -f docker-compose-frontend.yml down -v
./scripts/start-frontend-dev.sh
```

## Additional Commands

### Manual Service Control

```bash
# Start specific service
docker-compose -f docker-compose-frontend.yml up -d backend

# Stop specific service
docker-compose -f docker-compose-frontend.yml stop keycloak

# Remove and recreate service
docker-compose -f docker-compose-frontend.yml rm -f backend
docker-compose -f docker-compose-frontend.yml up -d backend
```

### Check Service Health

```bash
# All services status
docker-compose -f docker-compose-frontend.yml ps

# Check container health
docker inspect quote-backend-frontend --format='{{.State.Health.Status}}'
docker inspect quote-keycloak-frontend --format='{{.State.Health.Status}}'
docker inspect quote-postgres-frontend --format='{{.State.Health.Status}}'

# Test backend endpoint
curl http://localhost:8080/actuator/health
```

### Frontend-Only Development

If backend services are already running:

```bash
# Just start Angular dev server
cd frontend
npm start

# Run tests
npm test                 # Watch mode
npm run test:ci         # Single run
npm run test:coverage   # With coverage

# Build for production
npm run build
```

## Jenkins Integration

These scripts complement the Jenkins CI/CD pipeline. See:
- `Jenkinsfile` - Complete build pipeline
- `references/JENKINS_PIPELINE_GUIDE.md` - Pipeline documentation
