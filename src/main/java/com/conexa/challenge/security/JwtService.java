package com.conexa.challenge.security;

import com.conexa.challenge.config.JwtProperties;
import com.conexa.challenge.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Emite y valida tokens JWT (HS256) con el username como subject y los roles como claim.
 */
@Slf4j
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtService(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expiration = properties.expiration();
    }

    public String generateToken(String username, Collection<Role> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim(ROLES_CLAIM, roles.stream().map(Role::name).sorted().toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(secretKey)
                .compact();
    }

    public long expiresInSeconds() {
        return expiration.toSeconds();
    }

    /**
     * Valida firma y vigencia en un solo parseo.
     *
     * @return la identidad del token, o vacío si es inválido, está vencido o no tiene subject
     */
    public Optional<JwtPrincipal> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getSubject() == null) {
                return Optional.empty();
            }
            return Optional.of(new JwtPrincipal(claims.getSubject(), rolesFrom(claims)));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private static Set<Role> rolesFrom(Claims claims) {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        if (claims.get(ROLES_CLAIM) instanceof List<?> names) {
            names.forEach(name -> Role.parse(String.valueOf(name)).ifPresent(roles::add));
        }
        return roles;
    }
}
