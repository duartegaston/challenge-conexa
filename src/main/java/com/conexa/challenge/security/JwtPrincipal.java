package com.conexa.challenge.security;

import com.conexa.challenge.model.Role;
import java.util.Set;

/**
 * Identidad extraída de un JWT válido.
 */
public record JwtPrincipal(String username, Set<Role> roles) {}
