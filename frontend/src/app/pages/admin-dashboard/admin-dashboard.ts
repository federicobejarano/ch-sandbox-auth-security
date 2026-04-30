import { AsyncPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { RouterLink } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

import { ProtectedUser } from '../../models/protected-user.interface';
import { AuthService } from '../../services/auth.service';
import { ProtectedResourceService } from '../../services/protected-resource.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
  imports: [
    AsyncPipe,
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonList,
    IonItem,
    IonLabel,
    IonNote,
    IonButton,
  ],
})
export class AdminDashboard implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly protectedResourceService = inject(ProtectedResourceService);

  protected readonly role = this.authService.getUserRole();
  protected users$!: Observable<ProtectedUser[]>;

  ngOnInit(): void {
    this.users$ = this.protectedResourceService.getAdminUsers();
  }

  logout(): void {
    this.authService.logout();
  }
}
