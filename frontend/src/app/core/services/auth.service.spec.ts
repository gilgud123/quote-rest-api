import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check if user is logged in', async () => {
    const isLoggedIn = await service.isLoggedIn();
    expect(typeof isLoggedIn).toBe('boolean');
  });

  it('should get username', async () => {
    const username = await service.getUsername();
    expect(username).toBeDefined();
    expect(typeof username).toBe('string');
  });

  it('should get user roles', () => {
    const roles = service.getRoles();
    expect(Array.isArray(roles)).toBe(true);
  });

  it('should check if user has role', () => {
    const hasUserRole = service.hasRole('USER');
    expect(typeof hasUserRole).toBe('boolean');
  });

  it('should get token', async () => {
    const token = await service.getToken();
    // Token can be empty string if not authenticated or a JWT string if authenticated
    expect(typeof token).toBe('string');
  });

  it('should handle login', () => {
    expect(() => service.login()).not.toThrow();
  });

  it('should handle logout', () => {
    expect(() => service.logout()).not.toThrow();
  });

  it('should refresh token', async () => {
    const result = await service.refreshToken();
    expect(typeof result).toBe('boolean');
  });
});
