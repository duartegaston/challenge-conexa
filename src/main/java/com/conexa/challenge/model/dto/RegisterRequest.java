package com.conexa.challenge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un usuario. El username no distingue mayúsculas (se guarda en minúsculas).
 */
public record RegisterRequest(
        @NotBlank
                @Pattern(
                        regexp = "^[A-Za-z0-9._-]{3,50}$",
                        message = "must be 3-50 characters: letters, digits, '.', '_' or '-'")
                String username,
        @NotBlank @Size(min = 8, max = 72, message = "must be between 8 and 72 characters") String password) {}
