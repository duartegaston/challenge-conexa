package com.conexa.challenge.swapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiRawListResponse(
        int count,
        String next,
        String previous,
        List<Map<String, Object>> results) {
}
