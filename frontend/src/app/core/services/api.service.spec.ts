import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ApiService } from './api.service';
import { environment } from '../../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ApiService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should make GET request', () => {
    const mockData = { id: 1, name: 'Test' };

    service.get<any>('/test').subscribe((data) => {
      expect(data).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${apiUrl}/test`);
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('should make GET request with query params', () => {
    const params = { page: 0, size: 10 };

    service.get<any>('/test', params).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/test?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should make POST request', () => {
    const postData = { name: 'New Item' };
    const mockResponse = { id: 1, ...postData };

    service.post<any>('/test', postData).subscribe((data) => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${apiUrl}/test`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(postData);
    req.flush(mockResponse);
  });

  it('should make PUT request', () => {
    const putData = { id: 1, name: 'Updated Item' };

    service.put<any>('/test/1', putData).subscribe((data) => {
      expect(data).toEqual(putData);
    });

    const req = httpMock.expectOne(`${apiUrl}/test/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(putData);
  });

  it('should make PATCH request', () => {
    const patchData = { name: 'Patched Item' };

    service.patch<any>('/test/1', patchData).subscribe((data) => {
      expect(data).toEqual(patchData);
    });

    const req = httpMock.expectOne(`${apiUrl}/test/1`);
    expect(req.request.method).toBe('PATCH');
    req.flush(patchData);
  });

  it('should make DELETE request', () => {
    service.delete<void>('/test/1').subscribe();

    const req = httpMock.expectOne(`${apiUrl}/test/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should filter null/undefined query params', () => {
    const params = { page: 0, size: null, filter: undefined };

    service.get<any>('/test', params).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/test?page=0`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
