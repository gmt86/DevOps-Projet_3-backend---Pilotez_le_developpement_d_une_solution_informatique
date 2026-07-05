package com.datashare.backend.service;

import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.dto.file.FichierUploadRequestDTO;
import com.datashare.backend.entity.Fichier;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.exception.ErrorCode;
import com.datashare.backend.mapper.FichierMapper;
import com.datashare.backend.repository.FichierRepository;
import com.datashare.backend.repository.UtilisateurRepository;
import com.datashare.backend.service.impl.FichierService;
import com.datashare.backend.service.impl.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service métier pour la gestion des fichiers.
 * Orchestre le stockage physique (StorageService) et les métadonnées (FichierRepository).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FichierServiceImpl implements FichierService {

    private final FichierRepository fichierRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StorageService storageService;
    private final FichierMapper fichierMapper;
    private final PasswordEncoder passwordEncoder;

    // Types de fichiers interdits selon les specs
    private static final List<String> FORBIDDEN_TYPES = List.of(
            "application/x-msdownload",  // .exe
            "application/x-bat",          // .bat
            "application/x-sh"            // .sh
    );

    // Taille maximale : 1 Go en octets
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L;

    /**
     * Upload un fichier — vérifie les contraintes, sauvegarde physiquement
     * et enregistre les métadonnées en base.
     */
    @Override
    public FichierResponseDTO uploadFichier(MultipartFile file, FichierUploadRequestDTO request, Long userId) {
        log.debug("Uploading file: {} for user: {}", file.getOriginalFilename(), userId);

        // Vérification taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        // Vérification type de fichier
        if (FORBIDDEN_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Récupération de l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Génération du nom de fichier unique
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // Sauvegarde physique du fichier
        String cheminStockage = storageService.saveFile(file, userId, fileName);

        // Encodage du mot de passe si présent
        String encodedPassword = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : null;

        // Construction de l'entité Fichier
        Fichier fichier = Fichier.builder()
                .utilisateur(utilisateur)
                .nom(file.getOriginalFilename())
                .typeFichier(file.getContentType())
                .taille(file.getSize())
                .dateExpiration(request.getDateExpiration())
                .password(encodedPassword)
                .tokenTelechargement(UUID.randomUUID())
                .cheminStockage(cheminStockage)
                .build();

        // Sauvegarde des métadonnées en base
        fichierRepository.save(fichier);
        log.info("File uploaded successfully: {} for user: {}", fichier.getNom(), userId);

        return fichierMapper.toDTO(fichier);
    }

    /**
     * Retourne les métadonnées d'un fichier via son token de téléchargement.
     */
    @Override
    public FichierResponseDTO getFichierByToken(UUID token) {
        Fichier fichier = fichierRepository.findByTokenTelechargement(token)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        return fichierMapper.toDTO(fichier);
    }

    /**
     * Télécharge le fichier physique via son token.
     * Vérifie l'expiration et le mot de passe si nécessaire.
     */
    @Override
    public Resource downloadFichier(UUID token, String password) {
        Fichier fichier = fichierRepository.findByTokenTelechargement(token)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        // Vérification expiration
        if (fichierMapper.isExpire(fichier)) {
            throw new AppException(ErrorCode.FILE_EXPIRED);
        }

        // Vérification mot de passe si fichier protégé
        if (fichier.getPassword() != null) {
            if (password == null || !passwordEncoder.matches(password, fichier.getPassword())) {
                throw new AppException(ErrorCode.INVALID_PASSWORD);
            }
        }

        // Récupération du fichier physique
        try {
            Path filePath = storageService.getFilePath(fichier.getCheminStockage());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new AppException(ErrorCode.FILE_NOT_FOUND);
            }
            return resource;
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
    }

    /**
     * Retourne l'historique des fichiers d'un utilisateur.
     */
    @Override
    public List<FichierResponseDTO> getFichiersByUtilisateur(Long userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return fichierRepository.findByUtilisateur(utilisateur)
                .stream()
                .map(fichierMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Supprime un fichier — vérifie que l'utilisateur en est le propriétaire.
     */
    @Override
    public void deleteFichier(UUID id, Long userId) {
        Fichier fichier = fichierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        // Vérification propriétaire
        if (!fichier.getUtilisateur().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Suppression physique
        storageService.deleteFile(fichier.getCheminStockage());

        // Suppression métadonnées
        fichierRepository.delete(fichier);
        log.info("File deleted: {} by user: {}", fichier.getNom(), userId);
    }
}