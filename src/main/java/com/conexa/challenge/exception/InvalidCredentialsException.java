package com.conexa.challenge.exception;

/**
 * Credenciales inválidas en el login. El mensaje es siempre el mismo para no revelar si el usuario existe.
 */
public class InvalidCredentialsException extends RuntimeException {

    public static final String MESSAGE = "Invalid username or password";

    public InvalidCredentialsException() {
        super(MESSAGE);
    }
}
