import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { Quote, QuotePage } from '../../../../core/models';
import { QuoteDeleteDialog } from '../quote-delete-dialog/quote-delete-dialog';

@Component({
  selector: 'app-quote-list',
  imports: [CommonModule, QuoteDeleteDialog],
  templateUrl: './quote-list.html',
  styleUrl: './quote-list.scss',
  standalone: true,
})
export class QuoteList implements OnInit {
  quotes: Quote[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  loading = false;
  error: string | null = null;
  showDeleteDialog = false;
  quoteToDelete: Quote | null = null;

  constructor(
    private quoteService: QuoteService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadQuotes();
  }

  loadQuotes(): void {
    this.loading = true;
    this.error = null;

    this.quoteService.getQuotes(this.pageIndex, this.pageSize, 'id,desc').subscribe({
      next: (page: QuotePage) => {
        this.quotes = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load quotes';
        this.loading = false;
        console.error('Error loading quotes:', err);
      },
    });
  }

  createQuote(): void {
    this.router.navigate(['/quotes/create']);
  }

  editQuote(quote: Quote): void {
    this.router.navigate(['/quotes/edit', quote.id]);
  }

  openDeleteDialog(quote: Quote): void {
    this.quoteToDelete = quote;
    this.showDeleteDialog = true;
  }

  confirmDelete(): void {
    if (this.quoteToDelete?.id) {
      this.quoteService.deleteQuote(this.quoteToDelete.id).subscribe({
        next: () => {
          this.showDeleteDialog = false;
          this.quoteToDelete = null;
          this.loadQuotes();
        },
        error: (err) => {
          this.error = 'Failed to delete quote';
          console.error('Error deleting quote:', err);
          this.showDeleteDialog = false;
          this.quoteToDelete = null;
        },
      });
    }
  }

  cancelDelete(): void {
    this.showDeleteDialog = false;
    this.quoteToDelete = null;
  }

  nextPage(): void {
    if ((this.pageIndex + 1) * this.pageSize < this.totalElements) {
      this.pageIndex++;
      this.loadQuotes();
    }
  }

  previousPage(): void {
    if (this.pageIndex > 0) {
      this.pageIndex--;
      this.loadQuotes();
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
