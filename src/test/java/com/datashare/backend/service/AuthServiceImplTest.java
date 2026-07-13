package com.datashare.backend.service;

import com.datashare.backend.dto.auth.RegisterRequestDTO;
import com.datashare.backend.dto.auth.AuthResponseDTO;
import com.datashare.backend.dto.auth.LoginRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.mapper.UtilisateurMapper;
import com.datashare.backend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour AuthServiceImpl.
 * Vérifie la logique d'inscription et de connexion des utilisateurs.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtServiceImpl jwtServiceImpl;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("test@datashare.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@datashare.com");
        loginRequest.setPassword("password123");

        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .build();
    }

    /**
     * Test US03 — Inscription réussie d'un nouvel utilisateur.
     */
    @Test
    void register_shouldSucceed_whenEmailNotExists() {
        // Given
        when(utilisateurRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(utilisateurMapper.toEntity(registerRequest)).thenReturn(utilisateur);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // When
        authService.register(registerRequest);

        // Then
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    /**
     * Test US03 — Inscription échouée si email déjà utilisé.
     */
    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        when(utilisateurRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(AppException.class);

        verify(utilisateurRepository, never()).save(any());
    }

    /**
     * Test US04 — Connexion réussie avec credentials valides.
     */
    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(utilisateur);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtServiceImpl.generateToken(utilisateur)).thenReturn("jwt-token");

        // When
        AuthResponseDTO response = authService.login(loginRequest);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
    }

    /**
     * Test US04 — Connexion échouée avec mauvais credentials.
     */
    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AppException(com.datashare.backend.exception.ErrorCode.INVALID_CREDENTIALS));

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AppException.class);
    }
}