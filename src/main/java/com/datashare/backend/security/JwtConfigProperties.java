package com.datashare.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration JWT chargées depuis application.yaml.
 * Utilise le préfixe "jwt" pour mapper les valeurs automatiquement.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtConfigProperties(String secret, long expiration) {}