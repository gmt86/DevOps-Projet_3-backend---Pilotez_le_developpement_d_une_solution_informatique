package com.datashare.backend.mapper;

import com.datashare.backend.dto.auth.RegisterRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour UtilisateurMapper.
 * Vérifie la conversion entre RegisterRequestDTO et entité Utilisateur.
 */
@SpringBootTest
class UtilisateurMapperTest {

    @Autowired
    private UtilisateurMapper utilisateurMapper;

    /**
     * Test — conversion RegisterRequestDTO → Utilisateur.
     */
    @Test
    void toEntity_shouldMapEmailCorrectly() {
        // Given
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@datashare.com");
        dto.setPassword("password123");

        // When
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);

        // Then
        assertThat(utilisateur.getEmail()).isEqualTo("test@datashare.com");
    }

    /**
     * Test — id et dateCreation ignorés lors du mapping.
     */
    @Test
    void toEntity_shouldIgnoreIdAndDateCreation() {
        // Given
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("test@datashare.com");
        dto.setPassword("password123");

        // When
        Utilisateur utilisateur = utilisateurMapper.toEntity(dto);

        // Then
        assertThat(utilisateur.getId()).isNull();
        assertThat(utilisateur.getDateCreation()).isNull();
    }
}