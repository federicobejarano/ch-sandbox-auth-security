import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], {
      queryParams: { redirectUrl: state.url },
    });
  }

  const requiredRole = route.data?.['requiredRole'] as string | undefined;
  if (requiredRole && authService.getUserRole() !== `ROLE_${requiredRole}`) {
    return router.createUrlTree(['/unauthorized']);
  }

  return true;
};
