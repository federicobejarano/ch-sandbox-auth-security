package com.example.ch_users_e2e_sandbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ch_users_e2e_sandbox.dto.UserRegistrationRequest;
import com.example.ch_users_e2e_sandbox.dto.UserRegistrationResponse;
import com.example.ch_users_e2e_sandbox.entity.User;
import com.example.ch_users_e2e_sandbox.repository.UserRepository;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserAndPersistThroughJpaInfrastructure() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "  Ada Lovelace  ",
                "  Ada@Example.COM  ",
                "premium");

        UserRegistrationResponse response = userService.registerUser(request);
        List<User> persistedUsers = userRepository.findAll();

        assertThat(userService).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("ADA LOVELACE");
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.membershipType()).isEqualTo("premium");
        assertThat(response.createdAt()).isNotNull();

        assertThat(persistedUsers).hasSize(1);
        assertThat(persistedUsers.getFirst().getId()).isEqualTo(response.id());
        assertThat(persistedUsers.getFirst().getName()).isEqualTo("ADA LOVELACE");
        assertThat(persistedUsers.getFirst().getEmail()).isEqualTo("ada@example.com");
        assertThat(persistedUsers.getFirst().getMembershipType()).isEqualTo("premium");
        assertThat(persistedUsers.getFirst().getCreatedAt())
                .isEqualTo(response.createdAt().truncatedTo(ChronoUnit.MICROS));
    }
}
