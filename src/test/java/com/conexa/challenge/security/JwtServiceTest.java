package com.conexa.challenge.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.config.JwtProperties;
import com.conexa.challenge.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-must-be-at-least-32-chars!!";

    private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(60)));

    @Test
    void roundTripKeepsUsernameAndRoles() {
        String token = jwtService.generateToken("luke", Set.of(Role.USER, Role.ADMIN));

        assertThat(jwtService.parse(token)).hasValueSatisfying(principal -> {
            assertThat(principal.username()).isEqualTo("luke");
            assertThat(principal.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        });
        assertThat(jwtService.expiresInSeconds()).isEqualTo(3600);
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.generateToken("leia", Set.of(Role.USER));
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        assertThat(jwtService.parse(tampered)).isEmpty();
    }

    @Test
    void rejectsGarbageAndBlankTokens() {
        assertThat(jwtService.parse("not-a-jwt")).isEmpty();
        assertThat(jwtService.parse("")).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        var expiring = new JwtService(new JwtProperties(SECRET, Duration.ofSeconds(-10)));

        assertThat(jwtService.parse(expiring.generateToken("luke", Set.of(Role.USER)))).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        var other = new JwtService(new JwtProperties("another-secret-also-at-least-32-chars!!", Duration.ofMinutes(5)));

        assertThat(jwtService.parse(other.generateToken("luke", Set.of(Role.USER)))).isEmpty();
    }

    @Test
    void rejectsUnsignedToken() {
        String unsigned = Jwts.builder().subject("luke").compact();

        assertThat(jwtService.parse(unsigned)).isEmpty();
    }

    @Test
    void rejectsTokenWithoutSubject() {
        String token = Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(jwtService.parse(token)).isEmpty();
    }

    @Test
    void ignoresUnknownRolesInTheClaim() {
        String token = Jwts.builder()
                .subject("luke")
                .claim("roles", List.of("USER", "SUPERUSER"))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(jwtService.parse(token)).hasValueSatisfying(principal ->
                assertThat(principal.roles()).containsExactly(Role.USER));
    }
}
