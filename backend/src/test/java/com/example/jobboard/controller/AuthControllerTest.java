package com.example.jobboard.controller;

import com.example.jobboard.dto.request.LoginRequestDto;
import com.example.jobboard.dto.request.RegisterRequestDto;
import com.example.jobboard.dto.response.AuthResponseDto;
import com.example.jobboard.security.CustomUserDetailsService;
import com.example.jobboard.security.JwtUtil;
import com.example.jobboard.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AuthController} using @WebMvcTest.
 * Tests registration and login flows including validation failures.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/auth/register — should register successfully and return 201")
    void register_ValidRequest_ReturnsCreated() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role("CANDIDATE")
                .build();

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token("jwt-token-here")
                .role("CANDIDATE")
                .email("john@example.com")
                .name("John Doe")
                .build();

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"))
                .andExpect(jsonPath("$.data.role").value("CANDIDATE"));
    }

    @Test
    @DisplayName("POST /api/auth/register — should return 400 for missing email")
    void register_MissingEmail_ReturnsBadRequest() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .name("John Doe")
                .password("password123")
                .role("CANDIDATE")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register — should return 400 for invalid email format")
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .name("John Doe")
                .email("not-an-email")
                .password("password123")
                .role("CANDIDATE")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login — should login successfully and return 200")
    void login_ValidCredentials_ReturnsOk() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        AuthResponseDto authResponse = AuthResponseDto.builder()
                .token("jwt-token-here")
                .role("CANDIDATE")
                .email("john@example.com")
                .name("John Doe")
                .build();

        when(authService.login(any(LoginRequestDto.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token-here"));
    }

    @Test
    @DisplayName("POST /api/auth/login — should return 400 for missing password")
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        LoginRequestDto request = LoginRequestDto.builder()
                .email("john@example.com")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
