package com.conexa.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TTL y tamaño máximo de la caché Caffeine de SWAPI.
 */
@ConfigurationProperties(prefix = "cache.swapi")
public record CacheProperties(long ttlMinutes, long maxSize) {
}
