import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthorService } from './author.service';
import { Author, AuthorPage } from '../../../core/models';
import { environment } from '../../../../environments/environment';

describe('AuthorService', () => {
  let service: AuthorService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;

  const mockAuthor: Author = {
    id: 1,
    name: 'Socrates',
    biography: 'Ancient Greek philosopher',
    birthYear: -469,
    deathYear: -399,
  };

  const mockAuthorPage: AuthorPage = {
    content: [mockAuthor],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AuthorService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get authors with pagination', () => {
    service.getAuthors(0, 10, 'name,asc').subscribe((page) => {
      expect(page).toEqual(mockAuthorPage);
      expect(page.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/authors?page=0&size=10&sort=name,asc`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuthorPage);
  });

  it('should get single author by id', () => {
    service.getAuthor(1).subscribe((author) => {
      expect(author).toEqual(mockAuthor);
    });

    const req = httpMock.expectOne(`${apiUrl}/authors/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuthor);
  });

  it('should create new author', () => {
    const newAuthor: Author = {
      name: 'Plato',
      biography: 'Student of Socrates',
      birthYear: -428,
      deathYear: -348,
    };

    service.createAuthor(newAuthor).subscribe((author) => {
      expect(author.id).toBe(2);
      expect(author.name).toBe('Plato');
    });

    const req = httpMock.expectOne(`${apiUrl}/authors`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newAuthor);
    req.flush({ id: 2, ...newAuthor });
  });

  it('should update existing author', () => {
    const updatedAuthor: Author = { ...mockAuthor, biography: 'Updated biography' };

    service.updateAuthor(1, updatedAuthor).subscribe((author) => {
      expect(author.biography).toBe('Updated biography');
    });

    const req = httpMock.expectOne(`${apiUrl}/authors/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedAuthor);
  });

  it('should delete author', () => {
    service.deleteAuthor(1).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/authors/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should search authors by name', () => {
    service.searchAuthors('Socrates', 0, 10).subscribe((page) => {
      expect(page.content[0].name).toBe('Socrates');
    });

    const req = httpMock.expectOne(`${apiUrl}/authors/search?name=Socrates&page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAuthorPage);
  });
});
