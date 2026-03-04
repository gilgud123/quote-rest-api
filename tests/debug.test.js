const { test, expect, request } = require('@playwright/test');

const BASE_URL = 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api/v1`;

async function getAuthToken(apiContext) {
  try {
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
      console.error('Failed to get auth token. Status:', response.status());
    }
  } catch (error) {
    console.error('Could not get auth token:', error.message);
  }
  return null;
}

test.describe('Debug Test', () => {
  let apiContext;
  let authToken;

  test.beforeAll(async ({ playwright }) => {
    apiContext = await playwright.request.newContext({
      baseURL: API_BASE
    });
    
    authToken = await getAuthToken(apiContext);
    console.log('Auth token obtained:', authToken ? 'YES' : 'NO');
    if (authToken) {
      console.log('Token length:', authToken.length);
      console.log('Token preview:', authToken.substring(0, 50) + '...');
    }
  });

  test.afterAll(async () => {
    await apiContext.dispose();
  });

  test('Debug: GET /authors with detailed logging', async () => {
    console.log('\n=== MAKING REQUEST ===');
    console.log('URL:', `${API_BASE}/authors`);
    console.log('Auth header:', authToken ? `Bearer ${authToken.substring(0, 20)}...` : 'NO TOKEN');
    
    const response = await apiContext.get('http://localhost:8080/api/v1/authors', {
      params: { page: 0, size: 10 },
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });

    console.log('\n=== RESPONSE ===');
    console.log('Status:', response.status());
    console.log('Status Text:', response.statusText());
    console.log('Headers:', JSON.stringify(response.headers(), null, 2));
    
    const bodyText = await response.text();
    console.log('Body length:', bodyText.length);
    console.log('Body preview:', bodyText.substring(0, 500));
    
    if (response.status() !== 200) {
      console.log('\n=== FULL ERROR BODY ===');
      console.log(bodyText);
    }

    expect(response.status()).toBe(200);
  });
});
