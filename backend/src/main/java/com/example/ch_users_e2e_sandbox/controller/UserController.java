package com.example.ch_users_e2e_sandbox.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ch_users_e2e_sandbox.dto.UserRegistrationRequest;
import com.example.ch_users_e2e_sandbox.dto.UserRegistrationResponse;
import com.example.ch_users_e2e_sandbox.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = Objects.requireNonNull(userService, "UserService no puede ser null.");
    }

    @PostMapping
    public ResponseEntity<UserRegistrationResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserRegistrationResponse>> getAllUsers() {
        List<UserRegistrationResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
