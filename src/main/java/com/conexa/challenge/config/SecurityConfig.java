package com.conexa.challenge.config;

import com.conexa.challenge.model.Role;
import com.conexa.challenge.security.JwtAuthenticationFilter;
import com.conexa.challenge.security.JwtService;
import com.conexa.challenge.security.SecurityProblemHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad JWT stateless. Toda la autorización por rol vive acá, en un solo lugar:
 * <ul>
 *   <li>público: login, registro, UI estática, health y documentación;</li>
 *   <li>{@code /api/v1/admin/**}: ADMIN;</li>
 *   <li>{@code /api/v1/auth/me}: cualquier usuario autenticado;</li>
 *   <li>resto de {@code /api/v1/**}: USER.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/", "/index.html", "/app.html", "/error",
        "/actuator/health", "/actuator/health/**", "/actuator/info",
        "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtService jwtService, SecurityProblemHandler problemHandler) throws Exception {
        http
                // API stateless con Bearer en header: sin cookies ni sesión no hay vector CSRF.
                .csrf(AbstractHttpConfigurer::disable) // NOSONAR java:S4502 - JWT en header, sin sesión ni cookies
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register")
                        .permitAll()
                        .requestMatchers(PUBLIC_PATHS)
                        .permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/v1/auth/me")
                        .authenticated()
                        .requestMatchers("/api/v1/**")
                        .hasRole(Role.USER.name())
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(problemHandler).accessDeniedHandler(problemHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
