package com.datashare.backend.dto.file;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
    
/**
 * DTO représentant les données reçues lors de l'upload d'un fichier.
 * Le fichier physique est reçu séparément via MultipartFile.
 * multipart/form-data
    ├── fichier        → binaire (le vrai fichier)
    └── dateExpiration → texte JSON
    └── password       → texte JSON
 */
@Data
public class FichierUploadRequestDTO {

    /**
     * Date d'expiration du fichier — doit être dans le futur.
     * Maximum 7 jours selon les specs.
     */
    @NotNull(message = "La date d'expiration est requise.")
    @Future(message = "La date d'expiration doit être dans le futur.")
    private LocalDateTime dateExpiration;

    /**
     * Mot de passe optionnel pour protéger le téléchargement.
     * Minimum 6 caractères si renseigné.
     */
    private String password;
}