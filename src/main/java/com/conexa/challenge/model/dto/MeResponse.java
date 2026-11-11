package com.conexa.challenge.model.dto;

import java.util.List;

/**
 * Perfil del usuario autenticado.
 */
public record MeResponse(String username, List<String> roles) {
}
