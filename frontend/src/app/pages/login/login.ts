import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonHeader,
  IonInput,
  IonItem,
  IonText,
  IonTitle,
  IonToolbar,
  ToastController,
} from '@ionic/angular/standalone';

import { LoginRequest } from '../../models/login-request.interface';
import { ValidationErrorResponse } from '../../models/validation-error-response.interface';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-page',
  templateUrl: './login.html',
  styleUrl: './login.css',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonItem,
    IonInput,
    IonButton,
    IonText,
  ],
})
export class LoginPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toastController = inject(ToastController);

  protected isSubmitting = false;

  readonly loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  get emailCtrl(): FormControl<string> {
    return this.loginForm.controls.email;
  }

  get passwordCtrl(): FormControl<string> {
    return this.loginForm.controls.password;
  }

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    const redirectUrl = this.route.snapshot.queryParamMap.get('redirectUrl');
    void this.router.navigateByUrl(redirectUrl || '/users');
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
    if (this.loginForm.invalid || this.isSubmitting) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.clearServerErrors();
    this.isSubmitting = true;

    const payload: LoginRequest = this.loginForm.getRawValue();

    this.authService.login(payload).subscribe({
      next: () => {
        const redirectUrl = this.route.snapshot.queryParamMap.get('redirectUrl');
        void this.router.navigateByUrl(redirectUrl || '/users');
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.applyServerErrors(error);
        void this.presentToast(this.resolveErrorMessage(error), 'danger');
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  private clearServerErrors(): void {
    [this.emailCtrl, this.passwordCtrl].forEach((control) => {
      if (!control.hasError('serverError')) {
        return;
      }

      const remainingErrors = { ...(control.errors ?? {}) };
      delete remainingErrors['serverError'];
      control.setErrors(Object.keys(remainingErrors).length > 0 ? remainingErrors : null);
    });
  }

  private applyServerErrors(error: HttpErrorResponse): void {
    if (error.status === 401) {
      this.passwordCtrl.setErrors({
        ...(this.passwordCtrl.errors ?? {}),
        serverError: 'Las credenciales son inválidas.',
      });
      this.passwordCtrl.markAsTouched();
      return;
    }

    const validationError = error.error as ValidationErrorResponse | null;
    Object.entries(validationError?.fieldErrors ?? {}).forEach(([field, message]) => {
      const control = this.loginForm.get(field);

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

  private resolveErrorMessage(error: HttpErrorResponse): string {
    if (typeof error.error?.message === 'string') {
      return error.error.message;
    }

    if (error.status === 401) {
      return 'Email o password incorrectos.';
    }

    return 'No se pudo iniciar sesión.';
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
