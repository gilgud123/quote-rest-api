import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { QuoteService } from '../../services/quote.service';
import { AuthorService } from '../../../authors/services/author.service';
import { Quote, Author } from '../../../../core/models';

@Component({
  selector: 'app-quote-edit',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './quote-edit.html',
  styleUrl: './quote-edit.scss',
  standalone: true,
})
export class QuoteEdit implements OnInit {
  quoteForm!: FormGroup;
  quoteId!: number;
  authors: Author[] = [];
  isSubmitting = false;
  isLoading = true;
  isLoadingAuthors = true;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private quoteService: QuoteService,
    private authorService: AuthorService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.quoteForm = this.fb.group({
      text: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(1000)]],
      context: ['', [Validators.maxLength(255)]],
      category: ['', [Validators.maxLength(50)]],
      authorId: [null, [Validators.required]],
    });

    this.route.params.subscribe((params) => {
      this.quoteId = +params['id'];
      this.loadAuthors();
      this.loadQuote();
    });
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

  loadQuote(): void {
    this.quoteService.getQuote(this.quoteId).subscribe({
      next: (quote) => {
        this.quoteForm.patchValue({
          text: quote.text,
          context: quote.context || '',
          category: quote.category || '',
          authorId: quote.authorId,
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load quote. Please try again.';
        this.isLoading = false;
        console.error('Error loading quote:', err);
      },
    });
  }

  onSubmit(): void {
    if (this.quoteForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.error = null;

      const quote: Quote = {
        id: this.quoteId,
        ...this.quoteForm.value,
      };

      this.quoteService.updateQuote(this.quoteId, quote).subscribe({
        next: () => {
          this.router.navigate(['/quotes']);
        },
        error: (err) => {
          this.error = 'Failed to update quote. Please try again.';
          this.isSubmitting = false;
          console.error('Error updating quote:', err);
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
