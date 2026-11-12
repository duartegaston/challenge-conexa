package com.conexa.challenge.controller;

import com.conexa.challenge.service.PeopleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Personajes de Star Wars.
 */
@RestController
@RequestMapping("/api/v1/people")
@Tag(name = "People")
public class PeopleController extends AbstractCatalogController<PeopleService> {

    public PeopleController(PeopleService service) {
        super(service);
    }
}
