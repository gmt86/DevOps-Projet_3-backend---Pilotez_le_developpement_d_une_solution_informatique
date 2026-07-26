package com.datashare.backend.service;

import com.datashare.backend.configuration.StorageConfigProperties;
import com.datashare.backend.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour StorageServiceImpl.
 * Vérifie la sauvegarde, suppression et récupération des fichiers physiques.
 */
@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private StorageConfigProperties storageConfigProperties;

    @InjectMocks
    private StorageServiceImpl storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        when(storageConfigProperties.path()).thenReturn(tempDir.toString());
    }

    /**
     * Test — initialisation du dossier de stockage.
     */
    @Test
    void init_shouldCreateStorageDirectory() {
        // When
        storageService.init();

        // Then
        assertThat(Files.exists(tempDir)).isTrue();
    }

    /**
     * Test — sauvegarde réussie d'un fichier.
     */
    @Test
    void saveFile_shouldSaveFileSuccessfully() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "fichier", "test.pdf", "application/pdf", "contenu test".getBytes()
        );

        // When
        String chemin = storageService.saveFile(file, 1L, "uuid-test.pdf");

        // Then
        assertThat(chemin).isEqualTo("1/uuid-test.pdf");
        assertThat(Files.exists(tempDir.resolve("1/uuid-test.pdf"))).isTrue();
    }

    /**
     * Test — suppression réussie d'un fichier existant.
     */
    @Test
    void deleteFile_shouldDeleteExistingFile() throws IOException {
        // Given
        Path userDir = tempDir.resolve("1");
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve("test.pdf");
        Files.write(filePath, "contenu".getBytes());

        // When
        storageService.deleteFile("1/test.pdf");

        // Then
        assertThat(Files.exists(filePath)).isFalse();
    }

    /**
     * Test — suppression d'un fichier inexistant ne lève pas d'exception.
     */
    @Test
    void deleteFile_shouldNotThrowException_whenFileNotFound() {
        // When / Then — aucune exception levée
        storageService.deleteFile("1/fichier-inexistant.pdf");
    }

    /**
     * Test — getFilePath retourne le chemin absolu correct.
     */
    @Test
    void getFilePath_shouldReturnAbsolutePath() {
        // When
        Path path = storageService.getFilePath("1/test.pdf");

        // Then
        assertThat(path.isAbsolute()).isTrue();
        assertThat(path.toString()).contains("1/test.pdf");
    }

    /**
     * Test — saveFile échoue avec un fichier invalide.
     */
    @Test
    void saveFile_shouldThrowException_whenIOError() {
        // Given — dossier de stockage invalide
        when(storageConfigProperties.path()).thenReturn("/chemin/invalide/inexistant");

        MockMultipartFile file = new MockMultipartFile(
            "fichier", "test.pdf", "application/pdf", "contenu".getBytes()
        );

        // When / Then
        assertThatThrownBy(() -> storageService.saveFile(file, 1L, "test.pdf"))
            .isInstanceOf(AppException.class);
    }
}