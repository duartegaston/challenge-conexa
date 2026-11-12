package com.conexa.challenge.model.dto;

import java.util.List;

/**
 * Perfil de un usuario: username y roles.
 */
public record MeResponse(String username, List<String> roles) {}
