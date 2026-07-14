package com.datashare.backend.service;

import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.security.JwtConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour JwtServiceImpl.
 * Vérifie la génération et validation des tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private JwtConfigProperties jwtConfigProperties;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private Utilisateur utilisateur;

    private static final String SECRET = "test_secret_key_very_long_and_secure_for_testing_purposes";
    private static final long EXPIRATION = 86400000L; // 24 heures

    @BeforeEach
    void setUp() {
        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .build();

        when(jwtConfigProperties.secret()).thenReturn(SECRET);
        when(jwtConfigProperties.expiration()).thenReturn(EXPIRATION);
    }

    /**
     * Test — génération d'un token JWT valide.
     */
    @Test
    void generateToken_shouldReturnValidToken() {
        // When
        String token = jwtService.generateToken(utilisateur);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    /**
     * Test — extraction de l'email depuis le token.
     */
    @Test
    void extractUsername_shouldReturnEmail() {
        // Given
        String token = jwtService.generateToken(utilisateur);

        // When
        String email = jwtService.extractUsername(token);

        // Then
        assertThat(email).isEqualTo("test@datashare.com");
    }

    /**
     * Test — validation d'un token valide.
     */
    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // Given
        String token = jwtService.generateToken(utilisateur);

        // When
        boolean isValid = jwtService.validateToken(token, utilisateur);

        // Then
        assertThat(isValid).isTrue();
    }

    /**
     * Test — validation échouée si mauvais utilisateur.
     */
    @Test
    void validateToken_shouldReturnFalse_whenUserIsDifferent() {
        // Given
        String token = jwtService.generateToken(utilisateur);

        Utilisateur autreUtilisateur = Utilisateur.builder()
                .id(2L)
                .email("autre@datashare.com")
                .password("encodedPassword")
                .build();

        // When
        boolean isValid = jwtService.validateToken(token, autreUtilisateur);

        // Then
        assertThat(isValid).isFalse();
    }

    /**
     * Test — token expiré → isTokenExpired = true.
     */
    @Test
    void isTokenExpired_shouldReturnTrue_whenTokenIsExpired() {
        // Given — token expiré immédiatement
        when(jwtConfigProperties.expiration()).thenReturn(-1000L);
        String token = jwtService.generateToken(utilisateur);

         // When / Then
    assertThatThrownBy(() -> jwtService.isTokenExpired(token))
            .isInstanceOf(AppException.class);
    }
}