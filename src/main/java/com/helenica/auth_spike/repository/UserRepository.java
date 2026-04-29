package com.helenica.auth_spike.repository;

import com.helenica.auth_spike.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User}.
 *
 * <p>The implementation is generated at runtime by Spring Data: the bean is a
 * dynamic proxy backed by {@code SimpleJpaRepository}, with derived query
 * methods translated to JPQL by parsing the method name.
 *
 * <p>{@link #findByEmail(String)} is the only addition over the plain
 * {@code JpaRepository} contract; it is consumed by
 * {@code AppUserDetailsService.loadUserByUsername(...)} during authentication.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
