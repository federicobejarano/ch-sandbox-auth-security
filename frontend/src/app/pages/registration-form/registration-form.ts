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
  ToastController,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';

import { RegisterRequest } from '../../models/register-request.interface';
import { ValidationErrorResponse } from '../../models/validation-error-response.interface';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-registration-form',
  templateUrl: './registration-form.html',
  styleUrl: './registration-form.css',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonRouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonItem,
    IonInput,
  ],
})
export class RegistrationForm {
  private readonly authService = inject(AuthService);
  private readonly toastController = inject(ToastController);

  readonly registrationForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(8)],
    }),
  });

  get nameCtrl(): FormControl<string> {
    return this.registrationForm.controls.name;
  }

  get emailCtrl(): FormControl<string> {
    return this.registrationForm.controls.email;
  }

  get passwordCtrl(): FormControl<string> {
    return this.registrationForm.controls.password;
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

    this.clearServerErrors();
    const payload: RegisterRequest = this.registrationForm.getRawValue();

    this.authService.register(payload).subscribe({
      next: (response) => {
        void this.presentToast(
          `Usuario registrado correctamente: ${response.email}`,
          'success',
        );
        this.registrationForm.reset({
          name: '',
          email: '',
          password: '',
        });
      },
      error: (error: HttpErrorResponse) => {
        const validationError = error.error as ValidationErrorResponse | null;
        const message = validationError?.message ?? 'No se pudo registrar el usuario.';

        this.applyServerErrors(validationError?.fieldErrors ?? {});
        void this.presentToast(message, 'danger');
      },
    });
  }

  private clearServerErrors(): void {
    [this.nameCtrl, this.emailCtrl, this.passwordCtrl].forEach((control) => {
      if (!control.hasError('serverError')) {
        return;
      }

      const remainingErrors = { ...(control.errors ?? {}) };
      delete remainingErrors['serverError'];
      control.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
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
