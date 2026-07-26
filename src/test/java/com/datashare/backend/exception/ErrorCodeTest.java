package com.datashare.backend.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour ErrorCode.
 * Vérifie les codes HTTP et messages d'erreur.
 */
class ErrorCodeTest {

    @Test
    void emailAlreadyExists_shouldHave409StatusCode() {
        assertThat(ErrorCode.EMAIL_ALREADY_EXISTS.getStatusCode()).isEqualTo(409);
        assertThat(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()).isNotEmpty();
    }

    @Test
    void invalidCredentials_shouldHave401StatusCode() {
        assertThat(ErrorCode.INVALID_CREDENTIALS.getStatusCode()).isEqualTo(401);
        assertThat(ErrorCode.INVALID_CREDENTIALS.getMessage()).isNotEmpty();
    }

    @Test
    void fileNotFound_shouldHave404StatusCode() {
        assertThat(ErrorCode.FILE_NOT_FOUND.getStatusCode()).isEqualTo(404);
        assertThat(ErrorCode.FILE_NOT_FOUND.getMessage()).isNotEmpty();
    }

    @Test
    void fileExpired_shouldHave410StatusCode() {
        assertThat(ErrorCode.FILE_EXPIRED.getStatusCode()).isEqualTo(410);
        assertThat(ErrorCode.FILE_EXPIRED.getMessage()).isNotEmpty();
    }

    @Test
    void fileTooLarge_shouldHave400StatusCode() {
        assertThat(ErrorCode.FILE_TOO_LARGE.getStatusCode()).isEqualTo(400);
        assertThat(ErrorCode.FILE_TOO_LARGE.getMessage()).isNotEmpty();
    }
}
