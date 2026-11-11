package com.conexa.challenge.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI con esquema Bearer JWT y tags por área.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI challengeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Challenge Conexa API")
                        .description(
                                """
                                API REST Java 21 / Spring Boot que proxya el catálogo Star Wars \
                                ([swapi.tech](https://www.swapi.tech)) con autenticación JWT Bearer, \
                                roles USER/ADMIN y caché Caffeine. \
                                Usar **Authorize** con el `accessToken` de `POST /api/v1/auth/login`.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Gaston Duarte")
                                .url("https://github.com/duartegaston"))
                        .license(new License().name("Demo / Challenge")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .tags(List.of(
                        new Tag().name("Auth").description("Login JWT y perfil"),
                        new Tag().name("People").description("Personajes SWAPI"),
                        new Tag().name("Films").description("Películas SWAPI"),
                        new Tag().name("Starships").description("Naves SWAPI"),
                        new Tag().name("Vehicles").description("Vehículos SWAPI")))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Pegar solo el JWT (sin prefijo Bearer)")));
    }
}
