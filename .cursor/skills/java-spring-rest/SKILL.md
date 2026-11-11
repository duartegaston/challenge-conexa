---
name: java-spring-rest
description: >-
  Applies the Java 21, Spring Boot, and REST conventions already used in
  challenge-conexa. Use when adding or changing controllers, services,
  SwapiClient, security, DTOs, Flyway, or API tests in this repo.
---

# Java, Spring y REST de este repo

Seguir lo que el código ya hace. No introducir otro estilo de capas, otro cliente HTTP ni otro formato de error.

## Al cambiar código

1. Ubicar la capa correcta. El controller no llama a SWAPI; el servicio de recurso delega en `CatalogService` salvo una regla propia (films: `title` antes que `name`).
2. Reutilizar `PageResponse`, `SwapiItem` y `ErrorResponse`. No crear un sobre genérico nuevo.
3. Mapear fallos de dominio a las excepciones que ya existen: `InvalidCredentialsException`, `ResourceNotFoundException`, `SwapiUnavailableException`.
4. Si el cambio toca listados o detalles SWAPI, usar los nombres de `CacheConfig` en `@Cacheable`.
5. Cubrir el cambio con el mismo tipo de test que el vecino: unit del servicio o mapper, `@WebMvcTest` si es contrato del controller, `@SpringBootTest` solo si hace falta H2 o el filtro JWT. Mockear `SwapiClient` o usar WireMock. Nunca `https://www.swapi.tech` en un test.

## Contrato

- Base `/api/v1`. Auth: `POST /auth/login` público, `GET /auth/me` con JWT.
- Catálogo: `GET /{people|films|starships|vehicles}` y `GET /{recurso}/{id}`.
- Lista feliz: `{ content, page, limit, total, hasNext }`.
- Roles en el token con prefijo `ROLE_` en el filtro. Los recursos piden `USER`. `ADMIN` se siembra y no autoriza endpoints distintos.

## Qué no agregar

- Driver PostgreSQL, sesión de servidor, `ResponseEntity` en el caso feliz, SDK de Star Wars, ni persistencia del catálogo.
- Secretos reales en `application.yml` o en el README. Overrides por env: `JWT_SECRET`, `SEED_USER_PASSWORD`, `SEED_ADMIN_PASSWORD`.
