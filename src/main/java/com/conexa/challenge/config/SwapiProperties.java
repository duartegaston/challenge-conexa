package com.conexa.challenge.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Conexión y caché del cliente de SWAPI.
 *
 * @param baseUrl        URL base de la API (ej. {@code https://www.swapi.tech/api})
 * @param connectTimeout timeout de conexión
 * @param readTimeout    timeout de lectura
 * @param cache          configuración de la caché local
 */
@Validated
@ConfigurationProperties(prefix = "swapi")
public record SwapiProperties(
        @NotBlank String baseUrl,
        @NotNull @DefaultValue("3s") Duration connectTimeout,
        @NotNull @DefaultValue("5s") Duration readTimeout,
        @Valid @DefaultValue Cache cache) {

    /**
     * TTL por caché y tamaño máximo. Los detalles casi no cambian, por eso viven más que los listados.
     */
    public record Cache(
            @NotNull @DefaultValue("5m") Duration listsTtl,
            @NotNull @DefaultValue("1h") Duration detailsTtl,
            @Positive @DefaultValue("300") long maxSize) {}
}
