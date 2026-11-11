package com.conexa.challenge.model.dto;

import java.time.Instant;

/**
 * Cuerpo uniforme de error HTTP de la API.
 */
public record ErrorResponse(int status, String message, Instant timestamp) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, Instant.now());
    }
}
