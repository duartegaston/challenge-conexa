package com.conexa.challenge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI con esquema Bearer JWT. No fija {@code servers}: springdoc toma la URL del request,
 * así "Try it out" funciona igual en local y en el deploy.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI challengeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Challenge Conexa API")
                        .description(
                                """
                                API REST que expone el catálogo Star Wars \
                                ([swapi.tech](https://www.swapi.tech)) con autenticación JWT Bearer, \
                                roles USER/ADMIN y caché Caffeine. \
                                Flujo: `POST /api/v1/auth/login` (o `/register`) → **Authorize** con el \
                                `accessToken` → probar los recursos. Los errores son `application/problem+json`.
                                """)
                        .version("v1")
                        .contact(new Contact().name("Gaston Duarte").url("https://github.com/duartegaston"))
                        .license(new License().name("Demo / Challenge")))
                .tags(List.of(
                        new Tag().name("Auth").description("Registro, login JWT y perfil"),
                        new Tag().name("People").description("Personajes SWAPI"),
                        new Tag().name("Films").description("Películas SWAPI"),
                        new Tag().name("Starships").description("Naves SWAPI"),
                        new Tag().name("Vehicles").description("Vehículos SWAPI"),
                        new Tag().name("Admin").description("Operaciones administrativas (rol ADMIN)")))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Pegar solo el JWT (sin el prefijo Bearer)")));
    }
}
