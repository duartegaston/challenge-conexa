package com.conexa.challenge.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Envelope flexible de listado SWAPI (swapi.tech puede usar results o result).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiRawListResponse(
        Integer totalRecords,
        Integer total_records,
        String next,
        String previous,
        JsonNode results,
        JsonNode result) {

    public long totalOrZero() {
        if (total_records != null) {
            return total_records;
        }
        if (totalRecords != null) {
            return totalRecords;
        }
        return 0;
    }
}
