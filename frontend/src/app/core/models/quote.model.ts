export interface Quote {
  id?: number;
  text: string;
  context?: string;
  category?: string;
  authorId: number;
  authorName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface QuotePage {
  content: Quote[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
