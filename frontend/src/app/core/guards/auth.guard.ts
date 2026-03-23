import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { keycloak } from '../../app.config';

export const authGuard: CanActivateFn = async (route, state) => {
  const router = inject(Router);

  const isLoggedIn = keycloak?.authenticated ?? false;

  if (!isLoggedIn) {
    await keycloak?.login({
      redirectUri: window.location.origin + state.url,
    });
    return false;
  }

  // Optional: Check for required roles
  const requiredRoles = route.data['roles'] as string[];
  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = keycloak?.realmAccess?.roles ?? [];
    const hasRole = requiredRoles.some((role) => userRoles.includes(role));

    if (!hasRole) {
      router.navigate(['/unauthorized']);
      return false;
    }
  }

  return true;
};
