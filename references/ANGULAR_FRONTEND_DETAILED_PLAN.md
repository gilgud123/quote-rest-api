# Angular 20 Frontend Module - Detailed Implementation Plan

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Phase 1: Multi-Module Maven Setup](#phase-1-multi-module-maven-setup)
4. [Phase 2: Angular Module Initialization](#phase-2-angular-module-initialization)
5. [Phase 3: Core Infrastructure](#phase-3-core-infrastructure)
6. [Phase 4: Feature Implementation](#phase-4-feature-implementation)
7. [Phase 5: Testing](#phase-5-testing)
8. [Phase 6: Integration & CI/CD](#phase-6-integration--cicd)
9. [Testing Strategy](#testing-strategy)
10. [Deployment Strategy](#deployment-strategy)

---

## Overview

### Goal

Add an Angular 20 frontend as a separate Maven module to provide a full-featured web UI for the Quote REST API, with complete CRUD operations for Authors and Quotes, integrated with Keycloak OAuth2 authentication.

### Architecture Decision

- **Multi-module Maven project**: Parent POM with two modules (backend, frontend)
- **Development setup**: Separate servers (Angular: 4200, Backend: 8080)
- **Build integration**: frontend-maven-plugin for Node/npm in Maven lifecycle
- **Authentication**: Keycloak OAuth2 with JWT tokens
- **UI Framework**: Angular 20 + Angular Material

### Final Project Structure

```
quote-rest-api/
├── pom.xml                              # Parent POM
├── backend/                             # Backend module
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/katya/quoterestapi/
│   │   └── main/resources/
│   ├── Dockerfile
│   └── target/
├── frontend/                            # Frontend module (NEW)
│   ├── pom.xml                          # Maven + frontend-maven-plugin
│   ├── package.json
│   ├── angular.json
│   ├── tsconfig.json
│   ├── proxy.conf.json
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.ts
│   │   │   ├── app.config.ts
│   │   │   ├── app.routes.ts
│   │   │   ├── core/
│   │   │   │   ├── services/
│   │   │   │   │   ├── auth.service.ts
│   │   │   │   │   └── api.service.ts
│   │   │   │   ├── guards/
│   │   │   │   │   └── auth.guard.ts
│   │   │   │   ├── interceptors/
│   │   │   │   │   ├── auth.interceptor.ts
│   │   │   │   │   └── error.interceptor.ts
│   │   │   │   └── models/
│   │   │   │       ├── author.model.ts
│   │   │   │       └── quote.model.ts
│   │   │   ├── features/
│   │   │   │   ├── authors/
│   │   │   │   │   ├── authors.routes.ts
│   │   │   │   │   ├── services/
│   │   │   │   │   │   └── author.service.ts
│   │   │   │   │   └── components/
│   │   │   │   │       ├── author-list/
│   │   │   │   │       ├── author-create/
│   │   │   │   │       ├── author-edit/
│   │   │   │   │       └── author-delete-dialog/
│   │   │   │   └── quotes/
│   │   │   │       ├── quotes.routes.ts
│   │   │   │       ├── services/
│   │   │   │       │   └── quote.service.ts
│   │   │   │       └── components/
│   │   │   │           ├── quote-list/
│   │   │   │           ├── quote-create/
│   │   │   │           ├── quote-edit/
│   │   │   │           └── quote-delete-dialog/
│   │   │   └── shared/
│   │   │       └── components/
│   │   │           └── unauthorized/
│   │   ├── assets/
│   │   ├── environments/
│   │   │   ├── environment.ts
│   │   │   ├── environment.development.ts
│   │   │   └── environment.production.ts
│   │   ├── index.html
│   │   ├── main.ts
│   │   └── styles.scss
│   ├── dist/                            # Build output
│   └── node_modules/
├── docker-compose.yml
├── Jenkinsfile
└── README.md
```

---

## Prerequisites

### Required Software

- **Node.js 20.x or 22.x (LTS)** - JavaScript runtime (NOT 25.x - unsupported by Angular 20)
- **npm 10.x+** - Package manager
- **Angular CLI 20.x** - Command-line interface
- **Maven 3.6+** - Build tool
- **Java 17** - Backend runtime
- **Docker** - For PostgreSQL and Keycloak

### Installation Commands

```bash
# Install Node.js 20.x LTS (Recommended - Windows)
# Download from: https://nodejs.org/en/download/
# Choose "20.x.x LTS (Recommended For Most Users)"

# If you have Node 25.x, uninstall and install Node 20.x LTS:
# 1. Uninstall current Node.js from Control Panel
# 2. Download Node.js 20.x LTS from https://nodejs.org
# 3. Install and restart terminal

# Or use nvm (Node Version Manager) to switch versions:
# Windows: https://github.com/coreybutler/nvm-windows
nvm install 20
nvm use 20

# Install Angular CLI globally
npm install -g @angular/cli@20

# Verify installations
node --version        # Should be v20.x.x or v22.x.x (NOT v25.x.x)
npm --version         # Should be 10.x.x or higher
ng version            # Should be 20.x.x
```

**Important:** Angular 20 requires Node.js 20.x (Iron LTS) or 22.x. Node.js 25.x is NOT supported and will cause build failures.

### Keycloak Configuration (Automated)

The Keycloak frontend client will be automatically configured via a new realm JSON file that includes both the backend and frontend client configurations. No manual Keycloak admin console steps required!

---

## Phase 1: Multi-Module Maven Setup

**Status: ✅ COMPLETED**

**Notes on Actual Implementation:**
- Steps were completed in order 1.0 → 1.1 → 1.3 → 1.2 → 1.4 → 1.4.1 (backend Dockerfile) → 1.6
- Several fixes were required for Docker compatibility (see Step 1.0.1, 1.0.2, 1.4.1)
- PostgreSQL upgraded from 12 to 13 for Keycloak compatibility
- Spotless check must be skipped in Docker builds due to line ending differences

### Step 1.0: Create Keycloak Realm Configuration for Frontend

✅ **COMPLETED**

Before restructuring the project, create the Keycloak realm configuration that includes the frontend client.

#### Create `keycloak/realm-quote-frontend.json`:

This file extends the existing backend-only realm configuration to include the Angular frontend client.

```json
{
  "realm": "quote",
  "enabled": true,
  "roles": {
    "realm": [
      {
        "name": "USER",
        "description": "Read-only API access"
      },
      {
        "name": "ADMIN",
        "description": "Full API access"
      }
    ]
  },
  "clients": [
    {
      "clientId": "quote-api",
      "enabled": true,
      "publicClient": true,
      "directAccessGrantsEnabled": true,
      "standardFlowEnabled": false,
      "serviceAccountsEnabled": false,
      "protocol": "openid-connect",
      "redirectUris": ["*"],
      "webOrigins": ["*"]
    },
    {
      "clientId": "quote-frontend",
      "name": "Quote Frontend Application",
      "description": "Angular frontend client for Quote REST API",
      "enabled": true,
      "publicClient": true,
      "directAccessGrantsEnabled": true,
      "standardFlowEnabled": true,
      "implicitFlowEnabled": false,
      "serviceAccountsEnabled": false,
      "protocol": "openid-connect",
      "baseUrl": "http://localhost:4200",
      "rootUrl": "http://localhost:4200",
      "adminUrl": "http://localhost:4200",
      "redirectUris": [
        "http://localhost:4200/*"
      ],
      "webOrigins": [
        "http://localhost:4200",
        "+"
      ],
      "attributes": {
        "pkce.code.challenge.method": "S256"
      },
      "protocolMappers": [
        {
          "name": "audience-mapper",
          "protocol": "openid-connect",
          "protocolMapper": "oidc-audience-mapper",
          "config": {
            "included.client.audience": "quote-api",
            "id.token.claim": "false",
            "access.token.claim": "true"
          }
        }
      ]
    }
  ],
  "users": [
    {
      "username": "api-user",
      "firstName": "API",
      "lastName": "User",
      "email": "api-user@example.com",
      "enabled": true,
      "emailVerified": true,
      "credentials": [
        {
          "type": "password",
          "value": "password",
          "temporary": false
        }
      ],
      "realmRoles": ["USER"]
    },
    {
      "username": "api-admin",
      "firstName": "API",
      "lastName": "Admin",
      "email": "api-admin@example.com",
      "enabled": true,
      "emailVerified": true,
      "credentials": [
        {
          "type": "password",
          "value": "password",
          "temporary": false
        }
      ],
      "realmRoles": ["ADMIN", "USER"]
    },
    {
      "username": "frontend-user",
      "firstName": "Frontend",
      "lastName": "User",
      "email": "frontend-user@example.com",
      "enabled": true,
      "emailVerified": true,
      "credentials": [
        {
          "type": "password",
          "value": "password",
          "temporary": false
        }
      ],
      "realmRoles": ["USER"]
    },
    {
      "username": "frontend-admin",
      "firstName": "Frontend",
      "lastName": "Admin",
      "email": "frontend-admin@example.com",
      "enabled": true,
      "emailVerified": true,
      "credentials": [
        {
          "type": "password",
          "value": "password",
          "temporary": false
        }
      ],
      "realmRoles": ["ADMIN", "USER"]
    }
  ]
}
```

**Key Features of Frontend Client Configuration:**
- **Client ID**: `quote-frontend` - matches what Angular app expects
- **Public Client**: `true` - no client secret required (SPA security model)
- **Standard Flow**: `true` - enables OAuth2 Authorization Code Flow
- **PKCE**: Enabled via `pkce.code.challenge.method: S256` for enhanced security
- **Redirect URIs**: `http://localhost:4200/*` - allows callbacks from Angular dev server
- **Web Origins**: `http://localhost:4200` - enables CORS from frontend
- **Audience Mapper**: Includes `quote-api` in token audience for backend validation

**Test Users:**
- **frontend-user** / **password** - Regular user with USER role
- **frontend-admin** / **password** - Admin user with ADMIN and USER roles
- **api-user** / **password** - Backend-only user (original)
- **api-admin** / **password** - Backend-only admin (original)

#### Create `docker-compose-frontend.yml`:

This dedicated Docker Compose file includes all services needed for frontend development with the new realm configuration.

**Note:** Initial plan specified PostgreSQL 12, but Keycloak requires PostgreSQL 13+. Updated below.

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:13
    container_name: quote-postgres-frontend
    environment:
      POSTGRES_DB: quotedb
      POSTGRES_USER: quoteuser
      POSTGRES_PASSWORD: quotepass
    ports:
      - "5433:5432"  # Different port to avoid conflicts
    volumes:
      - postgres-frontend-data:/var/lib/postgresql/data
    networks:
      - quote-frontend-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U quoteuser -d quotedb"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: quote-keycloak-frontend
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/quotedb
      KC_DB_USERNAME: quoteuser
      KC_DB_PASSWORD: quotepass
      KC_HOSTNAME: localhost
      KC_HOSTNAME_PORT: 9090
      KC_HOSTNAME_STRICT: false
      KC_HTTP_ENABLED: true
      KC_HEALTH_ENABLED: true
    command:
      - start-dev
      - --import-realm
    ports:
      - "9090:8080"
    volumes:
      - ./keycloak/realm-quote-frontend.json:/opt/keycloak/data/import/realm-quote-frontend.json:ro
    networks:
      - quote-frontend-network
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 10
      start_period: 60s

  backend:
    build:
      context: .
      dockerfile: backend/Dockerfile
    container_name: quote-backend-frontend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/quotedb
      SPRING_DATASOURCE_USERNAME: quoteuser
      SPRING_DATASOURCE_PASSWORD: quotepass
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/quote
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI: http://keycloak:8080/realms/quote/protocol/openid-connect/certs
      # CORS configuration for frontend
      CORS_ALLOWED_ORIGINS: http://localhost:4200
    ports:
      - "8080:8080"
    networks:
      - quote-frontend-network
    depends_on:
      postgres:
        condition: service_healthy
      keycloak:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

networks:
  quote-frontend-network:
    driver: bridge

volumes:
  postgres-frontend-data:
    driver: local
```

**Key Features:**
- **Separate PostgreSQL port** (5433) to avoid conflicts with existing backend-only setup
- **Keycloak auto-imports** `realm-quote-frontend.json` on startup
- **Backend service** included for complete development environment
- **CORS_ALLOWED_ORIGINS** environment variable for frontend integration
- **Health checks** on all services for reliable startup
- **Dedicated network** (`quote-frontend-network`) for service isolation

**Usage:**

```bash
# Start full frontend development stack
docker-compose -f docker-compose-frontend.yml up -d

# Check service status
docker-compose -f docker-compose-frontend.yml ps

# View logs
docker-compose -f docker-compose-frontend.yml logs -f keycloak

# Stop services
docker-compose -f docker-compose-frontend.yml down

# Stop and remove volumes (clean slate)
docker-compose -f docker-compose-frontend.yml down -v
```

**Access:**
- **Backend API**: http://localhost:8080
- **Keycloak Admin**: http://localhost:9090 (admin/admin)
- **PostgreSQL**: localhost:5433
- **Frontend** (once running): http://localhost:4200

#### Step 1.0.1: Docker Compose Adjustments (Completed During Implementation)

**Issues Encountered and Fixed:**

1. **PostgreSQL Version**: Keycloak requires PostgreSQL 13+
   - Changed `postgres:12` → `postgres:13`
   - Error: "database version of at least '13.0.0', but the actual version is '12.22.0'"
2. **Keycloak Healthcheck**: Original healthcheck command failed
   - Changed from complex HTTP check to simple TCP check
   - Original used `wget` which isn't installed in Keycloak container
   - New: `["CMD-SHELL", "exec 3<>/dev/tcp/localhost/8080 || exit 1"]`
3. **Backend Build Context**: Multi-module structure requires root context
   - Changed from `context: ./backend, dockerfile: ../Dockerfile`
   - To: `context: ., dockerfile: backend/Dockerfile`
   - Allows copying both parent POM and backend module

### Step 1.1: Backup Current Project

✅ **COMPLETED**

```bash
# Create a backup
cd C:\Users\Katya de Vries\IdeaProjects
cp -r quote-rest-api quote-rest-api-backup
```

### Step 1.2: Create Parent POM

Create `pom.xml` in project root:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.katya</groupId>
    <artifactId>quote-application</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>Quote Application</name>
    <description>Multi-module project for Quote REST API with Angular frontend</description>

    <modules>
        <module>backend</module>
        <!-- <module>frontend</module> -->  <!-- Uncomment after Phase 2 -->
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 1.3: Move Backend to Subdirectory

✅ **COMPLETED**

```powershell
# Create backend directory
New-Item -ItemType Directory -Path "backend" -Force

# Move existing files to backend/
Move-Item -Path "src" -Destination "backend\src" -Force
Move-Item -Path "target" -Destination "backend\target" -Force
Move-Item -Path "pom.xml" -Destination "backend\pom.xml.old" -Force

# Files that stay at root level:
# - docker-compose.yml, docker-compose-frontend.yml
# - README.md, Jenkinsfile, Dockerfile
# - keycloak/, postman/, scripts/, tests/
```

### Step 1.4: Update Backend POM

✅ **COMPLETED**

Create new `backend/pom.xml` with parent reference (old POM saved as `pom.xml.old`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Add parent reference -->
    <parent>
        <groupId>com.katya</groupId>
        <artifactId>quote-application</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>quote-rest-api</artifactId>
    <packaging>jar</packaging>

    <name>Quote REST API Backend</name>
    <description>REST API for managing quotes and authors</description>

    <!-- Remove parent section for spring-boot-starter-parent -->
    <!-- Spring Boot will be managed via dependencyManagement -->

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.1</spring-boot.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <lombok.version>1.18.30</lombok.version>
        <testcontainers.version>1.19.3</testcontainers.version>
        <spotless.version>2.43.0</spotless.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- Keep all existing dependencies -->
    <dependencies>
        <!-- ... existing dependencies ... -->
    </dependencies>

    <!-- Keep all existing build configuration -->
    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin with repackage goal -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <!-- ... other plugins: maven-compiler-plugin, jacoco, spotless ... -->
        </plugins>
    </build>
</project>
```

**Important:** The Spring Boot Maven Plugin MUST have the `repackage` goal explicitly defined in executions to create an executable JAR with the proper manifest.

### Step 1.4.1: Create Backend Dockerfile

✅ **COMPLETED**

Create `backend/Dockerfile` for multi-module structure:

```dockerfile
# Multi-stage Dockerfile for Spring Boot Quote REST API (Multi-module structure)
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent pom first
COPY pom.xml ./pom.xml

# Copy backend module
COPY backend/pom.xml ./backend/pom.xml
COPY backend/src ./backend/src

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
```

**Key Changes from Original Dockerfile:**
- Build context is root (not backend), so paths are `backend/pom.xml` and `backend/src`
- Uses `mvn install -pl backend -am` to build specific module with its parent
- **Must skip Spotless**: `-Dspotless.check.skip=true` (Docker has CRLF/LF issues)
- Copies parent POM first for multi-module dependency resolution

### Step 1.5: Update Backend CORS Configuration

⏭️ **SKIPPED** - CORS already configured in existing SecurityConfig

The existing `SecurityConfig.java` already has CORS configuration. No changes needed at this step.

### Step 1.6: Verify Multi-Module Setup

✅ **COMPLETED**

```powershell
# Test build from root (temporarily comment out frontend module)
mvn clean install -DskipTests

# Expected output:
# [INFO] Reactor Summary:
# [INFO] Quote Application ................................. SUCCESS
# [INFO] Quote REST API Backend ............................ SUCCESS
# [INFO] BUILD SUCCESS
```

### Step 1.7: Test Docker Compose Frontend Setup

✅ **COMPLETED**

```powershell
# Start all services
docker-compose -f docker-compose-frontend.yml up -d --build

# Check status
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Expected output:
# quote-backend-frontend    Up (healthy)              0.0.0.0:8080->8080/tcp
# quote-keycloak-frontend   Up (healthy)              0.0.0.0:9090->8080/tcp
# quote-postgres-frontend   Up (healthy)              0.0.0.0:5433->5432/tcp

# Test backend health
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
# Output: {"status":"UP"}

# Test Swagger UI
Start-Process "http://localhost:8080/swagger-ui/index.html"

# Test Keycloak realm
Invoke-RestMethod -Uri "http://localhost:9090/realms/quote"
# Should return realm info with quote-frontend client

# Stop services
docker-compose -f docker-compose-frontend.yml down
```

**Verification Checklist:**
- ✅ PostgreSQL 13 running on port 5433
- ✅ Keycloak running on port 9090 with quote realm imported
- ✅ Backend API running on port 8080
- ✅ Swagger UI accessible
- ✅ All containers healthy

**Test Users Available:**
- `frontend-user` / `password` (USER role)
- `frontend-admin` / `password` (ADMIN + USER roles)
- `api-user` / `password` (USER role)
- `api-admin` / `password` (ADMIN + USER roles)

---

## Phase 1: Summary & Lessons Learned

### ✅ Phase 1 Complete - Multi-Module Maven Setup

All steps completed successfully! The project is now restructured as a multi-module Maven project with:
- Parent POM managing backend module (frontend module to be added in Phase 2)
- Backend moved to `backend/` subdirectory with updated POM
- Keycloak realm configuration with `quote-frontend` client
- Docker Compose setup for full development environment

### Key Files Created/Modified:

1. **`pom.xml`** (root) - Parent POM
2. **`backend/pom.xml`** - Backend module POM with parent reference
3. **`backend/Dockerfile`** - Multi-module Docker build
4. **`keycloak/realm-quote-frontend.json`** - Realm with frontend client
5. **`docker-compose-frontend.yml`** - Complete dev environment

### Lessons Learned & Fixes Applied:

1. **Spring Boot Maven Plugin**: Must explicitly define `<execution>` with `repackage` goal to create executable JAR
2. **PostgreSQL Version**: Keycloak requires PostgreSQL 13+ (not 12)
3. **Keycloak Healthcheck**: Use simple TCP check instead of HTTP check (wget not available)
4. **Docker Build Context**: Must build from root to access both parent POM and backend module
5. **Spotless in Docker**: Must skip with `-Dspotless.check.skip=true` due to line ending differences (Windows CRLF vs Linux LF)
6. **Module Order**: Comment out frontend module in parent POM until Phase 2 creates it

### Current Project Structure:

```
quote-rest-api/
├── pom.xml                              # Parent POM (modules: backend)
├── backend/                             # Backend module
│   ├── pom.xml                          # Backend POM with parent reference
│   ├── Dockerfile                       # Multi-module Docker build
│   ├── src/                             # Java source code
│   └── target/                          # Build output
├── keycloak/
│   ├── realm-quote.json                 # Original realm (backend only)
│   └── realm-quote-frontend.json        # New realm with frontend client
├── docker-compose.yml                   # Original backend-only setup
├── docker-compose-frontend.yml          # New full dev environment
└── [other root files...]
```

### Ready for Phase 2!

The foundation is now in place to create the Angular frontend module. Next steps will:
1. Create `frontend/` directory
2. Initialize Angular 20 project
3. Install Angular Material and Keycloak adapter
4. Create frontend Maven POM with frontend-maven-plugin

---

## Phase 2: Angular Module Initialization

### Step 2.1: Create Frontend Directory

```bash
cd C:\Users\Katya de Vries\IdeaProjects\quote-rest-api
mkdir frontend
cd frontend
```

### Step 2.2: Initialize Angular Project

```bash
# Create new Angular 20 project (standalone components)
ng new . --directory ./ --routing --style scss --standalone --skip-git

# Answer prompts:
# - Would you like to add Angular routing? Yes
# - Which stylesheet format? SCSS
# - Enable Server-Side Rendering (SSR) and Static Site Generation (SSG/Prerendering)? No
```

### Step 2.3: Install Required Dependencies

```bash
cd frontend

# Angular Material
ng add @angular/material
# Choose theme: Indigo/Pink or custom
# Set up global typography styles: Yes
# Include Angular animations: Yes

# Keycloak Angular adapter
npm install keycloak-angular keycloak-js --save

# Additional utilities
npm install rxjs @angular/common/http --save
```

### Step 2.4: Create Frontend Maven POM

Create `frontend/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.katya</groupId>
        <artifactId>quote-application</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>quote-frontend</artifactId>
    <packaging>jar</packaging>

    <name>Quote Application Frontend</name>
    <description>Angular 20 frontend for Quote REST API</description>

    <properties>
        <node.version>v20.18.3</node.version>
        <npm.version>10.9.2</npm.version>
        <frontend-maven-plugin.version>1.15.1</frontend-maven-plugin.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>${frontend-maven-plugin.version}</version>
                <configuration>
                    <nodeVersion>${node.version}</nodeVersion>
                    <npmVersion>${npm.version}</npmVersion>
                    <workingDirectory>${project.basedir}</workingDirectory>
                </configuration>
                <executions>
                    <!-- Install Node and npm -->
                    <execution>
                        <id>install node and npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                    </execution>

                    <!-- Install npm dependencies -->
                    <execution>
                        <id>npm install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <arguments>install</arguments>
                        </configuration>
                    </execution>

                    <!-- Build Angular app -->
                    <execution>
                        <id>npm run build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>prepare-package</phase>
                        <configuration>
                            <arguments>run build --configuration production</arguments>
                        </configuration>
                    </execution>

                    <!-- Optional: Run tests -->
                    <execution>
                        <id>npm run test</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>test</phase>
                        <configuration>
                            <arguments>run test -- --watch=false --browsers=ChromeHeadless</arguments>
                            <skip>true</skip> <!-- Enable when tests are ready -->
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Step 2.5: Configure Angular Build Output

Edit `frontend/angular.json` to set output path:

```json
{
  "projects": {
    "quote-frontend": {
      "architect": {
        "build": {
          "options": {
            "outputPath": "dist/quote-frontend",
            "baseHref": "/",
            // ... other options
          }
        }
      }
    }
  }
}
```

### Step 2.6: Create Development Proxy Configuration

Create `frontend/proxy.conf.json`:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  }
}
```

Update `frontend/angular.json` to use proxy:

```json
{
  "projects": {
    "quote-frontend": {
      "architect": {
        "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json"
          }
        }
      }
    }
  }
}
```

---

## Phase 3: Core Infrastructure

### Step 3.1: Create Environment Configuration

Edit `frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'quote-api-realm',
    clientId: 'quote-frontend'
  }
};
```

Edit `frontend/src/environments/environment.development.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: '/api/v1', // Uses proxy
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'quote-api-realm',
    clientId: 'quote-frontend'
  }
};
```

### Step 3.2: Create Core Models

Create `frontend/src/app/core/models/author.model.ts`:

```typescript
export interface Author {
  id?: number;
  name: string;
  biography?: string;
  birthDate?: string;
  deathDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthorPage {
  content: Author[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

Create `frontend/src/app/core/models/quote.model.ts`:

```typescript
export interface Quote {
  id?: number;
  text: string;
  source?: string;
  authorId: number;
  authorName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface QuotePage {
  content: Quote[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

### Step 3.3: Initialize Keycloak

Edit `frontend/src/app/app.config.ts`:

```typescript
import { ApplicationConfig, provideAppInitializer, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideClientHydration } from '@angular/platform-browser';
import Keycloak from 'keycloak-js';

import { routes } from './app.routes';
import { environment } from '../environments/environment';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';

export let keycloak: Keycloak;

function initializeKeycloak(): Promise<boolean> {
  keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId,
  });

  return keycloak.init({
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
    checkLoginIframe: false,
    pkceMethod: 'S256',
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideClientHydration(),
    provideAppInitializer(initializeKeycloak),
  ],
};
```

### Step 3.4: Create Authentication Service

Create `frontend/src/app/core/services/auth.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { KeycloakProfile } from 'keycloak-js';
import { keycloak } from '../../app.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  async isLoggedIn(): Promise<boolean> {
    return keycloak?.authenticated ?? false;
  }

  async getUserProfile(): Promise<KeycloakProfile | null> {
    if (!keycloak || !keycloak.authenticated) {
      return null;
    }
    try {
      return await keycloak.loadUserProfile();
    } catch (error) {
      console.error('Failed to load user profile:', error);
      return null;
    }
  }

  async getUsername(): Promise<string> {
    const profile = await this.getUserProfile();
    return profile?.username || keycloak?.tokenParsed?.['preferred_username'] || 'Unknown User';
  }

  login(): void {
    keycloak?.login();
  }

  logout(): void {
    keycloak?.logout({ redirectUri: window.location.origin });
  }

  getRoles(): string[] {
    return keycloak?.realmAccess?.roles ?? [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  async getToken(): Promise<string> {
    try {
      await keycloak?.updateToken(5);
      return keycloak?.token ?? '';
    } catch (error) {
      console.error('Failed to refresh token:', error);
      return '';
    }
  }

  getTokenParsed() {
    return keycloak?.tokenParsed;
  }

  async refreshToken(): Promise<boolean> {
    try {
      const refreshed = await keycloak?.updateToken(30);
      return refreshed ?? false;
    } catch (error) {
      console.error('Failed to refresh token:', error);
      return false;
    }
  }
}
```

### Step 3.5: Create Auth Guard

Create `frontend/src/app/core/guards/auth.guard.ts`:

```typescript
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { keycloak } from '../../app.config';

export const authGuard: CanActivateFn = async (route, state) => {
  const router = inject(Router);

  const isLoggedIn = keycloak?.authenticated ?? false;

  if (!isLoggedIn) {
    await keycloak?.login({
      redirectUri: window.location.origin + state.url
    });
    return false;
  }

  // Optional: Check for required roles
  const requiredRoles = route.data['roles'] as string[];
  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = keycloak?.realmAccess?.roles ?? [];
    const hasRole = requiredRoles.some(role => userRoles.includes(role));

    if (!hasRole) {
      router.navigate(['/unauthorized']);
      return false;
    }
  }

  return true;
};
```

### Step 3.6: Create HTTP Interceptors

The auth interceptor is already created. Here's the updated version:

`frontend/src/app/core/interceptors/auth.interceptor.ts`:

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { keycloak } from '../../app.config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip adding token for assets and non-API requests
  if (req.url.includes('/assets') || !req.url.includes('/api')) {
    return next(req);
  }

  // Add Bearer token if authenticated
  if (keycloak?.authenticated && keycloak?.token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${keycloak.token}`,
      },
    });
    return next(clonedRequest);
  }

  return next(req);
};
```

`frontend/src/app/core/interceptors/error.interceptor.ts`:

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      // Error handling will be implemented later
      console.error('HTTP Error:', error);
      return throwError(() => error);
    })
  );
};
```

        switchMap(token => {
          if (token) {
            const cloned = req.clone({
              setHeaders: {
                Authorization: `Bearer ${token}`
              }
            });
            return next(cloned);
          }
          return next(req);
        })

);
};

```

Create `frontend/src/app/core/interceptors/error.interceptor.ts`:

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError(error => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        errorMessage = error.error?.message || 
                       `Error: ${error.status} - ${error.statusText}`;
      }

      snackBar.open(errorMessage, 'Close', {
        duration: 5000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });

      return throwError(() => error);
    })
  );
};
```

### Step 3.7: Create API Base Service

Create `frontend/src/app/core/services/api.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  get<T>(endpoint: string, params?: any): Observable<T> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key].toString());
        }
      });
    }
    return this.http.get<T>(`${this.apiUrl}${endpoint}`, { params: httpParams });
  }

  post<T>(endpoint: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.apiUrl}${endpoint}`, body);
  }

  put<T>(endpoint: string, body: any): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}${endpoint}`, body);
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}${endpoint}`);
  }
}
```

---

## Phase 4: Feature Implementation

### Step 4.1: Create Authors Service

Create `frontend/src/app/features/authors/services/author.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Author, AuthorPage } from '../../../core/models/author.model';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {
  private endpoint = '/authors';

  constructor(private api: ApiService) {}

  getAuthors(page: number = 0, size: number = 10, sort?: string): Observable<AuthorPage> {
    const params: any = { page, size };
    if (sort) {
      params.sort = sort;
    }
    return this.api.get<AuthorPage>(this.endpoint, params);
  }

  getAuthor(id: number): Observable<Author> {
    return this.api.get<Author>(`${this.endpoint}/${id}`);
  }

  createAuthor(author: Author): Observable<Author> {
    return this.api.post<Author>(this.endpoint, author);
  }

  updateAuthor(id: number, author: Author): Observable<Author> {
    return this.api.put<Author>(`${this.endpoint}/${id}`, author);
  }

  deleteAuthor(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }

  searchAuthors(name: string, page: number = 0, size: number = 10): Observable<AuthorPage> {
    return this.api.get<AuthorPage>(`${this.endpoint}/search`, { name, page, size });
  }
}
```

### Step 4.2: Create Author List Component

```bash
cd frontend/src/app/features/authors
ng generate component components/author-list --standalone
```

Edit `author-list.component.ts`:

```typescript
import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { AuthorService } from '../../services/author.service';
import { Author } from '../../../../core/models/author.model';
import { AuthorDeleteDialogComponent } from '../author-delete-dialog/author-delete-dialog.component';

@Component({
  selector: 'app-author-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatDialogModule,
    MatSnackBarModule,
    ReactiveFormsModule
  ],
  templateUrl: './author-list.component.html',
  styleUrls: ['./author-list.component.scss']
})
export class AuthorListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name', 'biography', 'birthDate', 'actions'];
  dataSource = new MatTableDataSource<Author>();
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  searchControl = new FormControl('');

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private authorService: AuthorService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAuthors();
    this.setupSearch();
  }

  setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(searchTerm => {
        this.pageIndex = 0;
        this.loadAuthors(searchTerm || undefined);
      });
  }

  loadAuthors(searchTerm?: string): void {
    const sortParam = this.sort?.active ? 
      `${this.sort.active},${this.sort.direction}` : 
      'name,asc';

    if (searchTerm) {
      this.authorService.searchAuthors(searchTerm, this.pageIndex, this.pageSize)
        .subscribe(page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
        });
    } else {
      this.authorService.getAuthors(this.pageIndex, this.pageSize, sortParam)
        .subscribe(page => {
          this.dataSource.data = page.content;
          this.totalElements = page.totalElements;
        });
    }
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAuthors(this.searchControl.value || undefined);
  }

  onSortChange(sort: Sort): void {
    this.loadAuthors(this.searchControl.value || undefined);
  }

  createAuthor(): void {
    this.router.navigate(['/authors/create']);
  }

  editAuthor(author: Author): void {
    this.router.navigate(['/authors/edit', author.id]);
  }

  deleteAuthor(author: Author): void {
    const dialogRef = this.dialog.open(AuthorDeleteDialogComponent, {
      width: '400px',
      data: author
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && author.id) {
        this.authorService.deleteAuthor(author.id).subscribe(() => {
          this.snackBar.open('Author deleted successfully', 'Close', { duration: 3000 });
          this.loadAuthors();
        });
      }
    });
  }
}
```

Edit `author-list.component.html`:

```html
<div class="author-list-container">
  <div class="header">
    <h1>Authors</h1>
    <button mat-raised-button color="primary" (click)="createAuthor()">
      <mat-icon>add</mat-icon>
      Add Author
    </button>
  </div>

  <mat-form-field class="search-field" appearance="outline">
    <mat-label>Search Authors</mat-label>
    <input matInput [formControl]="searchControl" placeholder="Search by name...">
    <mat-icon matSuffix>search</mat-icon>
  </mat-form-field>

  <div class="table-container">
    <table mat-table [dataSource]="dataSource" matSort (matSortChange)="onSortChange($event)">
      <ng-container matColumnDef="id">
        <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
        <td mat-cell *matCellDef="let author">{{ author.id }}</td>
      </ng-container>

      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
        <td mat-cell *matCellDef="let author">{{ author.name }}</td>
      </ng-container>

      <ng-container matColumnDef="biography">
        <th mat-header-cell *matHeaderCellDef>Biography</th>
        <td mat-cell *matCellDef="let author">
          {{ author.biography | slice:0:100 }}{{ author.biography?.length > 100 ? '...' : '' }}
        </td>
      </ng-container>

      <ng-container matColumnDef="birthDate">
        <th mat-header-cell *matHeaderCellDef mat-sort-header>Birth Date</th>
        <td mat-cell *matCellDef="let author">{{ author.birthDate | date:'mediumDate' }}</td>
      </ng-container>

      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef>Actions</th>
        <td mat-cell *matCellDef="let author">
          <button mat-icon-button color="primary" (click)="editAuthor(author)" matTooltip="Edit">
            <mat-icon>edit</mat-icon>
          </button>
          <button mat-icon-button color="warn" (click)="deleteAuthor(author)" matTooltip="Delete">
            <mat-icon>delete</mat-icon>
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>

    <mat-paginator
      [length]="totalElements"
      [pageSize]="pageSize"
      [pageSizeOptions]="[5, 10, 25, 50]"
      (page)="onPageChange($event)"
      showFirstLastButtons>
    </mat-paginator>
  </div>
</div>
```

### Step 4.3: Create Author Form Components

*Similar pattern for author-create, author-edit, author-delete-dialog components*

### Step 4.4: Create Quotes Service and Components

*Follow same pattern as Authors - service, list, create, edit, delete-dialog components*

### Step 4.5: Create Routing

Edit `frontend/src/app/app.routes.ts`:

```typescript
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/authors',
    pathMatch: 'full'
  },
  {
    path: 'authors',
    canActivate: [authGuard],
    loadChildren: () => import('./features/authors/authors.routes').then(m => m.AUTHOR_ROUTES)
  },
  {
    path: 'quotes',
    canActivate: [authGuard],
    loadChildren: () => import('./features/quotes/quotes.routes').then(m => m.QUOTE_ROUTES)
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./shared/components/unauthorized/unauthorized.component')
      .then(m => m.UnauthorizedComponent)
  },
  {
    path: '**',
    redirectTo: '/authors'
  }
];
```

Create `frontend/src/app/features/authors/authors.routes.ts`:

```typescript
import { Routes } from '@angular/router';
import { AuthorListComponent } from './components/author-list/author-list.component';
import { AuthorCreateComponent } from './components/author-create/author-create.component';
import { AuthorEditComponent } from './components/author-edit/author-edit.component';

export const AUTHOR_ROUTES: Routes = [
  { path: '', component: AuthorListComponent },
  { path: 'create', component: AuthorCreateComponent },
  { path: 'edit/:id', component: AuthorEditComponent }
];
```

---

## Phase 5: Testing

### Step 5.1: Unit Tests for Core Services

**Create test for AuthService:**

Create `frontend/src/app/core/services/auth.service.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let mockKeycloak: any;

  beforeEach(() => {
    // Mock Keycloak instance
    mockKeycloak = {
      authenticated: true,
      tokenParsed: {
        preferred_username: 'testuser',
        email: 'test@example.com',
        given_name: 'Test',
        family_name: 'User',
        realm_access: { roles: ['USER', 'ADMIN'] }
      },
      token: 'mock-token',
      login: jasmine.createSpy('login').and.returnValue(Promise.resolve()),
      logout: jasmine.createSpy('logout').and.returnValue(Promise.resolve()),
      updateToken: jasmine.createSpy('updateToken').and.returnValue(Promise.resolve(true))
    };

    TestBed.configureTestingModule({
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check if user is logged in', () => {
    expect(service.isLoggedIn()).toBe(true);
  });

  it('should get user profile', () => {
    const profile = service.getUserProfile();
    expect(profile).toBeDefined();
    expect(profile?.username).toBe('testuser');
    expect(profile?.email).toBe('test@example.com');
  });

  it('should get user roles', () => {
    const roles = service.getRoles();
    expect(roles).toContain('USER');
    expect(roles).toContain('ADMIN');
  });

  it('should check if user has role', () => {
    expect(service.hasRole('USER')).toBe(true);
    expect(service.hasRole('ADMIN')).toBe(true);
    expect(service.hasRole('SUPERADMIN')).toBe(false);
  });

  it('should get token', () => {
    expect(service.getToken()).toBe('mock-token');
  });
});
```

**Create test for ApiService:**

Create `frontend/src/app/core/services/api.service.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should make GET request', () => {
    const mockData = { id: 1, name: 'Test' };
    
    service.get<any>('/test').subscribe(data => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${apiUrl}/test`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should make GET request with query params', () => {
    const params = { page: 0, size: 10 };
    
    service.get<any>('/test', params).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/test?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should make POST request', () => {
    const postData = { name: 'New Item' };
    const mockResponse = { id: 1, ...postData };
    
    service.post<any>('/test', postData).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/test`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(postData);
    req.flush(mockResponse);
  });

  it('should make PUT request', () => {
    const putData = { id: 1, name: 'Updated Item' };
    
    service.put<any>('/test/1', putData).subscribe(data => {
      expect(data).toEqual(putData);
    });

    const req = httpMock.expectOne(`${apiUrl}/test/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(putData);
  });

  it('should make DELETE request', () => {
    service.delete<void>('/test/1').subscribe();

    const req = httpMock.expectOne(`${apiUrl}/test/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should filter null/undefined query params', () => {
    const params = { page: 0, size: null, filter: undefined };
    
    service.get<any>('/test', params).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/test?page=0`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
```

### Step 5.2: Unit Tests for Feature Services

**Create test for AuthorService:**

Create `frontend/src/app/features/authors/services/author.service.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthorService } from './author.service';
import { Author, AuthorPage } from '../../../core/models';
import { environment } from '../../../../environments/environment';

describe('AuthorService', () => {
  let service: AuthorService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;

  const mockAuthor: Author = {
    id: 1,
    name: 'Socrates',
    biography: 'Ancient Greek philosopher',
    birthYear: -469,
    deathYear: -399
  };

  const mockAuthorPage: AuthorPage = {
    content: [mockAuthor],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthorService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get authors with pagination', () => {
    service.getAuthors(0, 10, 'name,asc').subscribe(page => {
      expect(page).toEqual(mockAuthorPage);
      expect(page.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/authors?page=0&size=10&sort=name,asc`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuthorPage);
  });

  it('should get single author by id', () => {
    service.getAuthor(1).subscribe(author => {
      expect(author).toEqual(mockAuthor);
    });

    const req = httpMock.expectOne(`${apiUrl}/authors/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuthor);
  });

  it('should create new author', () => {
    const newAuthor: Author = {
      name: 'Plato',
      biography: 'Student of Socrates',
      birthYear: -428,
      deathYear: -348
    };

    service.createAuthor(newAuthor).subscribe(author => {
      expect(author.id).toBe(2);
      expect(author.name).toBe('Plato');
    });

    const req = httpMock.expectOne(`${apiUrl}/authors`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newAuthor);
    req.flush({ id: 2, ...newAuthor });
  });

  it('should update existing author', () => {
    const updatedAuthor: Author = { ...mockAuthor, biography: 'Updated biography' };

    service.updateAuthor(1, updatedAuthor).subscribe(author => {
      expect(author.biography).toBe('Updated biography');
    });

    const req = httpMock.expectOne(`${apiUrl}/authors/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedAuthor);
  });

  it('should delete author', () => {
    service.deleteAuthor(1).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/authors/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should search authors by name', () => {
    service.searchAuthors('Socrates', 0, 10).subscribe(page => {
      expect(page.content[0].name).toBe('Socrates');
    });

    const req = httpMock.expectOne(`${apiUrl}/authors/search?name=Socrates&page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuthorPage);
  });
});
```

### Step 5.3: Component Tests

**Create test for AuthorList component:**

Create `frontend/src/app/features/authors/components/author-list/author-list.spec.ts`:

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { AuthorList } from './author-list';
import { AuthorService } from '../../services/author.service';
import { Author, AuthorPage } from '../../../../core/models';

describe('AuthorList', () => {
  let component: AuthorList;
  let fixture: ComponentFixture<AuthorList>;
  let authorService: jasmine.SpyObj<AuthorService>;

  const mockAuthorPage: AuthorPage = {
    content: [
      { id: 1, name: 'Socrates', biography: 'Greek philosopher', birthYear: -469, deathYear: -399 },
      { id: 2, name: 'Plato', biography: 'Student of Socrates', birthYear: -428, deathYear: -348 }
    ],
    totalElements: 2,
    totalPages: 1,
    size: 10,
    number: 0
  };

  beforeEach(async () => {
    const authorServiceSpy = jasmine.createSpyObj('AuthorService', [
      'getAuthors',
      'getAuthor',
      'createAuthor',
      'updateAuthor',
      'deleteAuthor',
      'searchAuthors'
    ]);

    await TestBed.configureTestingModule({
      imports: [AuthorList],
      providers: [
        { provide: AuthorService, useValue: authorServiceSpy },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    authorService = TestBed.inject(AuthorService) as jasmine.SpyObj<AuthorService>;
    fixture = TestBed.createComponent(AuthorList);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load authors on init', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));

    fixture.detectChanges(); // triggers ngOnInit

    expect(authorService.getAuthors).toHaveBeenCalledWith(0, 10, 'name,asc');
    expect(component.authors.length).toBe(2);
    expect(component.totalElements).toBe(2);
  });

  it('should handle loading state', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));

    expect(component.loading).toBe(false);
    
    component.loadAuthors();
    expect(component.loading).toBe(true);

    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.loading).toBe(false);
    });
  });

  it('should handle error when loading authors fails', () => {
    const errorMessage = 'Failed to load authors';
    authorService.getAuthors.and.returnValue(throwError(() => new Error('Server error')));

    component.ngOnInit();

    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBe(false);
  });

  it('should navigate to next page', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));
    component.pageIndex = 0;
    component.totalElements = 20;

    component.nextPage();

    expect(component.pageIndex).toBe(1);
    expect(authorService.getAuthors).toHaveBeenCalled();
  });

  it('should not navigate to next page when on last page', () => {
    component.pageIndex = 0;
    component.pageSize = 10;
    component.totalElements = 5;

    const initialPage = component.pageIndex;
    component.nextPage();

    expect(component.pageIndex).toBe(initialPage);
  });

  it('should navigate to previous page', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));
    component.pageIndex = 1;

    component.previousPage();

    expect(component.pageIndex).toBe(0);
    expect(authorService.getAuthors).toHaveBeenCalled();
  });

  it('should not navigate to previous page when on first page', () => {
    component.pageIndex = 0;

    component.previousPage();

    expect(component.pageIndex).toBe(0);
  });

  it('should calculate hasNextPage correctly', () => {
    component.pageIndex = 0;
    component.pageSize = 10;
    component.totalElements = 20;
    expect(component.hasNextPage).toBe(true);

    component.totalElements = 5;
    expect(component.hasNextPage).toBe(false);
  });

  it('should calculate hasPreviousPage correctly', () => {
    component.pageIndex = 0;
    expect(component.hasPreviousPage).toBe(false);

    component.pageIndex = 1;
    expect(component.hasPreviousPage).toBe(true);
  });
});
```

### Step 5.4: Guard Tests

**Create test for Auth Guard:**

Create `frontend/src/app/core/guards/auth.guard.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let router: jasmine.SpyObj<Router>;
  let mockKeycloak: any;
  let mockRoute: ActivatedRouteSnapshot;
  let mockState: RouterStateSnapshot;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerSpy }
      ]
    });

    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockRoute = {} as ActivatedRouteSnapshot;
    mockState = { url: '/authors' } as RouterStateSnapshot;

    // Mock keycloak instance
    mockKeycloak = {
      authenticated: true,
      tokenParsed: {
        realm_access: { roles: ['USER'] }
      },
      login: jasmine.createSpy('login')
    };
  });

  it('should allow access when user is authenticated', () => {
    const canActivate = TestBed.runInInjectionContext(() => 
      authGuard(mockRoute, mockState)
    );

    expect(canActivate).toBe(true);
  });

  it('should redirect to login when user is not authenticated', () => {
    mockKeycloak.authenticated = false;

    const canActivate = TestBed.runInInjectionContext(() => 
      authGuard(mockRoute, mockState)
    );

    expect(canActivate).toBe(false);
    expect(mockKeycloak.login).toHaveBeenCalled();
  });

  it('should check required roles', () => {
    mockRoute.data = { roles: ['ADMIN'] };
    mockKeycloak.tokenParsed.realm_access.roles = ['USER'];

    const canActivate = TestBed.runInInjectionContext(() => 
      authGuard(mockRoute, mockState)
    );

    expect(canActivate).toBe(false);
  });

  it('should allow access when user has required role', () => {
    mockRoute.data = { roles: ['ADMIN'] };
    mockKeycloak.tokenParsed.realm_access.roles = ['USER', 'ADMIN'];

    const canActivate = TestBed.runInInjectionContext(() => 
      authGuard(mockRoute, mockState)
    );

    expect(canActivate).toBe(true);
  });
});
```

### Step 5.5: Interceptor Tests

**Create test for Auth Interceptor:**

Create `frontend/src/app/core/interceptors/auth.interceptor.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let mockKeycloak: any;

  beforeEach(() => {
    mockKeycloak = {
      authenticated: true,
      token: 'mock-jwt-token'
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header to API requests', () => {
    httpClient.get('/api/v1/authors').subscribe();

    const req = httpMock.expectOne('/api/v1/authors');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer mock-jwt-token');
    req.flush({});
  });

  it('should not add Authorization header to non-API requests', () => {
    httpClient.get('/assets/config.json').subscribe();

    const req = httpMock.expectOne('/assets/config.json');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should not add Authorization header when not authenticated', () => {
    mockKeycloak.authenticated = false;

    httpClient.get('/api/v1/authors').subscribe();

    const req = httpMock.expectOne('/api/v1/authors');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
```

**Create test for Error Interceptor:**

Create `frontend/src/app/core/interceptors/error.interceptor.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should handle 401 Unauthorized error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(401);
        done();
      }
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
  });

  it('should handle 403 Forbidden error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(403);
        done();
      }
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  it('should handle 404 Not Found error', (done) => {
    httpClient.get('/api/v1/authors/999').subscribe({
      error: (error) => {
        expect(error.status).toBe(404);
        done();
      }
    });

    const req = httpMock.expectOne('/api/v1/authors/999');
    req.flush('Not Found', { status: 404, statusText: 'Not Found' });
  });

  it('should handle 500 Server Error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(500);
        done();
      }
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Internal Server Error', { status: 500, statusText: 'Internal Server Error' });
  });
});
```

### Step 5.6: Run Tests

**Run all tests:**

```bash
cd frontend
npm test
```

**Run tests with code coverage:**

```bash
npm test -- --code-coverage
```

**View coverage report:**

Open `frontend/coverage/index.html` in a browser.

**Test configuration in `angular.json`:**

```json
{
  "test": {
    "builder": "@angular-devkit/build-angular:karma",
    "options": {
      "main": "src/test.ts",
      "polyfills": ["zone.js", "zone.js/testing"],
      "tsConfig": "tsconfig.spec.json",
      "karmaConfig": "karma.conf.js",
      "codeCoverage": true,
      "codeCoverageExclude": [
        "src/**/*.spec.ts",
        "src/test.ts"
      ]
    }
  }
}
```

**Update `karma.conf.js` for CI:**

```javascript
module.exports = function(config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      clearContext: false,
      jasmine: {
        random: false
      }
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'lcovonly' }
      ],
      check: {
        global: {
          statements: 80,
          branches: 70,
          functions: 80,
          lines: 80
        }
      }
    },
    reporters: ['progress', 'kjhtml', 'coverage'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['Chrome'],
    customLaunchers: {
      ChromeHeadlessCI: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-gpu']
      }
    },
    singleRun: false,
    restartOnFileChange: true
  });
};
```

### Step 5.7: Enable Tests in Maven Build

Edit `frontend/pom.xml` to uncomment the test execution:

```xml
<execution>
    <id>npm run test</id>
    <goals>
        <goal>npm</goal>
    </goals>
    <phase>test</phase>
    <configuration>
        <arguments>run test -- --watch=false --browsers=ChromeHeadlessCI --code-coverage</arguments>
        <!-- Remove skip tag to enable tests -->
    </configuration>
</execution>
```

---

## Phase 6: Integration & CI/CD

### Step 6.1: Frontend Development Workflow

The `docker-compose-frontend.yml` file (created in Phase 1, Step 1.0) provides a complete development environment.

**Start Development Environment:**

```bash
# Terminal 1: Start all backend services
docker-compose -f docker-compose-frontend.yml up -d

# Wait for services to be healthy (check logs)
docker-compose -f docker-compose-frontend.yml logs -f

# Terminal 2: Start Angular development server
cd frontend
npm start
```

**Access Points:**
- **Frontend UI**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Keycloak Admin**: http://localhost:9090 (admin/admin)

**Test Users** (pre-configured in realm):
- **frontend-user** / **password** - Regular user
- **frontend-admin** / **password** - Admin user

**Development Workflow:**
1. Backend changes: Restart backend container or run Spring Boot locally
2. Frontend changes: Auto-reload with `ng serve` hot module replacement
3. Keycloak changes: Edit `realm-quote-frontend.json` and restart Keycloak container

**Optional**: Add development notes to main `docker-compose.yml`:

```yaml
# Note: For frontend development, use docker-compose-frontend.yml instead
# docker-compose -f docker-compose-frontend.yml up -d
```

### Step 6.2: Update Jenkinsfile

Add Node.js installation and frontend build:

```groovy
stage('Setup') {
    steps {
        echo '⚙️ Setting up environment...'
        sh '''
            # Install Docker CLI
            # ... existing Docker setup ...
            
            # Install Node.js 20.x
            curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
            apt-get install -y nodejs
            
            # Verify installations
            node --version
            npm --version
        '''
    }
}

stage('Build') {
    steps {
        echo '🔨 Building application...'
        sh '''
            # Build both backend and frontend
            mvn ${MAVEN_CLI_OPTS} clean install
        '''
    }
}
```

### Step 6.3: Create Development Scripts

Create `scripts/start-frontend-dev.sh`:

```bash
#!/bin/bash
# Start frontend development environment with Docker services

echo "🚀 Starting Quote REST API - Frontend Development Environment"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Start all services with frontend-specific Docker Compose
echo "📦 Starting PostgreSQL, Keycloak, and Backend services..."
docker-compose -f docker-compose-frontend.yml up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
echo "   This may take 60-90 seconds for Keycloak initialization..."

# Wait for backend health check
until docker-compose -f docker-compose-frontend.yml ps | grep "quote-backend-frontend" | grep -q "healthy"; do
    echo "   ⏳ Waiting for backend to be healthy..."
    sleep 5
done

echo "✅ All services are ready!"
echo ""
echo "📊 Service Status:"
docker-compose -f docker-compose-frontend.yml ps
echo ""

# Start Angular development server
echo "🎨 Starting Angular frontend development server..."
cd frontend

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing npm dependencies (first time setup)..."
    npm install
fi

echo ""
echo "✅ Development environment is ready!"
echo ""
echo "🌐 Access Points:"
echo "   Frontend:  http://localhost:4200"
echo "   Backend:   http://localhost:8080"
echo "   Swagger:   http://localhost:8080/swagger-ui.html"
echo "   Keycloak:  http://localhost:9090 (admin/admin)"
echo ""
echo "👤 Test Users:"
echo "   frontend-user  / password  (USER role)"
echo "   frontend-admin / password  (ADMIN role)"
echo ""
echo "🔧 Starting Angular dev server..."
echo "   Press Ctrl+C to stop the frontend"
echo "   Use 'docker-compose -f docker-compose-frontend.yml down' to stop backend services"
echo ""

# Start Angular dev server (foreground)
npm start

# Cleanup message (shown after Ctrl+C)
echo ""
echo "🛑 Frontend stopped. Backend services still running."
echo "   To stop all services: docker-compose -f docker-compose-frontend.yml down"
```

Create `scripts/stop-frontend-dev.sh`:

```bash
#!/bin/bash
# Stop frontend development environment

echo "🛑 Stopping Quote REST API - Frontend Development Environment"

# Stop Docker services
docker-compose -f docker-compose-frontend.yml down

echo "✅ All services stopped!"
echo ""
echo "💡 To remove volumes and clean database: docker-compose -f docker-compose-frontend.yml down -v"
```

Create `scripts/start-frontend-dev.ps1` (Windows PowerShell version):

```powershell
# Start frontend development environment with Docker services

Write-Host "🚀 Starting Quote REST API - Frontend Development Environment" -ForegroundColor Green
Write-Host ""

# Check if Docker is running
try {
    docker info 2>&1 | Out-Null
} catch {
    Write-Host "❌ Error: Docker is not running. Please start Docker and try again." -ForegroundColor Red
    exit 1
}

# Start all services
Write-Host "📦 Starting PostgreSQL, Keycloak, and Backend services..."
docker-compose -f docker-compose-frontend.yml up -d

# Wait for services
Write-Host "⏳ Waiting for services to be ready..."
Write-Host "   This may take 60-90 seconds for Keycloak initialization..."

Start-Sleep -Seconds 10

# Wait for backend to be healthy
$maxAttempts = 24
$attempt = 0
do {
    $containerStatus = docker-compose -f docker-compose-frontend.yml ps --format json | ConvertFrom-Json | Where-Object { $_.Name -eq "quote-backend-frontend" }
    if ($containerStatus.Health -eq "healthy") {
        break
    }
    Write-Host "   ⏳ Waiting for backend to be healthy... ($attempt/$maxAttempts)"
    Start-Sleep -Seconds 5
    $attempt++
} while ($attempt -lt $maxAttempts)

Write-Host "✅ All services are ready!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Service Status:"
docker-compose -f docker-compose-frontend.yml ps
Write-Host ""

# Start Angular development server
Write-Host "🎨 Starting Angular frontend development server..."
Set-Location frontend

# Check if node_modules exists
if (-not (Test-Path "node_modules")) {
    Write-Host "📦 Installing npm dependencies (first time setup)..."
    npm install
}

Write-Host ""
Write-Host "✅ Development environment is ready!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Access Points:" -ForegroundColor Cyan
Write-Host "   Frontend:  http://localhost:4200"
Write-Host "   Backend:   http://localhost:8080"
Write-Host "   Swagger:   http://localhost:8080/swagger-ui.html"
Write-Host "   Keycloak:  http://localhost:9090 (admin/admin)"
Write-Host ""
Write-Host "👤 Test Users:" -ForegroundColor Cyan
Write-Host "   frontend-user  / password  (USER role)"
Write-Host "   frontend-admin / password  (ADMIN role)"
Write-Host ""
Write-Host "🔧 Starting Angular dev server..." -ForegroundColor Yellow
Write-Host "   Press Ctrl+C to stop the frontend"
Write-Host "   Use 'docker-compose -f docker-compose-frontend.yml down' to stop backend services"
Write-Host ""

# Start Angular dev server
npm start

# Cleanup message
Write-Host ""
Write-Host "🛑 Frontend stopped. Backend services still running." -ForegroundColor Yellow
Write-Host "   To stop all services: docker-compose -f docker-compose-frontend.yml down"
```

---

## Testing Strategy

### Manual Testing Checklist

1. **Authentication**
   - [ ] User can access login page
   - [ ] User can log in with Keycloak credentials
   - [ ] JWT token is stored and sent with requests
   - [ ] Token refresh works automatically
   - [ ] User can log out successfully
2. **Authors CRUD**
   - [ ] List authors with pagination
   - [ ] Search authors by name
   - [ ] Create new author with validation
   - [ ] Edit existing author
   - [ ] Delete author (with confirmation)
   - [ ] Error handling displays messages
3. **Quotes CRUD**
   - [ ] List quotes with author names
   - [ ] Create quote with author selection
   - [ ] Edit existing quote
   - [ ] Delete quote (with confirmation)
   - [ ] Pagination and filtering work
4. **Integration**
   - [ ] CORS allows frontend requests
   - [ ] Proxy configuration works in development
   - [ ] Maven build completes successfully
   - [ ] Jenkins pipeline builds both modules

### Automated Testing (Future)

- Unit tests for services (Jasmine/Karma)
- E2E tests (Playwright - already available)
- Component tests (Angular Testing Library)

---

## Deployment Strategy

### Development

**Recommended: Use the automated development script**

```bash
# Linux/Mac
./scripts/start-frontend-dev.sh

# Windows PowerShell
.\scripts\start-frontend-dev.ps1
```

**Or manually:**

```bash
# Start all services with automated Keycloak realm import
docker-compose -f docker-compose-frontend.yml up -d

# Wait for services to be healthy (60-90 seconds)
docker-compose -f docker-compose-frontend.yml logs -f

# In a new terminal, start Angular
cd frontend
npm start
```

**Access:**
- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- Keycloak: http://localhost:9090 (admin/admin)
- Test with: frontend-user / password

### Production (Future Enhancement)

Option 1: Serve Angular from Spring Boot static resources
Option 2: Separate deployments with reverse proxy (nginx)
Option 3: CDN for frontend static files

---

## Keycloak Configuration Details

### Automated Setup via Docker

The plan includes automated Keycloak configuration that eliminates manual setup steps:

**Files Created:**
1. **`keycloak/realm-quote-frontend.json`** - Extended realm configuration with frontend client
2. **`docker-compose-frontend.yml`** - Complete development stack with auto-import

**Pre-configured Test Users:**
- **frontend-user** / **password** - Regular USER role
- **frontend-admin** / **password** - ADMIN + USER roles
- **api-user** / **password** - Backend-only USER (for API testing)
- **api-admin** / **password** - Backend-only ADMIN (for API testing)

**Frontend Client Configuration:**
- **Client ID**: `quote-frontend`
- **Type**: Public client (no secret required for SPA)
- **Security**: PKCE enabled (S256 challenge method)
- **Flow**: OAuth2 Authorization Code Flow with PKCE
- **Redirect URIs**: `http://localhost:4200/*`
- **Web Origins**: `http://localhost:4200`, `+` (same-origin)
- **Base URL**: `http://localhost:4200`

**Key Security Features:**
- PKCE (Proof Key for Code Exchange) prevents authorization code interception
- No client secret stored in browser (public client model)
- Audience mapper ensures tokens are valid for backend API
- Token refresh handled automatically by keycloak-angular library

**Startup Process:**
1. Docker Compose starts Keycloak container
2. Keycloak automatically imports `realm-quote-frontend.json` via `--import-realm` flag
3. Realm `quote` created with both `quote-api` and `quote-frontend` clients
4. All test users created with hashed passwords
5. Ready for Angular app to connect immediately

**Verification:**

```bash
# After starting docker-compose-frontend.yml
# Open Keycloak Admin Console: http://localhost:9090
# Login: admin / admin
# Select "quote" realm
# Check Clients: should see "quote-api" and "quote-frontend"
# Check Users: should see 4 users (api-user, api-admin, frontend-user, frontend-admin)
```

---

## Troubleshooting Guide

### Common Issues

**1. CORS Errors**
- Verify SecurityConfig allows http://localhost:4200
- Check proxy.conf.json is configured
- Ensure Angular dev server uses proxy

**2. Keycloak Authentication Fails**
- **NEW**: Verify you're using `docker-compose-frontend.yml` (includes realm auto-import)
- Check realm name is `quote` (not `quote-api-realm`)
- Verify client ID is `quote-frontend` in Angular environment config
- Check redirect URIs match exactly: `http://localhost:4200/*`
- Ensure client is set to "public" (check realm JSON)
- Verify Keycloak is accessible at http://localhost:9090
- Check browser console for CORS errors from Keycloak

**2a. Realm Not Found Error**
- Make sure you used `docker-compose-frontend.yml` not `docker-compose.yml`
- Check Keycloak logs: `docker-compose -f docker-compose-frontend.yml logs keycloak`
- Verify `realm-quote-frontend.json` exists in `keycloak/` directory
- Restart Keycloak: `docker-compose -f docker-compose-frontend.yml restart keycloak`

**3. Maven Build Fails**
- **CRITICAL**: Check Node.js version must be 20.x or 22.x (NOT 25.x)
- Run `node --version` - if v25.x, downgrade to v20.x LTS
- Uninstall Node 25.x and install Node 20.x LTS from nodejs.org
- Or use nvm: `nvm install 20 && nvm use 20`
- Clear npm cache: `npm cache clean --force`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`
- Verify frontend-maven-plugin version is 1.15.1+

**4. Node.js Version Unsupported Error**

```
Angular CLI: 20.3.15
Node: 25.2.1 (Unsupported)
```

**Solution**: Angular 20 does NOT support Node.js 25.x
- Uninstall Node.js 25.x
- Install Node.js 20.x LTS from https://nodejs.org/en/download/
- Or use nvm: `nvm install 20.18.3 && nvm use 20`
- Verify: `node --version` should show v20.x.x
- Clear npm cache: `npm cache clean --force`
- Reinstall dependencies: `npm install`

**5. API Calls Fail**
- Check backend is running on port 8080
- Verify API endpoints match backend
- Check JWT token in request headers
- Review browser console for errors

---

## Next Steps After Implementation

1. **Add Unit Tests** - Write tests for services and components
2. **E2E Testing** - Use Playwright tests for full workflows
3. **Performance Optimization** - Lazy loading, caching, code splitting
4. **Accessibility** - ARIA labels, keyboard navigation
5. **Production Build** - Optimize for production deployment
6. **Documentation** - User guide, API documentation
7. **CI/CD Enhancement** - Automated tests, deployment pipelines

---

## Reference Links

- Angular Documentation: https://angular.dev
- Angular Material: https://material.angular.io
- Keycloak Angular: https://github.com/mauriciovigolo/keycloak-angular
- Frontend Maven Plugin: https://github.com/eirslett/frontend-maven-plugin

