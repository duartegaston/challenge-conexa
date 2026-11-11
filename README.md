# Challenge Conexa — API Star Wars (SWAPI)

API REST en **Java 21 / Spring Boot** que integra [SWAPI](https://swapi.dev/), con autenticación JWT + roles, UI de demo con estética Star Wars, OpenAPI, tests y deploy en host gratuito.

> **Estado actual:** Fase 4 — UI demo Star Wars + API completa (auth, SWAPI client, resources).

Repo: https://github.com/duartegaston/challenge-conexa  
Autor: [duartegaston](https://github.com/duartegaston) (Gaston Duarte)

---

## Cómo correr (local)

### Requisitos
- Java 21+
- Maven 3.9+ (o usar el wrapper `./mvnw`)

### Arranque (perfil `local`, H2 en memoria + Flyway)

```bash
./mvnw spring-boot:run
```

- App / UI demo: http://localhost:8080/ (login) → http://localhost:8080/app.html  
- Health: http://localhost:8080/actuator/health  
- Swagger UI: http://localhost:8080/swagger-ui.html  

El perfil por defecto es `local` (H2). PostgreSQL se usa en `prod` (deploy).

Variables de entorno de ejemplo: ver `.env.example`.

---

## Credenciales seed

Usuarios sembrados al arranque si no existen (`UserDataSeeder`). Passwords configurables vía env (`SEED_USER_PASSWORD`, `SEED_ADMIN_PASSWORD`); defaults de demo:

| Username | Password | Roles |
|----------|----------|-------|
| `user` | `user123` | USER |
| `admin` | `admin123` | USER, ADMIN |

Solo para demo local. En prod, cambiar secretos JWT y passwords seed.

### Login (curl)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"user123"}'
```

Respuesta: `{ "accessToken", "tokenType": "Bearer", "expiresIn", "roles" }`.

### Endpoint protegido

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .accessToken)

curl -s http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

Sin token → `401 Unauthorized`.

### Recursos SWAPI (curl)

```bash
# Listado paginado + filtro por nombre (búsqueda remota SWAPI `search`)
curl -s "http://localhost:8080/api/v1/people?page=1&limit=5&name=sky" \
  -H "Authorization: Bearer $TOKEN"

# Detalle por id
curl -s http://localhost:8080/api/v1/people/1 \
  -H "Authorization: Bearer $TOKEN"

# Films aceptan `title` o `name` (title tiene prioridad)
curl -s "http://localhost:8080/api/v1/films?title=hope" \
  -H "Authorization: Bearer $TOKEN"
```

Respuesta de listado: `{ "content", "page", "limit", "total", "hasNext" }`.  
`limit` trunca la página actual (SWAPI entrega de a ~10). El filtro `name`/`title` se reenvía a SWAPI como `search` (no es filtro local).

---

## Endpoints principales

Base: `/api/v1`.

| Método | Path | Auth | Estado |
|--------|------|------|--------|
| POST | `/api/v1/auth/login` | Público | Disponible |
| GET | `/api/v1/auth/me` | JWT | Disponible |
| GET | `/api/v1/people` | JWT + USER | Disponible (`page`, `limit`, `name`) |
| GET | `/api/v1/people/{id}` | JWT + USER | Disponible |
| GET | `/api/v1/films` | JWT + USER | Disponible (`page`, `limit`, `name`/`title`) |
| GET | `/api/v1/films/{id}` | JWT + USER | Disponible |
| GET | `/api/v1/starships` | JWT + USER | Disponible (`page`, `limit`, `name`) |
| GET | `/api/v1/starships/{id}` | JWT + USER | Disponible |
| GET | `/api/v1/vehicles` | JWT + USER | Disponible (`page`, `limit`, `name`) |
| GET | `/api/v1/vehicles/{id}` | JWT + USER | Disponible |
| GET | `/actuator/health` | Público | Disponible |
| GET | `/swagger-ui.html` | Público | Disponible (Bearer JWT) |

---

## Decisiones

Cada fila resume una decisión cerrada del plan (ADR-style: Decision / Why).

| Decisión | Por qué |
|----------|---------|
| **REST API + UI demo mínima con look Star Wars** | El brief pide REST; la UI demuestra auth + listados; el estilo SW ancla el dominio sin convertir el frontend en el foco. |
| **JWT Bearer + Spring Security + roles; seed al startup; sin registro público** | Stack a demostrar; setup reproducible para el evaluador; menor superficie. |
| **Cliente HTTP vivo a SWAPI + caché Caffeine (TTL corto); no persistir catálogo** | SWAPI es la fuente de verdad; evita sync/ETL; Caffeine mitiga latencia. |
| **Paginación: forward de `page`/`limit` SWAPI; shape normalizado** | Sin store local no hay Pageable JPA; contrato transparente. |
| **PostgreSQL solo para usuarios/auth + Flyway** | Solo se necesita identidad/roles; el resto es proxy a SWAPI. |
| **OpenAPI/Swagger obligatorio; README en español; código en inglés** | Contrato vivo + README alineado al idioma del challenge. |
| **Maven + Java 21 + Spring Boot MVC en capas clásicas** | Stack del brief; packages por capa (`controller`/`service`/`model`/…) más legibles que por feature para este tamaño. |
| **Sonar en CI (plus); deploy Render/Fly; Docker opcional** | Calidad visible y path de entrega sin bloquear el MVP. |
| **Desarrollo con Cursor + metodología SDD (plan → fases → ejecución)** | Trazabilidad de alcance y entregas incrementales con DoD por fase. El README documenta cómo se usó Cursor. |
| **Autoría Git = GitHub `duartegaston` (regla dura)** | El historial debe atribuirse al candidato; nunca identidad Cursor/bot/agente. |
| **UI demo con estética Star Wars (tema, tipografía, colores, atmósfera)** | Refuerza el dominio SWAPI; no una UI admin genérica. |

### ADR (Decision / Why)

#### ADR-01 — Forma de la aplicación
- **Decision:** API REST + UI de demo mínima con estética Star Wars. Sin GraphQL.
- **Why:** Cumple el brief REST y deja claro el valor del backend.

#### ADR-02 — Autenticación y autorización
- **Decision:** JWT Bearer + Spring Security + roles; seed al startup; sin registro público.
- **Why:** Stack a demostrar; setup reproducible; menor superficie.

#### ADR-03 — Integración SWAPI
- **Decision:** Cliente HTTP vivo + caché Caffeine corta; no persistir catálogo.
- **Why:** Fuente de verdad externa; evita sync/ETL.

#### ADR-04 — Paginación
- **Decision:** Forward de `page`/`limit` SWAPI; respuesta normalizada.
- **Why:** Sin store local; cliente de demo simple.

#### ADR-05 — Persistencia
- **Decision:** PostgreSQL únicamente para `UserEntity` (auth); schema con Flyway; H2 en `local`/`test`.
- **Why:** Solo identidad/roles; el resto es proxy. El nombre `UserEntity` evita choque con `User` de Spring Security.

#### ADR-06 — Documentación e idioma
- **Decision:** OpenAPI obligatorio; README ES; código EN; Javadoc breve en español en clases clave.
- **Why:** Contrato claro + README alineado al challenge.

#### ADR-07 — Calidad y deploy
- **Decision:** Sonar en CI como plus; deploy Render/Fly; Docker opcional; Maven/Java 21/Spring Boot MVC en capas clásicas (`config`, `controller`, `service`, `model`, `repository`, `security`, `client`, `exception`).
- **Why:** Job context, path de entrega visible y estructura familiar para evaluadores.

#### ADR-08 — Metodología Cursor / SDD
- **Decision:** Desarrollar con Cursor en estilo SDD: generar el plan, partir en fases con DoD, ejecutar fase por fase.
- **Why:** Alcance controlado, PRs acotados y evidencia del proceso (no solo del resultado).

#### ADR-09 — Autoría Git
- **Decision:** Todos los commits se autoran como GitHub user `duartegaston`. Prohibido Cursor bot u otras identidades de agente.
- **Why:** El historial debe reflejar al candidato.

#### ADR-10 — Estética UI demo
- **Decision:** La UI puede y debe usar estilos inspirados en Star Wars.
- **Why:** Dominio reconocible sin inflar el alcance de frontend.

---

## Metodología: Cursor + SDD

Este proyecto se desarrolla con **Cursor** siguiendo un flujo tipo **SDD (Spec-/Plan-Driven Development)**:

1. **Plan** — documento vivo con alcance, ADRs, arquitectura y fases (`docs/plan` del workspace de trabajo).
2. **Fases** — cada fase tiene checklist y Definition of Done (scaffold → security → SWAPI → resources → UI → tests → docs → deploy).
3. **Ejecución** — una fase a la vez; PRs acotados; sin saltar a negocio antes de que el scaffold compile y arranque.

Cursor se usa como copiloto de implementación sobre ese plan (estructura, código, tests, README), no como autor de los commits.

---

## Arquitectura en capas

```
com.conexa.challenge
├── config       # Security, OpenAPI, Cache, properties
├── controller   # Auth + resources REST
├── service      # Auth, catalog, people/films/starships/vehicles, seed
├── client       # SwapiClient, mapper, properties
├── security     # JWT filter, JwtService, UserDetails
├── model        # UserEntity, Role + dto/
├── repository   # UserRepository
└── exception    # Handler + excepciones de dominio
```

UI estática (Fase 4): `src/main/resources/static/`.

Flujo: HTTP → JWT filter → Controller → Service → SwapiClient/UserRepository → DTO → Response.

---

## SWAPI + caché

- Cliente: `RestClient` (`SwapiClient`) con timeouts `swapi.connect-timeout-ms` / `swapi.read-timeout-ms`.
- Caché: Caffeine in-memory (`swapi-lists`, `swapi-details`), TTL `cache.swapi.ttl-minutes` (default 5), max-size configurable.
- No se persiste el catálogo Star Wars en DB (solo usuarios).
- Fallo de SWAPI → `SwapiUnavailableException` → HTTP **502 Bad Gateway**.
- Controllers REST: `/api/v1/{people|films|starships|vehicles}` (+ `/{id}`), JWT + rol `USER`.

---

## UI demo (look Star Wars)

Estática servida por Spring (`src/main/resources/static/`):

1. Abrir http://localhost:8080/  
2. Login con seed (`user` / `user123`) — el JWT queda en `sessionStorage`  
3. Catálogo en `/app.html`: tabs People / Films / Starships / Vehicles, filtro, paginación y detalle al click  
4. Sin token o 401 → vuelve al login  

Atmósfera: fondo espacial, tipografía Orbitron/Exo 2, acentos oro crawl + azul sable. No es un admin genérico.

---

## Tests

```bash
./mvnw clean test
```

Suite consolidada (Fase 5):

| Tipo | Ejemplos |
|------|----------|
| Unit | `JwtServiceTest`, `AuthServiceTest`, `PeopleServiceTest`, `CatalogServiceTest`, `SwapiMapperTest` |
| Slice | `@WebMvcTest` `PeopleControllerWebMvcTest`; `@DataJpaTest` `UserRepositoryTest` |
| Integration | `@SpringBootTest` + H2: `AuthIntegrationTest`, `AuthPeopleIntegrationTest` (login→JWT→people), `SwapiClientTest` (WireMock), resource tests |

Sin red real a SWAPI en CI. Cobertura mínima: auth, un resource (people) y cliente SWAPI.

---

## Calidad / Sonar

Pendiente — Fase 6 (plus).

---

## Deploy

Pendiente — Fase 7 (Render o Fly.io).

---

## Autoría Git

**Regla dura:** todos los commits de este repo se autoran como GitHub user **`duartegaston`** (`Gaston Duarte <duartegaston07@gmail.com>`). No se usan identidades Cursor, cloud agent ni bots.

Verificar: `git log -1 --format='%an <%ae>'`.

---

## Posibles mejoras

- Refresh tokens / rotación de secretos JWT  
- Stale-if-error en caché SWAPI  
- Observabilidad (request id, métricas)  
- Rate limiting frente a SWAPI  
