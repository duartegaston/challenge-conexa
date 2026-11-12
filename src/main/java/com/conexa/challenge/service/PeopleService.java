package com.conexa.challenge.service;

import com.conexa.challenge.client.SwapiClient;
import com.conexa.challenge.client.SwapiResource;
import org.springframework.stereotype.Service;

/**
 * Catálogo de personajes (people) de SWAPI.
 */
@Service
public class PeopleService extends AbstractCatalogService {

    public PeopleService(SwapiClient swapiClient) {
        super(swapiClient, SwapiResource.PEOPLE);
    }
}
