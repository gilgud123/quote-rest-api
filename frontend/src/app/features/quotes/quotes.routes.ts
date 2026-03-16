import { Routes } from '@angular/router';
import { QuoteList } from './components/quote-list/quote-list';
import { QuoteCreate } from './components/quote-create/quote-create';
import { QuoteEdit } from './components/quote-edit/quote-edit';

export const QUOTE_ROUTES: Routes = [
  { path: '', component: QuoteList },
  { path: 'create', component: QuoteCreate },
  { path: 'edit/:id', component: QuoteEdit },
];
