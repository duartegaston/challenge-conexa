package com.conexa.challenge.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    public static final String SWAPI_LISTS = "swapi-lists";
    public static final String SWAPI_DETAILS = "swapi-details";

    @Bean
    CacheManager cacheManager(CacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(SWAPI_LISTS, SWAPI_DETAILS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.ttlMinutes(), TimeUnit.MINUTES)
                .maximumSize(properties.maxSize()));
        return cacheManager;
    }
}
