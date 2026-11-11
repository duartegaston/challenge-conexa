package com.conexa.challenge.starships.controller;

import com.conexa.challenge.common.dto.PageResponse;
import com.conexa.challenge.starships.service.StarshipsService;
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
@RequestMapping("/api/v1/starships")
@Validated
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Starships")
public class StarshipsController {

    private final StarshipsService starshipsService;

    public StarshipsController(StarshipsService starshipsService) {
        this.starshipsService = starshipsService;
    }

    @GetMapping
    @Operation(summary = "List starships", description = "Paged list. Filter by name uses SWAPI remote search.")
    public PageResponse<SwapiItem> list(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) String name) {
        return starshipsService.list(page, limit, name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get starship by id")
    public SwapiItem getById(@PathVariable @NotBlank String id) {
        return starshipsService.getById(id);
    }
}
