import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/authors',
    pathMatch: 'full',
  },
  {
    path: 'authors',
    canActivate: [authGuard],
    loadChildren: () => import('./features/authors/authors.routes').then((m) => m.AUTHOR_ROUTES),
  },
  {
    path: 'quotes',
    canActivate: [authGuard],
    loadChildren: () => import('./features/quotes/quotes.routes').then((m) => m.QUOTE_ROUTES),
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./shared/components/unauthorized/unauthorized.component').then(
        (m) => m.UnauthorizedComponent
      ),
  },
  {
    path: '**',
    redirectTo: '/authors',
  },
];
