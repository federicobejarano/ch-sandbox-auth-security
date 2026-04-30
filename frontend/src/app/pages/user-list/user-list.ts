import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';
import { RouterLink } from '@angular/router';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton,
  IonList,
  IonItem,
  IonLabel,
  IonBadge,
  IonSpinner,
  IonButton,
} from '@ionic/angular/standalone';
import { ProtectedUser } from '../../models/protected-user.interface';
import { AuthService } from '../../services/auth.service';
import { ProtectedResourceService } from '../../services/protected-resource.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
  imports: [
    AsyncPipe,
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButtons,
    IonBackButton,
    IonList,
    IonItem,
    IonLabel,
    IonBadge,
    IonSpinner,
    IonButton,
  ],
})
export class UserList implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly protectedResourceService = inject(ProtectedResourceService);

  protected readonly roleUiMap: Record<
    string,
    { label: string; color: 'primary' | 'warning' | 'medium' }
  > = {
    ROLE_USER: {
      label: 'Usuario',
      color: 'primary',
    },
    ROLE_ADMIN: {
      label: 'Administrador',
      color: 'warning',
    },
    USER: {
      label: 'Usuario',
      color: 'primary',
    },
    ADMIN: {
      label: 'Administrador',
      color: 'warning',
    },
  };

  protected readonly currentRole = this.authService.getUserRole();
  users$!: Observable<ProtectedUser[]>;

  ngOnInit(): void {
    this.users$ = this.protectedResourceService.getUsers();
  }

  logout(): void {
    this.authService.logout();
  }
}