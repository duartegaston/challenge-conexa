package com.conexa.challenge.service;

import com.conexa.challenge.exception.InvalidCredentialsException;
import com.conexa.challenge.exception.UsernameAlreadyExistsException;
import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import com.conexa.challenge.model.dto.LoginRequest;
import com.conexa.challenge.model.dto.LoginResponse;
import com.conexa.challenge.model.dto.MeResponse;
import com.conexa.challenge.model.dto.RegisterRequest;
import com.conexa.challenge.repository.UserRepository;
import com.conexa.challenge.security.JwtService;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Registro de usuarios, login (valida credenciales y emite JWT) y perfil del usuario autenticado.
 * Los usernames no distinguen mayúsculas: se normalizan a minúsculas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalize(request.username()), request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }

        Set<Role> roles = rolesOf(authentication);
        String token = jwtService.generateToken(authentication.getName(), roles);
        return new LoginResponse(token, TOKEN_TYPE, jwtService.expiresInSeconds(), roleNames(roles));
    }

    /**
     * Crea un usuario con rol USER.
     *
     * @throws UsernameAlreadyExistsException si el username ya está tomado
     */
    public MeResponse register(RegisterRequest request) {
        String username = normalize(request.username());
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        try {
            UserEntity user = userRepository.saveAndFlush(
                    new UserEntity(username, passwordEncoder.encode(request.password()), Set.of(Role.USER)));
            log.info("Registered user '{}'", user.getUsername());
            return new MeResponse(user.getUsername(), roleNames(user.getRoles()));
        } catch (DataIntegrityViolationException ex) {
            // Dos registros simultáneos del mismo username: gana el constraint único de la base.
            throw new UsernameAlreadyExistsException(username);
        }
    }

    public MeResponse me(Authentication authentication) {
        return new MeResponse(authentication.getName(), roleNames(rolesOf(authentication)));
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private static Set<Role> rolesOf(Authentication authentication) {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            Role.parse(authority.getAuthority()).ifPresent(roles::add);
        }
        return roles;
    }

    private static List<String> roleNames(Set<Role> roles) {
        return roles.stream().map(Role::name).sorted().toList();
    }
}
