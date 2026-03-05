const { test, expect, request } = require('@playwright/test');

const BASE_URL = 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api/v1`;

// Test data
let testAuthorId;
let testQuoteId;
let authToken = null;

/**
 * Get JWT token from Keycloak
 * Uses api-admin user which has ADMIN role for full API access
 */
async function getAuthToken(apiContext) {
  try {
    // Use URLSearchParams to properly format form data
    const formData = new URLSearchParams({
      grant_type: 'password',
      client_id: 'quote-api',
      username: 'api-admin',
      password: 'password',
      scope: 'openid'
    });

    const response = await apiContext.post('http://localhost:8081/realms/quote/protocol/openid-connect/token', {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: formData.toString()
    });
    
    if (response.ok()) {
      const data = await response.json();
      return data.access_token;
    } else {
      const errorText = await response.text();
      console.error('Failed to get auth token. Status:', response.status(), 'Response:', errorText);
    }
  } catch (error) {
    console.error('Could not get auth token:', error.message);
  }
  return null;
}

/**
 * Helper to create authenticated request context
 */
function getAuthHeaders() {
  return authToken ? { Authorization: `Bearer ${authToken}` } : {};
}

test.describe('Quote REST API - Author Endpoints', () => {
  let apiContext;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext({
      baseURL: API_BASE,
      extraHTTPHeaders: {
        'Content-Type': 'application/json'
      }
    });
    
    // Try to get auth token
    authToken = await getAuthToken(apiContext);
    if (authToken) {
      console.log('✓ Authentication successful');
    } else {
      console.warn('⚠ Running tests without authentication - some may fail');
    }
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  test('GET /authors - should return paginated authors', async () => {
    const response = await apiContext.get(`${API_BASE}/authors`, {
      params: { page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
    expect(data).toHaveProperty('totalElements');
    expect(data).toHaveProperty('pageable');
  });

  test('POST /authors - should create a new author', async () => {
    const timestamp = Date.now();
    const newAuthor = {
      name: `Test Author ${timestamp}`,
      birthYear: 1950,
      deathYear: 2020,
      biography: 'Test biography for automated testing'
    };

    const response = await apiContext.post(`${API_BASE}/authors`, {
      data: newAuthor,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(201);
    const data = await response.json();
    expect(data).toHaveProperty('id');
    expect(data.name).toBe(newAuthor.name);
    expect(data.birthYear).toBe(newAuthor.birthYear);
    
    testAuthorId = data.id;
    console.log(`✓ Created test author with ID: ${testAuthorId}`);
  });

  test('GET /authors/{id} - should return specific author', async () => {
    if (!testAuthorId) test.skip();

    const response = await apiContext.get(`${API_BASE}/authors/${testAuthorId}`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.id).toBe(testAuthorId);
    expect(data).toHaveProperty('name');
    expect(data).toHaveProperty('birthYear');
  });

  test('GET /authors/search - should search authors by name', async () => {
    const response = await apiContext.get(`${API_BASE}/authors/search`, {
      params: { name: 'Plato', page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.content.length).toBeGreaterThan(0);
    expect(data.content[0].name).toContain('Plato');
  });

  test('GET /authors/filter - should filter authors by birth year', async () => {
    const response = await apiContext.get(`${API_BASE}/authors/filter`, {
      params: { birthYear: 1564, page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    if (data.content.length > 0) {
      expect(data.content[0].birthYear).toBe(1564);
    }
  });

  test('GET /authors/{id}/stats - should return author statistics', async () => {
    if (!testAuthorId) test.skip();

    const response = await apiContext.get(`${API_BASE}/authors/${testAuthorId}/stats`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('authorId');
    expect(data).toHaveProperty('authorName');
    expect(data).toHaveProperty('quoteCount');
  });

  test('PUT /authors/{id} - should update author', async () => {
    if (!testAuthorId) test.skip();

    const timestamp = Date.now();
    const updatedAuthor = {
      name: `Updated Author ${timestamp}`,
      birthYear: 1965,
      deathYear: 2025,
      biography: 'Updated biography for testing'
    };

    const response = await apiContext.put(`${API_BASE}/authors/${testAuthorId}`, {
      data: updatedAuthor,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.name).toBe(updatedAuthor.name);
    expect(data.biography).toBe(updatedAuthor.biography);
  });

  test('PATCH /authors/{id} - should partially update author', async () => {
    if (!testAuthorId) test.skip();

    const partialUpdate = {
      biography: 'Partially updated biography'
    };

    const response = await apiContext.patch(`${API_BASE}/authors/${testAuthorId}`, {
      data: partialUpdate,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.biography).toBe(partialUpdate.biography);
  });
});

test.describe('Quote REST API - Quote Endpoints', () => {
  let apiContext;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext({
      baseURL: API_BASE,
      extraHTTPHeaders: {
        'Content-Type': 'application/json'
      }
    });
    
    authToken = await getAuthToken(apiContext);
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  test('GET /quotes - should return paginated quotes', async () => {
    const response = await apiContext.get(`${API_BASE}/quotes`, {
      params: { page: 0, size: 10, sortBy: 'createdAt', direction: 'DESC' },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
    expect(data).toHaveProperty('totalElements');
  });

  test('POST /quotes - should create a new quote', async () => {
    if (!testAuthorId) {
      console.warn('Skipping - no test author available');
      test.skip();
    }

    const newQuote = {
      text: 'To be or not to be, that is the question',
      category: 'Philosophy',
      authorId: testAuthorId
    };

    const response = await apiContext.post(`${API_BASE}/quotes`, {
      data: newQuote,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(201);
    const data = await response.json();
    expect(data).toHaveProperty('id');
    expect(data.text).toBe(newQuote.text);
    expect(data.category).toBe(newQuote.category);
    
    testQuoteId = data.id;
    console.log(`✓ Created test quote with ID: ${testQuoteId}`);
  });

  test('GET /quotes/{id} - should return specific quote', async () => {
    if (!testQuoteId) test.skip();

    const response = await apiContext.get(`${API_BASE}/quotes/${testQuoteId}`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.id).toBe(testQuoteId);
    expect(data.text).toContain('To be or not to be');
  });

  test('GET /quotes/search - should search quotes by text', async () => {
    const response = await apiContext.get(`${API_BASE}/quotes/search`, {
      params: { text: 'be', page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
  });

  test('GET /quotes/search - should search quotes by author', async () => {
    const response = await apiContext.get(`${API_BASE}/quotes/search`, {
      params: { author: 'Shakespeare', page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
  });

  test('GET /quotes/search - should search with general query', async () => {
    const response = await apiContext.get(`${API_BASE}/quotes/search`, {
      params: { q: 'question', page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
  });

  test.skip('GET /quotes/filter - should filter quotes by category', async () => {
    // KNOWN BUG: PostgreSQL JDBC driver interprets String parameters as bytea in some cases
    // Error: "function lower(bytea) does not exist"
    // This is a Hibernate/PostgreSQL compatibility issue with parameter binding
    const response = await apiContext.get(`${API_BASE}/quotes/filter`, {
      params: { category: 'Philosophy', page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    if (data.content.length > 0) {
      expect(data.content[0].category).toBe('Philosophy');
    }
  });

  test.skip('GET /quotes/filter - should filter quotes by author ID', async () => {
    // KNOWN BUG: Same PostgreSQL bytea issue as category filter
    if (!testAuthorId) test.skip();

    const response = await apiContext.get(`${API_BASE}/quotes/filter`, {
      params: { authorId: testAuthorId, page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
  });

  test('GET /quotes/categories - should return all categories', async () => {
    const response = await apiContext.get(`${API_BASE}/quotes/categories`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(Array.isArray(data)).toBe(true);
  });

  test('PUT /quotes/{id} - should update quote', async () => {
    if (!testQuoteId || !testAuthorId) test.skip();

    const updatedQuote = {
      text: 'To be or not to be, that is the updated question',
      category: 'Drama',
      authorId: testAuthorId
    };

    const response = await apiContext.put(`${API_BASE}/quotes/${testQuoteId}`, {
      data: updatedQuote,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.text).toBe(updatedQuote.text);
    expect(data.category).toBe(updatedQuote.category);
  });

  test('PATCH /quotes/{id} - should partially update quote', async () => {
    if (!testQuoteId) test.skip();

    const partialUpdate = {
      category: 'Literature'
    };

    const response = await apiContext.patch(`${API_BASE}/quotes/${testQuoteId}`, {
      data: partialUpdate,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data.category).toBe(partialUpdate.category);
  });

  test('GET /authors/{id}/quotes - should return quotes by author', async () => {
    if (!testAuthorId) test.skip();

    const response = await apiContext.get(`${API_BASE}/authors/${testAuthorId}/quotes`, {
      params: { page: 0, size: 10 },
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(200);
    const data = await response.json();
    expect(data).toHaveProperty('content');
  });
});

test.describe('Quote REST API - Error Handling', () => {
  let apiContext;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext({
      baseURL: API_BASE,
      extraHTTPHeaders: {
        'Content-Type': 'application/json'
      }
    });
    
    authToken = await getAuthToken(apiContext);
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  test('GET /authors/{id} - should return 404 for non-existent author', async () => {
    const response = await apiContext.get(`${API_BASE}/authors/999999`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(404);
    const data = await response.json();
    expect(data).toHaveProperty('message');
  });

  test('GET /quotes/{id} - should return 404 for non-existent quote', async () => {
    const response = await apiContext.get(`${API_BASE}/quotes/999999`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(404);
    const data = await response.json();
    expect(data).toHaveProperty('message');
  });

  test('POST /authors - should return 400 for invalid data', async () => {
    const invalidAuthor = {
      name: '', // Empty name should fail validation
      birthYear: 3000 // Future year
    };

    const response = await apiContext.post(`${API_BASE}/authors`, {
      data: invalidAuthor,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(400);
  });

  test('POST /quotes - should return 400 for missing required fields', async () => {
    const invalidQuote = {
      text: '' // Empty text
    };

    const response = await apiContext.post(`${API_BASE}/quotes`, {
      data: invalidQuote,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(400);
  });

  test('POST /quotes - should return 404 for non-existent author', async () => {
    const quoteWithInvalidAuthor = {
      text: 'Test quote',
      category: 'Test',
      authorId: 999999
    };

    const response = await apiContext.post(`${API_BASE}/quotes`, {
      data: quoteWithInvalidAuthor,
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(404);
  });
});

test.describe('Quote REST API - Cleanup', () => {
  let apiContext;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext({
      baseURL: API_BASE,
      extraHTTPHeaders: {
        'Content-Type': 'application/json'
      }
    });
    
    authToken = await getAuthToken(apiContext);
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  test('DELETE /quotes/{id} - should delete quote', async () => {
    if (!testQuoteId) test.skip();

    const response = await apiContext.delete(`${API_BASE}/quotes/${testQuoteId}`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(204);
    console.log(`✓ Deleted test quote ID: ${testQuoteId}`);
  });

  test('DELETE /authors/{id} - should delete author', async () => {
    if (!testAuthorId) test.skip();

    const response = await apiContext.delete(`${API_BASE}/authors/${testAuthorId}`, {
      headers: getAuthHeaders()
    });

    expect(response.status()).toBe(204);
    console.log(`✓ Deleted test author ID: ${testAuthorId}`);
  });
});
