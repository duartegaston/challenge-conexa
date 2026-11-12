package com.conexa.challenge.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Usuarios de demo que se crean al arrancar si no existen.
 *
 * @param enabled       {@code false} desactiva el seed (por ejemplo en un deploy real)
 * @param userPassword  password del usuario {@code user}
 * @param adminPassword password del usuario {@code admin}
 */
@Validated
@ConfigurationProperties(prefix = "app.security.seed")
public record SeedProperties(
        @DefaultValue("true") boolean enabled, @NotBlank String userPassword, @NotBlank String adminPassword) {}
