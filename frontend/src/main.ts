import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config'; // registra provideIonicAngular() <- podemos usar Ionic
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
