package com.conexa.challenge.controller;

import com.conexa.challenge.config.OpenApiConfig;
import com.conexa.challenge.model.dto.LoginRequest;
import com.conexa.challenge.model.dto.LoginResponse;
import com.conexa.challenge.model.dto.MeResponse;
import com.conexa.challenge.model.dto.RegisterRequest;
import com.conexa.challenge.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro, login y perfil.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Login y emisión de JWT",
            description = "Público. Devuelve `{ accessToken, tokenType, expiresIn, roles }`.")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(
            summary = "Registro de usuario",
            description = "Público. Crea un usuario con rol USER. 409 si el username ya existe.")
    public MeResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Usuario autenticado actual")
    public MeResponse me(Authentication authentication) {
        return authService.me(authentication);
    }
}
