package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import org.springframework.stereotype.Service;

/**
 * Catálogo de vehículos (vehicles) de SWAPI.
 */
@Service
public class VehiclesService extends AbstractCatalogService {

    public VehiclesService(SwapiClient swapiClient) {
        super(swapiClient, SwapiResource.VEHICLES);
    }
}
