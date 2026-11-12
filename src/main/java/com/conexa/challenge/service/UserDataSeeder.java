package com.conexa.challenge.service;

import com.conexa.challenge.config.SeedProperties;
import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import com.conexa.challenge.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea los usuarios de demo ({@code user} y {@code admin}) al arrancar si todavía no existen.
 * Se desactiva con {@code app.security.seed.enabled=false}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.security.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UserDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedIfAbsent("user", seedProperties.userPassword(), Set.of(Role.USER));
        seedIfAbsent("admin", seedProperties.adminPassword(), Set.of(Role.USER, Role.ADMIN));
    }

    private void seedIfAbsent(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(new UserEntity(username, passwordEncoder.encode(rawPassword), roles));
        log.info("Seeded user '{}' with roles {}", username, roles);
    }
}
