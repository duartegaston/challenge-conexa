package com.conexa.challenge.controller;

import com.conexa.challenge.config.OpenApiConfig;
import com.conexa.challenge.service.CacheAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operaciones administrativas. Solo rol ADMIN (regla en {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(name = "Admin")
public class AdminController {

    private final CacheAdminService cacheAdminService;

    @DeleteMapping("/cache")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Vaciar las cachés de SWAPI", description = "Solo ADMIN.")
    public void evictCaches() {
        cacheAdminService.evictAll();
    }
}
