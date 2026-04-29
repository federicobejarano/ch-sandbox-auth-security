package com.helenica.auth_spike.service;

import com.helenica.auth_spike.dto.AuthResponse;
import com.helenica.auth_spike.dto.LoginRequest;
import com.helenica.auth_spike.dto.RegisterRequest;
import com.helenica.auth_spike.entity.Role;
import com.helenica.auth_spike.entity.User;
import com.helenica.auth_spike.repository.UserRepository;
import com.helenica.auth_spike.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER);

        User saved = userRepository.save(user);

        return new AuthResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getRole().name(),
                null);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                token);
    }
}
