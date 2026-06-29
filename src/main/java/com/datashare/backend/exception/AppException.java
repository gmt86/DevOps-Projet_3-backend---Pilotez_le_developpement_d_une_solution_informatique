package com.datashare.backend.exception;

import lombok.Getter;
/**
 * Exception globale de l'application.
 * Utilise {@link ErrorCode} pour centraliser les codes HTTP et messages d'erreur.
 * Évite la duplication de try/catch dans les contrôleurs et services.
 */
@Getter
public class AppException extends RuntimeException {

    private final int statusCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.statusCode = errorCode.getStatusCode();
    }
}