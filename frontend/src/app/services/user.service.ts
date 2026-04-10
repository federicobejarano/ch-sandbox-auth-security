import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { UserRegistrationRequest } from '../models/user-registration.interface';
import { UserRegistrationResponse } from '../models/user-registration-response.interface';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly httpClient = inject(HttpClient);
  private readonly usersUrl = `${environment.apiUrl}/users`;

  // Learning Note: HttpClient devuelve Observables "cold";
  // la llamada real ocurre cuando un consumidor se suscribe.
  register(
    request: UserRegistrationRequest,
  ): Observable<UserRegistrationResponse> {
    return this.httpClient.post<UserRegistrationResponse>(this.usersUrl, request);
  }

  getAll(): Observable<UserRegistrationResponse[]> {
    return this.httpClient.get<UserRegistrationResponse[]>(this.usersUrl);
  }
}
