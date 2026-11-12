package com.conexa.challenge.exception;

/**
 * Se intentó registrar un username que ya existe.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username '" + username + "' is already taken");
    }
}
