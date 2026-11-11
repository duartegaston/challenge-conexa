package com.conexa.challenge.service;

import com.conexa.challenge.exception.InvalidCredentialsException;
import com.conexa.challenge.model.Role;
import com.conexa.challenge.model.UserEntity;
import com.conexa.challenge.model.dto.LoginRequest;
import com.conexa.challenge.model.dto.LoginResponse;
import com.conexa.challenge.repository.UserRepository;
import com.conexa.challenge.security.JwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Orquesta el login: valida credenciales y emite JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Autentica al usuario y devuelve el access token.
     */
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserEntity user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(user.getUsername(), user.getRoles());
        List<String> roles = user.getRoles().stream().map(Role::name).sorted().toList();
        return new LoginResponse(token, "Bearer", jwtService.getExpiresInSeconds(), roles);
    }
}
