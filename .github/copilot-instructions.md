# Copilot instructions for this project

## Project Overview

This project is a **Quote REST API** - a Spring Boot application for managing authors and their quotes. The application provides:

- RESTful API endpoints for CRUD operations on authors and quotes
- Pagination, filtering, and search capabilities
- JWT-based authentication using Keycloak OAuth2 resource server
- Swagger/OpenAPI documentation
- PostgreSQL for production, H2 for testing

**Tech Stack:**
- **Spring Boot 3.2.1** for application framework
- **Spring Data JPA** with Hibernate for ORM
- **Spring Security** with OAuth2 Resource Server for JWT authentication
- **PostgreSQL** as the primary database
- **MapStruct** for DTO mapping
- **Lombok** for boilerplate reduction
- **Maven** for build and dependency management
- **Testcontainers** for integration testing

## Anti-hallucination guidelines:

Refer to anti-hallucination-guidelines.md and follow these guidelines at all times

# When you need to call tools from the shell, use this rubric:

- **Find Files:**  
  `fd`
- **Find Text:**  
  `rg` (ripgrep)
- **Find Code Structure (AST-based):** `ast-grep`
  - For **Java**:
    - `.java` ? `ast-grep --lang java -p '<pattern>'`
  - For **XML**:
    - `.xml` ? `ast-grep --lang xml -p '<pattern>'`
  - For **Properties Files**:
    - `.properties` ? `rg '<pattern>'` (text-based, since AST doesn�t apply here)
  - For other languages, set `--lang` appropriately (e.g., `--lang rust`).
- **Select among matches:**  
  Pipe to `fzf`
- **Structured Data:**
  - JSON: `jq`
  - YAML/XML: `yq`

## Technologies

|   Technology    | Version |               Purpose                |
|-----------------|---------|--------------------------------------|
| Java            | 17      | Core language                        |
| Spring Boot     | 3.2.1   | Application framework                |
| Spring Data JPA | (boot)  | Data access layer                    |
| Hibernate       | (boot)  | ORM implementation                   |
| PostgreSQL      | 12+     | Primary database                     |
| H2              | (test)  | In-memory database for tests         |
| MapStruct       | 1.5.5   | DTO/Entity mapping                   |
| Lombok          | 1.18.30 | Reduce boilerplate code              |
| Testcontainers  | 1.19.3  | Integration testing with Docker      |
| Keycloak        | latest  | OAuth2/JWT authentication server     |
| Swagger/OpenAPI | 2.3.0   | API documentation                    |
| JaCoCo          | 0.8.11  | Code coverage reporting              |
| Maven           | 3.6+    | Build tool and dependency management |

## Project Structure

```
src/main/java/com/katya/quoterestapi/
├── config/           # Security, OpenAPI, JWT converters
├── controller/       # REST endpoints (AuthorController, QuoteController)
├── dto/              # Data Transfer Objects
├── entity/           # JPA entities (Author, Quote)
├── exception/        # Custom exceptions and global error handler
├── mapper/           # MapStruct interfaces for DTO/Entity mapping
├── repository/       # Spring Data JPA repositories
└── service/          # Business logic layer
```

---

## JPA/Hibernate Guidelines

- Use **JPA annotations** (`@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue`) for entity mapping
- Entities use Lombok annotations (`@Data`, `@Entity`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Follow the existing entity structure in `Author.java` and `Quote.java`
- Use `@OneToMany` and `@ManyToOne` relationships appropriately with cascade types

---

## Spring Configuration

- **Annotations-based configuration only** - no XML configuration files
- Use `@RestController` for REST endpoints, `@Service` for business logic, `@Repository` for data access
- Spring Boot auto-configuration handles most setup
- Beans are automatically discovered through component scanning
- Configuration classes use `@Configuration` annotation (see `SecurityConfig`, `OpenApiConfig`)
- Application properties in `src/main/resources/application.yml`

## Maven Configuration

- This is a **single-module project** (no parent/child structure)
- All dependencies are defined in the root `pom.xml`
- Use version properties for shared dependency versions (e.g., `${mapstruct.version}`, `${lombok.version}`)
- Spring Boot manages versions for most dependencies through `spring-boot-starter-parent`
- When adding new dependencies, check if Spring Boot already manages the version

### Building and Running

**Compile and test:**

```bash
mvn clean install
```

**Run the application:**

```bash
mvn spring-boot:run
```

**Run with Docker (includes PostgreSQL and Keycloak):**

```bash
docker compose up --build
```

Refer to README.md for detailed instructions on Docker setup, Keycloak configuration, and API access.

## Running Tests

**Run all tests:**

```bash
mvn test
```

**Run tests and skip compilation:**

```bash
mvn test -DskipTests=false
```

**Run a specific test class:**

```bash
mvn test -Dtest=QuoteServiceTest
```

**Run a specific test method:**

```bash
mvn test -Dtest=QuoteServiceTest#testGetQuoteById
```

**Generate JaCoCo coverage report:**

```bash
mvn test
# View report at: target/site/jacoco/index.html
```

**Integration tests with Testcontainers:**
- Integration tests automatically spin up Docker containers (PostgreSQL)
- Ensure Docker is running before executing integration tests
- Tests use the `@Testcontainers` and `@Container` annotations

## Logging

- Use Lombok's `@Slf4j` annotation for logging
- Log at appropriate levels:
  - **DEBUG**: Detailed diagnostic information
  - **INFO**: General informational messages (service calls, API requests)
  - **WARN**: Warning messages for potentially harmful situations
  - **ERROR**: Error events that might still allow the application to continue
- **Never log sensitive data**: passwords, tokens, personal information, credit card numbers
- Use parameterized logging for performance: `log.info("Processing quote with ID: {}", quoteId)`
- Log exceptions with context: `log.error("Failed to save quote: {}", quote.getId(), exception)`
- Example usage:

  ```java
  @Slf4j
  @Service
  public class QuoteService {
      public QuoteDTO getQuote(Long id) {
          log.debug("Fetching quote with ID: {}", id);
          // ... logic
          log.info("Successfully retrieved quote: {}", id);
      }
  }
  ```

## Coding Standards

- Follow existing code style and naming conventions
- Use Java 17 features where appropriate (records, text blocks, enhanced switch, etc.)
- Use Lombok to reduce boilerplate (`@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor`)
- Keep controllers thin - delegate business logic to services
- Use DTOs for API contracts, entities for database models
- Apply validation annotations on DTOs (`@NotNull`, `@NotBlank`, `@Size`, etc.)
- Write clear JavaDoc for public methods and classes
- Follow REST naming conventions:
  - Collections: `/api/v1/quotes`
  - Single resource: `/api/v1/quotes/{id}`
  - Use HTTP methods appropriately (GET, POST, PUT, DELETE)

## Testing Guidelines

- Write unit tests using **JUnit 5** and **Mockito**
- Integration tests should use **Testcontainers** for real database testing
- Test naming: `testMethodName_Scenario_ExpectedResult` (e.g., `testGetQuote_WhenNotFound_ThrowsException`)
- Mock external dependencies in unit tests
- Aim for **80%+ code coverage** for new features
- Test edge cases: null values, empty collections, invalid input
- Use `@WebMvcTest` for controller tests, `@DataJpaTest` for repository tests
- Example structure:

  ```java
  @ExtendWith(MockitoExtension.class)
  class QuoteServiceTest {
      @Mock private QuoteRepository quoteRepository;
      @InjectMocks private QuoteService quoteService;

      @Test
      void testGetQuote_WhenExists_ReturnsQuote() {
          // given, when, then
      }
  }
  ```

## API Documentation

- API is documented using **Swagger/OpenAPI 3**
- Access Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Use annotations for better documentation:
  - `@Tag` for controller-level description
  - `@Operation` for endpoint description
  - `@ApiResponse` for response documentation
- Keep OpenAPI config in `OpenApiConfig.java`

## Security and Authentication

- Application uses **OAuth2 Resource Server** with JWT tokens from Keycloak
- Protected endpoints require valid JWT in `Authorization: Bearer <token>` header
- Role-based access control: `@PreAuthorize("hasRole('ADMIN')")`
- Security configuration in `SecurityConfig.java`
- JWT claims converted to Spring Security authorities via `KeycloakRoleConverter`
- Public endpoints (health checks, Swagger) are explicitly permitted

## When Making Changes

**Adding new entity attributes:**
1. Add field to entity class with JPA annotations
2. Update the corresponding DTO
3. Update MapStruct mapper interface (if needed)
4. Update database schema in `src/main/resources/schema.sql`
5. Update tests to cover new field

**Adding new endpoints:**
1. Add method to controller with proper annotations (`@GetMapping`, `@PostMapping`, etc.)
2. Implement business logic in service layer
3. Add validation on DTOs
4. Update OpenAPI documentation
5. Write unit and integration tests
6. Test with Swagger UI or Postman

**Code Review Checklist:**
- Follows existing patterns and conventions
- Proper error handling and validation
- No null pointer risks - use Optional where appropriate
- DTOs properly validated with Jakarta Validation
- Tests cover happy path and edge cases
- No sensitive data in logs
- MapStruct mappers updated for new fields
