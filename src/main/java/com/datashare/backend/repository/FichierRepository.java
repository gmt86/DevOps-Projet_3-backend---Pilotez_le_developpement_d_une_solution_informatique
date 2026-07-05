package com.datashare.backend.repository;

import com.datashare.backend.entity.Fichier;
import com.datashare.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des fichiers en base de données.
 * Fournit les opérations CRUD et des requêtes personnalisées.
 */
@Repository
public interface FichierRepository extends JpaRepository<Fichier, UUID> {

    /**
     * Récupère tous les fichiers d'un utilisateur.
     */
    List<Fichier> findByUtilisateur(Utilisateur utilisateur);

    /**
     * Récupère un fichier par son token de téléchargement.
     */
    Optional<Fichier> findByTokenTelechargement(UUID token);

    /**
     * Récupère les fichiers expirés avant une date donnée.
     * Utilisé pour la purge automatique des fichiers expirés.
     */
    List<Fichier> findByDateExpirationBefore(LocalDateTime date);
}