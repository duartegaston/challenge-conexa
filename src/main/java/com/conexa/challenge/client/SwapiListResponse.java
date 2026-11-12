package com.conexa.challenge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Respuesta de listado de swapi.tech. Tiene dos formas:
 * <ul>
 *   <li>Listado paginado por el origen: {@code results} (uid, name, url) + {@code total_records} y {@code next}.</li>
 *   <li>Búsqueda y {@code /films}: {@code result} con todos los elementos ({@code properties}, {@code uid}),
 *       sin paginar.</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiListResponse(
        @JsonProperty("total_records") Integer totalRecords,
        String next,
        List<Map<String, Object>> results,
        List<Map<String, Object>> result) {

    List<Map<String, Object>> rows() {
        if (results != null) {
            return results;
        }
        return result != null ? result : List.of();
    }

    /** {@code true} si el origen ya paginó la respuesta. */
    boolean isPaginatedBySource() {
        return totalRecords != null;
    }
}
