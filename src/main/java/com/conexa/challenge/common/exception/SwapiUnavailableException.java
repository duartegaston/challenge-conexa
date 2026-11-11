package com.conexa.challenge.common.exception;

public class SwapiUnavailableException extends RuntimeException {

    public SwapiUnavailableException(String message) {
        super(message);
    }

    public SwapiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
