package com.conexa.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.conexa.challenge.config.SeedProperties;
import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import com.conexa.challenge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserDataSeeder seeder() {
        return new UserDataSeeder(userRepository, passwordEncoder, new SeedProperties(true, "user-pass", "admin-pass"));
    }

    @Test
    void createsUserAndAdminWhenMissing() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode("user-pass")).thenReturn("user-hash");
        when(passwordEncoder.encode("admin-pass")).thenReturn("admin-hash");

        seeder().run(null);

        ArgumentCaptor<UserEntity> saved = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(UserEntity::getUsername).containsExactly("user", "admin");
        assertThat(saved.getAllValues()).extracting(UserEntity::getPasswordHash).containsExactly("user-hash", "admin-hash");
        assertThat(saved.getAllValues().get(0).getRoles()).containsExactly(Role.USER);
        assertThat(saved.getAllValues().get(1).getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void isIdempotentWhenUsersAlreadyExist() {
        when(userRepository.existsByUsername(any())).thenReturn(true);

        seeder().run(null);

        verify(userRepository, never()).save(any());
    }
}
