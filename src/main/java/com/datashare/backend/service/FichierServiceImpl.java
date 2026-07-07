package com.datashare.backend.service;

import com.datashare.backend.configuration.StorageConfigProperties;
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
    private final StorageConfigProperties storageConfigProperties;

     

    /**
     * Upload un fichier — vérifie les contraintes, sauvegarde physiquement
     * et enregistre les métadonnées en base.
     */
   
    @Override
    public FichierResponseDTO uploadFichier(MultipartFile file, FichierUploadRequestDTO requestDTO, Long userId) {       

        try {

            String nomFicchier = file.getOriginalFilename();
            String typeFichier = file.getContentType();
            UUID tokenTelechargement = UUID.randomUUID();// Génération du token unique 

            log.debug("Uploading file: {} for user: {}", nomFicchier, userId);


            // Vérification taille
            if (file.getSize() > storageConfigProperties.maxFileSizeBytes()) {
                throw new AppException(ErrorCode.FILE_TOO_LARGE);
            }

            // chargement liste de type de fichier interdite
            List<String> forbiddenTypes = List.of(storageConfigProperties.forbiddenTypes().split(",") );

            // Vérification type de fichier
            if (forbiddenTypes.contains(typeFichier)) {
                throw new AppException(ErrorCode.INVALID_FILE_TYPE);
            }

            // Récupération de l'utilisateur
            Utilisateur utilisateur = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

            // creation du nom de fichier unique pour stockage local
            String storageFileName = tokenTelechargement + "-" + nomFicchier;

            // Sauvegarde physique du fichier
            String cheminStockage = storageService.saveFile(file, userId, storageFileName);

            // Encodage du mot de passe si présent
            String encodedPassword = requestDTO.getPassword() != null ? passwordEncoder.encode(requestDTO.getPassword()) : null;

            // Construction de l'entité Fichier
            Fichier fichier = Fichier.builder()
                    .utilisateur(utilisateur)
                    .nom(nomFicchier)
                    .typeFichier(typeFichier)
                    .taille(file.getSize())
                    .dateExpiration(requestDTO.getDateExpiration())
                    .password(encodedPassword)
                    .tokenTelechargement(tokenTelechargement)
                    .cheminStockage(cheminStockage)
                    .build();

            // Sauvegarde des métadonnées en base
            fichierRepository.save(fichier);
            log.info("File uploaded successfully: {} for user: {}", fichier.getNom(), userId);

            return fichierMapper.toDTO(fichier);

        } catch (AppException e) {
            throw e; // On laisse remonter les AppException
        } catch (Exception e) {
            log.error("Unexpected error uploading file: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_STORAGE_ERROR);
        }
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

            if (fichierMapper.isExpire(fichier)) {
                throw new AppException(ErrorCode.FILE_EXPIRED);
            }

            if (fichier.getPassword() != null) {
                if (password == null || !passwordEncoder.matches(password, fichier.getPassword())) {
                    throw new AppException(ErrorCode.INVALID_PASSWORD);
                }
            }

            try {
                Path filePath = storageService.getFilePath(fichier.getCheminStockage());
                Resource resource = new UrlResource(filePath.toUri());
                if (!resource.exists()) {
                    throw new AppException(ErrorCode.FILE_NOT_FOUND);
                }
                return resource;
            } catch (AppException e) {
                throw e;
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

            if (!fichier.getUtilisateur().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            storageService.deleteFile(fichier.getCheminStockage());
            fichierRepository.delete(fichier);
            log.info("File deleted: {} by user: {}", fichier.getNom(), userId);
        }
}