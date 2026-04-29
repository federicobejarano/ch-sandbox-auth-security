package com.helenica.auth_spike.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity backing the {@code users} table.
 *
 * <p>Evolution from Spike 1: drops {@code membershipType} and adds
 * {@code password} (BCrypt hash) plus {@code role} (enum). See Section 4.1
 * of the technical design for the full diff and justification.
 *
 * <p><b>Access strategy:</b> {@code @Id} is placed on a field, so Hibernate
 * uses field-level reflection for every column. Getters are exposed for
 * application use only; setters are intentionally omitted to keep
 * security-critical state ({@code password}, {@code role}) immutable after
 * construction.
 *
 * <p><b>No-arg constructor:</b> Required by the JPA spec for proxy creation
 * and result-set hydration via reflection. It is {@code protected} (not
 * {@code public}) so application code cannot accidentally instantiate an
 * empty {@code User}.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected User() {
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
