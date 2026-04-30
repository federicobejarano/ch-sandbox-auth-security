package com.helenica.auth_spike.dto;

import java.util.Map;
import java.util.Objects;

public record ValidationErrorResponse(
        int status,
        String message,
        Map<String, String> fieldErrors) {

    public ValidationErrorResponse {
        message = Objects.requireNonNull(message, "message cannot be null");
        fieldErrors = Map.copyOf(Objects.requireNonNull(fieldErrors, "fieldErrors cannot be null"));
    }
}
