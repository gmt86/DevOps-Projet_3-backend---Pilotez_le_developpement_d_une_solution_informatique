package com.datashare.backend.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour GlobalExceptionHandler.
 * Vérifie la gestion centralisée des erreurs HTTP.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    /**
     * Test — AppException retourne le bon code HTTP et message.
     */
    @Test
    void handleAppException_shouldReturnCorrectStatusAndMessage() {
        // Given
        AppException exception = new AppException(ErrorCode.FILE_NOT_FOUND);

        // When
        ResponseEntity<Map<String, String>> response = 
            globalExceptionHandler.handleAppException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message"))
            .isEqualTo(ErrorCode.FILE_NOT_FOUND.getMessage());
    }

    /**
     * Test — AppException 401 retourne Unauthorized.
     */
    @Test
    void handleAppException_shouldReturn401_forInvalidCredentials() {
        // Given
        AppException exception = new AppException(ErrorCode.INVALID_CREDENTIALS);

        // When
        ResponseEntity<Map<String, String>> response = 
            globalExceptionHandler.handleAppException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    /**
     * Test — AppException 409 retourne Conflict.
     */
    @Test
    void handleAppException_shouldReturn409_forEmailAlreadyExists() {
        // Given
        AppException exception = new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        // When
        ResponseEntity<Map<String, String>> response = 
            globalExceptionHandler.handleAppException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    /**
     * Test — AppException 410 retourne Gone pour fichier expiré.
     */
    @Test
    void handleAppException_shouldReturn410_forFileExpired() {
        // Given
        AppException exception = new AppException(ErrorCode.FILE_EXPIRED);

        // When
        ResponseEntity<Map<String, String>> response = 
            globalExceptionHandler.handleAppException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(410);
    }

    /**
     * Test — MissingServletRequestPartException retourne 400.
     */
    @Test
    void handleMissingPart_shouldReturn400() {
        // Given
        MissingServletRequestPartException exception = 
            new MissingServletRequestPartException("fichier");

        // When
        ResponseEntity<Map<String, String>> response = 
            globalExceptionHandler.handleMissingPart(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsKey("message");
    }

    /**
     * Test — Exception générique retourne 500.
     */
    @Test
    void handleGenericException_shouldReturn500() {
        // Given
        Exception exception = new RuntimeException("Erreur inattendue");

        // When
        ResponseEntity<Map<String, String>> response = 
            globalExceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsKey("message");
    }
}