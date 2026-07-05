package com.datashare.backend.service.impl;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface définissant les opérations de stockage physique des fichiers.
 * Sépare la logique de stockage de la logique métier.
 */
public interface StorageService {

    /**
     * Sauvegarde un fichier sur le disque.
     * @param file le fichier à sauvegarder
     * @param userId l'identifiant de l'utilisateur propriétaire
     * @param fileName le nom du fichier avec UUID
     * @return le chemin de stockage relatif
     */
    String saveFile(MultipartFile file, Long userId, String fileName);

    /**
     * Supprime un fichier du disque.
     * @param cheminStockage le chemin du fichier à supprimer
     */
    void deleteFile(String cheminStockage);

    /**
     * Retourne le chemin absolu d'un fichier.
     * @param cheminStockage le chemin relatif du fichier
     */
    Path getFilePath(String cheminStockage);
}