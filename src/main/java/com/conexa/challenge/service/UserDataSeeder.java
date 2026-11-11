package com.conexa.challenge.service;

import com.conexa.challenge.config.SeedProperties;
import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import com.conexa.challenge.repository.UserRepository;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Siembra usuarios de demo al arranque cuando aún no existen.
 */
@Component
@RequiredArgsConstructor
public class UserDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataSeeder.class);

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
        UserEntity user = new UserEntity(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(rawPassword),
                roles,
                true,
                Instant.now());
        userRepository.save(user);
        log.info("Seeded user '{}' with roles {}", username, roles);
    }
}
