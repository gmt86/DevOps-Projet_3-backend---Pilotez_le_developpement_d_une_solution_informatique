package com.datashare.backend.mapper;

import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.entity.Fichier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;

/**
 * Mappeur MapStruct pour la conversion entre l'entité Fichier et ses DTOs: Entité Fichier → FichierResponseDTO
 * 
 * FichierUploadRequestDTO → Entité Fichier  sera construit manuellement dans FichierServiceImpl avec @Builder  
 * car FichierUploadRequestDTO ne contient que dateExpiration et password  
 * alors les autres champs de l'entité (nom, taille, typeFichier, tokenTelechargement...)  
 * viennent du MultipartFile et sont calculés dans le service.
 */
@Mapper(componentModel = "spring")
public interface FichierMapper {

    /**
     * Convertit une entité Fichier en FichierResponseDTO.  
     */
    @Mapping(target = "estProtege", source = "fichier", qualifiedByName = "isProtege")
    @Mapping(target = "estExpire", source = "fichier", qualifiedByName = "isExpire")
    FichierResponseDTO toDTO(Fichier fichier);

    /**
     * Vérifie si le fichier est protégé par mot de passe.
     */
    @Named("isProtege")
    default boolean isProtege(Fichier fichier) {
        return fichier.getPassword() != null;
    }

    /**
     * Vérifie si le fichier est expiré.
     */
    @Named("isExpire")
    default boolean isExpire(Fichier fichier) {
        return fichier.getDateExpiration().isBefore(LocalDateTime.now());
    }
}