import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ProtectedUser } from '../models/protected-user.interface';

@Injectable({
  providedIn: 'root',
})
export class ProtectedResourceService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getUsers(): Observable<ProtectedUser[]> {
    return this.http.get<ProtectedUser[]>(`${this.apiUrl}/users`);
  }

  getAdminUsers(): Observable<ProtectedUser[]> {
    return this.http.get<ProtectedUser[]>(`${this.apiUrl}/admin/users`);
  }
}
