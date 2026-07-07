package com.datashare.backend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration du stockage local des fichiers.
 * Utilise le préfixe "storage" pour mapper les valeurs depuis application.yaml.
 */
@ConfigurationProperties(prefix = "storage")
public record StorageConfigProperties(
    String path , 
    String forbiddenTypes , 
    long maxFileSizeBytes, 
    int defaultExpirationDays,  
    int maxExpirationDays) {}