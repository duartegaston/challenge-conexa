package com.conexa.challenge.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Firma y expiración de los JWT.
 *
 * <p>No hay secreto por defecto fuera de los perfiles {@code local} y {@code test}: si falta o es muy corto
 * (HS256 pide al menos 256 bits), la aplicación no arranca.
 */
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "must be at least 32 characters (256 bits) long") String secret,
        @NotNull @DefaultValue("60m") Duration expiration) {}
