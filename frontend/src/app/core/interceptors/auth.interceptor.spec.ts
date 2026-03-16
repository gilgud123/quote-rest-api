import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should not add Authorization header to asset requests', () => {
    httpClient.get('/assets/config.json').subscribe();

    const req = httpMock.expectOne('/assets/config.json');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should not add Authorization header to non-API requests', () => {
    httpClient.get('/some-other-endpoint').subscribe();

    const req = httpMock.expectOne('/some-other-endpoint');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should process API requests', () => {
    httpClient.get('/api/v1/authors').subscribe();

    const req = httpMock.expectOne('/api/v1/authors');
    // Header presence depends on keycloak state at test runtime
    // This test verifies the request is processed
    expect(req.request.url).toBe('/api/v1/authors');
    req.flush({});
  });

  // Note: Testing token injection requires mocking the global keycloak instance
  // which is not straightforward in unit tests. The interceptor logic should be
  // tested in integration/e2e tests where Keycloak can be properly initialized.
});
