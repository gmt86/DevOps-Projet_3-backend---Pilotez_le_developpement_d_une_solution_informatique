package com.datashare.backend.service.impl;

import com.datashare.backend.dto.auth.AuthResponseDTO;
import com.datashare.backend.dto.auth.LoginRequestDTO;
import com.datashare.backend.dto.auth.RegisterRequestDTO;

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