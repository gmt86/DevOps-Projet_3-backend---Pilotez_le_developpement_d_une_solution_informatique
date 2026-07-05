package com.datashare.backend.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO représentant les métadonnées d'un fichier retournées au frontend.
 * Ne contient jamais le chemin de stockage physique ni le mot de passe.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichierResponseDTO {

    private UUID id;
    private String nom;
    private String typeFichier;
    private Long taille;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateCreation;
    private UUID tokenTelechargement;

    /** Indique si le fichier est protégé par mot de passe */
    private boolean estProtege;

    /** Indique si le fichier est expiré */
    private boolean estExpire;
}