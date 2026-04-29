package com.helenica.auth_spike.entity;

/**
 * System role used by Spring Security for authorization decisions.
 *
 * <p>Mirrors the {@code rol_sistema} field of the main project schema
 * ({@code data_schema.md} Key Decision #1) and is intentionally distinct
 * from the business-domain {@code categoria} concept.
 *
 * <p>Persisted via {@code @Enumerated(EnumType.STRING)} on the {@link User}
 * entity so that the literal names ({@code "USER"}, {@code "ADMIN"}) reach
 * the database. {@code EnumType.ORDINAL} is rejected because reordering the
 * constants would silently corrupt existing rows.
 */
public enum Role {
    USER,
    ADMIN
}
