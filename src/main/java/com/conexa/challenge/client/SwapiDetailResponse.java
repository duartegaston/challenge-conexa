package com.conexa.challenge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * Respuesta de detalle de swapi.tech: {@code result} con {@code uid} y {@code properties}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwapiDetailResponse(Map<String, Object> result) {}
