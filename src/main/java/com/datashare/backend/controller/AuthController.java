package com.datashare.backend.controller;

import com.datashare.backend.dto.auth.AuthResponseDTO;
import com.datashare.backend.dto.auth.LoginRequestDTO;
import com.datashare.backend.dto.auth.RegisterRequestDTO;
import com.datashare.backend.service.impl.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion de l'authentification.
 * Expose les endpoints d'inscription et de connexion.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints d'inscription et de connexion")
public class AuthController {

    private final AuthService authService;

    /**
     * Inscrit un nouvel utilisateur.
     * POST /api/auth/register
     */
    @Operation(summary = "Inscription", description = "Crée un nouveau compte utilisateur")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.debug("POST /api/auth/register");
       
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
               .body(Map.of("message", "Compte créé avec succès"));
    }

    /**
     * Connecte un utilisateur existant.
     * POST /api/auth/login
     */
    @Operation(summary = "Connexion", description = "Connecte un utilisateur et retourne un token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.debug("POST /api/auth/login");
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}