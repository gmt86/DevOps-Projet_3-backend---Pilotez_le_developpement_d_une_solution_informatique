package com.datashare.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un fichier uploadé sur la plateforme DataShare.
 * Contient les métadonnées du fichier ainsi que le token de téléchargement unique.
 */
@Entity
@Table(name = "fichiers")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true) //dit à Lombok : Pour comparer deux objets Fichier, 
                                                 //utilise uniquement les champs marqués avec @EqualsAndHashCode.Include
                                                 //Sans cette annotation, Cela peut provoquer des boucles infinies 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fichier {

    // =====================
    // IDENTIFIANT
    // =====================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) //Génère automatiquement la valeur de cet id en utilisant le générateur nommé UUID  
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    // =====================
    // RELATIONS
    // =====================
    @ManyToOne(fetch = FetchType.LAZY)//Ne charge l'utilisateur que si on en a besoin — pas automatiquement
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    // =====================
    // DONNÉES FICHIER
    // =====================
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_fichier", nullable = false)
    private String typeFichier;

    @Column(name = "taille", nullable = false)
    private Long taille;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    // =====================
    // SÉCURITÉ & PARTAGE
    // =====================
    @Column(name = "token_telechargement", nullable = false, unique = true, updatable = false)
    private UUID tokenTelechargement;

    @Column(name = "password")
    private String password;

    // =====================
    // DATES
    // =====================
    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;
}