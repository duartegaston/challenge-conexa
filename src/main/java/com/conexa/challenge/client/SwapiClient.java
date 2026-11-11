package com.conexa.challenge.client;

import com.conexa.challenge.exception.ResourceNotFoundException;
import com.conexa.challenge.exception.SwapiUnavailableException;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import com.conexa.challenge.model.dto.SwapiRawListResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente HTTP hacia SWAPI con caché en memoria (sin persistir el catálogo).
 */
@Component
@RequiredArgsConstructor
public class SwapiClient {

    private static final Logger log = LoggerFactory.getLogger(SwapiClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient swapiRestClient;
    private final SwapiMapper swapiMapper;

    /**
     * Obtiene un listado paginado (y opcionalmente filtrado) de un recurso SWAPI.
     */
    @Cacheable(
            cacheNames = "swapi-lists",
            key = "#resource.path() + ':' + #page + ':' + (#search == null ? '' : #search)")
    public PageResponse<SwapiItem> getList(SwapiResource resource, int page, String search) {
        int safePage = Math.max(page, 1);
        try {
            SwapiRawListResponse raw = swapiRestClient
                    .get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/{resource}/").queryParam("page", safePage);
                        if (search != null && !search.isBlank()) {
                            uriBuilder.queryParam("search", search.trim());
                        }
                        return uriBuilder.build(resource.path());
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new SwapiUnavailableException(
                                "SWAPI list request failed with status " + response.getStatusCode().value());
                    })
                    .body(SwapiRawListResponse.class);

            if (raw == null) {
                throw new SwapiUnavailableException("SWAPI returned an empty list body");
            }
            return swapiMapper.toPage(raw, resource, safePage);
        } catch (SwapiUnavailableException | ResourceNotFoundException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("SWAPI list error for {}: {}", resource.path(), ex.getStatusCode().value());
            throw new SwapiUnavailableException("SWAPI is unavailable", ex);
        } catch (RestClientException ex) {
            log.warn("SWAPI list connection error for {}: {}", resource.path(), ex.toString());
            throw new SwapiUnavailableException("SWAPI is unavailable", ex);
        }
    }

    /**
     * Obtiene el detalle de un recurso SWAPI por id.
     */
    @Cacheable(cacheNames = "swapi-details", key = "#resource.path() + ':' + #id")
    public SwapiItem getById(SwapiResource resource, String id) {
        try {
            Map<String, Object> body = swapiRestClient
                    .get()
                    .uri("/{resource}/{id}/", resource.path(), id)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ResourceNotFoundException(
                                resource.path() + " with id " + id + " was not found");
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new SwapiUnavailableException(
                                "SWAPI detail request failed with status " + response.getStatusCode().value());
                    })
                    .body(MAP_TYPE);

            if (body == null || body.isEmpty()) {
                throw new SwapiUnavailableException("SWAPI returned an empty detail body");
            }
            return swapiMapper.toItem(body, resource);
        } catch (SwapiUnavailableException | ResourceNotFoundException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.warn("SWAPI detail error for {}/{}: {}", resource.path(), id, ex.getStatusCode().value());
            throw new SwapiUnavailableException("SWAPI is unavailable", ex);
        } catch (RestClientException ex) {
            log.warn("SWAPI detail connection error for {}/{}: {}", resource.path(), id, ex.toString());
            throw new SwapiUnavailableException("SWAPI is unavailable", ex);
        }
    }
}
