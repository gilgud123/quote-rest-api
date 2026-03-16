import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Quote } from '../../../../core/models';

@Component({
  selector: 'app-quote-delete-dialog',
  imports: [CommonModule],
  templateUrl: './quote-delete-dialog.html',
  styleUrl: './quote-delete-dialog.scss',
})
export class QuoteDeleteDialog {
  @Input() quote!: Quote;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
