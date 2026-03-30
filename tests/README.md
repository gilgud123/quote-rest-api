# Quote REST API - Playwright Tests

Automated browser-based API tests using Playwright for the Quote REST API.

## Prerequisites

- Node.js 18+ installed
- Quote REST API running on `http://localhost:8080`
- Keycloak running on `http://localhost:8081` (optional, for authentication tests)

## Setup

1. Install dependencies:
   ```bash
   cd tests
   npm install
   npx playwright install
   ```

2. Configure Keycloak (if testing with authentication):
   - Realm: `quote`
   - Client: `quote-api`
   - Test user: `api-admin` / `password`

## Running Tests

### Run all tests:
```bash
npm test
```

### Run tests in headed mode (visible browser):
```bash
npm run test:headed
```

### Run tests in UI mode (interactive):
```bash
npm run test:ui
```

### Run tests in debug mode:
```bash
npm run test:debug
```

### View test report:
```bash
npm run test:report
```

## Test Coverage

### Author Endpoints
- ✅ GET `/api/v1/authors` - List all authors with pagination
- ✅ POST `/api/v1/authors` - Create new author
- ✅ GET `/api/v1/authors/{id}` - Get author by ID
- ✅ PUT `/api/v1/authors/{id}` - Update author (full)
- ✅ PATCH `/api/v1/authors/{id}` - Update author (partial)
- ✅ DELETE `/api/v1/authors/{id}` - Delete author
- ✅ GET `/api/v1/authors/search` - Search authors by name
- ✅ GET `/api/v1/authors/filter` - Filter authors by birth year
- ✅ GET `/api/v1/authors/{id}/stats` - Get author statistics
- ✅ GET `/api/v1/authors/{id}/quotes` - Get quotes by author

### Quote Endpoints
- ✅ GET `/api/v1/quotes` - List all quotes with pagination
- ✅ POST `/api/v1/quotes` - Create new quote
- ✅ GET `/api/v1/quotes/{id}` - Get quote by ID
- ✅ PUT `/api/v1/quotes/{id}` - Update quote (full)
- ✅ PATCH `/api/v1/quotes/{id}` - Update quote (partial)
- ✅ DELETE `/api/v1/quotes/{id}` - Delete quote
- ✅ GET `/api/v1/quotes/search` - Search quotes (by text, author, or general query)
- GET `/api/v1/quotes/filter` - Filter quotes (by category, author ID) — tests currently skipped/disabled in `api.test.js`
- ✅ GET `/api/v1/quotes/categories` - Get all categories

### Error Handling
- ✅ 404 responses for non-existent resources
- ✅ 400 responses for invalid data
- ✅ Validation error handling

## Test Structure

Tests are organized into describe blocks:
1. **Author Endpoints** - CRUD operations for authors
2. **Quote Endpoints** - CRUD operations for quotes
3. **Error Handling** - Edge cases and error responses
4. **Cleanup** - Delete test data after runs

## Authentication

Tests attempt to authenticate with Keycloak to obtain a JWT token. If authentication fails, tests will still run but may fail on protected endpoints.

To bypass security for testing, you can temporarily disable security in the Spring Boot application's security configuration.

## Troubleshooting

**Tests fail with 401 Unauthorized:**
- Ensure Keycloak is running and configured correctly
- Check username/password in the test file
- Verify the JWT token is being obtained successfully

**Tests fail with connection errors:**
- Ensure the Quote REST API is running on http://localhost:8080
- Check that Docker containers are up: `docker compose ps`

**Tests timeout:**
- Increase timeout in `playwright.config.js`
- Check application logs for slow responses

## Output

Test results are saved to:
- HTML report: `test-results/html/index.html`
- JSON report: `test-results/results.json`
- Screenshots (on failure): `test-results/`
- Videos (on failure): `test-results/`

## API Contract Tests (GitHub Actions)

In addition to the Playwright tests above, a **Newman-based API contract test** workflow is configured in `.github/workflows/api-contract.yml`. It runs the Postman collection located at `postman/Quote REST API.postman_collection.json` against the full Docker Compose stack on every push to `main`/`master`. Results are published as JUnit reports in the GitHub Actions run summary.
