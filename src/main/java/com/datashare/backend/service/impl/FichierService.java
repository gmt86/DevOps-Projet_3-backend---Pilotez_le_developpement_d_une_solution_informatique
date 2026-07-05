package com.datashare.backend.service.impl;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.dto.file.FichierUploadRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface définissant les opérations métier sur les fichiers.
 * Gère l'upload, le téléchargement, l'historique et la suppression.
 */
public interface FichierService {

    /**
     * Upload un fichier et sauvegarde ses métadonnées en base.
     */
    FichierResponseDTO uploadFichier(MultipartFile file, FichierUploadRequestDTO request, Long userId);

    /**
     * Retourne les métadonnées d'un fichier via son token.
     */
    FichierResponseDTO getFichierByToken(UUID token);

    /**
     * Télécharge un fichier via son token.
     */
    Resource downloadFichier(UUID token, String password);

    /**
     * Retourne l'historique des fichiers d'un utilisateur.
     */
    List<FichierResponseDTO> getFichiersByUtilisateur(Long userId);

    /**
     * Supprime un fichier.
     */
    void deleteFichier(UUID id, Long userId);
}