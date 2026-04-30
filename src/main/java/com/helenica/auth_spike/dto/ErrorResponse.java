package com.helenica.auth_spike.dto;

public record ErrorResponse(
        int status,
        String message) {
}
