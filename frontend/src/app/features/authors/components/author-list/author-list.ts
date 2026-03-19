import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthorService } from '../../services/author.service';
import { Author, AuthorPage } from '../../../../core/models';
import { AuthorDeleteDialog } from '../author-delete-dialog/author-delete-dialog';

@Component({
  selector: 'app-author-list',
  imports: [CommonModule, AuthorDeleteDialog],
  templateUrl: './author-list.html',
  styleUrl: './author-list.scss',
})
export class AuthorList implements OnInit {
  authors: Author[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;
  error: string | null = null;
  showDeleteDialog = false;
  authorToDelete: Author | null = null;

  constructor(
    private authorService: AuthorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAuthors();
  }

  loadAuthors(): void {
    this.loading = true;
    this.error = null;

    this.authorService.getAuthors(this.pageIndex, this.pageSize, 'name,asc').subscribe({
      next: (page: AuthorPage) => {
        this.authors = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load authors';
        this.loading = false;
        console.error('Error loading authors:', err);
      },
    });
  }

  createAuthor(): void {
    this.router.navigate(['/authors/create']);
  }

  editAuthor(author: Author): void {
    this.router.navigate(['/authors/edit', author.id]);
  }

  openDeleteDialog(author: Author): void {
    this.authorToDelete = author;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.authorToDelete?.id) {
      this.authorService.deleteAuthor(this.authorToDelete.id).subscribe({
        next: () => {
          this.showDeleteDialog = false;
          this.authorToDelete = null;
          this.loadAuthors();
        },
        error: (err) => {
          this.error = 'Failed to delete author';
          console.error('Error deleting author:', err);
          this.showDeleteDialog = false;
          this.authorToDelete = null;
        },
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.authorToDelete = null;
  }

  nextPage(): void {
    if ((this.pageIndex + 1) * this.pageSize < this.totalElements) {
      this.pageIndex++;
      this.loadAuthors();
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.loadAuthors();
    }
  }

  get hasNextPage(): boolean {
    return (this.pageIndex + 1) * this.pageSize < this.totalElements;
  }

  get hasPreviousPage(): boolean {
    return this.pageIndex > 0;
  }

  get currentPageStart(): number {
    return this.pageIndex * this.pageSize + 1;
  }

  get currentPageEnd(): number {
    return Math.min((this.pageIndex + 1) * this.pageSize, this.totalElements);
  }
}
