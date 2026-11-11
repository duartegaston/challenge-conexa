package com.conexa.challenge.user;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds demo users on startup when missing. Passwords come from config / env (see README).
 */
@Component
public class UserDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String userPassword;
    private final String adminPassword;

    public UserDataSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.seed.user-password}") String userPassword,
            @Value("${app.security.seed.admin-password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPassword = userPassword;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedIfAbsent("user", userPassword, Set.of(Role.USER));
        seedIfAbsent("admin", adminPassword, Set.of(Role.USER, Role.ADMIN));
    }

    private void seedIfAbsent(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User(
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
