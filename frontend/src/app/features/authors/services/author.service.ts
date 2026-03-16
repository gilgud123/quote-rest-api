import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Author, AuthorPage } from '../../../core/models';

@Injectable({
  providedIn: 'root',
})
export class AuthorService {
  private endpoint = '/authors';

  constructor(private api: ApiService) {}

  getAuthors(page: number = 0, size: number = 10, sort?: string): Observable<AuthorPage> {
    const params: any = { page, size };
    if (sort) {
      params.sort = sort;
    }
    return this.api.get<AuthorPage>(this.endpoint, params);
  }

  getAuthor(id: number): Observable<Author> {
    return this.api.get<Author>(`${this.endpoint}/${id}`);
  }

  createAuthor(author: Author): Observable<Author> {
    return this.api.post<Author>(this.endpoint, author);
  }

  updateAuthor(id: number, author: Author): Observable<Author> {
    return this.api.put<Author>(`${this.endpoint}/${id}`, author);
  }

  deleteAuthor(id: number): Observable<void> {
    return this.api.delete<void>(`${this.endpoint}/${id}`);
  }

  searchAuthors(name: string, page: number = 0, size: number = 10): Observable<AuthorPage> {
    return this.api.get<AuthorPage>(`${this.endpoint}/search`, { name, page, size });
  }
}
