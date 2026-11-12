package com.conexa.challenge.model.dto;

import java.util.List;

/**
 * Respuesta de login: JWT, su vigencia en segundos y los roles del usuario.
 */
public record LoginResponse(String accessToken, String tokenType, long expiresIn, List<String> roles) {}
