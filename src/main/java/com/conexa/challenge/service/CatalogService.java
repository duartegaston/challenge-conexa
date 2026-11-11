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
     * Lista un recurso con paginación, límite local y filtro por nombre (search remoto).
     */
    public PageResponse<SwapiItem> list(SwapiResource resource, int page, Integer limit, String name) {
        int safePage = Math.max(page, 1);
        String search = blankToNull(name);
        PageResponse<SwapiItem> fromSwapi = swapiClient.getList(resource, safePage, search);
        return applyLimit(fromSwapi, limit);
    }

    /**
     * Obtiene un ítem por id.
     */
    public SwapiItem getById(SwapiResource resource, String id) {
        return swapiClient.getById(resource, id);
    }

    static PageResponse<SwapiItem> applyLimit(PageResponse<SwapiItem> page, Integer limit) {
        int safeLimit = normalizeLimit(limit);
        List<SwapiItem> content = page.content().stream().limit(safeLimit).toList();
        return new PageResponse<>(content, page.page(), safeLimit, page.total(), page.hasNext());
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
