package com.conexa.challenge.model.dto;

import java.util.List;

/**
 * Página normalizada para los listados del catálogo.
 */
public record PageResponse<T>(List<T> content, int page, int limit, long total, boolean hasNext) {

    /**
     * Pagina en memoria una lista completa. Se usa cuando el origen devuelve todos los resultados juntos
     * (búsquedas y {@code /films} en SWAPI ignoran {@code page} y {@code limit}).
     *
     * @param all   todos los elementos
     * @param page  página, base 1
     * @param limit tamaño de página
     */
    public static <T> PageResponse<T> slice(List<T> all, int page, int limit) {
        int from = (int) Math.min((long) (page - 1) * limit, all.size());
        int to = Math.min(from + limit, all.size());
        return new PageResponse<>(List.copyOf(all.subList(from, to)), page, limit, all.size(), to < all.size());
    }
}
