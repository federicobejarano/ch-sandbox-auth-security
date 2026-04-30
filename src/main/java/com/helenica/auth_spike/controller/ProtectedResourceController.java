package com.helenica.auth_spike.controller;

import com.helenica.auth_spike.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProtectedResourceController {

    private final UserRepository userRepository;

    public ProtectedResourceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        List<UserSummary> users = userRepository.findAll().stream()
                .map(user -> new UserSummary(user.getId(), user.getEmail(), user.getRole().name()))
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummary>> getAdminUserView() {
        List<UserSummary> users = userRepository.findAll().stream()
                .map(user -> new UserSummary(user.getId(), user.getEmail(), user.getRole().name()))
                .toList();

        return ResponseEntity.ok(users);
    }

    public record UserSummary(Long id, String email, String role) {
    }
}
