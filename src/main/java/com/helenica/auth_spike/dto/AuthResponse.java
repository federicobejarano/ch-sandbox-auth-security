package com.helenica.auth_spike.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record AuthResponse(
        Long id,
        String email,
        String role,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String token) {
}
