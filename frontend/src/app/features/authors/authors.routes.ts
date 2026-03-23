import { Routes } from '@angular/router';
import { AuthorList } from './components/author-list/author-list';
import { AuthorCreate } from './components/author-create/author-create';
import { AuthorEdit } from './components/author-edit/author-edit';

export const AUTHOR_ROUTES: Routes = [
  { path: '', component: AuthorList },
  { path: 'create', component: AuthorCreate },
  { path: 'edit/:id', component: AuthorEdit },
];
