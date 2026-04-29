package com.helenica.auth_spike.security;

import com.helenica.auth_spike.entity.User;
import com.helenica.auth_spike.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Adapter that bridges the application's {@link User} JPA entity with
 * Spring Security's {@link UserDetails} contract.
 *
 * <p><b>Why an adapter and not the entity itself:</b> {@code User} is a
 * persistence-layer concern; coupling it to Spring Security by making it
 * implement {@code UserDetails} would drag the security API into the
 * domain model and force the entity to expose framework-shaped accessors
 * ({@code getAuthorities()}, {@code isAccountNonLocked()}, ...). Keeping
 * the boundary explicit preserves the entity as a pure aggregate root and
 * lets Security depend on its own immutable view.
 *
 * <p><b>Why {@code email} as username:</b> the {@code sub} claim of the
 * issued JWT carries the email, and {@link UserRepository#findByEmail}
 * is the only stable, unique lookup we expose. Spring Security calls this
 * method with whatever string the {@code DaoAuthenticationProvider}
 * receives as the {@code principal} of the
 * {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken},
 * which in our flow is always the email submitted on {@code POST /api/auth/login}.
 *
 * <p><b>Exception contract:</b> when the lookup fails we throw
 * {@link UsernameNotFoundException}. {@code DaoAuthenticationProvider}
 * deliberately translates this exception into a generic
 * {@code BadCredentialsException} before it surfaces, so an attacker
 * cannot distinguish "unknown email" from "wrong password" by observing
 * the response. This is a deliberate timing/oracle defence baked into
 * Spring Security and we must not subvert it by leaking the original
 * cause to the controller layer.
 *
 * <p><b>Authority encoding:</b> we delegate to
 * {@code org.springframework.security.core.userdetails.User.builder()}
 * (NOT our entity) and call {@code .roles(role.name())}. The builder
 * automatically prepends {@code ROLE_}, which is the prefix
 * {@code hasRole("ADMIN")} expects when matching authorities. Mixing
 * {@code .roles()} with {@code hasAuthority()} (or vice versa) silently
 * fails authorisation; the spike sticks to {@code .roles()} +
 * {@code hasRole()} consistently.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
