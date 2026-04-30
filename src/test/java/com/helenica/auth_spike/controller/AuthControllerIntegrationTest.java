package com.helenica.auth_spike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerReturns201Created() throws Exception {
        Map<String, String> registerRequest = Map.of(
                "name", "Integration User",
                "email", "integration.user@helenica.org",
                "password", "securepass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("integration.user@helenica.org"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginReturnsJwtToken() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "email", "user@helenica.org",
                "password", "user1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("user@helenica.org"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token", matchesPattern("^[^.]+\\.[^.]+\\.[^.]+$")));
    }

    @Test
    void getUsersWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsersWithValidTokenReturns200() throws Exception {
        String token = loginAndExtractToken("user@helenica.org", "user1234");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem("admin@helenica.org")))
                .andExpect(jsonPath("$[*].email", hasItem("user@helenica.org")));
    }

    @Test
    void getAdminUsersWithUserTokenReturns403() throws Exception {
        String token = loginAndExtractToken("user@helenica.org", "user1234");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdminUsersWithAdminTokenReturns200() throws Exception {
        String token = loginAndExtractToken("admin@helenica.org", "admin123");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem("admin@helenica.org")))
                .andExpect(jsonPath("$[*].email", hasItem("user@helenica.org")));
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        Map<String, String> loginRequest = Map.of(
                "email", email,
                "password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}
