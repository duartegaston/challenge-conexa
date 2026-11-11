# Challenge Conexa — API Star Wars (SWAPI)

API REST en **Java 21 / Spring Boot** que integra [SWAPI](https://swapi.dev/), con autenticación JWT + roles, UI de demo con estética Star Wars, OpenAPI, tests y deploy en host gratuito.

> **Estado actual:** Fase 0 — scaffold. Aún no hay JWT, cliente SWAPI ni pantallas de UI.

Repo: https://github.com/duartegaston/challenge-conexa  
Autor: [duartegaston](https://github.com/duartegaston) (Gaston Duarte)

---

## Cómo correr (local)

### Requisitos
- Java 21+
- Maven 3.9+ (o usar el wrapper `./mvnw`)

### Arranque (perfil `local`, H2 en memoria)

```bash
./mvnw spring-boot:run
```

- App: http://localhost:8080  
- Health: http://localhost:8080/actuator/health  
- Swagger UI: http://localhost:8080/swagger-ui.html  

El perfil por defecto es `local` (H2). PostgreSQL se usa en `prod` (Fase 1+ / deploy).

Variables de entorno de ejemplo: ver `.env.example`.

---

## Credenciales seed

Pendiente — Fase 1 (usuarios sembrados al arranque).

---

## Endpoints principales

Base prevista: `/api/v1` (Fases 1–3).

| Método | Path | Auth | Estado |
|--------|------|------|--------|
| POST | `/api/v1/auth/login` | Público | Pendiente Fase 1 |
| GET | `/api/v1/people` … | JWT + USER | Pendiente Fase 3 |
| GET | `/api/v1/films` … | JWT + USER | Pendiente Fase 3 |
| GET | `/api/v1/starships` … | JWT + USER | Pendiente Fase 3 |
| GET | `/api/v1/vehicles` … | JWT + USER | Pendiente Fase 3 |
| GET | `/actuator/health` | Público | Disponible (Fase 0) |

---

## Decisiones

Cada fila resume una decisión cerrada del plan (ADR-style: Decision / Why).

| Decisión | Por qué |
|----------|---------|
| **REST API + UI demo mínima con look Star Wars** | El brief pide REST; la UI demuestra auth + listados; el estilo SW ancla el dominio sin convertir el frontend en el foco. |
| **JWT Bearer + Spring Security + roles; seed al startup; sin registro público** | Stack a demostrar; setup reproducible para el evaluador; menor superficie. |
| **Cliente HTTP vivo a SWAPI + caché Caffeine (TTL corto); no persistir catálogo** | SWAPI es la fuente de verdad; evita sync/ETL; Caffeine mitiga latencia. |
| **Paginación: forward de `page`/`limit` SWAPI; shape normalizado** | Sin store local no hay Pageable JPA; contrato transparente. |
| **PostgreSQL solo para usuarios/auth** | Solo se necesita identidad/roles; el resto es proxy a SWAPI. |
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
- **Decision:** PostgreSQL únicamente para `User` (auth).
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

Pendiente — Fase 2 (`RestClient` + Caffeine, TTL configurable).

---

## UI demo (look Star Wars)

Pendiente — Fase 4. Login + listados paginados/filtrables con tipografía, colores y atmósfera inspirados en Star Wars (no admin genérico).

---

## Tests

```bash
./mvnw test
```

Pendiente ampliar en Fases 1–5. En Fase 0: smoke `contextLoads`.

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

---

## Nota Fase 0

`SecurityConfig` deja `permitAll` de forma temporal para que la app arranque. Se reemplaza en Fase 1 con JWT + roles (deny-by-default).
