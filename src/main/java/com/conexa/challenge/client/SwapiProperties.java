package com.conexa.challenge.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de conexión al cliente HTTP de SWAPI.
 */
@ConfigurationProperties(prefix = "swapi")
public record SwapiProperties(String baseUrl, int connectTimeoutMs, int readTimeoutMs) {
}
