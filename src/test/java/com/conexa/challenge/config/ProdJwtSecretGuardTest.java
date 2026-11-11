package com.conexa.challenge.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProdJwtSecretGuardTest {

    @Test
    void rejectsMissingAndDefaultSecrets() {
        assertThatThrownBy(() -> ProdJwtSecretGuard.requireSecureSecret(null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ProdJwtSecretGuard.requireSecureSecret("  "))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ProdJwtSecretGuard.requireSecureSecret(ProdJwtSecretGuard.INSECURE_DEFAULT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void acceptsPrivateSecret() {
        assertThatCode(() -> ProdJwtSecretGuard.requireSecureSecret("a-private-secret-that-is-not-the-repo-default"))
                .doesNotThrowAnyException();
    }
}
