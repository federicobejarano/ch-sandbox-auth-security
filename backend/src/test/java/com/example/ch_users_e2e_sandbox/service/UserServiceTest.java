package com.example.ch_users_e2e_sandbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.ch_users_e2e_sandbox.dto.UserRegistrationRequest;
import com.example.ch_users_e2e_sandbox.dto.UserRegistrationResponse;
import com.example.ch_users_e2e_sandbox.entity.User;
import com.example.ch_users_e2e_sandbox.repository.UserRepository;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void registerUser_shouldMapDtoToEntityAndPersist() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "  test user  ",
                "  Test@Example.COM  ",
                "standard");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserRegistrationResponse response = userService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User persistedUser = userCaptor.getValue();

        assertThat(persistedUser.getName()).isEqualTo("TEST USER");
        assertThat(persistedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(persistedUser.getMembershipType()).isEqualTo("standard");
        assertThat(persistedUser.getCreatedAt()).isNotNull();

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("TEST USER");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.membershipType()).isEqualTo("standard");
        assertThat(response.createdAt()).isEqualTo(persistedUser.getCreatedAt());
    }

    @Test
    void getAllUsers_shouldReturnMappedResponses() {
        LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 3, 26, 10, 15);
        LocalDateTime secondCreatedAt = LocalDateTime.of(2026, 3, 26, 11, 45);

        User firstUser = new User("MARIA", "maria@example.com", "premium", firstCreatedAt);
        firstUser.setId(1L);

        User secondUser = new User("JUAN", "juan@example.com", "standard", secondCreatedAt);
        secondUser.setId(2L);

        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));

        List<UserRegistrationResponse> responses = userService.getAllUsers();

        verify(userRepository, times(1)).findAll();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(
                        UserRegistrationResponse::id,
                        UserRegistrationResponse::name,
                        UserRegistrationResponse::email,
                        UserRegistrationResponse::membershipType,
                        UserRegistrationResponse::createdAt)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, "MARIA", "maria@example.com", "premium", firstCreatedAt),
                        org.assertj.core.groups.Tuple.tuple(2L, "JUAN", "juan@example.com", "standard", secondCreatedAt));
    }
}
