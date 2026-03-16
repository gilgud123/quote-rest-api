import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Auth token will be handled by Keycloak's bearer interceptor
  return next(req);
};
