package com.conexa.challenge.swapi;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.swapi.dto.SwapiItem;
import com.conexa.challenge.swapi.dto.SwapiRawListResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SwapiMapper {

    private static final Pattern ID_FROM_URL = Pattern.compile(".*/(\\d+)/?$");
    private static final int SWAPI_PAGE_SIZE = 10;

    public PageResponse<SwapiItem> toPage(SwapiRawListResponse raw, SwapiResource resource, int page) {
        List<SwapiItem> content = raw.results() == null
                ? List.of()
                : raw.results().stream().map(row -> toItem(row, resource)).toList();
        boolean hasNext = raw.next() != null && !raw.next().isBlank();
        return new PageResponse<>(content, page, SWAPI_PAGE_SIZE, raw.count(), hasNext);
    }

    public SwapiItem toItem(Map<String, Object> fields, SwapiResource resource) {
        Map<String, Object> copy = fields == null ? Map.of() : new LinkedHashMap<>(fields);
        String url = stringValue(copy.get("url"));
        String id = extractId(url);
        String name = stringValue(copy.get(resource.displayField()));
        if (name.isBlank()) {
            name = stringValue(copy.get("name"));
        }
        if (name.isBlank()) {
            name = stringValue(copy.get("title"));
        }
        return new SwapiItem(id, name, Map.copyOf(copy));
    }

    public String extractId(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        Matcher matcher = ID_FROM_URL.matcher(url.trim());
        return matcher.matches() ? matcher.group(1) : "";
    }

    private static String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
