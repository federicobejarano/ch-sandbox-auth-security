export interface ValidationErrorResponse {
  status: number;
  message: string;
  fieldErrors: Record<string, string>;
}
