import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import {
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
    IonButton,
    IonContent,
    IonHeader,
    IonInput,
    IonItem,
    IonRouterLink,
    IonSelect,
    IonSelectOption,
    ToastController,
    IonTitle,
    IonToolbar,
} from '@ionic/angular/standalone';

import { UserRegistrationRequest } from '../../models/user-registration.interface';
import { UserRegistrationResponse } from '../../models/user-registration-response.interface';
import { ValidationErrorResponse } from '../../models/validation-error-response.interface';
import { UserService } from '../../services/user.service';

@Component({
    selector: 'app-registration-form',
    templateUrl: './registration-form.html',
    styleUrl: './registration-form.css',
    imports: [
        // formularios
        ReactiveFormsModule,

        // navegación
        RouterLink, IonRouterLink,

        // componentes estructurales Ionic
        IonHeader, IonToolbar,
        IonTitle, IonContent,

        // componentes Ionic de formulario
        IonButton, IonItem, IonInput,
        IonSelect, IonSelectOption
    ],
})
export class RegistrationForm {
    private readonly userService = inject(UserService);
    private readonly toastController = inject(ToastController);

    readonly registrationForm = new FormGroup({

        // campos del formulario

        name: new FormControl('', {
            nonNullable: true,
            validators: [
                Validators.required,
                Validators.minLength(2),
            ],
        }),

        email: new FormControl('', {
            nonNullable: true,
            validators: [
                Validators.required,
                Validators.email,
            ],
        }),

        membershipType: new FormControl('', {
            nonNullable: true,
            validators: [
                Validators.required,
            ],
        }),

        // handler de submit

    });

    get nameCtrl(): FormControl<string> {
        return this.registrationForm.controls.name;
    }

    get emailCtrl(): FormControl<string> {
        return this.registrationForm.controls.email;
    }

    get membershipTypeCtrl(): FormControl<string> {
        return this.registrationForm.controls.membershipType;
    }

    getErrorText(
        control: FormControl<string>,
        errorMap: Record<string, string>,
    ): string {
        if (!control.touched || control.valid) {
            return '';
        }

        for (const [errorKey, message] of Object.entries(errorMap)) {
            if (control.hasError(errorKey)) {
                if (errorKey === 'serverError') {
                    return (control.getError('serverError') as string | undefined) ?? message;
                }

                return message;
            }
        }

        return '';
    }

    onSubmit(): void {

        if (this.registrationForm.invalid) {
            this.registrationForm.markAllAsTouched();
            return;
        }

        const payload: UserRegistrationRequest = this.registrationForm.getRawValue();

        // Learning Note: el request HTTP no se ejecuta al crear el Observable.
        // Se dispara recién cuando subscribe() activa el flujo reactivo de HttpClient.
        this.userService.register(payload).subscribe({
            next: (response: UserRegistrationResponse) => {
                void this.presentToast(
                    `Usuario registrado correctamente: ${response.name}`,
                    'success',
                );
                this.registrationForm.reset({
                    name: '',
                    email: '',
                    membershipType: '',
                });
            },
            error: (error: HttpErrorResponse) => {
                const validationError = error.error as ValidationErrorResponse | null;
                const fieldErrors = validationError?.fieldErrors ?? {};
                const message = validationError?.message ?? 'Error inesperado del servidor';

                // Learning Note: setErrors() permite proyectar una validación del backend
                // sobre el mismo estado reactivo que ya consume el template de Ionic.
                this.applyServerErrors(fieldErrors);
                void this.presentToast(message, 'danger');
            },
        });
    }

    private applyServerErrors(fieldErrors: Record<string, string>): void {
        Object.entries(fieldErrors).forEach(([fieldName, message]) => {
            const control = this.registrationForm.get(fieldName);

            if (!control) {
                return;
            }

            control.setErrors({
                ...(control.errors ?? {}),
                serverError: message,
            });
            control.markAsTouched();
        });
    }

    private async presentToast(
        message: string,
        color: 'success' | 'danger' | 'warning',
    ): Promise<void> {
        const toast = await this.toastController.create({
            message,
            duration: 3000,
            position: 'bottom',
            color,
        });

        await toast.present();
    }
}
