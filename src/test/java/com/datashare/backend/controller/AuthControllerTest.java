package com.datashare.backend.controller;
import com.datashare.backend.dto.auth.AuthResponseDTO;
import com.datashare.backend.dto.auth.LoginRequestDTO;
import com.datashare.backend.dto.auth.RegisterRequestDTO;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.exception.ErrorCode;
import com.datashare.backend.security.CustomUserDetailsService;
import com.datashare.backend.security.JwtConfigProperties;
import com.datashare.backend.service.JwtServiceImpl;
import com.datashare.backend.service.impl.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

     @MockitoBean
    private JwtServiceImpl jwtServiceImpl;  

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService; 

    @MockitoBean
    private JwtConfigProperties jwtConfigProperties;

    /**
     * Test POST /api/auth/register — inscription réussie.
     */
    @Test
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@datashare.com");
        request.setPassword("password123");

        doNothing().when(authService).register(any(RegisterRequestDTO.class));

        // When / Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Compte créé avec succès"));
    }

    /**
     * Test POST /api/auth/register — email déjà utilisé.
     */
    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        // Given
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("test@datashare.com");
        request.setPassword("password123");

        doThrow(new AppException(ErrorCode.EMAIL_ALREADY_EXISTS))
                .when(authService).register(any(RegisterRequestDTO.class));

        // When / Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()));
    }

    /**
     * Test POST /api/auth/login — connexion réussie.
     */
    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@datashare.com");
        request.setPassword("password123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(new AuthResponseDTO("jwt-token"));

        // When / Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));
    }

    /**
     * Test POST /api/auth/login — mauvais credentials.
     */
    @Test
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        // Given
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("test@datashare.com");
        request.setPassword("wrongpassword");

        doThrow(new AppException(ErrorCode.INVALID_CREDENTIALS))
                .when(authService).login(any(LoginRequestDTO.class));

        // When / Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CREDENTIALS.getMessage()));
    }
}