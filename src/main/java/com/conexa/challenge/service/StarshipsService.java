package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import org.springframework.stereotype.Service;

/**
 * Catálogo de naves (starships) de SWAPI.
 */
@Service
public class StarshipsService extends AbstractCatalogService {

    public StarshipsService(SwapiClient swapiClient) {
        super(swapiClient, SwapiResource.STARSHIPS);
    }
}
