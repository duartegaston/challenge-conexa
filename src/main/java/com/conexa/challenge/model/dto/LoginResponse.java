package com.conexa.challenge.model.dto;

import java.util.List;

/**
 * Respuesta de login con JWT y roles del usuario.
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn, List<String> roles) {
}
