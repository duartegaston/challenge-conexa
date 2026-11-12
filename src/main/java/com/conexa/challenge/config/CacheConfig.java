package com.conexa.challenge.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cachés Caffeine de SWAPI: una para listados y otra para detalles, cada una con su TTL.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String SWAPI_LISTS = "swapi-lists";
    public static final String SWAPI_DETAILS = "swapi-details";

    @Bean
    CacheManager cacheManager(SwapiProperties properties) {
        SwapiProperties.Cache cache = properties.cache();
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(SWAPI_LISTS, build(cache.listsTtl(), cache.maxSize()));
        cacheManager.registerCustomCache(SWAPI_DETAILS, build(cache.detailsTtl(), cache.maxSize()));
        return cacheManager;
    }

    private static Cache<Object, Object> build(Duration ttl, long maxSize) {
        return Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .recordStats()
                .build();
    }
}
