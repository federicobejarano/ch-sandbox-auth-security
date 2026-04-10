import { Component } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';

/* El componente raíz es un shell arquitectónico:
    NO contiene UI propia; su única responsabilidad
    es montar la jerarquía que Ionic requiere para
    gestionar navegación, transiciones y layout de
    plataforma
*/
@Component({
    selector: 'app-root',
    imports: [ IonApp, IonRouterOutlet ],
    template: `
        <ion-app>
            <ion-router-outlet />
        </ion-app>`,
    styles: [],
})export class App {}