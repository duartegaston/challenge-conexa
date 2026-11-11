package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiResource;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio de naves (starships) de SWAPI.
 */
@Service
@RequiredArgsConstructor
public class StarshipsService {

    private final CatalogService catalogService;

    public PageResponse<SwapiItem> list(int page, Integer limit, String name) {
        return catalogService.list(SwapiResource.STARSHIPS, page, limit, name);
    }

    public SwapiItem getById(String id) {
        return catalogService.getById(SwapiResource.STARSHIPS, id);
    }
}
