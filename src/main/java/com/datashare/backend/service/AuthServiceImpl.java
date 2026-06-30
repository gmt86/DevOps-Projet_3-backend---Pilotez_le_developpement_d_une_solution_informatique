package com.datashare.backend.service;

import com.datashare.backend.dto.AuthResponseDTO;
import com.datashare.backend.dto.LoginRequestDTO;
import com.datashare.backend.dto.RegisterRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.exception.ErrorCode;
import com.datashare.backend.mapper.UtilisateurMapper;
import com.datashare.backend.repository.UtilisateurRepository;
import com.datashare.backend.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service d'authentification.
 * Gère l'inscription et la connexion des utilisateurs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final UtilisateurMapper utilisateurMapper;

    /**
     * Inscrit un nouvel utilisateur.
     * Vérifie que l'email n'existe pas déjà, encode le mot de passe
     * et retourne un token JWT.
     */
    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        log.debug("Registering new user with email: {}", request.getEmail());

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Utilisateur utilisateur = utilisateurMapper.toEntity(request);
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));

        utilisateurRepository.save(utilisateur);
        log.info("User registered successfully: {}", request.getEmail());

        String token = jwtServiceImpl.generateToken(utilisateur);
        return new AuthResponseDTO(token);
    }

    /**
     * Connecte un utilisateur existant.
     * Vérifie les credentials via Spring Security et retourne un token JWT.
     */
    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.debug("Attempting login for user: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        log.info("User logged in successfully: {}", request.getEmail());

        String token = jwtServiceImpl.generateToken(utilisateur);
        return new AuthResponseDTO(token);
    }
}