package com.conexa.challenge.client;

import com.conexa.challenge.config.CacheConfig;
import com.conexa.challenge.exception.ResourceNotFoundException;
import com.conexa.challenge.exception.SwapiUnavailableException;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente de SWAPI (https://www.swapi.tech) con caché Caffeine. Los errores del origen salen como
 * {@link SwapiUnavailableException} (o {@link ResourceNotFoundException} para un 404 de detalle) y nunca se cachean.
 *
 * <p>Los argumentos llegan ya validados y normalizados desde el servicio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SwapiClient {

    private final RestClient swapiRestClient;
    private final SwapiMapper swapiMapper;

    /**
     * Lista un recurso con paginación y filtro opcional por nombre/título. La clave de caché ignora
     * mayúsculas en la búsqueda.
     */
    @Cacheable(
            cacheNames = CacheConfig.SWAPI_LISTS,
            key = "#resource.path() + ':' + #page + ':' + #limit + ':' + (#search?.toLowerCase() ?: '')")
    public PageResponse<CatalogItem> getList(SwapiResource resource, int page, int limit, String search) {
        SwapiListResponse raw = fetch("list " + resource.path(), () -> swapiRestClient
                .get()
                .uri(uriBuilder -> {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("resource", resource.path());
                    uriBuilder.path("/{resource}").queryParam("page", page).queryParam("limit", limit);
                    if (search != null) {
                        // Variable de URI (no literal) para que "{", "&" o "=" del texto se codifiquen.
                        uriBuilder.queryParam(resource.displayField(), "{search}");
                        variables.put("search", search);
                    }
                    return uriBuilder.build(variables);
                })
                .retrieve()
                .body(SwapiListResponse.class));
        return swapiMapper.toPage(raw, resource, page, limit);
    }

    /**
     * Detalle de un recurso por id.
     *
     * @throws ResourceNotFoundException si SWAPI responde 404
     */
    @Cacheable(cacheNames = CacheConfig.SWAPI_DETAILS, key = "#resource.path() + ':' + #id")
    public CatalogItem getById(SwapiResource resource, String id) {
        SwapiDetailResponse raw = fetch("detail " + resource.path() + "/" + id, () -> swapiRestClient
                .get()
                .uri("/{resource}/{id}", resource.path(), id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {
                    throw new ResourceNotFoundException(resource.path() + " with id " + id + " was not found");
                })
                .body(SwapiDetailResponse.class));

        if (raw.result() == null || raw.result().isEmpty()) {
            throw new SwapiUnavailableException("SWAPI returned an empty detail for " + resource.path() + "/" + id);
        }
        return swapiMapper.toItem(raw.result(), resource);
    }

    /**
     * Ejecuta la llamada y traduce cualquier falla del origen (status de error, timeout, cuerpo ilegible o
     * vacío) a {@link SwapiUnavailableException}. Las excepciones de dominio lanzadas desde un status
     * handler no son {@code RestClientException} y pasan sin tocarse.
     */
    private <T> T fetch(String description, Supplier<T> call) {
        try {
            T body = call.get();
            if (body == null) {
                throw new SwapiUnavailableException("SWAPI returned an empty response for " + description);
            }
            return body;
        } catch (RestClientResponseException ex) {
            log.warn("SWAPI {} failed with status {}", description, ex.getStatusCode().value());
            throw new SwapiUnavailableException("SWAPI is unavailable", ex);
        } catch (RestClientException ex) {
            log.warn("SWAPI {} failed: {}", description, ex.toString());
            throw new SwapiUnavailableException("SWAPI is unavailable", ex);
        }
    }
}
