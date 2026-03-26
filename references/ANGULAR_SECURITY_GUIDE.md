# Angular Security Guide (Keycloak + Quote REST API)

This document describes how an Angular frontend should authenticate against Keycloak and authorize calls to the Quote REST API.

## 1) Backend expectations (summary)

Security rules from `src/main/java/com/katya/quoterestapi/config/SecurityConfig.java`:

- All API endpoints require authentication.
- GET on `/api/v1/**` requires role `USER` or `ADMIN`.
- POST/PUT/PATCH/DELETE on `/api/v1/**` requires role `ADMIN`.
- `/actuator/health` is public.
- `/swagger-ui.html`, `/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**` are public (for docs only).
- `/actuator/**` (except health) requires role `ADMIN`.

JWT role mapping from `KeycloakRoleConverter`:

- Roles can be assigned as **realm roles** (under `realm_access.roles`) or **client roles** (under `resource_access[clientId].roles`).
- The backend expects `ROLE_USER` and/or `ROLE_ADMIN` after conversion. In Keycloak, assign `USER` and/or `ADMIN` (without the `ROLE_` prefix).

The resource server validates issuers:

- **Default profile (local):** `http://localhost:8081/realms/quote`
- **Docker profile:** accepts both:
  - `http://keycloak:8080/realms/quote` (internal Docker network)
  - `http://localhost:8081/realms/quote` (external access)

Configure via `app.security.keycloak.accepted-issuers` in application.yml

## 2) Keycloak client settings for SPA

Create or configure a client in the `quote` realm. Recommended settings for Angular:

- **Client ID:** `quote-frontend` (the Angular SPA client; the backend uses `quote-api` for JWT validation)
- **Client type:** Public (SPAs cannot securely store client secrets)
- **Standard flow:** Enabled (Authorization Code with PKCE)
- **Direct access grants:** Disabled (not recommended for SPAs; enable only for backend/CLI testing)
- **Root URL:** `http://localhost:4200`
- **Valid redirect URIs:** `http://localhost:4200/*`
- **Valid post-logout redirect URIs:** `http://localhost:4200/*`
- **Web origins:** `http://localhost:4200` (for CORS)
- **Access Type:** Public (in older Keycloak versions)

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
import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: 'http://localhost:9090',
  realm: 'quote',
  clientId: 'quote-frontend',
};

class KeycloakService {
  private keycloak: Keycloak;

  constructor() {
    this.keycloak = new Keycloak(keycloakConfig);
  }

  async init(): Promise<boolean> {
    try {
      return await this.keycloak.init({
        onLoad: 'login-required',
        pkceMethod: 'S256',
        checkLoginIframe: false,
      });
    } catch (error) {
      console.error('Keycloak initialization failed:', error);
      throw error;
    }
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  async updateToken(minValiditySeconds = 30): Promise<string | undefined> {
    try {
      await this.keycloak.updateToken(minValiditySeconds);
      return this.keycloak.token;
    } catch (error) {
      console.error('Token refresh failed:', error);
      await this.keycloak.login();
      throw error;
    }
  }

  getUserRoles(): string[] {
    const realmRoles = this.keycloak.realmAccess?.roles || [];
    const clientRoles = this.keycloak.resourceAccess?.['quote-api']?.roles || [];
    return Array.from(new Set([...realmRoles, ...clientRoles]));
  }

  logout(): void {
    this.keycloak.logout();
  }

  getUsername(): string | undefined {
    return this.keycloak.tokenParsed?.preferred_username;
  }
}

export const keycloakService = new KeycloakService();
```

Initialize early, for example in `main.ts`:

```ts
// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';
import { keycloakService } from './app/auth/keycloak.service';

(async () => {
  try {
    await keycloakService.init();
    await bootstrapApplication(AppComponent, appConfig);
  } catch (error) {
    console.error('Application initialization failed:', error);
  }
})();
```

## 4) Attach Bearer token to API calls

Use an Angular HTTP interceptor:

```ts
// src/app/auth/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { keycloakService } from './keycloak.service';
import { from, switchMap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip token for non-API requests
  if (!req.url.includes('/api/')) {
    return next(req);
  }

  // Ensure token is valid (refresh if needed)
  return from(keycloakService.updateToken()).pipe(
    switchMap(token => {
      if (token) {
        req = req.clone({
          setHeaders: { Authorization: `Bearer ${token}` },
        });
      }
      return next(req);
    })
  );
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

Base URLs:

- **API:** `http://localhost:8080/api/v1`
- **Keycloak (frontend dev):** `http://localhost:9090`
- **Token issuer (frontend dev):** `http://localhost:9090/realms/quote`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

Example API calls:

```typescript
// GET requires USER or ADMIN role
this.http.get('http://localhost:8080/api/v1/authors?page=0&size=10&sortBy=name&direction=ASC')

// POST requires ADMIN role
this.http.post('http://localhost:8080/api/v1/quotes', { text: '...', authorId: 1 })

// Health check (public, no auth required)
this.http.get('http://localhost:8080/actuator/health')
```

## 7) Notes on token issuance

- The backend validates the token issuer (`iss`) and signature via Keycloak JWKs.
- The SPA should use **Authorization Code + PKCE** to obtain tokens.
- Direct password grant is not recommended for browsers, but can be enabled for local testing.

## 8) CORS

The backend currently does not have explicit CORS configuration. If you encounter CORS errors:

**Option 1: Add CORS configuration to Spring Boot**

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

**Option 2: Configure Keycloak Web Origins**

Ensure `quote-api` client has `http://localhost:4200` in the **Web Origins** setting.

## 9) Quick troubleshooting

**401 Unauthorized on API calls:**
- Verify the token issuer (`iss` claim) matches accepted issuers. For the frontend dev environment (`docker-compose-frontend.yml`), the issuer is `http://localhost:9090/realms/quote`. For the backend-only Docker setup (`docker-compose.yml`), the issuer is `http://localhost:8081/realms/quote`.
- Check roles in JWT: use [jwt.io](https://jwt.io) to decode token and verify `realm_access.roles` or `resource_access.quote-api.roles` includes `USER` or `ADMIN`
- Ensure token hasn't expired: check `exp` claim
- Verify Bearer token is sent: `Authorization: Bearer <token>`

**403 Forbidden:**
- User authenticated but lacks required role
- GET requires `USER` or `ADMIN`
- POST/PUT/PATCH/DELETE requires `ADMIN`

**CORS errors:**
- Add `http://localhost:4200` to Keycloak client's **Web Origins**
- Consider adding CORS configuration to Spring Boot (see section 8)

**Token refresh issues:**
- Token refresh fails if refresh token expired (default: 30 min)
- Call `keycloakService.updateToken()` before API calls
- Interceptor should handle automatic refresh

**Role not recognized:**
- Ensure roles are assigned as `USER` and `ADMIN` in Keycloak (without `ROLE_` prefix)
- Backend adds `ROLE_` prefix automatically via `KeycloakRoleConverter`
- Check role mapping for both realm roles and client-specific roles under `quote-api`

