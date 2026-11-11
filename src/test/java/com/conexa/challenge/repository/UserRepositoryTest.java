package com.conexa.challenge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Slice de repositorio: persistencia de UserEntity sobre H2 de test.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindByUsernameWhenUserExists() {
        UserEntity saved = userRepository.save(new UserEntity(
                UUID.randomUUID(),
                "obiwan",
                "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUV",
                Set.of(Role.USER),
                true,
                Instant.now()));

        assertThat(userRepository.findByUsername("obiwan"))
                .isPresent()
                .get()
                .extracting(UserEntity::getUsername, UserEntity::getId)
                .containsExactly("obiwan", saved.getId());
        assertThat(userRepository.existsByUsername("obiwan")).isTrue();
        assertThat(userRepository.existsByUsername("vader")).isFalse();
    }
}
