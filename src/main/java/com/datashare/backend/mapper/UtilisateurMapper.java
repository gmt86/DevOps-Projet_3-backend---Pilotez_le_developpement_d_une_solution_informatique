package com.datashare.backend.mapper;

import com.datashare.backend.dto.auth.RegisterRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mappeur MapStruct pour la conversion entre l'entité Utilisateur et ses DTOs.
 * Évite la duplication de code de conversion dans les services et contrôleurs.
 */
@Mapper(componentModel = "spring")
public interface UtilisateurMapper {

    /**
     * Convertit un RegisterRequestDTO en entité Utilisateur.
     * Le mot de passe sera encodé dans le service avant la sauvegarde.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    Utilisateur toEntity(RegisterRequestDTO dto);
}