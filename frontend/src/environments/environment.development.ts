export const environment = {
  production: false,
  apiUrl: '/api/v1', // Uses proxy
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'quote',
    clientId: 'quote-frontend',
  },
};
