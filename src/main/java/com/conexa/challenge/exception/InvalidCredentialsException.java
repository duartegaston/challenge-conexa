package com.conexa.challenge.exception;

/**
 * Credenciales inválidas en el intento de login.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
