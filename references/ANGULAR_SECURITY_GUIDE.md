# Angular Security Guide (Keycloak + Quote REST API)

This document describes how an Angular frontend should authenticate against Keycloak and authorize calls to the Quote REST API.

## 1) Backend expectations (summary)

Security rules from `src/main/java/com/katya/quoterestapi/config/SecurityConfig.java`:

- All API endpoints require authentication.
- GET on `/api/v1/**` requires role `USER` or `ADMIN`.
- POST/PUT/PATCH/DELETE on `/api/v1/**` requires role `ADMIN`.
- `/swagger-ui/**` and `/api-docs/**` are public (for docs only).

JWT role mapping from `KeycloakRoleConverter`:

- Roles can be assigned as **realm roles** (under `realm_access.roles`) or **client roles** (under `resource_access[clientId].roles`).
- The backend expects `ROLE_USER` and/or `ROLE_ADMIN` after conversion. In Keycloak, assign `USER` and/or `ADMIN` (without the `ROLE_` prefix).

The resource server validates issuers:

- For Docker profile, accepted issuers include:
  - `http://keycloak:8080/realms/quote`
  - `http://localhost:8081/realms/quote`

## 2) Keycloak client settings for SPA

Create or configure a client in the `quote` realm. Recommended settings for Angular:

- **Client ID:** `quote-api` (matches `app.security.keycloak.client-id`)
- **Client type:** Public
- **Standard flow:** Enabled (Authorization Code with PKCE)
- **Direct access grants:** Optional (only for local tests or CLI calls)
- **Root URL:** `http://localhost:4200`
- **Valid redirect URIs:** `http://localhost:4200/*`
- **Web origins:** `http://localhost:4200`

Roles:

- Create realm roles `USER` and `ADMIN` (or client roles under `quote-api`).
- Assign `USER` to regular users, `ADMIN` to admins.

## 3) Angular implementation (recommended: keycloak-js)

Install dependencies:

```bash
npm i keycloak-js
```

Create a small auth service. Example using `keycloak-js` directly:

```ts
// src/app/auth/keycloak.service.ts
import Keycloak, { KeycloakInstance } from 'keycloak-js';

const keycloakConfig = {
  url: 'http://localhost:8081',
  realm: 'quote',
  clientId: 'quote-api',
};

class KeycloakService {
  private keycloak: KeycloakInstance;

  constructor() {
    this.keycloak = new Keycloak(keycloakConfig);
  }

  async init(): Promise<boolean> {
    return this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });
  }

  getToken(): string | undefined {
    return this.keycloak.token || undefined;
  }

  async updateToken(minValiditySeconds = 30): Promise<string | undefined> {
    const refreshed = await this.keycloak.updateToken(minValiditySeconds);
    return refreshed ? this.keycloak.token || undefined : this.keycloak.token || undefined;
  }

  getUserRoles(): string[] {
    const realmRoles = this.keycloak.realmAccess?.roles || [];
    const clientRoles = this.keycloak.resourceAccess?.['quote-api']?.roles || [];
    return Array.from(new Set([...realmRoles, ...clientRoles]));
  }
}

export const keycloakService = new KeycloakService();
```

Initialize early, for example in `main.ts`:

```ts
// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { keycloakService } from './app/auth/keycloak.service';

(async () => {
  await keycloakService.init();
  await bootstrapApplication(AppComponent);
})();
```

## 4) Attach Bearer token to API calls

Use an Angular HTTP interceptor:

```ts
// src/app/auth/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { keycloakService } from './keycloak.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.includes('/api/')) {
    return next(req);
  }

  const token = keycloakService.getToken();
  if (!token) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  }));
};
```

Register it (Angular standalone example):

```ts
// src/app/app.config.ts
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './auth/auth.interceptor';

export const appConfig = {
  providers: [
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
```

## 5) Role-based UI handling

Example helper:

```ts
const roles = keycloakService.getUserRoles();
const canRead = roles.includes('USER') || roles.includes('ADMIN');
const canWrite = roles.includes('ADMIN');
```

## 6) API endpoints

Base URL (local Docker profile):

- API: `http://localhost:8080/api/v1`
- Token issuer: `http://localhost:8081/realms/quote`

Example API call:

```ts
// GET requires USER or ADMIN
GET http://localhost:8080/api/v1/authors?page=0&size=10&sortBy=name&direction=ASC

// POST requires ADMIN
POST http://localhost:8080/api/v1/quotes
```

## 7) Notes on token issuance

- The backend validates the token issuer (`iss`) and signature via Keycloak JWKs.
- The SPA should use **Authorization Code + PKCE** to obtain tokens.
- Direct password grant is not recommended for browsers, but can be enabled for local testing.

## 8) CORS

The backend does not define a CORS configuration in code. If the Angular app is hosted on a different origin, you may need to add a CORS configuration in the backend or enable it at the proxy/gateway level.

## 9) Quick troubleshooting

- 401 on API calls: confirm the token `iss` matches `http://localhost:8081/realms/quote` and roles include `USER` or `ADMIN`.
- 401 on token requests: do not send `Authorization: Bearer <token>` when requesting a token.
- Role mismatch: ensure roles are assigned as `USER`/`ADMIN` in realm or client roles for `quote-api`.

