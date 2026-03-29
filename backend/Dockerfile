# Multi-stage Dockerfile for Spring Boot Quote REST API (Multi-module structure)
# Stage 1: Build the application
FROM maven:3-eclipse-temurin-25 AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./pom.xml

# Copy backend module
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src

# Copy frontend pom to satisfy parent module requirements (not building frontend here)
COPY frontend/pom.xml ./frontend/pom.xml

# Build from parent directory - skip spotless check in Docker build
RUN mvn clean install -pl backend -am -DskipTests -Dspotless.check.skip=true -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR from build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
