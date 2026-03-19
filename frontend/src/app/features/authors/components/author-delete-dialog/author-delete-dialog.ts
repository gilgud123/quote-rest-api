import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Author } from '../../../../core/models';

@Component({
  selector: 'app-author-delete-dialog',
  imports: [CommonModule],
  templateUrl: './author-delete-dialog.html',
  styleUrl: './author-delete-dialog.scss',
})
export class AuthorDeleteDialog {
  @Input() author!: Author;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
