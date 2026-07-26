package com.datashare.backend.security;

import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.service.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour JwtAuthenticationFilter.
 * Vérifie le filtrage des requêtes HTTP selon le token JWT.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .build();
    }

    /**
     * Test — endpoint public bypass le filtre JWT.
     */
    @Test
    void doFilterInternal_shouldBypassFilter_forPublicEndpoint() throws Exception {
        // Given
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    /**
     * Test — requête sans token → anonyme.
     */
    @Test
    void doFilterInternal_shouldContinue_whenNoToken() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/fichiers");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }

    /**
     * Test — token valide → utilisateur authentifié.
     */
    @Test
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/fichiers");
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUsername("valid-token")).thenReturn("test@datashare.com");
        when(userDetailsService.loadUserByUsername("test@datashare.com")).thenReturn(utilisateur);
        when(jwtService.validateToken("valid-token", utilisateur)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).validateToken("valid-token", utilisateur);
    }

    /**
     * Test — token invalide → rejet.
     */
    @Test
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsInvalid() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/fichiers");
        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.extractUsername("invalid-token")).thenReturn("test@datashare.com");
        when(userDetailsService.loadUserByUsername("test@datashare.com")).thenReturn(utilisateur);
        when(jwtService.validateToken("invalid-token", utilisateur)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    /**
     * Test — GET endpoint public fichiers bypass authentification.
     */
    @Test
    void doFilterInternal_shouldBypassFilter_forPublicFichiersEndpoint() throws Exception {
        // Given
        request.setMethod("GET");
        request.setRequestURI("/api/fichiers/some-token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
    }
}