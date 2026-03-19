import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Quote, QuotePage } from '../../../core/models';

@Injectable({
  providedIn: 'root',
})
export class QuoteService {
  private endpoint = '/quotes';

  constructor(private api: ApiService) {}

  getQuotes(page: number = 0, size: number = 10, sort?: string): Observable<QuotePage> {
    const params: any = { page, size };
    if (sort) {
      params.sort = sort;
    }
    return this.api.get<QuotePage>(this.endpoint, params);
  }

  getQuote(id: number): Observable<Quote> {
    return this.api.get<Quote>(`${this.endpoint}/${id}`);
  }

  createQuote(quote: Quote): Observable<Quote> {
    return this.api.post<Quote>(this.endpoint, quote);
  }

  updateQuote(id: number, quote: Quote): Observable<Quote> {
    return this.api.put<Quote>(`${this.endpoint}/${id}`, quote);
  }

  deleteQuote(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }

  searchQuotes(text: string, page: number = 0, size: number = 10): Observable<QuotePage> {
    return this.api.get<QuotePage>(`${this.endpoint}/search`, { text, page, size });
  }

  getQuotesByAuthor(authorId: number, page: number = 0, size: number = 10): Observable<QuotePage> {
    return this.api.get<QuotePage>(`${this.endpoint}/author/${authorId}`, { page, size });
  }
}
