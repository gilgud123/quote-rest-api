# Quote REST API

Full-stack application for managing authors and their quotes. Backend built with Spring Boot, frontend with Angular 20, secured with Keycloak OAuth2/JWT.

## Features

- **Backend API**:
  - CRUD for authors and quotes
  - Pagination, filtering, and search
  - Validation and global error handling
  - Swagger/OpenAPI docs
  - Unit and integration tests
  - JaCoCo coverage reporting
- **Frontend UI**:
  - Angular 20 with standalone components
  - Keycloak OAuth2 authentication
  - Lazy-loaded feature modules
  - Complete CRUD with responsive UI
  - 81 unit tests with 79% coverage
- **Infrastructure**:
  - Code formatting with Spotless
  - Docker Compose for development
  - MCP servers for AI-assisted development

## Tech Stack

### Backend

- Spring Boot 3.2.1
- Java 17
- Spring Security with OAuth2 Resource Server
- PostgreSQL (runtime), H2 (tests)
- MapStruct, Lombok, Hibernate
- Maven, JaCoCo

### Frontend

- Angular 20
- TypeScript 5.9.2
- Keycloak JS 26.2.3
- RxJS, Standalone Components
- Karma + Jasmine for testing
- Node.js 22.12, npm 10.9

## Prerequisites

- Java 17+ (aligned with `pom.xml`)
- Maven 3.6+
- Node.js 22.12+ (for frontend)
- Docker & Docker Compose (for full stack)
- PostgreSQL 12+ (or use Docker)

## Configuration

Default database settings live in `backend/src/main/resources/application.yml`.

### Local PostgreSQL

Update the datasource values if needed:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### Schema and Sample Data

- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

## Run Locally

```powershell
cd "C:\Users\Katya de Vries\IdeaProjects\quote-rest-api"
mvn spring-boot:run -pl backend
```

## Run with Docker

```powershell
cd "C:\Users\Katya de Vries\IdeaProjects\quote-rest-api"
docker compose up --build
```

After startup, Keycloak is available at `http://localhost:8081`.

## Frontend Development

### Quick Start

Start the full development environment with backend, Keycloak, and PostgreSQL:

```powershell
# Terminal 1: Start all backend services
docker-compose -f docker-compose-frontend.yml up -d

# Wait for services to be healthy (check with docker ps)
# Terminal 2: Start Angular development server
cd frontend
npm install  # First time only
npm start
```

### Access Points

- **Frontend UI**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Keycloak Admin**: http://localhost:9090 (admin/admin)

### Test Users

Pre-configured in the Keycloak realm:
- **frontend-user** / **password** - Regular user (USER role)
- **frontend-admin** / **password** - Admin user (ADMIN role)

### Frontend Commands

```powershell
cd frontend

# Development server with hot reload
npm start

# Run tests
npm test                 # Interactive watch mode
npm run test:ci         # Single run (for CI)
npm run test:coverage   # With coverage report

# Build for production
npm run build

# Lint and format
mvn spotless:apply -pl frontend
```

### Development Workflow

1. **Backend changes**: Modify code, then restart backend container or run Spring Boot locally
2. **Frontend changes**: Auto-reload with `ng serve` hot module replacement (HMR)
3. **Keycloak changes**: Edit `keycloak/realm-quote-frontend.json` and restart Keycloak container

### Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/           # Services, guards, interceptors, models
│   │   ├── features/       # Authors and Quotes modules
│   │   ├── shared/         # Shared components
│   │   └── app.routes.ts   # Main routing
│   ├── environments/       # Environment configs
│   └── styles.scss         # Global styles
├── angular.json            # Angular CLI config
├── package.json            # npm dependencies
└── pom.xml                 # Maven integration
```

### Testing

Frontend has 81 unit tests with ~79% coverage:
- Core services (auth, api)
- Feature services (authors, quotes)
- Components (lists, forms, dialogs)
- Guards and interceptors

```powershell
# Run tests with coverage
cd frontend
npm run test:coverage

# View coverage report
start coverage/quote-frontend/index.html
```

### Troubleshooting

**Port conflicts:**
- Frontend dev server uses port 4200
- Backend uses port 8080
- Keycloak uses port 9090
- PostgreSQL uses port 5433

**Keycloak connection issues:**
- Ensure Keycloak is healthy: `docker ps`
- Check realm import: `docker logs quote-keycloak-frontend`
- Verify realm config: `keycloak/realm-quote-frontend.json`

**Build issues:**
- Clear npm cache: `npm clean-install`
- Clear Maven cache: `mvn clean`
- Restart Docker services: `docker-compose -f docker-compose-frontend.yml restart`

## Verify schema/data load

```powershell
docker exec -i quote-postgres psql -U quoteuser -d quotedb -f /docker-entrypoint-initdb.d/verify-d
```

## API Docs

After the app starts:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Tests

```powershell
mvn test -pl backend
```

### Coverage

JaCoCo report:

- HTML: `backend/target/site/jacoco/index.html`
- XML: `backend/target/site/jacoco/jacoco.xml`

## Code Formatting

This project uses **Spotless** to enforce consistent code formatting. See [SPOTLESS.md](references/SPOTLESS.md) for details.

```powershell
# Apply formatting to all files
mvn spotless:apply

# Check formatting (runs automatically during verify phase)
mvn spotless:check
```

## MCP Servers for AI Development

This project includes configurations for **MCP (Model Context Protocol)** servers that extend Claude's capabilities:

- **Playwright MCP**: Test REST API endpoints in browser
- **PostgreSQL MCP**: Query and inspect database directly

See [MCP_SETUP.md](tests/MCP_SETUP.md) for installation and usage guide.

**Quick Start:**

```powershell
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Configure GitHub Copilot CLI (see MCP_SETUP.md)

# 3. Use Copilot to test and inspect your API
```

## CI/CD with Jenkins

This project includes a complete Jenkins CI/CD pipeline for automated builds, testing, and Docker image creation.

### Quick Start

```powershell
# Start Jenkins
.\scripts\jenkins\jenkins-docker.ps1 start

# Access Jenkins at http://localhost:8090
# Get initial password
.\scripts\jenkins\jenkins-docker.ps1 password
```

### Pipeline Features

- ✅ Automated builds with Maven
- ✅ Unit and integration tests
- ✅ Code quality checks (Spotless)
- ✅ Code coverage reporting (JaCoCo)
- ✅ Playwright API tests
- ✅ Docker image building
- ✅ Comprehensive test reporting

### Documentation

- **[JENKINS_SETUP.md](references/JENKINS_SETUP.md)** - Installation and configuration
- **[JENKINS_PIPELINE_GUIDE.md](references/JENKINS_PIPELINE_GUIDE.md)** - Pipeline usage and troubleshooting

### Pipeline Stages

1. 📦 Checkout → 🔨 Build → 🧪 Unit Tests
2. ✨ Code Quality → 🔧 Integration Tests
3. 📊 Coverage → 📦 Package
4. 🚀 Start Services → 🎭 Playwright Tests
5. 🐳 Docker Build

**Total Duration**: ~8-12 minutes

## Notes

- Integration tests in this project use H2 and Spring Boot test context with seeded data.
- If you switch integration tests to Testcontainers, update the test configuration accordingly.

## Keycloak (Step 9)

The API uses Keycloak-issued JWTs. In Docker, Keycloak is started automatically and imports a realm.

### Default Realm and Users

- Realm: `quote`
- Client ID: `quote-api`
- Users:
  - `api-user` / `password` (role `USER`)
  - `api-admin` / `password` (roles `ADMIN`, `USER`)

### Get an Access Token (Docker)

```powershell
$token = docker run --rm --network quote-network curlimages/curl:8.5.0 `
  -s -X POST "http://keycloak:8080/realms/quote/protocol/openid-connect/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "client_id=quote-api&grant_type=password&username=api-user&password=password" `
  | ConvertFrom-Json | Select-Object -ExpandProperty access_token
```

### Call the API

```powershell
Invoke-RestMethod -Headers @{ Authorization = "Bearer $token" } -Uri "http://localhost:8080/api/v1/quotes"
```

### Why Postman Tokens Can Return 401

When the app runs in Docker, it expects issuer `http://keycloak:8080/realms/quote`.
Tokens requested from `http://localhost:8081` have issuer `http://localhost:8081/realms/quote` and will be rejected.
Use the Docker token command above or align the app issuer with the external URL.
