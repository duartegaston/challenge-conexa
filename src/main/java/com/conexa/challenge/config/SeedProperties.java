package com.conexa.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Passwords de los usuarios seed de demo.
 */
@ConfigurationProperties(prefix = "app.security.seed")
public record SeedProperties(String userPassword, String adminPassword) {
}
