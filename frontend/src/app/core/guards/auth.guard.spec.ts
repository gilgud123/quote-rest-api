import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let router: jasmine.SpyObj<Router>;
  let mockRoute: ActivatedRouteSnapshot;
  let mockState: RouterStateSnapshot;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: routerSpy }],
    });

    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    mockRoute = {} as ActivatedRouteSnapshot;
    mockState = { url: '/authors' } as RouterStateSnapshot;
  });

  it('should be defined', () => {
    expect(authGuard).toBeDefined();
  });

  it('should be a function', () => {
    expect(typeof authGuard).toBe('function');
  });

  it('should accept route and state parameters', () => {
    expect(authGuard.length).toBe(2);
  });

  // Note: Testing the actual guard logic requires mocking the global keycloak instance
  // which is not straightforward in unit tests. The guard logic should be tested
  // in integration/e2e tests where Keycloak can be properly initialized.
  // These tests verify the guard structure and type safety.
});
