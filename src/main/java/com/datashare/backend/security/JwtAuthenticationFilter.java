package com.datashare.backend.security;

import com.datashare.backend.service.JwtServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT exécuté une seule fois par requête HTTP.
 * Intercepte les requêtes, extrait et valide le token JWT
 * puis authentifie l'utilisateur dans le contexte de sécurité Spring.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtServiceImpl jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (isPublicEndpoint(request) || !isValidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = extractJwt(authHeader);
        final String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(username, jwt, request);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Vérifie si l'endpoint est public (pas besoin de token).
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/login")
                || path.startsWith("/api/transfers/");
    }

    /**
     * Vérifie si l'en-tête Authorization est valide.
     */
    private boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * Extrait le JWT de l'en-tête Authorization.
     */
    private String extractJwt(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Authentifie l'utilisateur si le token est valide.
     */
    private void authenticateUser(String username, String jwt, HttpServletRequest request) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (jwtService.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("User authenticated successfully: {}", username);
        } else {
            log.warn("Invalid JWT token for user: {}", username);
        }
    }
}