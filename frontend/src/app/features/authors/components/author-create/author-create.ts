import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthorService } from '../../services/author.service';
import { Author } from '../../../../core/models';

@Component({
  selector: 'app-author-create',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './author-create.html',
  styleUrl: './author-create.scss',
  standalone: true,
})
export class AuthorCreate implements OnInit {
  authorForm!: FormGroup;
  isSubmitting = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authorService: AuthorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      biography: ['', [Validators.maxLength(1000)]],
      birthYear: [null, [Validators.min(-500), Validators.max(2100)]],
      deathYear: [null, [Validators.min(-500), Validators.max(2100)]],
    });
  }

  onSubmit(): void {
    if (this.authorForm.valid && !this.isSubmitting) {
      this.isSubmitting = true;
      this.error = null;

      const author: Author = this.authorForm.value;

      this.authorService.createAuthor(author).subscribe({
        next: () => {
          this.router.navigate(['/authors']);
        },
        error: (err) => {
          this.error = 'Failed to create author. Please try again.';
          this.isSubmitting = false;
          console.error('Error creating author:', err);
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
