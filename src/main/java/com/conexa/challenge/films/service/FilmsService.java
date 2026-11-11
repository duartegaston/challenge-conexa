package com.conexa.challenge.films.service;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.swapi.CatalogService;
import com.conexa.challenge.swapi.SwapiResource;
import com.conexa.challenge.swapi.dto.SwapiItem;
import org.springframework.stereotype.Service;

@Service
public class FilmsService {

    private final CatalogService catalogService;

    public FilmsService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public PageResponse<SwapiItem> list(int page, Integer limit, String name, String title) {
        String search = firstNonBlank(title, name);
        return catalogService.list(SwapiResource.FILMS, page, limit, search);
    }

    public SwapiItem getById(String id) {
        return catalogService.getById(SwapiResource.FILMS, id);
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }
}
