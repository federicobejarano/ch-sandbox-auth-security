package com.example.ch_users_e2e_sandbox.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ch_users_e2e_sandbox.dto.UserRegistrationRequest;
import com.example.ch_users_e2e_sandbox.dto.UserRegistrationResponse;
import com.example.ch_users_e2e_sandbox.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void registerUser_shouldReturnCreatedWithResponseBody() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 27, 10, 30);
        UserRegistrationResponse response = new UserRegistrationResponse(
                1L,
                "ADA LOVELACE",
                "ada@example.com",
                "premium",
                createdAt);

        when(userService.registerUser(new UserRegistrationRequest(
                "Ada Lovelace",
                "ada@example.com",
                "premium"))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "name": "Ada Lovelace",
                          "email": "ada@example.com",
                          "membershipType": "premium"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADA LOVELACE"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.membershipType").value("premium"))
                .andExpect(jsonPath("$.createdAt").value("2026-03-27T10:30:00"));
    }

    @Test
    void registerUser_withInvalidEmailShouldReturnStructuredValidationError() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "name": "Ada Lovelace",
                          "email": "   ",
                          "membershipType": "premium"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Error de validacion."))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void getAllUsers_shouldReturnOkWithUserList() throws Exception {
        LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 3, 27, 9, 0);
        LocalDateTime secondCreatedAt = LocalDateTime.of(2026, 3, 27, 9, 30);

        when(userService.getAllUsers()).thenReturn(List.of(
                new UserRegistrationResponse(1L, "ADA LOVELACE", "ada@example.com", "premium", firstCreatedAt),
                new UserRegistrationResponse(2L, "GRACE HOPPER", "grace@example.com", "standard", secondCreatedAt)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ADA LOVELACE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("GRACE HOPPER"));
    }
}
