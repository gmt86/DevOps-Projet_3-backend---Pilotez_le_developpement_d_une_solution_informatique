package com.datashare.backend.controller;

import com.datashare.backend.dto.file.DownloadRequestDTO;
import com.datashare.backend.dto.file.FichierResponseDTO;
import com.datashare.backend.dto.file.FichierUploadRequestDTO;
import com.datashare.backend.entity.Utilisateur;
import com.datashare.backend.service.impl.FichierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Fichiers", description = "Endpoints de gestion des fichiers")
public class FichierController {

    private final FichierService fichierService;

    /**
     * Upload un fichier.
     * POST /api/fichiers
     */
    @Operation(summary = "Upload fichier", description = "Upload un fichier avec date d'expiration optionnelle et mot de passe")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FichierResponseDTO> uploadFichier(
            @RequestPart("fichier") MultipartFile file,// si fichier abscent, MissingServletRequestPartException de GlobalExceptionHandler est lancée par Spring avant d'entrer dans le contrôleur
            @RequestPart("request") @Valid FichierUploadRequestDTO requestDTO, //@Valid déclenche la validation des annotations comme @NotNull, @Future... sur les champs d'un objet    
            @AuthenticationPrincipal Utilisateur utilisateur ) //@AuthenticationPrincipal est injecté par Spring Security depuis le token JWT car l'utilisateur est déjà authentifié et validé avant d'arriver ici.
            
            {
               
               log.debug("POST /api/fichiers - user: {}", utilisateur.getId());             
       
        FichierResponseDTO response = fichierService.uploadFichier(file, requestDTO, utilisateur.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retourne les métadonnées d'un fichier via son token.
     * GET /api/fichiers/{token}
     */
    @Operation(summary = "Métadonnées fichier", description = "Retourne les métadonnées d'un fichier via son token")
    @GetMapping("/{token}")
    public ResponseEntity<FichierResponseDTO> getFichierByToken(@PathVariable UUID token) {
        log.debug("GET /api/fichiers/{}", token);
        return ResponseEntity.ok(fichierService.getFichierByToken(token));
    }

    /**
     * Télécharge un fichier via son token.
     * POST /api/fichiers/{token}/download
     */
    @Operation(summary = "Télécharger fichier", description = "Télécharge un fichier via son token avec mot de passe optionnel dans le body")
    @PostMapping("/{token}/download")
    public ResponseEntity<Resource> downloadFichier(@PathVariable UUID token, @RequestBody(required = false) DownloadRequestDTO request ) 
    {
        log.debug("POST /api/fichiers/{}/download", token);
        String password = request != null ? request.getPassword() : null;
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
    @Operation(summary = "Historique fichiers", description = "Retourne la liste des fichiers de l'utilisateur connecté")
    @GetMapping
    public ResponseEntity<List<FichierResponseDTO>> getFichiers(@AuthenticationPrincipal Utilisateur utilisateur ) 
    {
        log.debug("GET /api/fichiers - user: {}", utilisateur.getId());
        return ResponseEntity.ok(fichierService.getFichiersByUtilisateur(utilisateur.getId()));
    }

    /**
     * Supprime un fichier.
     * DELETE /api/fichiers/{id}
     */
    @Operation(summary = "Supprimer fichier", description = "Supprime un fichier appartenant à l'utilisateur connecté")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFichier(@PathVariable UUID id,  @AuthenticationPrincipal Utilisateur utilisateur ) 
    {
        log.debug("DELETE /api/fichiers/{} - user: {}", id, utilisateur.getId());
        fichierService.deleteFichier(id, utilisateur.getId());
        return ResponseEntity.noContent().build();
    }
}