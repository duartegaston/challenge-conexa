package com.conexa.challenge.controller;

import com.conexa.challenge.service.StarshipsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Naves de Star Wars.
 */
@RestController
@RequestMapping("/api/v1/starships")
@Tag(name = "Starships")
public class StarshipsController extends AbstractCatalogController<StarshipsService> {

    public StarshipsController(StarshipsService service) {
        super(service);
    }
}
