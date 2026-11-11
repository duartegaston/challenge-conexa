package com.conexa.challenge.swapi;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.swapi.dto.SwapiItem;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Shared catalog operations over SWAPI. Feature services delegate here.
 */
@Service
public class CatalogService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;

    private final SwapiClient swapiClient;

    public CatalogService(SwapiClient swapiClient) {
        this.swapiClient = swapiClient;
    }

    public PageResponse<SwapiItem> list(SwapiResource resource, int page, Integer limit, String name) {
        int safePage = Math.max(page, 1);
        String search = blankToNull(name);
        PageResponse<SwapiItem> fromSwapi = swapiClient.getList(resource, safePage, search);
        return applyLimit(fromSwapi, limit);
    }

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
