package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.exception.ResourceNotFoundException;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

/**
 * Lógica común de los servicios de catálogo. Cada recurso extiende esta clase indicando su
 * {@link SwapiResource}; Films agrega además su propio criterio de búsqueda.
 *
 * <p>La validación de {@code page} y {@code limit} (rangos) y del formato de {@code id} se hace en el
 * controller; acá solo se resuelven el valor por defecto y la normalización del texto de búsqueda.
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
     * Lista con paginación y filtros opcionales por id y por nombre (o título en films).
     *
     * <p>Sin {@code id}, el filtro de nombre se resuelve en SWAPI. Con {@code id}, se consulta ese detalle y
     * el resultado es una página con 0 o 1 elementos: queda vacía si el id no existe o si, junto con
     * {@code search}, el nombre no coincide (coincidencia parcial, sin distinguir mayúsculas).
     *
     * @param page   página, base 1
     * @param limit  tamaño de página; {@code null} usa {@link #DEFAULT_LIMIT}
     * @param id     id del elemento; vacío equivale a sin filtro
     * @param search texto a buscar; vacío equivale a sin filtro
     */
    public PageResponse<CatalogItem> list(int page, Integer limit, String id, String search) {
        int effectiveLimit = limit != null ? limit : DEFAULT_LIMIT;
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;

        if (StringUtils.hasText(id)) {
            return PageResponse.slice(findById(id.trim(), normalizedSearch), page, effectiveLimit);
        }
        return swapiClient.getList(resource, page, effectiveLimit, normalizedSearch);
    }

    public CatalogItem getById(String id) {
        return swapiClient.getById(resource, id);
    }

    private List<CatalogItem> findById(String id, String search) {
        try {
            CatalogItem item = swapiClient.getById(resource, id);
            return matches(item, search) ? List.of(item) : List.of();
        } catch (ResourceNotFoundException notFound) {
            return List.of();
        }
    }

    private static boolean matches(CatalogItem item, String search) {
        if (search == null) {
            return true;
        }
        return item.name() != null && item.name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }
}
