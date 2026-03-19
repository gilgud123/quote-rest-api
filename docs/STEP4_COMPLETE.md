# ? Step 4: Docker Configuration - COMPLETE

## What Was Created

### Docker Files (4)

1. ? **Dockerfile** - Multi-stage build for Spring Boot app
2. ? **docker-compose.yml** - Service orchestration (PostgreSQL + App)
3. ? **.dockerignore** - Optimize build context
4. ? **.env.example** - Environment variables template

### Helper Scripts (4)

5. ? **scripts/start-docker.sh** - Start services (Linux/Mac)
6. ? **scripts/start-docker.ps1** - Start services (Windows)
7. ? **scripts/stop-docker.sh** - Stop services (Linux/Mac)
8. ? **scripts/stop-docker.ps1** - Stop services (Windows)

### Configuration Updates (2)

9. ? **pom.xml** - Added Spring Boot Actuator dependency
10. ? **application.yml** - Added actuator health check configuration

### Documentation (1)

11. ? **DOCKER_SETUP_GUIDE.md** - Complete Docker guide

---

## Docker Architecture

```
???????????????????????????????????????????????????????????
?                  Docker Network (Bridge)                 ?
?                     quote-network                        ?
?                                                          ?
?  ????????????????????????    ???????????????????????????
?  ?   PostgreSQL 16      ?    ?  Spring Boot App       ??
?  ?   (postgres:alpine)  ?    ?  (Custom Build)        ??
?  ?                      ?    ?                        ??
?  ?  Container:          ?    ?  Container:            ??
?  ?    quote-postgres    ??????    quote-rest-api     ??
?  ?                      ?    ?                        ??
?  ?  Port: 5432          ?    ?  Port: 8080            ??
?  ?  DB: quotedb         ?    ?  Profile: docker       ??
?  ?  User: quoteuser     ?    ?  Health: /actuator     ??
?  ?                      ?    ?                        ??
?  ?  Volume:             ?    ?                        ??
?  ?    postgres-data     ?    ?                        ??
?  ????????????????????????    ???????????????????????????
?           ?                            ?                ?
???????????????????????????????????????????????????????????
            ?                            ?
         Port 5432                   Port 8080
            ?                            ?
       localhost:5432              localhost:8080
```

---

## Dockerfile Features

### Multi-Stage Build

? **Stage 1 (Build):**
- Uses Maven image with JDK
- Downloads dependencies (cached)
- Compiles and packages application
- Skips tests for faster build

? **Stage 2 (Runtime):**
- Uses JRE-only image (smaller)
- Copies JAR from build stage
- Runs as non-root user
- Includes health check

### Image Optimization

- ? Alpine Linux base (small size)
- ? Multi-stage build (no build tools in final image)
- ? Layer caching (dependencies cached separately)
- ? .dockerignore (excludes unnecessary files)

### Security

- ? Non-root user (spring:spring)
- ? JRE only (no development tools)
- ? Health checks enabled

### Performance

- ? Container-aware JVM
- ? Memory limits (-Xmx512m)
- ? Optimized for containerized environment

---

## docker-compose.yml Features

### Service Dependencies

```yaml
app:
  depends_on:
    postgres:
      condition: service_healthy
```

Application waits for database to be ready before starting.

### Health Checks

**PostgreSQL:**
- Command: `pg_isready`
- Interval: 10s
- Retries: 5

**Application:**
- URL: `/actuator/health`
- Interval: 30s
- Start period: 60s (allows time to initialize)

### Networking

- Custom bridge network
- Service discovery by name
- Isolated from other containers

### Data Persistence

- Named volume for PostgreSQL
- Data survives container restarts
- Can be backed up separately

### Auto-Restart

- `restart: unless-stopped`
- Containers restart on failure
- Won't restart if manually stopped

---

## How to Use

### 1. Start Everything

```bash
# Windows
.\scripts\start-docker.ps1

# Linux/Mac
./scripts/start-docker.sh

# Or manually
docker-compose up -d
```

### 2. Wait for Services

The scripts wait automatically, or check manually:

```bash
docker-compose ps
```

Both should show "healthy" status.

### 3. Access the Application

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API:** http://localhost:8080/api/v1
- **Health:** http://localhost:8080/actuator/health

### 4. Test API

```bash
# Get authors
curl http://localhost:8080/api/v1/authors

# Search for Socrates
curl "http://localhost:8080/api/v1/authors/search?name=Socrates"

# Get quotes
curl http://localhost:8080/api/v1/quotes
```

### 5. Stop When Done

```bash
# Windows
.\scripts\stop-docker.ps1

# Linux/Mac
./scripts/stop-docker.sh

# Or manually
docker-compose down
```

---

## Useful Commands

### View Application Logs

```bash
docker-compose logs -f app
```

### Access PostgreSQL Shell

```bash
docker-compose exec postgres psql -U quoteuser -d quotedb
```

### Run Database Queries

```bash
docker-compose exec postgres psql -U quoteuser -d quotedb -c "SELECT * FROM authors;"
```

### Check Database Statistics

```bash
docker-compose exec postgres psql -U quoteuser -d quotedb -f /docker-entrypoint-initdb.d/verify-db.sql
```

### Rebuild Application Only

```bash
docker-compose up --build -d app
```

### View Container Resource Usage

```bash
docker stats
```

### Inspect Network

```bash
docker network inspect quote-network
```

### Inspect Volume

```bash
docker volume inspect quote-postgres-data
```

---

## Environment Variables

### Database (postgres service)

- `POSTGRES_DB` - Database name (default: quotedb)
- `POSTGRES_USER` - Database user (default: quoteuser)
- `POSTGRES_PASSWORD` - Database password (default: quotepass)

### Application (app service)

- `SPRING_PROFILES_ACTIVE` - Active profile (default: docker)
- `SPRING_DATASOURCE_URL` - JDBC URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Override with .env File

Create `.env` in project root:

```env
POSTGRES_PASSWORD=my-secure-password
```

---

## Data Persistence

### Volume Location

Docker stores the volume data at:
- **Windows:** `\\wsl$\docker-desktop-data\data\docker\volumes\quote-postgres-data`
- **Linux:** `/var/lib/docker/volumes/quote-postgres-data`
- **macOS:** `~/Library/Containers/com.docker.docker/Data/...`

### Backup Database

```bash
docker-compose exec postgres pg_dump -U quoteuser quotedb > backup.sql
```

### Restore Database

```bash
cat backup.sql | docker-compose exec -T postgres psql -U quoteuser -d quotedb
```

---

## Development vs Production

### Development (Current Setup)

- Shows SQL in logs
- Verbose logging
- Default passwords
- Exposed ports for debugging

### Production Changes Needed

- Use secrets for passwords
- Minimal logging
- Configure resource limits
- Add reverse proxy (nginx)
- Enable SSL/TLS
- Configure monitoring
- Set up backups
- Use production database (not in container)

---

## Files Summary

```
Project Root:
??? Dockerfile                   ? Multi-stage build (2 stages)
??? docker-compose.yml           ? 2 services (postgres + app)
??? .dockerignore               ? Build optimization
??? .env.example                ? Environment template

scripts/:
??? start-docker.sh             ? Automated start (Linux/Mac)
??? start-docker.ps1            ? Automated start (Windows)
??? stop-docker.sh              ? Stop script (Linux/Mac)
??? stop-docker.ps1             ? Stop script (Windows)

Updated:
??? pom.xml                      ? Added actuator dependency
??? application.yml              ? Added actuator config

Documentation:
??? DOCKER_SETUP_GUIDE.md       ? Complete guide
```

---

## Build Status

? **Compilation:** Successful (with actuator)
? **Docker Files:** Valid
? **docker-compose:** Valid
? **Ready to run:** `docker-compose up -d`

---

## Next Steps

### Immediate:

- ? Docker configuration complete
- ?? Test with `docker-compose up -d`

### Coming Next:

- Step 5: Unit tests
- Step 6: Integration tests
- Step 7: README documentation

---

## ? Step 4: COMPLETE

Dockerfile and docker-compose.yml with PostgreSQL bridge are ready! ??
