package com.conexa.challenge.controller;

import com.conexa.challenge.model.dto.PageResponse;
import com.conexa.challenge.model.dto.SwapiItem;
import com.conexa.challenge.service.FilmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de películas Star Wars.
 */
@RestController
@RequestMapping("/api/v1/films")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Films")
public class FilmsController {

    private final FilmsService filmsService;

    @GetMapping
    @Operation(summary = "Listar films", description = "title o name → search remoto (title prioritario).")
    public PageResponse<SwapiItem> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String title) {
        return filmsService.list(page, limit, name, title);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de film por id")
    public SwapiItem getById(@PathVariable @NotBlank String id) {
        return filmsService.getById(id);
    }
}
