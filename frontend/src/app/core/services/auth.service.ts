import { Injectable } from '@angular/core';
import { KeycloakProfile } from 'keycloak-js';
import { keycloak } from '../../app.config';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  async isLoggedIn(): Promise<boolean> {
    return keycloak?.authenticated ?? false;
  }

  async getUserProfile(): Promise<KeycloakProfile | null> {
    if (!keycloak || !keycloak.authenticated) {
      return null;
    }
    try {
      return await keycloak.loadUserProfile();
    } catch (error) {
      console.error('Failed to load user profile:', error);
      return null;
    }
  }

  async getUsername(): Promise<string> {
    const profile = await this.getUserProfile();
    return profile?.username || keycloak?.tokenParsed?.['preferred_username'] || 'Unknown User';
  }

  login(): void {
    keycloak?.login();
  }

  logout(): void {
    keycloak?.logout({ redirectUri: window.location.origin });
  }

  getRoles(): string[] {
    return keycloak?.realmAccess?.roles ?? [];
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  async getToken(): Promise<string> {
    try {
      await keycloak?.updateToken(5);
      return keycloak?.token ?? '';
    } catch (error) {
      console.error('Failed to refresh token:', error);
      return '';
    }
  }

  getTokenParsed() {
    return keycloak?.tokenParsed;
  }

  async refreshToken(): Promise<boolean> {
    try {
      const refreshed = await keycloak?.updateToken(30);
      return refreshed ?? false;
    } catch (error) {
      console.error('Failed to refresh token:', error);
      return false;
    }
  }
}
