package com.conexa.challenge.controller;

import com.conexa.challenge.config.OpenApiConfig;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.service.AbstractCatalogService;
import com.conexa.challenge.service.FilmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Películas de Star Wars. No extiende {@link AbstractCatalogController} porque su listado acepta además
 * {@code title}, el filtro propio de SWAPI para films.
 */
@RestController
@RequestMapping("/api/v1/films")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Films")
public class FilmsController {

    private final FilmsService filmsService;

    @GetMapping
    @Operation(
            summary = "Listado paginado",
            description = "`title` (o su alias `name`) filtra por título; si vienen ambos, gana `title`. "
                    + "`id` filtra por id y devuelve 0 o 1 elementos; combinado con `title`, debe cumplir los dos.")
    public PageResponse<CatalogItem> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false) @Min(1) @Max(AbstractCatalogService.MAX_LIMIT) Integer limit,
            @RequestParam(required = false) @Pattern(regexp = "\\d+", message = "must be a number") String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String title) {
        return filmsService.list(page, limit, id, name, title);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle por id")
    public CatalogItem getById(@PathVariable @Pattern(regexp = "\\d+", message = "must be a number") String id) {
        return filmsService.getById(id);
    }
}
