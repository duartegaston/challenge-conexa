package com.conexa.challenge.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/**
 * Persistencia de {@link UserEntity} sobre H2 con el esquema real de Flyway.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void generatesIdAndCreationDateOnSave() {
        UserEntity saved = userRepository.saveAndFlush(user("obiwan", Role.USER));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void findsByUsernameWithRoles() {
        userRepository.saveAndFlush(user("obiwan", Role.USER, Role.ADMIN));
        entityManager.clear();

        assertThat(userRepository.findByUsername("obiwan"))
                .hasValueSatisfying(found -> assertThat(found.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN));
        assertThat(userRepository.existsByUsername("obiwan")).isTrue();
        assertThat(userRepository.existsByUsername("vader")).isFalse();
    }

    @Test
    void usernameIsUnique() {
        userRepository.saveAndFlush(user("yoda", Role.USER));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("yoda", Role.USER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rolesAreExposedReadOnly() {
        UserEntity saved = userRepository.saveAndFlush(user("han", Role.USER));

        assertThatThrownBy(() -> saved.getRoles().add(Role.ADMIN)).isInstanceOf(UnsupportedOperationException.class);
    }

    private static UserEntity user(String username, Role... roles) {
        return new UserEntity(username, "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUV", Set.of(roles));
    }
}
