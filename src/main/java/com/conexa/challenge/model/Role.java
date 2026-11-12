package com.conexa.challenge.model;

import java.util.Optional;

/**
 * Roles de autorización. {@link #authority()} es el nombre que usa Spring Security ({@code ROLE_USER}, ...).
 */
public enum Role {
    USER,
    ADMIN;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    public String authority() {
        return AUTHORITY_PREFIX + name();
    }

    /**
     * Interpreta {@code "ADMIN"} o {@code "ROLE_ADMIN"}; devuelve vacío si no es un rol conocido.
     */
    public static Optional<Role> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String name = value.startsWith(AUTHORITY_PREFIX) ? value.substring(AUTHORITY_PREFIX.length()) : value;
        for (Role role : values()) {
            if (role.name().equals(name)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }
}
