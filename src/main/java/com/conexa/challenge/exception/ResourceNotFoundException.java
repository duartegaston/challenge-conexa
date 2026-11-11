package com.conexa.challenge.exception;

/**
 * Recurso solicitado no encontrado (p. ej. id SWAPI inexistente).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
