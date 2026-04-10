package com.example.ch_users_e2e_sandbox.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.ch_users_e2e_sandbox.dto.UserRegistrationRequest;
import com.example.ch_users_e2e_sandbox.dto.UserRegistrationResponse;
import com.example.ch_users_e2e_sandbox.entity.User;
import com.example.ch_users_e2e_sandbox.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository no puede ser null.");
    }

    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {
        Objects.requireNonNull(request, "UserRegistrationRequest no puede ser null.");

        User userToPersist = mapToEntity(request);
        User savedUser = userRepository.save(userToPersist);

        return UserRegistrationResponse.fromEntity(savedUser);
    }

    public List<UserRegistrationResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserRegistrationResponse::fromEntity)
                .toList();
    }

    private User mapToEntity(UserRegistrationRequest request) {
        return new User(
                toUpperCase(request.name()),
                request.email(),
                request.membershipType(),
                currentTimestamp());
    }

    private String toUpperCase(String value) {
        return value != null ? value.toUpperCase(Locale.ROOT) : null;
    }

    private LocalDateTime currentTimestamp() {
        return LocalDateTime.now();
    }
}
