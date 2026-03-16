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
