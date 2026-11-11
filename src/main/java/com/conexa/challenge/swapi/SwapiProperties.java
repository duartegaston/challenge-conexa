package com.conexa.challenge.swapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swapi")
public record SwapiProperties(
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs) {
}
