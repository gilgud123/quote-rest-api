export interface Author {
  id?: number;
  name: string;
  biography?: string;
  birthYear?: number;
  deathYear?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface AuthorPage {
  content: Author[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
