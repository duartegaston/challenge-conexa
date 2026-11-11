package com.conexa.challenge.films.controller;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.films.service.FilmsService;
import com.conexa.challenge.swapi.dto.SwapiItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/films")
@Validated
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Films")
public class FilmsController {

    private final FilmsService filmsService;

    public FilmsController(FilmsService filmsService) {
        this.filmsService = filmsService;
    }

    @GetMapping
    @Operation(
            summary = "List films",
            description = "Paged list. Filter by title or name uses SWAPI remote search (title preferred).")
    public PageResponse<SwapiItem> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String title) {
        return filmsService.list(page, limit, name, title);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get film by id")
    public SwapiItem getById(@PathVariable @NotBlank String id) {
        return filmsService.getById(id);
    }
}
