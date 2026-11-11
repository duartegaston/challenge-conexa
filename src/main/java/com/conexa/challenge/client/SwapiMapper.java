package com.conexa.challenge.client;

import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import com.conexa.challenge.model.dto.SwapiRawListResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Convierte respuestas crudas de SWAPI (swapi.tech) al contrato interno de la API.
 */
@Component
public class SwapiMapper {

    private static final Pattern ID_FROM_URL = Pattern.compile(".*/(\\d+)/?$");

    /**
     * Mapea un listado SWAPI a {@link PageResponse}.
     */
    public PageResponse<SwapiItem> toPage(SwapiRawListResponse raw, SwapiResource resource, int page, int limit) {
        List<SwapiItem> content = new ArrayList<>();
        for (JsonNode node : iterable(firstArray(raw.results(), raw.result()))) {
            content.add(toItemFromNode(node, resource));
        }
        long total = raw.totalOrZero();
        if (total == 0) {
            total = content.size();
        }
        boolean hasNext = raw.next() != null && !raw.next().isBlank();
        return new PageResponse<>(List.copyOf(content), page, limit, total, hasNext);
    }

    /**
     * Normaliza un detalle SWAPI ({@code result.properties} o mapa plano).
     */
    @SuppressWarnings("unchecked")
    public SwapiItem toItemFromDetail(Map<String, Object> body, SwapiResource resource) {
        if (body == null || body.isEmpty()) {
            return new SwapiItem("", "", Map.of());
        }
        Object result = body.get("result");
        if (result instanceof Map<?, ?> resultMap) {
            Object props = resultMap.get("properties");
            Map<String, Object> fields = props instanceof Map<?, ?> p
                    ? new LinkedHashMap<>((Map<String, Object>) p)
                    : new LinkedHashMap<>();
            String uid = stringValue(resultMap.get("uid"));
            if (uid.isBlank()) {
                uid = extractId(stringValue(fields.get("url")));
            }
            String name = displayName(fields, resource);
            return new SwapiItem(uid, name, Map.copyOf(fields));
        }
        return toItem(body, resource);
    }

    /**
     * Normaliza un ítem de listado (uid/name/url o properties).
     */
    public SwapiItem toItem(Map<String, Object> fields, SwapiResource resource) {
        Map<String, Object> copy = fields == null ? Map.of() : new LinkedHashMap<>(fields);
        if (copy.get("properties") instanceof Map<?, ?> nested) {
            @SuppressWarnings("unchecked")
            Map<String, Object> props = new LinkedHashMap<>((Map<String, Object>) nested);
            String uid = stringValue(copy.get("uid"));
            if (uid.isBlank()) {
                uid = extractId(stringValue(props.get("url")));
            }
            return new SwapiItem(uid, displayName(props, resource), Map.copyOf(props));
        }
        String id = stringValue(copy.get("uid"));
        if (id.isBlank()) {
            id = extractId(stringValue(copy.get("url")));
        }
        return new SwapiItem(id, displayName(copy, resource), Map.copyOf(copy));
    }

    /**
     * Obtiene el id numérico desde la URL de SWAPI.
     */
    public String extractId(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        Matcher matcher = ID_FROM_URL.matcher(url.trim());
        return matcher.matches() ? matcher.group(1) : "";
    }

    private SwapiItem toItemFromNode(JsonNode node, SwapiResource resource) {
        Map<String, Object> map = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> map.put(entry.getKey(), jsonToJava(entry.getValue())));
        return toItem(map, resource);
    }

    private static Object jsonToJava(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            node.forEach(child -> list.add(jsonToJava(child)));
            return list;
        }
        if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            node.fields().forEachRemaining(e -> map.put(e.getKey(), jsonToJava(e.getValue())));
            return map;
        }
        return node.asText();
    }

    private static JsonNode firstArray(JsonNode primary, JsonNode fallback) {
        if (primary != null && primary.isArray()) {
            return primary;
        }
        if (fallback != null && fallback.isArray()) {
            return fallback;
        }
        return null;
    }

    private static Iterable<JsonNode> iterable(JsonNode array) {
        if (array == null || !array.isArray()) {
            return List.of();
        }
        return array::elements;
    }

    private static String displayName(Map<String, Object> fields, SwapiResource resource) {
        String name = stringValue(fields.get(resource.displayField()));
        if (name.isBlank()) {
            name = stringValue(fields.get("name"));
        }
        if (name.isBlank()) {
            name = stringValue(fields.get("title"));
        }
        return name;
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
