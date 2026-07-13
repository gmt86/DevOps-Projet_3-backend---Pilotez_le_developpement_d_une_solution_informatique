package com.datashare.backend.controller;

import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.exception.ErrorCode;
import com.datashare.backend.service.impl.FichierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur de gestion des fichiers.
 * Utilise @SpringBootTest pour tester les endpoints avec sécurité réelle.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FichierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FichierService fichierService;

    private FichierResponseDTO fichierResponse;

    @BeforeEach
    void setUp() {
        fichierResponse = FichierResponseDTO.builder()
                .id(UUID.randomUUID())
                .nom("test.pdf")
                .typeFichier("application/pdf")
                .taille(1024L)
                .dateExpiration(LocalDateTime.now().plusDays(7))
                .dateCreation(LocalDateTime.now())
                .tokenTelechargement(UUID.randomUUID())
                .estProtege(false)
                .estExpire(false)
                .build();
    }

        /**
     * Configure un utilisateur authentifié dans le contexte Spring Security.
     */
    private void authenticateUser() {
        Utilisateur utilisateur = Utilisateur.builder()
                .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(utilisateur, null, utilisateur.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Test GET /api/fichiers — liste des fichiers sans authentification.
     */
    @Test
    void getFichiers_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/fichiers"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test GET /api/fichiers — liste des fichiers avec authentification.
     */
    @Test
    @WithMockUser(username = "test@datashare.com")
    void getFichiers_shouldReturn200_whenAuthenticated() throws Exception {
        // Given
        authenticateUser();
        when(fichierService.getFichiersByUtilisateur(any())).thenReturn(List.of(fichierResponse));

        // When / Then
        mockMvc.perform(get("/api/fichiers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("test.pdf"));
    }

    /**
     * Test GET /api/fichiers/{token} — métadonnées fichier public.
     */
    @Test
    void getFichierByToken_shouldReturn200_whenTokenIsValid() throws Exception {
        // Given
        UUID token = fichierResponse.getTokenTelechargement();
        when(fichierService.getFichierByToken(token)).thenReturn(fichierResponse);

        // When / Then
        mockMvc.perform(get("/api/fichiers/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("test.pdf"));
    }

    /**
     * Test GET /api/fichiers/{token} — token invalide.
     */
    @Test
    void getFichierByToken_shouldReturn404_whenTokenIsInvalid() throws Exception {
        // Given
        UUID invalidToken = UUID.randomUUID();
        when(fichierService.getFichierByToken(invalidToken))
                .thenThrow(new AppException(ErrorCode.FILE_NOT_FOUND));

        // When / Then
        mockMvc.perform(get("/api/fichiers/{token}", invalidToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.FILE_NOT_FOUND.getMessage()));
    }

    /**
     * Test DELETE /api/fichiers/{id} — suppression sans authentification.
     */
    @Test
    void deleteFichier_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/fichiers/{id}", UUID.randomUUID())
            .header("Authorization", "")) // header vide — pas de token
            .andExpect(status().isUnauthorized());
    }

    /**
     * Test DELETE /api/fichiers/{id} — suppression avec authentification.
     */
    @Test
    @WithMockUser(username = "test@datashare.com")
    void deleteFichier_shouldReturn204_whenAuthenticated() throws Exception {
        // Given
        authenticateUser();
        UUID fichierId = UUID.randomUUID();
        doNothing().when(fichierService).deleteFichier(any(), any());

        // When / Then
        mockMvc.perform(delete("/api/fichiers/{id}", fichierId))
                .andExpect(status().isNoContent());
    }



}