export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'quote-api-realm',
    clientId: 'quote-frontend',
  },
};
