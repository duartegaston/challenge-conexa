# Contexto — challenge-conexa

API REST Java 21 / Spring Boot 3 que expone people, films, starships y vehicles de [SWAPI](https://www.swapi.tech/) con JWT, roles y una UI estática de demo. Repo de Gaston Duarte para el challenge de Conexa.

## Qué persiste y qué no

SWAPI es la fuente de verdad del catálogo. No se copia a la base. H2 guarda solo `UserEntity` (login y roles), con Flyway (`ddl-auto: validate`).

| Perfil | Base |
|--------|------|
| `local` (default) | H2 en memoria |
| `test` | H2 en memoria |
| `prod` (Fly) | H2 en archivo, `/data` |

No hay driver de PostgreSQL en el build. Local, test y prod usan H2. Local se levanta con `./mvnw spring-boot:run`.

## Capas

```
com.conexa.challenge
├── config       Security, OpenAPI, Cache, properties
├── controller   Auth + recursos; devuelven DTOs
├── service      Auth, CatalogService, un servicio por recurso, seed
├── client       RestClient, SwapiMapper, SwapiResource
├── security     Filtro JWT, JwtService, UserDetails
├── model        UserEntity, Role, dto/
├── repository   UserRepository
└── exception    @RestControllerAdvice
```

`CatalogService` centraliza page/limit/search. People, starships y vehicles delegan. Films prioriza `title` sobre `name`.

## HTTP

Base `/api/v1`. Login público. El resto de la API pide `hasRole('USER')`. Listados responden `PageResponse` (`content`, `page`, `limit`, `total`, `hasNext`). Errores: `ErrorResponse` vía advice (400, 401, 403, 404, 502). OpenAPI en `/swagger-ui.html` con scheme `bearerAuth`.

Caché Caffeine `swapi-lists` y `swapi-details` (TTL default 5 minutos). Nombres en `CacheConfig`. Los tests no llaman a SWAPI real: WireMock o mocks.

## Auth de demo

Seed al arranque si el usuario no existe: `user` / `user123` (USER) y `admin` / `admin123` (USER, ADMIN). Ningún endpoint exige ADMIN. El secreto JWT de `application.yml` es un fallback de local. Con el perfil `prod`, `ProdJwtSecretGuard` corta el arranque si `JWT_SECRET` falta o es ese default.

## Deploy

Docker multi-stage, Java 21, perfil `prod`. Fly.io, una VM shared de 512 MB, máquina que puede apagarse. El volumen H2 en `fly.toml` está comentado: sin volumen, el seed se recrea si el disco es nuevo.
