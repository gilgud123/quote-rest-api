# Java 21 Upgrade Plan

## Problem Statement
Upgrade the Quote REST API project from Java 17 to Java 21, including Spring Boot upgrade to 3.4.x, dependency updates, and Java 21 feature enablement (virtual threads).

## Approach
1. Update Java version in all Maven POMs
2. Upgrade Spring Boot from 3.2.1 to 3.4.x (latest stable)
3. Update all dependencies and plugins to Java 21-compatible versions
4. Update Docker configurations to use Java 21 base images
5. Update Jenkins CI/CD configurations
6. Enable Java 21 features (virtual threads)
7. Verify compatibility and run tests
8. Update documentation

## Todos

### update-parent-pom
**Title:** Update parent POM for Java 21
**Description:** Update `pom.xml` in project root:
- Change java.version from 17 to 21
- Update maven.compiler.source/target to 21
- Update maven-compiler-plugin source/target to 21
- Update spotless.version to latest (2.43.0 is current, check for newer)

### update-backend-pom
**Title:** Update backend POM for Java 21 and Spring Boot 3.4.x
**Description:** Update `backend/pom.xml`:
- Change java.version property to 21
- Upgrade spring-boot.version to 3.4.x (check for latest stable)
- Update mapstruct.version to 1.6.3 (latest with Java 21 support)
- Update lombok.version to 1.18.36 (latest)
- Update testcontainers.version to 1.20.4 (latest)
- Update maven-compiler-plugin version to 3.13.0
- Update jacoco-maven-plugin version to 0.8.12
- Update springdoc-openapi version to 2.7.0 (latest for Spring Boot 3.4)
- Update GoogleJavaFormat version in spotless to 1.25.2

### update-dockerfile
**Title:** Update main Dockerfile for Java 21
**Description:** Update `Dockerfile`:
- Change build stage base image from `maven:3.9-eclipse-temurin-17` to `maven:3.9-eclipse-temurin-21`
- Change runtime stage base image from `eclipse-temurin:17-jre-alpine` to `eclipse-temurin:21-jre-alpine`
- Review and update JVM options for Java 21 optimization

### update-jenkins-dockerfile
**Title:** Update Jenkins Dockerfile for Java 21
**Description:** Update `Dockerfile.jenkins`:
- Change base image from `jenkins/jenkins:lts-jdk17` to `jenkins/jenkins:lts-jdk21`

### update-jenkinsfile
**Title:** Update Jenkinsfile for Java 21
**Description:** Update `Jenkinsfile`:
- Change agent Docker image from `maven:3.9-eclipse-temurin-17` to `maven:3.9-eclipse-temurin-21`
- Review and update MAVEN_OPTS if needed

### enable-virtual-threads
**Title:** Enable Java 21 virtual threads in Spring Boot
**Description:** Add virtual threads configuration to `backend/src/main/resources/application.yml`:
- Add property: `spring.threads.virtual.enabled: true`
- This enables virtual threads for web requests and scheduled tasks
- Document the change in comments

### verify-application-config
**Title:** Review application configuration for compatibility
**Description:** Review `backend/src/main/resources/application.yml` and `application-docker.yml`:
- Ensure all Spring Boot properties are compatible with 3.4.x
- Check for any deprecated properties
- Update logging configuration if needed

### run-tests
**Title:** Run full test suite to verify compatibility
**Description:** Execute backend tests to ensure Java 21 compatibility:
- Run `mvn clean test` in backend directory
- Verify all unit tests pass
- Verify integration tests with Testcontainers pass
- Check JaCoCo coverage report generation

### verify-docker-build
**Title:** Verify Docker build with Java 21
**Description:** Build and test Docker images:
- Run `docker compose build` to build all images
- Verify build succeeds without errors
- Test container startup with `docker compose up -d`
- Verify health checks pass

### update-readme
**Title:** Update README.md with Java 21 requirements
**Description:** Update project documentation:
- Change Java version requirement from 17 to 21
- Update any build instructions
- Note Java 21 features enabled (virtual threads)
- Update technology table if present

## Dependencies

- update-backend-pom depends on update-parent-pom
- enable-virtual-threads depends on update-backend-pom
- verify-application-config depends on update-backend-pom
- run-tests depends on: update-parent-pom, update-backend-pom, enable-virtual-threads
- verify-docker-build depends on: update-dockerfile, update-jenkins-dockerfile, run-tests
- update-readme depends on verify-docker-build

## Key Decisions

### Spring Boot Version
Upgrading to Spring Boot 3.4.x (latest stable) for:
- Full Java 21 support including virtual threads
- Latest security patches
- Performance improvements
- Better Jakarta EE 10 support

### Virtual Threads
Enabling virtual threads via Spring Boot configuration:
- Improves throughput for I/O-bound REST API operations
- Reduces resource usage for concurrent requests
- No code changes required (automatic)
- Can be toggled via configuration property

### Dependency Updates
Updating all dependencies to latest compatible versions:
- Ensures full Java 21 compatibility
- Gets security patches
- Reduces technical debt
- Future-proofs the project

### Docker Images
Using Eclipse Temurin 21 images:
- Official OpenJDK distribution
- Well-maintained and secure
- Alpine-based runtime for smaller image size
- Consistent with existing approach

## Testing Strategy

1. **Unit Tests**: Run all existing unit tests with Java 21
2. **Integration Tests**: Verify Testcontainers work with Java 21
3. **Docker Build**: Ensure multi-stage build works with new base images
4. **Runtime Verification**: Start application in Docker and verify:
   - Application starts successfully
   - Health checks pass
   - API endpoints respond correctly
   - Virtual threads are active (check thread dumps)
5. **Jenkins CI**: Verify Jenkins pipeline runs successfully with Java 21

## Rollback Plan

If issues arise:
1. Git revert commits in reverse order
2. All changes are version-controlled
3. Docker compose down/up will rebuild with old versions
4. No database schema changes - safe to rollback

## Notes

- This is a multi-module project (parent + backend + frontend)
- Frontend module not affected by Java upgrade
- Backend uses MapStruct with Lombok - annotation processor order matters
- Testcontainers requires Docker daemon
- Virtual threads are a JVM feature, no code changes needed
- Spring Boot 3.4.x maintains compatibility with 3.2.x APIs
