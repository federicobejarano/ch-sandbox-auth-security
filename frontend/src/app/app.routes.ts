import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/registration-form/registration-form').then(
        (m) => m.RegistrationForm,
      ),
  },
  {
    path: 'users',
    loadComponent: () =>
      import('./pages/user-list/user-list').then((m) => m.UserList),
    canActivate: [authGuard],
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./pages/admin-dashboard/admin-dashboard').then(
        (m) => m.AdminDashboard,
      ),
    canActivate: [authGuard],
    data: { requiredRole: 'ADMIN' },
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./pages/unauthorized/unauthorized').then(
        (m) => m.UnauthorizedPage,
      ),
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
