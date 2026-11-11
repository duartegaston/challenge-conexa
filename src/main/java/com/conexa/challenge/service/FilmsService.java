package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de películas (films) de SWAPI.
 */
@Service
@RequiredArgsConstructor
public class FilmsService {

    private final CatalogService catalogService;

    /**
     * Lista films; {@code title} tiene prioridad sobre {@code name} para el search remoto.
     */
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
