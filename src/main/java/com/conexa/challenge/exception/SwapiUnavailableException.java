package com.conexa.challenge.exception;

/**
 * SWAPI no respondió correctamente o no es alcanzable.
 */
public class SwapiUnavailableException extends RuntimeException {

    public SwapiUnavailableException(String message) {
        super(message);
    }

    public SwapiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
