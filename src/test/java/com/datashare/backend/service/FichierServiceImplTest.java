package com.datashare.backend.service;

import com.datashare.backend.configuration.StorageConfigProperties;
import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.dto.file.FichierUploadRequestDTO;
import com.datashare.backend.entity.Fichier;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.mapper.FichierMapper;
import com.datashare.backend.repository.FichierRepository;
import com.datashare.backend.repository.UtilisateurRepository;
import com.datashare.backend.service.impl.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour FichierServiceImpl.
 * Vérifie la logique d'upload, téléchargement, historique et suppression.
 */
@ExtendWith(MockitoExtension.class)
class FichierServiceImplTest {

    @Mock
    private FichierRepository fichierRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private FichierMapper fichierMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StorageConfigProperties storageConfigProperties;

    @InjectMocks
    private FichierServiceImpl fichierService;

    private Utilisateur utilisateur;
    private Fichier fichier;
    private FichierUploadRequestDTO uploadRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        utilisateur = Utilisateur.builder()
                .id(1L)
                .email("test@datashare.com")
                .password("encodedPassword")
                .build();

        fichier = Fichier.builder()
                .id(UUID.randomUUID())
                .utilisateur(utilisateur)
                .nom("test.pdf")
                .typeFichier("application/pdf")
                .taille(1024L)
                .dateExpiration(LocalDateTime.now().plusDays(7))
                .tokenTelechargement(UUID.randomUUID())
                .cheminStockage("1/test.pdf")
                .build();

        uploadRequest = new FichierUploadRequestDTO();
        uploadRequest.setDateExpiration(LocalDateTime.now().plusDays(7));

        mockFile = new MockMultipartFile(
                "fichier",
                "test.pdf",
                "application/pdf",
                "contenu test".getBytes()
        );
    }

    /**
     * Test US01 — Upload réussi d'un fichier.
     */
    @Test
    void uploadFichier_shouldSucceed_whenFileIsValid() {
        // Given
        when(storageConfigProperties.maxFileSizeBytes()).thenReturn(1073741824L);
        when(storageConfigProperties.forbiddenTypes()).thenReturn("application/x-msdownload");
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(storageService.saveFile(any(), any(), any())).thenReturn("1/test.pdf");
        when(fichierRepository.save(any(Fichier.class))).thenReturn(fichier);
        when(fichierMapper.toDTO(any(Fichier.class))).thenReturn(new FichierResponseDTO());

        // When
        FichierResponseDTO response = fichierService.uploadFichier(mockFile, uploadRequest, 1L);

        // Then
        assertThat(response).isNotNull();
        verify(fichierRepository, times(1)).save(any(Fichier.class));
        verify(storageService, times(1)).saveFile(any(), any(), any());
    }

    /**
     * Test US01 — Upload échoué si fichier trop grand.
     */
    @Test
    void uploadFichier_shouldThrowException_whenFileTooLarge() {
        // Given
        when(storageConfigProperties.maxFileSizeBytes()).thenReturn(1L); // 1 octet max

        MockMultipartFile largeFile = new MockMultipartFile(
                "fichier", "large.pdf", "application/pdf", new byte[1024]
        );

        // When / Then
        assertThatThrownBy(() -> fichierService.uploadFichier(largeFile, uploadRequest, 1L))
                .isInstanceOf(AppException.class);

        verify(fichierRepository, never()).save(any());
    }

    /**
     * Test US02 — Récupération des métadonnées via token valide.
     */
    @Test
    void getFichierByToken_shouldReturnFichier_whenTokenIsValid() {
        // Given
        UUID token = fichier.getTokenTelechargement();
        when(fichierRepository.findByTokenTelechargement(token)).thenReturn(Optional.of(fichier));
        when(fichierMapper.toDTO(fichier)).thenReturn(new FichierResponseDTO());

        // When
        FichierResponseDTO response = fichierService.getFichierByToken(token);

        // Then
        assertThat(response).isNotNull();
    }

    /**
     * Test US02 — Exception si token invalide.
     */
    @Test
    void getFichierByToken_shouldThrowException_whenTokenIsInvalid() {
        // Given
        UUID invalidToken = UUID.randomUUID();
        when(fichierRepository.findByTokenTelechargement(invalidToken)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> fichierService.getFichierByToken(invalidToken))
                .isInstanceOf(AppException.class);
    }

    /**
     * Test US05 — Historique des fichiers d'un utilisateur.
     */
    @Test
    void getFichiersByUtilisateur_shouldReturnList_whenUserExists() {
        // Given
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(fichierRepository.findByUtilisateur(utilisateur)).thenReturn(List.of(fichier));
        when(fichierMapper.toDTO(fichier)).thenReturn(new FichierResponseDTO());

        // When
        List<FichierResponseDTO> result = fichierService.getFichiersByUtilisateur(1L);

        // Then
        assertThat(result).hasSize(1);
    }

    /**
     * Test US06 — Suppression réussie d'un fichier.
     */
    @Test
    void deleteFichier_shouldSucceed_whenUserIsOwner() {
        // Given
        UUID fichierId = fichier.getId();
        when(fichierRepository.findById(fichierId)).thenReturn(Optional.of(fichier));

        // When
        fichierService.deleteFichier(fichierId, 1L);

        // Then
        verify(storageService, times(1)).deleteFile(fichier.getCheminStockage());
        verify(fichierRepository, times(1)).delete(fichier);
    }

    /**
     * Test US06 — Exception si utilisateur non propriétaire.
     */
    @Test
    void deleteFichier_shouldThrowException_whenUserIsNotOwner() {
        // Given
        UUID fichierId = fichier.getId();
        when(fichierRepository.findById(fichierId)).thenReturn(Optional.of(fichier));

        // When / Then
        assertThatThrownBy(() -> fichierService.deleteFichier(fichierId, 99L))
                .isInstanceOf(AppException.class);

        verify(storageService, never()).deleteFile(any());
    }
}