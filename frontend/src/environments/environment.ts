// Default environment configuration
// This file is replaced by environment-specific files during build:
// - environment.production.ts for production builds
// - environment.development.ts for development builds
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  keycloak: {
    url: 'http://localhost:9090',
    realm: 'quote',
    clientId: 'quote-frontend',
  },
};
