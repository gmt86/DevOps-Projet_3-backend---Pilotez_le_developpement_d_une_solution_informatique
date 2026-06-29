package com.datashare.backend.service;

import com.datashare.backend.security.JwtConfigProperties;
import com.datashare.backend.service.impl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implémentation du service JWT.
 * Responsable de la génération, validation et extraction
 * des informations contenues dans les tokens JWT.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String CLAIM_AUTHORITIES = "authorities";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private final JwtConfigProperties jwtConfigProperties;

    @Override
    public String generateToken(UserDetails userDetails) {
        log.debug("Generating JWT token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = buildClaims(userDetails);
        return createToken(claims, userDetails.getUsername());
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            if (isValid) {
                log.debug("Token validated successfully for user: {}", username);
            } else {
                log.warn("Invalid token for user: {}", userDetails.getUsername());
            }
            return isValid;
        } catch (io.jsonwebtoken.JwtException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Construit les claims du token JWT.
     */
    private Map<String, Object> buildClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_AUTHORITIES, extractAuthorities(userDetails));
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE);
        return claims;
    }

    /**
     * Extrait les autorités de l'utilisateur au format String.
     */
    private String extractAuthorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    /**
     * Crée le token JWT avec les claims et le sujet.
     */
    private String createToken(Map<String, Object> claims, String userName) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtConfigProperties.expiration());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Récupère la clé de signature HMAC depuis la configuration.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfigProperties.secret().getBytes());
    }

    /**
     * Extrait la date d'expiration du token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait un claim spécifique du token.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait tous les claims du token — la signature est vérifiée ici.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}