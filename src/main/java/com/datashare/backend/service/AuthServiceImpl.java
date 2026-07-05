package com.datashare.backend.service;

import com.datashare.backend.dto.auth.AuthResponseDTO;
import com.datashare.backend.dto.auth.LoginRequestDTO;
import com.datashare.backend.dto.auth.RegisterRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.exception.AppException;
import com.datashare.backend.exception.ErrorCode;
import com.datashare.backend.mapper.UtilisateurMapper;
import com.datashare.backend.repository.UtilisateurRepository;
import com.datashare.backend.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

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

        try {
            utilisateurRepository.save(utilisateur);
            log.info("User registered successfully: {}", request.getEmail());
        } catch (DataAccessException e) {
            log.error("Database error while saving user: {}", e.getMessage());
            throw new AppException(ErrorCode.DATABASE_ERROR);
        }

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

        try {
            
            // authenticate() charge déjà l'utilisateur via CustomUserDetailsService
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // On récupère l'utilisateur depuis le résultat de l'authentification
            // authentication.getPrincipal() retourne l'objet UserDetails chargé par 
            // CustomUserDetailsService — qui est notre Utilisateur
            Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();

            log.info("User logged in successfully: {}", request.getEmail());

            String token = jwtServiceImpl.generateToken(utilisateur);
            return new AuthResponseDTO(token);


        } catch (AuthenticationException e) {
            /* Capture toutes les exceptions d'authentification Spring Security
             Les exceptions possibles lors du login :
             - BadCredentialsException -> Mauvais mot de passe
             - DisabledException -> Compte désactivé
             - LockedException -> Compte verrouillé
            */
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
       
    }
}