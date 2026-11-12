package com.conexa.challenge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciales de inicio de sesión. Los máximos evitan hashear passwords enormes (BCrypt usa 72 bytes).
 */
public record LoginRequest(
        @NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 72) String password) {}
