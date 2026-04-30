import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthResponse } from '../models/auth-response.interface';
import { LoginRequest } from '../models/login-request.interface';
import { RegisterRequest } from '../models/register-request.interface';

interface JwtPayload {
  exp?: number;
  role?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'auth_token';

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response) => {
        if (response.token) {
          this.setToken(response.token);
        }
      }),
    );
  }

  logout(): void {
    this.clearToken();
    void this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.getStorage()?.getItem(this.tokenKey) ?? null;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    return !this.isTokenExpired(token);
  }

  getUserRole(): string | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    const payload = this.decodePayload(token);
    return payload?.role ?? null;
  }

  private isTokenExpired(token: string): boolean {
    const payload = this.decodePayload(token);

    if (!payload?.exp) {
      return true;
    }

    return Date.now() >= payload.exp * 1000;
  }

  private decodePayload(token: string): JwtPayload | null {
    try {
      const payloadSegment = token.split('.')[1];

      if (!payloadSegment) {
        return null;
      }

      const normalizedBase64 = payloadSegment
        .replace(/-/g, '+')
        .replace(/_/g, '/');
      const paddedBase64 =
        normalizedBase64 +
        '='.repeat((4 - (normalizedBase64.length % 4)) % 4);

      // El payload decodificado se usa solo para UX; el backend sigue validando el JWT.
      return JSON.parse(atob(paddedBase64)) as JwtPayload;
    } catch {
      return null;
    }
  }

  private setToken(token: string): void {
    this.getStorage()?.setItem(this.tokenKey, token);
  }

  private clearToken(): void {
    this.getStorage()?.removeItem(this.tokenKey);
  }

  private getStorage(): Storage | null {
    if (typeof window === 'undefined') {
      return null;
    }

    return window.localStorage;
  }
}
