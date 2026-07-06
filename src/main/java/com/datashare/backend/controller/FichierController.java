package com.datashare.backend.controller;

import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.dto.file.FichierUploadRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.service.impl.FichierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur REST pour la gestion des fichiers.
 * Expose les endpoints d'upload, téléchargement, historique et suppression.
 */
@Slf4j
@RestController
@RequestMapping("/api/fichiers")
@RequiredArgsConstructor
public class FichierController {

    private final FichierService fichierService;

    /**
     * Upload un fichier.
     * POST /api/fichiers
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FichierResponseDTO> uploadFichier(
            @RequestPart("fichier") MultipartFile file,// si fichier abscent, MissingServletRequestPartException de GlobalExceptionHandler est lancée par Spring avant d'entrer dans le contrôleur
            @RequestPart("request") @Valid FichierUploadRequestDTO requestDTO, //@Valid déclenche la validation des annotations comme @NotNull, @Future... sur les champs d'un objet    
            @AuthenticationPrincipal Utilisateur utilisateur ) //@AuthenticationPrincipal est injecté par Spring Security depuis le token JWT car l'utilisateur est déjà authentifié et validé avant d'arriver ici.
            
            {
                log.info("=== POST /api/fichiers - user: {} ===", utilisateur.getId());
                // log.debug("POST /api/fichiers - user: {}", utilisateur.getId());
                log.debug("File name: {}", file.getOriginalFilename());
                log.debug("File size: {}", file.getSize());
                log.debug("Request: {}", requestDTO);
       
        FichierResponseDTO response = fichierService.uploadFichier(file, requestDTO, utilisateur.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retourne les métadonnées d'un fichier via son token.
     * GET /api/fichiers/{token}
     */
    @GetMapping("/{token}")
    public ResponseEntity<FichierResponseDTO> getFichierByToken(@PathVariable UUID token) {
        log.debug("GET /api/fichiers/{}", token);
        return ResponseEntity.ok(fichierService.getFichierByToken(token));
    }

    /**
     * Télécharge un fichier via son token.
     * POST /api/fichiers/{token}/download
     */
    @PostMapping("/{token}/download")
    public ResponseEntity<Resource> downloadFichier(
            @PathVariable UUID token,
            @RequestParam(required = false) String password
    ) {
        log.debug("POST /api/fichiers/{}/download", token);
        Resource resource = fichierService.downloadFichier(token, password);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Retourne l'historique des fichiers de l'utilisateur connecté.
     * GET /api/fichiers
     */
    @GetMapping
    public ResponseEntity<List<FichierResponseDTO>> getFichiers(
            @AuthenticationPrincipal Utilisateur utilisateur
    ) {
        log.debug("GET /api/fichiers - user: {}", utilisateur.getId());
        return ResponseEntity.ok(fichierService.getFichiersByUtilisateur(utilisateur.getId()));
    }

    /**
     * Supprime un fichier.
     * DELETE /api/fichiers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFichier(
            @PathVariable UUID id,
            @AuthenticationPrincipal Utilisateur utilisateur
    ) {
        log.debug("DELETE /api/fichiers/{} - user: {}", id, utilisateur.getId());
        fichierService.deleteFichier(id, utilisateur.getId());
        return ResponseEntity.noContent().build();
    }
}