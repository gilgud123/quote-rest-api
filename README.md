# Quote REST API

REST API for managing authors and their quotes. Built with Spring Boot, JPA, PostgreSQL, MapStruct, and validation.

## Features
- CRUD for authors and quotes
- Pagination, filtering, and search
- Validation and global error handling
- Swagger/OpenAPI docs
- Unit and integration tests
- JaCoCo coverage reporting

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

## Notes
- Integration tests in this project use H2 and Spring Boot test context with seeded data.
- If you switch integration tests to Testcontainers, update the test configuration accordingly.
