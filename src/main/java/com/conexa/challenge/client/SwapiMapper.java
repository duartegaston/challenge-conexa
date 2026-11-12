package com.conexa.challenge.client;

import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Convierte las respuestas de swapi.tech al contrato de la API.
 */
@Component
public class SwapiMapper {

    private static final Pattern ID_FROM_URL = Pattern.compile(".*/(\\d+)/?$");

    /**
     * Mapea un listado. Si el origen ya paginó se respeta su paginación; si devolvió todo junto
     * (búsquedas, films) se pagina acá.
     */
    public PageResponse<CatalogItem> toPage(SwapiListResponse raw, SwapiResource resource, int page, int limit) {
        var items = raw.rows().stream().map(row -> toItem(row, resource)).toList();
        if (raw.isPaginatedBySource()) {
            return new PageResponse<>(items, page, limit, raw.totalRecords(), StringUtils.hasText(raw.next()));
        }
        return PageResponse.slice(items, page, limit);
    }

    /**
     * Normaliza un elemento: tanto la fila de un listado ({@code uid}, {@code name}, {@code url})
     * como la forma con {@code properties} anidado.
     */
    public CatalogItem toItem(Map<String, Object> row, SwapiResource resource) {
        Map<String, Object> fields = row.get("properties") instanceof Map<?, ?> nested ? stringKeys(nested) : row;

        String id = text(row.get("uid"));
        if (id.isBlank()) {
            id = extractId(text(fields.get("url")));
        }
        return new CatalogItem(id, text(fields.get(resource.displayField())), immutableCopy(fields));
    }

    private static String extractId(String url) {
        Matcher matcher = ID_FROM_URL.matcher(url.trim());
        return matcher.matches() ? matcher.group(1) : "";
    }

    private static Map<String, Object> stringKeys(Map<?, ?> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(String.valueOf(key), value));
        return copy;
    }

    /** {@code Map.copyOf} no admite valores {@code null}, y SWAPI puede mandarlos. */
    private static Map<String, Object> immutableCopy(Map<String, Object> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
