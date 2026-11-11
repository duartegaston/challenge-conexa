package com.conexa.challenge.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de inicio de sesión.
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
