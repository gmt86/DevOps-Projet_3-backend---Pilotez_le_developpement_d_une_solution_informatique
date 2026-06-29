package com.datashare.backend.security;

import com.datashare.backend.exception.AppException;
import com.datashare.backend.exception.ErrorCode;
import com.datashare.backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service de chargement des détails utilisateur pour Spring Security.
 * Utilisé par le filtre JWT pour authentifier les utilisateurs via leur email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Charge un utilisateur par son email.
     * Appelé automatiquement par Spring Security lors de l'authentification.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}