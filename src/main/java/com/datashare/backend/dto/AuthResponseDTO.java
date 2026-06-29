package com.datashare.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO représentant la réponse retournée après une authentification réussie.
 * Contient le token JWT à utiliser pour les requêtes suivantes.
 */
@Data
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
}