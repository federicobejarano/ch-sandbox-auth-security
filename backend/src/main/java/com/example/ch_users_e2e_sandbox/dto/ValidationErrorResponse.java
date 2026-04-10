package com.example.ch_users_e2e_sandbox.dto;

import java.util.Map;
import java.util.Objects;

public record ValidationErrorResponse(
        int status,
        String message,
        Map<String, String> fieldErrors) {

    public ValidationErrorResponse {
        message = Objects.requireNonNull(message, "El mensaje de error no puede ser null.");
        fieldErrors = Map.copyOf(Objects.requireNonNull(fieldErrors, "Los errores por campo no pueden ser null."));
    }
}
