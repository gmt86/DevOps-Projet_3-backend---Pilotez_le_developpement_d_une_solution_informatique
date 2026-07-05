    package com.datashare.backend.configuration;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
    import org.springframework.core.io.FileSystemResource;


     /**
     * Configuration principale de l'application.
     * Charge les variables d'environnement depuis le fichier .env
     * afin de les rendre disponibles dans application.yaml via ${VARIABLE}.
     */

    @Configuration
    public class AppConfig {

        //PropertySourcesPlaceholderConfigurer est un objet que Spring doit créer et gérer lui-même pour charger le .env au démarrage.
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
            configurer.setLocation(new FileSystemResource(".env"));
            return configurer;
        }
    }