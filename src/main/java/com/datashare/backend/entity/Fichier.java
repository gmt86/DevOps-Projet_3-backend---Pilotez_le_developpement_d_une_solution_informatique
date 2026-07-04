package com.datashare.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un fichier uploadé sur la plateforme DataShare.
 * Contient les métadonnées du fichier ainsi que le token de téléchargement unique.
 */
@Entity
@Table(name = "fichiers")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fichier {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_fichier", nullable = false)
    private String typeFichier;

    @Column(name = "taille", nullable = false)
    private Long taille;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Column(name = "password")
    private String password;

    @Column(name = "token_telechargement", nullable = false, unique = true, updatable = false)
    private UUID tokenTelechargement;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;
}