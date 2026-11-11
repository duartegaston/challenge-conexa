package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Operaciones compartidas de catálogo SWAPI usadas por los servicios de recurso.
 */
@Service
@RequiredArgsConstructor
public class CatalogService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final SwapiClient swapiClient;

    /**
     * Lista un recurso con paginación, límite y filtro por nombre (query remoto en SWAPI).
     */
    public PageResponse<SwapiItem> list(SwapiResource resource, int page, Integer limit, String name) {
        int safePage = Math.max(page, 1);
        int safeLimit = normalizeLimit(limit);
        String search = blankToNull(name);
        PageResponse<SwapiItem> fromSwapi = swapiClient.getList(resource, safePage, safeLimit, search);
        return applyLimit(fromSwapi, safeLimit);
    }

    /**
     * Obtiene un ítem por id.
     */
    public SwapiItem getById(SwapiResource resource, String id) {
        return swapiClient.getById(resource, id);
    }

    static PageResponse<SwapiItem> applyLimit(PageResponse<SwapiItem> page, int limit) {
        List<SwapiItem> content = page.content().stream().limit(limit).toList();
        return new PageResponse<>(content, page.page(), limit, page.total(), page.hasNext());
    }

    static int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.min(Math.max(limit, 1), MAX_LIMIT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
