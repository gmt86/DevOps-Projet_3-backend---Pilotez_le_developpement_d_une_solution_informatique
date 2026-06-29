package com.datashare.backend.service.impl;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interface définissant les opérations de gestion des tokens JWT.
 */
public interface JwtService {

    String generateToken(UserDetails userDetails);

    String extractUsername(String token);

    boolean validateToken(String token, UserDetails userDetails);

    boolean isTokenExpired(String token);
}