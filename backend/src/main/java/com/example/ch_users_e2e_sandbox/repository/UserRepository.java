package com.example.ch_users_e2e_sandbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ch_users_e2e_sandbox.entity.User;

/**
 * Contrato de acceso a datos para {@link User}.
 * Spring Data genera su implementacion en runtime mediante un proxy.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
