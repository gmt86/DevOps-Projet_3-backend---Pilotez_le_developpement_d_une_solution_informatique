package com.datashare.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour l'entité Utilisateur.
 * Vérifie les comportements de UserDetails et le builder.
 */
class UtilisateurTest {

    private Utilisateur utilisateur;
    private LocalDateTime  now ;

    @BeforeEach
    void setUp() {
       
        now = LocalDateTime.now();
        utilisateur = Utilisateur.builder()
               .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .dateCreation(now)
                .build();
    }

    /**
     * Test — getUsername() retourne l'email.
     */
    @Test
    void getUsername_shouldReturnEmail() {
       
        // Then
        assertThat(utilisateur.getUsername()).isEqualTo("test@datashare.com");
    }

    /**
     * Test — compte toujours actif et non expiré.
     */
    @Test
    void userDetails_shouldAlwaysBeActive() {
       
        // Then
        assertThat(utilisateur.isAccountNonExpired()).isTrue();
        assertThat(utilisateur.isAccountNonLocked()).isTrue();
        assertThat(utilisateur.isCredentialsNonExpired()).isTrue();
        assertThat(utilisateur.isEnabled()).isTrue();
    }

    /**
     * Test — authorities toujours vide (pas de rôles dans le MVP).
     */
    @Test
    void getAuthorities_shouldReturnEmptyList() {
        
       // Then
        assertThat(utilisateur.getAuthorities()).isEmpty();
    }

    /**
     * Test — builder crée correctement l'entité.
     */
    @Test
    void builder_shouldCreateUtilisateurCorrectly() {
      
        // Then
        assertThat(utilisateur.getId()).isEqualTo(1L);
        assertThat(utilisateur.getEmail()).isEqualTo("test@datashare.com");
        assertThat(utilisateur.getPassword()).isEqualTo("encodedPassword");
        assertThat(utilisateur.getDateCreation()).isEqualTo(now);
    }
}