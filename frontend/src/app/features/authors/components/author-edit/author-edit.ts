import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthorService } from '../../services/author.service';
import { Author } from '../../../../core/models';

@Component({
  selector: 'app-author-edit',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './author-edit.html',
  styleUrl: './author-edit.scss',
})
export class AuthorEdit implements OnInit {
  authorForm!: FormGroup;
  authorId!: number;
  isSubmitting = false;
  isLoading = true;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authorService: AuthorService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.authorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      biography: ['', [Validators.maxLength(1000)]],
      birthYear: [null, [Validators.min(-500), Validators.max(2100)]],
      deathYear: [null, [Validators.min(-500), Validators.max(2100)]],
    });

    this.route.params.subscribe((params) => {
      this.authorId = +params['id'];
      this.loadAuthor();
    });
  }

  loadAuthor(): void {
    this.authorService.getAuthor(this.authorId).subscribe({
      next: (author) => {
        this.authorForm.patchValue({
          name: author.name,
          biography: author.biography || '',
          birthYear: author.birthYear,
          deathYear: author.deathYear,
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load author. Please try again.';
        this.isLoading = false;
        console.error('Error loading author:', err);
      },
    });
  }

  onSubmit(): void {
    if (this.authorForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.error = null;

      const author: Author = {
        id: this.authorId,
        ...this.authorForm.value,
      };

      this.authorService.updateAuthor(this.authorId, author).subscribe({
        next: () => {
          this.router.navigate(['/authors']);
        },
        error: (err) => {
          this.error = 'Failed to update author. Please try again.';
          this.isSubmitting = false;
          console.error('Error updating author:', err);
        },
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/authors']);
  }

  get name() {
    return this.authorForm.get('name');
  }

  get biography() {
    return this.authorForm.get('biography');
  }

  get birthYear() {
    return this.authorForm.get('birthYear');
  }

  get deathYear() {
    return this.authorForm.get('deathYear');
  }
}
