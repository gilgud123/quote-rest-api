import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthorList } from './author-list';
import { AuthorService } from '../../services/author.service';
import { Author, AuthorPage } from '../../../../core/models';

describe('AuthorList', () => {
  let component: AuthorList;
  let fixture: ComponentFixture<AuthorList>;
  let authorService: jasmine.SpyObj<AuthorService>;

  const mockAuthorPage: AuthorPage = {
    content: [
      {
        id: 1,
        name: 'Socrates',
        biography: 'Greek philosopher',
        birthYear: -469,
        deathYear: -399,
      },
      {
        id: 2,
        name: 'Plato',
        biography: 'Student of Socrates',
        birthYear: -428,
        deathYear: -348,
      },
    ],
    totalElements: 2,
    totalPages: 1,
    size: 10,
    number: 0,
  };

  beforeEach(async () => {
    const authorServiceSpy = jasmine.createSpyObj('AuthorService', [
      'getAuthors',
      'getAuthor',
      'createAuthor',
      'updateAuthor',
      'deleteAuthor',
      'searchAuthors',
    ]);

    await TestBed.configureTestingModule({
      imports: [AuthorList],
      providers: [
        { provide: AuthorService, useValue: authorServiceSpy },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    authorService = TestBed.inject(AuthorService) as jasmine.SpyObj<AuthorService>;
    fixture = TestBed.createComponent(AuthorList);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load authors on init', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));

    fixture.detectChanges(); // triggers ngOnInit

    expect(authorService.getAuthors).toHaveBeenCalledWith(0, 10, 'name,asc');
    expect(component.authors.length).toBe(2);
    expect(component.totalElements).toBe(2);
  });

  it('should handle loading state', fakeAsync(() => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));

    component.loadAuthors();

    tick(); // Process the observable

    expect(component.loading).toBe(false);
    expect(component.authors.length).toBe(2);
  }));

  it('should handle error when loading authors fails', () => {
    const errorMessage = 'Failed to load authors';
    authorService.getAuthors.and.returnValue(throwError(() => new Error('Server error')));

    component.ngOnInit();

    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBe(false);
  });

  it('should navigate to next page', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));
    component.pageIndex = 0;
    component.totalElements = 20;

    component.nextPage();

    expect(component.pageIndex).toBe(1);
    expect(authorService.getAuthors).toHaveBeenCalled();
  });

  it('should not navigate to next page when on last page', () => {
    component.pageIndex = 0;
    component.pageSize = 10;
    component.totalElements = 5;

    const initialPage = component.pageIndex;
    component.nextPage();

    expect(component.pageIndex).toBe(initialPage);
  });

  it('should navigate to previous page', () => {
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));
    component.pageIndex = 1;

    component.previousPage();

    expect(component.pageIndex).toBe(0);
    expect(authorService.getAuthors).toHaveBeenCalled();
  });

  it('should not navigate to previous page when on first page', () => {
    component.pageIndex = 0;

    component.previousPage();

    expect(component.pageIndex).toBe(0);
  });

  it('should calculate hasNextPage correctly', () => {
    component.pageIndex = 0;
    component.pageSize = 10;
    component.totalElements = 20;
    expect(component.hasNextPage).toBe(true);

    component.totalElements = 5;
    expect(component.hasNextPage).toBe(false);
  });

  it('should calculate hasPreviousPage correctly', () => {
    component.pageIndex = 0;
    expect(component.hasPreviousPage).toBe(false);

    component.pageIndex = 1;
    expect(component.hasPreviousPage).toBe(true);
  });

  it('should open delete dialog', () => {
    const author = mockAuthorPage.content[0];

    component.openDeleteDialog(author);

    expect(component.showDeleteDialog).toBe(true);
    expect(component.authorToDelete).toBe(author);
  });

  it('should cancel delete dialog', () => {
    component.authorToDelete = mockAuthorPage.content[0];
    component.showDeleteDialog = true;

    component.cancelDelete();

    expect(component.showDeleteDialog).toBe(false);
    expect(component.authorToDelete).toBeNull();
  });

  it('should confirm delete and reload authors', (done) => {
    const author = mockAuthorPage.content[0];
    component.authorToDelete = author;
    component.showDeleteDialog = true;

    authorService.deleteAuthor.and.returnValue(of(void 0));
    authorService.getAuthors.and.returnValue(of(mockAuthorPage));

    component.confirmDelete();

    fixture.whenStable().then(() => {
      expect(authorService.deleteAuthor).toHaveBeenCalledWith(author.id!);
      expect(component.showDeleteDialog).toBe(false);
      expect(component.authorToDelete).toBeNull();
      expect(authorService.getAuthors).toHaveBeenCalled();
      done();
    });
  });

  it('should handle delete error', (done) => {
    const author = mockAuthorPage.content[0];
    component.authorToDelete = author;
    component.showDeleteDialog = true;

    authorService.deleteAuthor.and.returnValue(throwError(() => new Error('Delete failed')));

    component.confirmDelete();

    fixture.whenStable().then(() => {
      expect(component.error).toBe('Failed to delete author');
      expect(component.showDeleteDialog).toBe(false);
      expect(component.authorToDelete).toBeNull();
      done();
    });
  });

  it('should calculate current page start', () => {
    component.pageIndex = 0;
    component.pageSize = 10;
    expect(component.currentPageStart).toBe(1);

    component.pageIndex = 1;
    expect(component.currentPageStart).toBe(11);
  });

  it('should calculate current page end', () => {
    component.pageIndex = 0;
    component.pageSize = 10;
    component.totalElements = 25;
    expect(component.currentPageEnd).toBe(10);

    component.pageIndex = 2;
    expect(component.currentPageEnd).toBe(25);
  });
});
