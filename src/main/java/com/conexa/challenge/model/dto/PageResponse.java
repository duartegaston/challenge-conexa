package com.conexa.challenge.model.dto;

import java.util.List;

/**
 * Página normalizada para listados de recursos SWAPI.
 */
public record PageResponse<T>(List<T> content, int page, int limit, long total, boolean hasNext) {
}
