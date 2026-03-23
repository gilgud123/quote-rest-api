import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { AuthorService } from '../../../authors/services/author.service';
import { Quote, Author } from '../../../../core/models';

@Component({
  selector: 'app-quote-create',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './quote-create.html',
  styleUrl: './quote-create.scss',
  standalone: true,
})
export class QuoteCreate implements OnInit {
  quoteForm!: FormGroup;
  authors: Author[] = [];
  isSubmitting = false;
  isLoadingAuthors = true;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private quoteService: QuoteService,
    private authorService: AuthorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.quoteForm = this.fb.group({
      text: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(1000)]],
      context: ['', [Validators.maxLength(255)]],
      category: ['', [Validators.maxLength(50)]],
      authorId: [null, [Validators.required]],
    });

    this.loadAuthors();
  }

  loadAuthors(): void {
    this.authorService.getAuthors(0, 100, 'name,asc').subscribe({
      next: (page) => {
        this.authors = page.content;
        this.isLoadingAuthors = false;
      },
      error: (err) => {
        this.error = 'Failed to load authors. Please try again.';
        this.isLoadingAuthors = false;
        console.error('Error loading authors:', err);
      },
    });
  }

  onSubmit(): void {
    if (this.quoteForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.error = null;

      const quote: Quote = this.quoteForm.value;

      this.quoteService.createQuote(quote).subscribe({
        next: () => {
          this.router.navigate(['/quotes']);
        },
        error: (err) => {
          this.error = 'Failed to create quote. Please try again.';
          this.isSubmitting = false;
          console.error('Error creating quote:', err);
        },
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/quotes']);
  }

  get text() {
    return this.quoteForm.get('text');
  }

  get context() {
    return this.quoteForm.get('context');
  }

  get category() {
    return this.quoteForm.get('category');
  }

  get authorId() {
    return this.quoteForm.get('authorId');
  }
}
