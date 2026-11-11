package com.conexa.challenge.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * Shape crudo de listado que devuelve SWAPI.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiRawListResponse(int count, String next, String previous, List<Map<String, Object>> results) {
}
