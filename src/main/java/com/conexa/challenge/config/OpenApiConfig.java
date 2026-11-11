package com.conexa.challenge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI con esquema Bearer JWT.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI challengeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Challenge Conexa API")
                        .description("Star Wars SWAPI proxy with JWT auth")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
