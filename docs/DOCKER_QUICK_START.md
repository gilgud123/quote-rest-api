# ? Quick Start - Docker

## Start the Application (1 Command)

### Windows

```powershell
.\scripts\start-docker.ps1
```

### Linux/Mac

```bash
./scripts/start-docker.sh
```

### Or Manually

```bash
docker-compose up -d
```

---

## Access the Application

Once started:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Endpoints:** http://localhost:8080/api/v1
- **Health Check:** http://localhost:8080/actuator/health
- **Authors:** http://localhost:8080/api/v1/authors
- **Quotes:** http://localhost:8080/api/v1/quotes

---

## Stop the Application

### Windows

```powershell
.\scripts\stop-docker.ps1
```

### Linux/Mac

```bash
./scripts/stop-docker.sh
```

### Or Manually

```bash
docker-compose down
```

---

## What Runs

1. **PostgreSQL 16** - Database with sample data (3 authors, 10 quotes)
2. **Spring Boot App** - REST API on port 8080

Both services:
- ? Auto-restart on failure
- ? Health checks enabled
- ? Data persists across restarts
- ? Connected via bridge network

---

## Test It Works

```bash
# Get all authors
curl http://localhost:8080/api/v1/authors

# Find Socrates
curl "http://localhost:8080/api/v1/authors/search?name=Socrates"

# Get all quotes
curl http://localhost:8080/api/v1/quotes
```

---

## Troubleshooting

### View Logs

```bash
docker-compose logs -f
```

### Check Status

```bash
docker-compose ps
```

### Restart

```bash
docker-compose restart
```

---

## ? Step 4: COMPLETE

Docker configuration is ready! Start with one command. ?
