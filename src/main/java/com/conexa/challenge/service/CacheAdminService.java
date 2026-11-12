package com.conexa.challenge.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Operaciones administrativas sobre las cachés de SWAPI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheAdminService {

    private final CacheManager cacheManager;

    public void evictAll() {
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(cache -> {
                    cache.clear();
                    log.info("Cache '{}' cleared", cache.getName());
                });
    }
}
