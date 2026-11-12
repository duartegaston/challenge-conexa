package com.conexa.challenge.controller;

import com.conexa.challenge.config.OpenApiConfig;
import com.conexa.challenge.model.dto.CatalogItem;
import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.service.AbstractCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Endpoints comunes de catálogo: listado paginado con filtro por nombre y detalle por id.
 * Cada recurso extiende esta clase y declara su {@code @RequestMapping} y su tag de OpenAPI.
 *
 * <p>El acceso (rol USER) se define en {@code SecurityConfig}; los errores, en {@code GlobalExceptionHandler}.
 */
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public abstract class AbstractCatalogController<S extends AbstractCatalogService> {

    private final S service;

    protected AbstractCatalogController(S service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listado paginado", description = "`name` filtra en SWAPI por nombre (coincidencia parcial).")
    public PageResponse<CatalogItem> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false) @Min(1) @Max(AbstractCatalogService.MAX_LIMIT) Integer limit,
            @RequestParam(required = false) String name) {
        return service.list(page, limit, name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle por id")
    public CatalogItem getById(@PathVariable @Pattern(regexp = "\\d+", message = "must be a number") String id) {
        return service.getById(id);
    }
}
