package com.datashare.backend.service;

import com.datashare.backend.configuration.StorageConfigProperties;
import com.datashare.backend.service.impl.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Implémentation du service de stockage local des fichiers.
 * Gère la sauvegarde et la suppression physique des fichiers sur le disque.
 * Organisation : {storagePath}/{userId}/{uuid}-{nomOriginal}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageConfigProperties storageConfigProperties;

    /**
     * Initialise le dossier de stockage au démarrage de l'application.
     * Crée le dossier s'il n'existe pas.
     */
    @PostConstruct //Crée le dossier uploads au démarrage si inexistant
    public void init() throws IOException {
        Path storagePath = Paths.get(storageConfigProperties.path());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.info("Storage directory created: {}", storagePath.toAbsolutePath());
        }
    }

    /**
     * Sauvegarde un fichier sur le disque dans le dossier de l'utilisateur.
     * @return le chemin relatif du fichier sauvegardé
     */
    @Override
    public String saveFile(MultipartFile file, Long userId, String fileName) throws IOException {
        // Crée le dossier utilisateur s'il n'existe pas
        Path userDirectory = Paths.get(storageConfigProperties.path(), String.valueOf(userId));
        Files.createDirectories(userDirectory);

        // Sauvegarde le fichier
        Path filePath = userDirectory.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);//Remplace le fichier s'il existe déjà

        log.info("File saved: {}", filePath.toAbsolutePath());

        // Retourne le chemin relatif
        return Paths.get(String.valueOf(userId), fileName).toString();
    }

    /**
     * Supprime un fichier du disque.
     */
    @Override
    public void deleteFile(String cheminStockage) throws IOException {
        Path filePath = Paths.get(storageConfigProperties.path(), cheminStockage);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted: {}", filePath.toAbsolutePath());
        } else {
            log.warn("File not found for deletion: {}", filePath.toAbsolutePath());
        }
    }

    /**
     * Retourne le chemin absolu d'un fichier à partir de son chemin relatif.
     */
    @Override
    public Path getFilePath(String cheminStockage) {
        return Paths.get(storageConfigProperties.path(), cheminStockage).toAbsolutePath();
    }
}