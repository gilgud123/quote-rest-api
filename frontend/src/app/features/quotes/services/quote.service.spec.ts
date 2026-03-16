import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { QuoteService } from './quote.service';
import { Quote, QuotePage } from '../../../core/models';
import { environment } from '../../../../environments/environment';

describe('QuoteService', () => {
  let service: QuoteService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl;

  const mockQuote: Quote = {
    id: 1,
    text: 'The unexamined life is not worth living',
    context: 'Apology',
    category: 'Philosophy',
    authorId: 1,
    authorName: 'Socrates',
  };

  const mockQuotePage: QuotePage = {
    content: [mockQuote],
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [QuoteService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(QuoteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get quotes with pagination', () => {
    service.getQuotes(0, 10, 'id,desc').subscribe((page) => {
      expect(page).toEqual(mockQuotePage);
      expect(page.content.length).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/quotes?page=0&size=10&sort=id,desc`);
    expect(req.request.method).toBe('GET');
    req.flush(mockQuotePage);
  });

  it('should get single quote by id', () => {
    service.getQuote(1).subscribe((quote) => {
      expect(quote).toEqual(mockQuote);
    });

    const req = httpMock.expectOne(`${apiUrl}/quotes/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockQuote);
  });

  it('should create new quote', () => {
    const newQuote: Quote = {
      text: 'I know that I know nothing',
      context: 'Symposium',
      category: 'Philosophy',
      authorId: 1,
    };

    service.createQuote(newQuote).subscribe((quote) => {
      expect(quote.id).toBe(2);
      expect(quote.text).toBe('I know that I know nothing');
    });

    const req = httpMock.expectOne(`${apiUrl}/quotes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newQuote);
    req.flush({ id: 2, ...newQuote });
  });

  it('should update existing quote', () => {
    const updatedQuote: Quote = { ...mockQuote, context: 'Updated context' };

    service.updateQuote(1, updatedQuote).subscribe((quote) => {
      expect(quote.context).toBe('Updated context');
    });

    const req = httpMock.expectOne(`${apiUrl}/quotes/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(updatedQuote);
  });

  it('should delete quote', () => {
    service.deleteQuote(1).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/quotes/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should search quotes by text', () => {
    service.searchQuotes('unexamined', 0, 10).subscribe((page) => {
      expect(page.content[0].text).toContain('unexamined');
    });

    const req = httpMock.expectOne(`${apiUrl}/quotes/search?text=unexamined&page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockQuotePage);
  });

  it('should get quotes by author', () => {
    service.getQuotesByAuthor(1, 0, 10).subscribe((page) => {
      expect(page.content[0].authorId).toBe(1);
    });

    const req = httpMock.expectOne(`${apiUrl}/quotes/author/1?page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush(mockQuotePage);
  });
});
