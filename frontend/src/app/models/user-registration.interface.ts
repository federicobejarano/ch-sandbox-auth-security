export interface UserRegistrationRequest {

    /* Nombre de Usuario: texto libre
    * Backend: @NotBlank @Size(min=2, max=100)
    * Frontend: Validators.required, Validators.minLength(2)
    */
    name: string;

    /* Correo
    * Backend: @NotBlank @Email
    * Frontend: Validators.required, Validators.email
    */
    email: string;

    /* Tipo de membresía en la comunidad
    * Backend: @NotBlank
    * Frontend: Validators.required (ion-select restringe las opciones)
    *   Valores posibles: 'standard' | 'premium' | 'honorary'
    */
    membershipType: string;
}