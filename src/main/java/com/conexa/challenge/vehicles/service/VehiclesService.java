package com.conexa.challenge.vehicles.service;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.swapi.CatalogService;
import com.conexa.challenge.swapi.SwapiResource;
import com.conexa.challenge.swapi.dto.SwapiItem;
import org.springframework.stereotype.Service;

@Service
public class VehiclesService {

    private final CatalogService catalogService;

    public VehiclesService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public PageResponse<SwapiItem> list(int page, Integer limit, String name) {
        return catalogService.list(SwapiResource.VEHICLES, page, limit, name);
    }

    public SwapiItem getById(String id) {
        return catalogService.getById(SwapiResource.VEHICLES, id);
    }
}
