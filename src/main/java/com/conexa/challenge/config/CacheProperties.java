package com.conexa.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache.swapi")
public record CacheProperties(long ttlMinutes, long maxSize) {
}
