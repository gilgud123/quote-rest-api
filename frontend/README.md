# Quote Frontend - Angular 20

Angular 20 frontend module for the Quote REST API. Provides a full-featured web UI with complete CRUD operations for Authors and Quotes, integrated with Keycloak OAuth2 authentication.

## Features

- **Angular 20** with standalone components
- **Keycloak OAuth2** authentication with JWT tokens
- **Lazy-loaded feature modules** for Authors and Quotes
- **Complete CRUD** with responsive UI
- **81 unit tests** with ~79% coverage
- **RxJS** for reactive programming
- **Material Design** styling

## Prerequisites

- Node.js 22.12+ 
- npm 10.9+
- Docker & Docker Compose (for backend services)

## Quick Start

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

**Or use the convenience scripts:**

```powershell
# Windows
.\scripts\start-frontend-dev.ps1

# Linux/Mac
./scripts/start-frontend-dev.sh
```

## Access Points

- **Frontend UI**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Keycloak Admin**: http://localhost:9090 (admin/admin)

## Test Users

Pre-configured in Keycloak realm:
- **frontend-user** / **password** - Regular user (USER role)
- **frontend-admin** / **password** - Admin user (ADMIN role)

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner:

```bash
npm test                 # Interactive watch mode
npm run test:ci         # Single run (for CI)
npm run test:coverage   # With coverage report
```

**Coverage report:** `coverage/quote-frontend/index.html`

## Running end-to-end tests

For end-to-end (e2e) testing:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/           # Services, guards, interceptors, models
│   │   │   ├── services/   # auth.service, api.service
│   │   │   ├── guards/     # auth.guard
│   │   │   ├── interceptors/ # auth.interceptor, error.interceptor
│   │   │   └── models/     # author.model, quote.model
│   │   ├── features/       # Feature modules
│   │   │   ├── authors/    # Author CRUD components
│   │   │   └── quotes/     # Quote CRUD components
│   │   ├── shared/         # Shared components
│   │   └── app.routes.ts   # Main routing configuration
│   ├── environments/       # Environment-specific configs
│   └── styles.scss         # Global styles
├── angular.json            # Angular CLI configuration
├── package.json            # npm dependencies and scripts
├── pom.xml                 # Maven integration for CI/CD
├── proxy.conf.json         # Dev server proxy for API calls
└── tsconfig.json           # TypeScript configuration
```

## Code Formatting

This project uses **Spotless** for consistent code formatting:

```powershell
# Apply formatting to frontend module
mvn spotless:apply -pl frontend

# Check formatting
mvn spotless:check -pl frontend
```

## Maven Integration

The frontend is integrated into the Maven build lifecycle via `frontend-maven-plugin`:

```powershell
# Build frontend with Maven (from project root)
mvn clean install -pl frontend

# This will:
# 1. Install Node.js and npm (if not present)
# 2. Run npm install
# 3. Run npm run build
# 4. Package the built app
```

## Troubleshooting

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
- Restart Docker services: `docker-compose -f docker-compose-frontend.yml restart`

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
