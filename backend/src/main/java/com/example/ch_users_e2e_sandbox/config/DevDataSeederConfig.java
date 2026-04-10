package com.example.ch_users_e2e_sandbox.config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.ch_users_e2e_sandbox.entity.User;
import com.example.ch_users_e2e_sandbox.repository.UserRepository;

@Configuration
@Profile("dev")
public class DevDataSeederConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevDataSeederConfig.class);

    @Bean
    CommandLineRunner devDataSeeder(UserRepository userRepository) {
        return args -> {
            LOGGER.info("Iniciando data seeding para el perfil dev.");

            if (userRepository.count() > 0) {
                LOGGER.info("Data seeding omitido: la tabla users ya contiene registros.");
                return;
            }

            List<User> seedUsers = List.of(
                    new User(
                            "SOFIA KALOGERAKIS",
                            "sofia.kalogerakis@helenica.org",
                            "standard",
                            LocalDateTime.of(2026, 3, 1, 10, 0)),
                    new User(
                            "LI",
                            "li.garcia@example.com",
                            "premium",
                            LocalDateTime.of(2026, 3, 5, 14, 30)),
                    new User(
                            "ANA MUÑOZ",
                            "ana.munoz@subdomain.example.com",
                            "honorary",
                            LocalDateTime.of(2026, 3, 10, 9, 15)));

            userRepository.saveAll(seedUsers);
            LOGGER.info("Data seeding completado: {} usuarios insertados en users.", seedUsers.size());
        };
    }
}
