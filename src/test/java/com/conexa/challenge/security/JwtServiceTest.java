package com.conexa.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.config.JwtProperties;
import com.conexa.challenge.model.Role;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del emisor/validador JWT.
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                new JwtProperties("phase5-test-secret-must-be-long-enough-256bits!!", 60));
    }

    @Test
    void shouldGenerateTokenWithSubjectAndRoles() {
        String token = jwtService.generateToken("luke", Set.of(Role.USER, Role.ADMIN));

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("luke");
        assertThat(jwtService.extractRoles(token)).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThat(jwtService.getExpiresInSeconds()).isEqualTo(3600);
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken("leia", Set.of(Role.USER));
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    void shouldRejectGarbageToken() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }
}
