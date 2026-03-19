package com.katya.quoterestapi.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base test configuration for integration tests.
 *
 * <p>Integration tests use a manually started PostgreSQL Docker container. Run the following
 * command to start the test database:
 *
 * <pre>
 * docker run -d --name quote-test-db \
 *   -e POSTGRES_DB=testdb \
 *   -e POSTGRES_USER=testuser \
 *   -e POSTGRES_PASSWORD=testpass \
 *   -p 5434:5432 \
 *   postgres:15-alpine
 * </pre>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5434/testdb",
      "spring.datasource.username=testuser",
      "spring.datasource.password=testpass",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.sql.init.mode=never"
    })
public abstract class BaseIntegrationTest {}
