package com.example.ch_users_e2e_sandbox.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.example.ch_users_e2e_sandbox.entity.User;

public record UserRegistrationResponse(
        Long id,
        String name,
        String email,
        String membershipType,
        LocalDateTime createdAt) {

    public static UserRegistrationResponse fromEntity(User entity) {
        Objects.requireNonNull(entity, "La entidad User no puede ser null.");

        return new UserRegistrationResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getMembershipType(),
                entity.getCreatedAt());
    }
}
