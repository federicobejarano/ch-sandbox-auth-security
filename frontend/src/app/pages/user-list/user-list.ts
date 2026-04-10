import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';
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
    IonNote,
    IonBadge,
    IonSpinner,
} from '@ionic/angular/standalone';
import { UserRegistrationResponse } from '../../models/user-registration-response.interface';
import { UserService } from '../../services/user.service';

@Component({
    selector: 'app-user-list',
    standalone: true,
    templateUrl: './user-list.html',
    styleUrl: './user-list.css',
    imports: [
        AsyncPipe,
        IonHeader,
        IonToolbar,
        IonTitle,
        IonContent,
        IonButtons,
        IonBackButton,
        IonList,
        IonItem,
        IonLabel,
        IonNote,
        IonBadge,
        IonSpinner,
    ]
})
export class UserList implements OnInit {
    private readonly userService = inject(UserService);
    protected readonly membershipUiMap: Record<
        string,
        { label: string; color: 'primary' | 'warning' | 'tertiary' | 'medium' }
    > = {
        standard: {
            label: 'Estándar',
            color: 'primary',
        },
        premium: {
            label: 'Premium',
            color: 'warning',
        },
        honorary: {
            label: 'Honorario',
            color: 'tertiary',
        },
    };

    // Learning Note: el componente expone el Observable y delega
    // la suscripcion al async pipe para evitar manejo manual del ciclo de vida.
    users$!: Observable<UserRegistrationResponse[]>;

    ngOnInit(): void {
        this.users$ = this.userService.getAll();
    }
}