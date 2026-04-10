import { UserRegistrationRequest } from './user-registration.interface';

export interface UserRegistrationResponse extends UserRegistrationRequest {
  id: number;
  createdAt: string;
}
