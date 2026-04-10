package com.example.ch_users_e2e_sandbox.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.ch_users_e2e_sandbox.entity.User;
import com.example.ch_users_e2e_sandbox.repository.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_shouldPersistUserAndReturnCreatedResponse() throws IOException, InterruptedException {
        HttpResponse<String> response = sendPost("""
                {
                  "name": "Ada Lovelace",
                  "email": "Ada@Example.COM",
                  "membershipType": "premium"
                }
                """);

        JsonNode responseBody = objectMapper.readTree(response.body());
        List<User> persistedUsers = userRepository.findAll();

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(responseBody.get("id").asLong()).isPositive();
        assertThat(responseBody.get("name").stringValue()).isEqualTo("ADA LOVELACE");
        assertThat(responseBody.get("email").stringValue()).isEqualTo("ada@example.com");
        assertThat(responseBody.get("membershipType").stringValue()).isEqualTo("premium");
        assertThat(responseBody.get("createdAt").stringValue()).isNotBlank();

        assertThat(persistedUsers).hasSize(1);
        assertThat(persistedUsers.getFirst().getId()).isEqualTo(responseBody.get("id").asLong());
        assertThat(persistedUsers.getFirst().getName()).isEqualTo("ADA LOVELACE");
        assertThat(persistedUsers.getFirst().getEmail()).isEqualTo("ada@example.com");
        assertThat(persistedUsers.getFirst().getMembershipType()).isEqualTo("premium");
        assertThat(persistedUsers.getFirst().getCreatedAt()).isNotNull();
    }

    @Test
    void registerUser_withInvalidPayloadShouldReturnStructuredValidationError() throws IOException, InterruptedException {
        HttpResponse<String> response = sendPost("""
                {
                  "name": "Ada Lovelace",
                  "email": "   ",
                  "membershipType": "premium"
                }
                """);

        JsonNode responseBody = objectMapper.readTree(response.body());

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(responseBody.get("status").asInt()).isEqualTo(400);
        assertThat(responseBody.get("message").stringValue()).isEqualTo("Error de validacion.");
        assertThat(responseBody.get("fieldErrors").get("email").stringValue()).isNotBlank();
        assertThat(userRepository.count()).isZero();
    }

    private HttpResponse<String> sendPost(String requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
