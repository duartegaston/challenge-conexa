package com.conexa.challenge.auth.service;

import com.conexa.challenge.auth.dto.LoginRequest;
import com.conexa.challenge.auth.dto.LoginResponse;
import com.conexa.challenge.user.Role;
import com.conexa.challenge.user.User;
import com.conexa.challenge.user.UserRepository;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(user.getUsername(), user.getRoles());
        List<String> roles = user.getRoles().stream().map(Role::name).sorted().toList();
        return new LoginResponse(token, "Bearer", jwtService.getExpiresInSeconds(), roles);
    }
}
