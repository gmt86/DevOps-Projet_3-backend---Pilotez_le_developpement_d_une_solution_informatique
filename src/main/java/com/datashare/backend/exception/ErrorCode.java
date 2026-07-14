package com.datashare.backend.exception;

import lombok.Getter;

@Getter
//classe d'énumération qui centralise tous les codes et messages d'erreur.
public enum ErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS(409, "Email déjà utilisé"),
    INVALID_CREDENTIALS(401, "Email ou mot de passe incorrect"),
    TOKEN_EXPIRED(401, "Le token a expiré."),

    // Ressources    
    RESOURCE_NOT_FOUND(404, "Ressource non trouvée"),
    FILE_NOT_FOUND(404, "Fichier non trouvé"),
    INVALID_PASSWORD(401, "mot de passe incorrect"),
    FILE_EXPIRED(410, "Ce lien a expiré."),

    // Fichiers
    MISSING_FILE(400, "Le fichier est obligatoire."),
    FILE_TOO_LARGE(400, "La taille du fichier dépasse la limite autorisée"),
    INVALID_FILE_TYPE(400, "Type de fichier non autorisé"),

    
    // Général
    UNAUTHORIZED(403, "Non autorisé"),
    BAD_REQUEST(400, "Requête invalide"),


    // Stockage
    FILE_STORAGE_ERROR(500, "Erreur lors du stockage du fichier."),
    FILE_DELETE_ERROR(500, "Erreur lors de la suppression du fichier."),
    STORAGE_INIT_ERROR(500, "Erreur lors de l'initialisation du dossier de stockage."),

    // Erreur base de données
    DATABASE_ERROR(500, "Une erreur est survenue lors de l'enregistrement.");


    private final int statusCode;
    private final String message;

    ErrorCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}