package com.helenica.auth_spike.config;

import com.helenica.auth_spike.entity.Role;
import com.helenica.auth_spike.entity.User;
import com.helenica.auth_spike.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Seeds the in-memory database with a known {@code ADMIN} and {@code USER}
 * account so that the login flow can be exercised end-to-end on the first
 * boot. The bean only materialises under the {@code dev} profile so that
 * future test/prod profiles never carry these credentials by accident.
 *
 * <p><b>Why a {@link PasswordEncoder} dependency instead of a fresh
 * {@code BCryptPasswordEncoder}:</b> the encoder used here MUST be the same
 * bean Spring Security wires into its {@code DaoAuthenticationProvider}.
 * Different encoder instances produce different hashes for the same input;
 * even if both happened to be BCrypt with the same strength, declaring a
 * second instance would split responsibility and invite drift.
 *
 * <p><b>Idempotency guard:</b> {@code userRepository.count() &gt; 0} short
 * circuits subsequent boots. With {@code ddl-auto=create-drop} the schema
 * is recreated on every restart, so the guard is mostly defensive — but it
 * makes the seeder safe to run against a persistent profile if one is added
 * later (e.g. {@code ddl-auto=update}).
 *
 * <p><b>Lifecycle:</b> {@link CommandLineRunner} executes after the
 * {@code ApplicationContext} is fully refreshed, so the {@code UserRepository}
 * proxy and the {@code PasswordEncoder} bean are guaranteed to be ready.
 */
@Configuration
@Profile("dev")
public class DevDataSeederConfig {

    @Bean
    CommandLineRunner devDataSeeder(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            userRepository.saveAll(List.of(
                    new User(
                            "ADMIN USER",
                            "admin@helenica.org",
                            passwordEncoder.encode("admin123"),
                            Role.ADMIN
                    ),
                    new User(
                            "REGULAR USER",
                            "user@helenica.org",
                            passwordEncoder.encode("user1234"),
                            Role.USER
                    )
            ));
        };
    }
}
