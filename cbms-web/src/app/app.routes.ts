import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'set-password',
    loadComponent: () =>
      import('./features/auth/pages/set-password/set-password.component').then(
        (m) => m.SetPasswordComponent
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'set-password',
  },
  {
    path: '**',
    redirectTo: 'set-password',
  },
];
