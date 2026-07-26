package com.datashare.backend.dto.file;

import lombok.Data;

/**
 * DTO pour la requête de téléchargement.
 * Contient le mot de passe optionnel transmis dans le corps de la requête.
 */
@Data
public class DownloadRequestDTO {
    private String password;
}