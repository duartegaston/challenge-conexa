# Challenge Conexa — API Star Wars (SWAPI)

API REST en **Java 21 / Spring Boot** que integra [SWAPI](https://swapi.dev/), con autenticación JWT + roles, UI de demo con estética Star Wars, OpenAPI, tests y deploy en host gratuito.

> **Estado actual:** Fase 2 — SWAPI client + caché Caffeine. Auth JWT listo. Controllers de recursos y UI pendientes (Fases 3–4).

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

- App: http://localhost:8080  
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

---

## Endpoints principales

Base: `/api/v1`.

| Método | Path | Auth | Estado |
|--------|------|------|--------|
| POST | `/api/v1/auth/login` | Público | Disponible (Fase 1) |
| GET | `/api/v1/auth/me` | JWT | Disponible (Fase 1) |
| GET | `/api/v1/people` … | JWT + USER | Pendiente Fase 3 |
| GET | `/api/v1/films` … | JWT + USER | Pendiente Fase 3 |
| GET | `/api/v1/starships` … | JWT + USER | Pendiente Fase 3 |
| GET | `/api/v1/vehicles` … | JWT + USER | Pendiente Fase 3 |
| GET | `/actuator/health` | Público | Disponible |
| GET | `/swagger-ui.html` | Público | Disponible |

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
| **Maven + Java 21 + Spring Boot MVC en capas** | Stack del brief; patrón layered a demostrar. |
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
- **Decision:** PostgreSQL únicamente para `User` (auth); schema con Flyway; H2 en `local`/`test`.
- **Why:** Solo identidad/roles; el resto es proxy.

#### ADR-06 — Documentación e idioma
- **Decision:** OpenAPI obligatorio; README ES; código EN.
- **Why:** Contrato claro + README alineado al challenge.

#### ADR-07 — Calidad y deploy
- **Decision:** Sonar en CI como plus; deploy Render/Fly; Docker opcional; Maven/Java 21/Spring Boot MVC.
- **Why:** Job context y path de entrega visible.

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
├── config          # Security, OpenAPI, Cache, seed
├── auth            # login, JWT, UserDetails
├── user            # entidad User, repository, seed
├── swapi           # client HTTP, cache, mappers
├── people | films | starships | vehicles
├── common          # exception, dto, validation
└── web             # UI demo (estética Star Wars)
```

Flujo: HTTP → JWT filter → Controller → Service → SwapiClient/UserRepository → DTO → Response.

---

## SWAPI + caché

- Cliente: `RestClient` (`SwapiClient`) con timeouts `swapi.connect-timeout-ms` / `swapi.read-timeout-ms`.
- Caché: Caffeine in-memory (`swapi-lists`, `swapi-details`), TTL `cache.swapi.ttl-minutes` (default 5), max-size configurable.
- No se persiste el catálogo Star Wars en DB (solo usuarios).
- Fallo de SWAPI → `SwapiUnavailableException` → HTTP **502 Bad Gateway**.
- Controllers REST de people/films/starships/vehicles: Fase 3.

---

## UI demo (look Star Wars)

Pendiente — Fase 4. Login + listados paginados/filtrables con tipografía, colores y atmósfera inspirados en Star Wars (no admin genérico).

---

## Tests

```bash
./mvnw test
```

- Fase 1: login ok/fail; endpoint protegido `401`/`200` (`AuthIntegrationTest`).
- Fase 2: WireMock — listado OK, cache hit (1 sola llamada HTTP), SWAPI 5xx → excepción; handler → 502 (`SwapiClientTest`, `GlobalExceptionHandlerTest`).

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
