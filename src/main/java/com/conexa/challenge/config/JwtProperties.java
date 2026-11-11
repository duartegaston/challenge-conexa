package com.conexa.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de firma y expiración de JWT.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
