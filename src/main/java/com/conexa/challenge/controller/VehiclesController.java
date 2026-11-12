package com.conexa.challenge.controller;

import com.conexa.challenge.service.VehiclesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vehículos de Star Wars.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles")
public class VehiclesController extends AbstractCatalogController<VehiclesService> {

    public VehiclesController(VehiclesService service) {
        super(service);
    }
}
