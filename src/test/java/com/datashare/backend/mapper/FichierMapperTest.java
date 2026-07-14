package com.datashare.backend.mapper;

import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.entity.Fichier;
import com.datashare.backend.entity.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour FichierMapper.
 * Vérifie la conversion entre entité Fichier et FichierResponseDTO.
 */
@SpringBootTest
class FichierMapperTest {

    @Autowired
    private FichierMapper fichierMapper;

    /**
     * Test — conversion fichier sans mot de passe → estProtege = false.
     */
    @Test
    void toDTO_shouldSetEstProtegeFalse_whenPasswordIsNull() {
        // Given
        Fichier fichier = buildFichier(null, LocalDateTime.now().plusDays(7));

        // When
        FichierResponseDTO dto = fichierMapper.toDTO(fichier);

        // Then
        assertThat(dto.isEstProtege()).isFalse();
        assertThat(dto.getNom()).isEqualTo("test.pdf");
        assertThat(dto.getTaille()).isEqualTo(1024L);
    }

    /**
     * Test — conversion fichier avec mot de passe → estProtege = true.
     */
    @Test
    void toDTO_shouldSetEstProtegeTrue_whenPasswordIsPresent() {
        // Given
        Fichier fichier = buildFichier("motdepasse", LocalDateTime.now().plusDays(7));

        // When
        FichierResponseDTO dto = fichierMapper.toDTO(fichier);

        // Then
        assertThat(dto.isEstProtege()).isTrue();
    }

    /**
     * Test — fichier expiré → estExpire = true.
     */
    @Test
    void toDTO_shouldSetEstExpireTrue_whenDateExpirationIsPast() {
        // Given
        Fichier fichier = buildFichier(null, LocalDateTime.now().minusDays(1));

        // When
        FichierResponseDTO dto = fichierMapper.toDTO(fichier);

        // Then
        assertThat(dto.isEstExpire()).isTrue();
    }

    /**
     * Test — fichier non expiré → estExpire = false.
     */
    @Test
    void toDTO_shouldSetEstExpireFalse_whenDateExpirationIsFuture() {
        // Given
        Fichier fichier = buildFichier(null, LocalDateTime.now().plusDays(7));

        // When
        FichierResponseDTO dto = fichierMapper.toDTO(fichier);

        // Then
        assertThat(dto.isEstExpire()).isFalse();
    }

    /**
     * Construit un fichier de test.
     */
    private Fichier buildFichier(String password, LocalDateTime dateExpiration) {
        Utilisateur utilisateur = Utilisateur.builder()
                .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .build();

        return Fichier.builder()
                .id(UUID.randomUUID())
                .utilisateur(utilisateur)
                .nom("test.pdf")
                .typeFichier("application/pdf")
                .taille(1024L)
                .dateExpiration(dateExpiration)
                .password(password)
                .tokenTelechargement(UUID.randomUUID())
                .cheminStockage("1/test.pdf")
                .build();
    }
}