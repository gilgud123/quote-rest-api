import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { QuoteList } from './quote-list';
import { QuoteService } from '../../services/quote.service';
import { Quote, QuotePage } from '../../../../core/models';

describe('QuoteList', () => {
  let component: QuoteList;
  let fixture: ComponentFixture<QuoteList>;
  let quoteService: jasmine.SpyObj<QuoteService>;

  const mockQuotePage: QuotePage = {
    content: [
      {
        id: 1,
        text: 'The only true wisdom is in knowing you know nothing.',
        authorId: 1,
        authorName: 'Socrates',
        context: 'Philosophy',
        category: 'Wisdom',
      },
      {
        id: 2,
        text: 'At the touch of love everyone becomes a poet.',
        authorId: 2,
        authorName: 'Plato',
        context: 'Love',
        category: 'Poetry',
      },
    ],
    totalElements: 2,
    totalPages: 1,
    size: 10,
    number: 0,
  };

  beforeEach(async () => {
    const quoteServiceSpy = jasmine.createSpyObj('QuoteService', [
      'getQuotes',
      'getQuote',
      'createQuote',
      'updateQuote',
      'deleteQuote',
      'searchQuotes',
      'getQuotesByAuthor',
    ]);

    await TestBed.configureTestingModule({
      imports: [QuoteList],
      providers: [
        { provide: QuoteService, useValue: quoteServiceSpy },
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    quoteService = TestBed.inject(QuoteService) as jasmine.SpyObj<QuoteService>;
    fixture = TestBed.createComponent(QuoteList);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load quotes on init', () => {
    quoteService.getQuotes.and.returnValue(of(mockQuotePage));

    fixture.detectChanges(); // triggers ngOnInit

    expect(quoteService.getQuotes).toHaveBeenCalledWith(0, 10, 'id,desc');
    expect(component.quotes.length).toBe(2);
    expect(component.totalElements).toBe(2);
  });

  it('should handle loading state', fakeAsync(() => {
    quoteService.getQuotes.and.returnValue(of(mockQuotePage));

    component.loadQuotes();

    tick(); // Process the observable

    expect(component.loading).toBe(false);
    expect(component.quotes.length).toBe(2);
  }));

  it('should handle error when loading quotes fails', () => {
    const errorMessage = 'Failed to load quotes';
    quoteService.getQuotes.and.returnValue(throwError(() => new Error('Server error')));

    component.ngOnInit();

    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBe(false);
  });

  it('should navigate to next page', () => {
    quoteService.getQuotes.and.returnValue(of(mockQuotePage));
    component.pageIndex = 0;
    component.totalElements = 20;

    component.nextPage();

    expect(component.pageIndex).toBe(1);
    expect(quoteService.getQuotes).toHaveBeenCalled();
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
    quoteService.getQuotes.and.returnValue(of(mockQuotePage));
    component.pageIndex = 1;

    component.previousPage();

    expect(component.pageIndex).toBe(0);
    expect(quoteService.getQuotes).toHaveBeenCalled();
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
    const quote = mockQuotePage.content[0];

    component.openDeleteDialog(quote);

    expect(component.showDeleteDialog).toBe(true);
    expect(component.quoteToDelete).toBe(quote);
  });

  it('should cancel delete dialog', () => {
    component.quoteToDelete = mockQuotePage.content[0];
    component.showDeleteDialog = true;

    component.cancelDelete();

    expect(component.showDeleteDialog).toBe(false);
    expect(component.quoteToDelete).toBeNull();
  });

  it('should confirm delete and reload quotes', (done) => {
    const quote = mockQuotePage.content[0];
    component.quoteToDelete = quote;
    component.showDeleteDialog = true;

    quoteService.deleteQuote.and.returnValue(of(void 0));
    quoteService.getQuotes.and.returnValue(of(mockQuotePage));

    component.confirmDelete();

    fixture.whenStable().then(() => {
      expect(quoteService.deleteQuote).toHaveBeenCalledWith(quote.id!);
      expect(component.showDeleteDialog).toBe(false);
      expect(component.quoteToDelete).toBeNull();
      expect(quoteService.getQuotes).toHaveBeenCalled();
      done();
    });
  });

  it('should handle delete error', (done) => {
    const quote = mockQuotePage.content[0];
    component.quoteToDelete = quote;
    component.showDeleteDialog = true;

    quoteService.deleteQuote.and.returnValue(throwError(() => new Error('Delete failed')));

    component.confirmDelete();

    fixture.whenStable().then(() => {
      expect(component.error).toBe('Failed to delete quote');
      expect(component.showDeleteDialog).toBe(false);
      expect(component.quoteToDelete).toBeNull();
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
