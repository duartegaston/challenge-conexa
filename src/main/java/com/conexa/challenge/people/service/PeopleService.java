package com.conexa.challenge.people.service;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.swapi.CatalogService;
import com.conexa.challenge.swapi.SwapiResource;
import com.conexa.challenge.swapi.dto.SwapiItem;
import org.springframework.stereotype.Service;

@Service
public class PeopleService {

    private final CatalogService catalogService;

    public PeopleService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public PageResponse<SwapiItem> list(int page, Integer limit, String name) {
        return catalogService.list(SwapiResource.PEOPLE, page, limit, name);
    }

    public SwapiItem getById(String id) {
        return catalogService.getById(SwapiResource.PEOPLE, id);
    }
}
