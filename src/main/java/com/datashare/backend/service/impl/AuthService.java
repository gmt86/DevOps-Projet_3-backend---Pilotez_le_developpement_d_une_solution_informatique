package com.datashare.backend.service.impl;

import com.datashare.backend.dto.AuthResponseDTO;
import com.datashare.backend.dto.LoginRequestDTO;
import com.datashare.backend.dto.RegisterRequestDTO;

/**
 * Interface définissant les opérations d'authentification.
 */
public interface AuthService {

    /**
     * Inscrit un nouvel utilisateur et retourne un token JWT.
     */
    AuthResponseDTO register(RegisterRequestDTO request);

    /**
     * Connecte un utilisateur existant et retourne un token JWT.
     */
    AuthResponseDTO login(LoginRequestDTO request);
}