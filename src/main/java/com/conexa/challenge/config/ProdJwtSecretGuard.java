package com.conexa.challenge.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * En prod el secreto JWT tiene que venir de fuera del repo. El default de
 * {@code application.yml} es público y no sirve para firmar tokens reales.
 */
@Component
@Profile("prod")
public class ProdJwtSecretGuard implements ApplicationRunner {

    static final String INSECURE_DEFAULT = "change-me-phase1-demo-secret-must-be-long-enough-256bits";

    private final JwtProperties jwtProperties;

    public ProdJwtSecretGuard(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        requireSecureSecret(jwtProperties.secret());
    }

    static void requireSecureSecret(String secret) {
        if (secret == null || secret.isBlank() || INSECURE_DEFAULT.equals(secret)) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to a private value when the prod profile is active");
        }
    }
}
