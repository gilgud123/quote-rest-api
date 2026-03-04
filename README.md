# Quote REST API

REST API for managing authors and their quotes. Built with Spring Boot, JPA, PostgreSQL, MapStruct, and validation.

## Features

- CRUD for authors and quotes
- Pagination, filtering, and search
- Validation and global error handling
- Swagger/OpenAPI docs
- Unit and integration tests
- JaCoCo coverage reporting
- Code formatting with Spotless
- MCP servers for AI-assisted development (Playwright & PostgreSQL)

## Tech Stack

- Spring Boot 3.2.1
- Java (project configured for 17 in `pom.xml`)
- PostgreSQL (runtime)
- H2 (tests)
- MapStruct, Lombok, Hibernate
- Maven, JaCoCo

## Prerequisites

- Java 17+ (aligned with `pom.xml`)
- Maven 3.6+
- PostgreSQL 12+ (or Docker)

## Configuration

Default database settings live in `src/main/resources/application.yml`.

### Local PostgreSQL

Update the datasource values if needed:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### Schema and Sample Data

- `src/main/resources/schema.sql`
- `src/main/resources/data.sql`

## Run Locally

```powershell
cd "C:\Users\Katya de Vries\IdeaProjects\quote-rest-api"
mvn spring-boot:run
```

## Run with Docker

```powershell
cd "C:\Users\Katya de Vries\IdeaProjects\quote-rest-api"
docker compose up --build
```

After startup, Keycloak is available at `http://localhost:8081`.

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
mvn test
```

### Coverage

JaCoCo report:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`

## Code Formatting

This project uses **Spotless** to enforce consistent code formatting. See [SPOTLESS.md](SPOTLESS.md) for details.

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

See [MCP_SETUP.md](MCP_SETUP.md) for installation and usage guide.

**Quick Start:**

```powershell
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Configure Claude Desktop (see MCP_SETUP.md)

# 3. Use Claude to test and inspect your API
```

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
