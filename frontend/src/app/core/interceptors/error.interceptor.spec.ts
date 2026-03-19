import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let consoleErrorSpy: jasmine.Spy;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);

    // Spy on console.error to suppress error logs in tests
    consoleErrorSpy = spyOn(console, 'error');
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should handle 400 Bad Request error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(400);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush({ message: 'Invalid data' }, { status: 400, statusText: 'Bad Request' });
  });

  it('should handle 401 Unauthorized error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(401);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
  });

  it('should handle 403 Forbidden error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(403);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
  });

  it('should handle 404 Not Found error', (done) => {
    httpClient.get('/api/v1/authors/999').subscribe({
      error: (error) => {
        expect(error.status).toBe(404);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors/999');
    req.flush({ message: 'Author not found' }, { status: 404, statusText: 'Not Found' });
  });

  it('should handle 500 Internal Server Error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(500);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush({ message: 'Database error' }, { status: 500, statusText: 'Internal Server Error' });
  });

  it('should handle network errors', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.error).toBeInstanceOf(ProgressEvent);
        expect(consoleErrorSpy).toHaveBeenCalled();
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.error(new ProgressEvent('Network error'));
  });

  it('should log error message to console', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: () => {
        expect(consoleErrorSpy).toHaveBeenCalledWith(
          'HTTP Error:',
          jasmine.any(String),
          jasmine.any(Object)
        );
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Error', { status: 500, statusText: 'Internal Server Error' });
  });

  it('should pass through the original error', (done) => {
    httpClient.get('/api/v1/authors').subscribe({
      error: (error) => {
        expect(error.status).toBe(404);
        expect(error.statusText).toBe('Not Found');
        done();
      },
    });

    const req = httpMock.expectOne('/api/v1/authors');
    req.flush('Not Found', { status: 404, statusText: 'Not Found' });
  });
});
