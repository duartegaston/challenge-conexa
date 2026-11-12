package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import org.springframework.util.StringUtils;

/**
 * Lógica común de los servicios de catálogo. Cada recurso extiende esta clase indicando su
 * {@link SwapiResource}; Films agrega además su propio criterio de búsqueda.
 *
 * <p>La validación de {@code page} y {@code limit} (rangos) se hace en el controller; acá solo se
 * resuelven el valor por defecto y la normalización del texto de búsqueda.
 */
public abstract class AbstractCatalogService {

    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 100;

    private final SwapiClient swapiClient;
    private final SwapiResource resource;

    protected AbstractCatalogService(SwapiClient swapiClient, SwapiResource resource) {
        this.swapiClient = swapiClient;
        this.resource = resource;
    }

    /**
     * Lista con paginación y filtro opcional por nombre (o título en films).
     *
     * @param page   página, base 1
     * @param limit  tamaño de página; {@code null} usa {@link #DEFAULT_LIMIT}
     * @param search texto a buscar; vacío equivale a sin filtro
     */
    public PageResponse<CatalogItem> list(int page, Integer limit, String search) {
        int effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;
        return swapiClient.getList(resource, page, effectiveLimit, normalizedSearch);
    }

    public CatalogItem getById(String id) {
        return swapiClient.getById(resource, id);
    }
}
