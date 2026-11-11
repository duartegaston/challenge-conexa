# Challenge Conexa — API Star Wars (SWAPI)

API REST en **Java 21 / Spring Boot** que integra [SWAPI](https://www.swapi.tech/) con autenticación JWT + roles, UI de demo con estética Star Wars, OpenAPI, tests y path de deploy en host gratuito.

> **Estado actual:** Fase 7 — deploy Fly.io (Docker + H2 file). URL pública: pendiente de `fly deploy` con `FLY_API_TOKEN` / cuenta de Gaston. SonarCloud sigue opcional (`SONAR_TOKEN`).

Repo: https://github.com/duartegaston/challenge-conexa  
Autor: [duartegaston](https://github.com/duartegaston) (Gaston Duarte)

---

## Cómo correr (local)

### Requisitos
- Java 21+
- Maven 3.9+ (o el wrapper `./mvnw`)

### Arranque (perfil `local`, H2 en memoria + Flyway)

```bash
./mvnw spring-boot:run
```

| Recurso | URL |
|---------|-----|
| UI login | http://localhost:8080/ |
| UI catálogo | http://localhost:8080/app.html |
| Health | http://localhost:8080/actuator/health |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Perfil por defecto: `local` (H2 en memoria). Prod (Fly) usa **H2 file** en `/data` — ver [Deploy](#deploy) y ADR-05. Variables: `.env.example`.

---

## Credenciales seed

Usuarios sembrados al arranque si no existen (`UserDataSeeder`). Overrides: `SEED_USER_PASSWORD`, `SEED_ADMIN_PASSWORD`.

| Username | Password | Roles |
|----------|----------|-------|
| `user` | `user123` | USER |
| `admin` | `admin123` | USER, ADMIN |

Solo para demo. En prod cambiar `JWT_SECRET` y passwords seed.

### Login (curl)

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"user123"}'
```

Respuesta: `{ "accessToken", "tokenType": "Bearer", "expiresIn", "roles" }`.

### Recursos (curl)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"user123"}' | jq -r .accessToken)

curl -s "http://localhost:8080/api/v1/people?page=1&limit=5&name=sky" \
  -H "Authorization: Bearer $TOKEN"

curl -s http://localhost:8080/api/v1/people/1 \
  -H "Authorization: Bearer $TOKEN"
```

Listado: `{ "content", "page", "limit", "total", "hasNext" }`.  
Filtro `name`/`title` → query remoto en SWAPI (`name` o `title`). Sin token → `401`.

---

## Endpoints principales

Base: `/api/v1`.

| Método | Path | Auth | Notas |
|--------|------|------|-------|
| POST | `/api/v1/auth/login` | Público | Emite JWT |
| GET | `/api/v1/auth/me` | JWT | Perfil |
| GET | `/api/v1/people` | JWT + USER | `page`, `limit`, `name` |
| GET | `/api/v1/people/{id}` | JWT + USER | Detalle |
| GET | `/api/v1/films` | JWT + USER | `page`, `limit`, `name`/`title` |
| GET | `/api/v1/films/{id}` | JWT + USER | Detalle |
| GET | `/api/v1/starships` | JWT + USER | `page`, `limit`, `name` |
| GET | `/api/v1/starships/{id}` | JWT + USER | Detalle |
| GET | `/api/v1/vehicles` | JWT + USER | `page`, `limit`, `name` |
| GET | `/api/v1/vehicles/{id}` | JWT + USER | Detalle |
| GET | `/actuator/health` | Público | Liveness |
| GET | `/swagger-ui.html` | Público | OpenAPI + Authorize Bearer |

---

## Decisiones

Cada fila resume una decisión cerrada (ADR-style: Decision / Why).

| Decisión | Por qué |
|----------|---------|
| **REST API + UI demo mínima con look Star Wars** | El brief pide REST; la UI demuestra auth + listados; el estilo SW ancla el dominio. |
| **JWT Bearer + Spring Security + roles; seed al startup; sin registro público** | Stack a demostrar; setup reproducible; menor superficie. |
| **Sin SDK Star Wars; Spring `RestClient` → https://www.swapi.tech + timeouts + Caffeine** | SWAPI es la fuente de verdad; RestClient nativo del stack MVC; Caffeine mitiga latencia; no dependencias de terceros de dominio SW. |
| **Controllers sin `ResponseEntity` envolviendo cada respuesta** | Controllers thin: el tipo de retorno + `@ResponseStatus` / defaults MVC fijan el status; errores vía `@RestControllerAdvice`. `ResponseEntity` solo si hiciera falta variar status/headers por rama. |
| **Paginación normalizada (`PageResponse`)** | Contrato estable hacia la UI; page/limit reenviados a SWAPI. |
| **H2 en prod (Fly) solo para `UserEntity` (auth/seed) + Flyway** | El challenge no exige persistir SWAPI; free deploy → sin Postgres managed de pago. |
| **OpenAPI obligatorio; README ES; código EN; Javadoc ES breve** | Contrato vivo + README alineado al challenge. |
| **Maven + Java 21 + capas clásicas** | Packages `config`/`controller`/`service`/`model`/`repository`/`security`/`client`/`exception`. |
| **SonarCloud vía GitHub Action (plus)** | Calidad visible cuando hay `SONAR_TOKEN`; no bloquea el MVP. |
| **Deploy Fly.io + Docker multi-stage (Java 21)** | Free/hobby path; `JWT_SECRET` vía Fly secrets. |
| **Cursor + SDD (plan → fases → ejecución)** | Alcance controlado y entregas incrementales. |
| **Autoría Git = `duartegaston` (regla dura)** | Historial del candidato; nunca identidad Cursor/bot. |
| **UI demo estética Star Wars** | Dominio reconocible; no admin genérico. |

### ADR (Decision / Why)

#### ADR-01 — Forma de la aplicación
- **Decision:** API REST + UI de demo mínima con estética Star Wars. Sin GraphQL.
- **Why:** Cumple el brief REST y deja claro el valor del backend.

#### ADR-02 — Autenticación y autorización
- **Decision:** JWT Bearer + Spring Security + roles; seed al startup; sin registro público.
- **Why:** Stack a demostrar; setup reproducible; menor superficie.

#### ADR-03 — Integración SWAPI (RestClient, sin SDK)
- **Decision:** No se usa librería/SDK específica de Star Wars. El acceso es **Spring `RestClient`** contra **https://www.swapi.tech/api**, con timeouts (`swapi.connect-timeout-ms` / `swapi.read-timeout-ms`) y **caché Caffeine** in-memory (`swapi-lists`, `swapi-details`, TTL configurable). El catálogo **no** se guarda en DB.
- **Why:** SWAPI es la fuente de verdad; RestClient es el cliente HTTP idiomático en Spring MVC 6; evita dependencias opacas de dominio; Caffeine reduce latencia y presión sobre el origen. Fallos → `SwapiUnavailableException` → HTTP **502**.

#### ADR-04 — Paginación
- **Decision:** Forward de `page`/`limit` a SWAPI; respuesta `PageResponse` normalizada.
- **Why:** Contrato transparente para la UI de demo.

#### ADR-05 — Persistencia (H2 en prod)
- **Decision:** **H2** para `UserEntity` (auth/seed) en **local, test y prod**. En prod (Fly): JDBC file `jdbc:h2:file:/data/challenge` (`H2_FILE_PATH`). Flyway en todos los perfiles. El driver PostgreSQL puede quedar en el POM por compatibilidad local opcional, pero **prod en Fly no usa Postgres managed** (ni Neon ni Fly Postgres de pago).
- **Why:** El challenge **no requiere persistir el catálogo SWAPI** (solo identidad). Un deploy gratuito no justifica Postgres managed. Sin volumen Fly montado en `/data`, el archivo H2 vive en el disco **efímero** del contenedor (se pierde en reinicios/redeploys; el seed recrea `user`/`admin` si la DB está vacía). Con volumen opcional (`fly volumes create` + `[[mounts]]` en `fly.toml`) los users sobreviven reinicios.

#### ADR-06 — Controllers y `ResponseEntity`
- **Decision:** Los controllers **devuelven DTOs / records directamente** (p. ej. `LoginResponse`, `PageResponse<SwapiItem>`), no envuelven cada método en `ResponseEntity<>`.
- **Why:** Mantienen controllers thin: el status HTTP exitoso sale de convenciones MVC (`200` por defecto, o `@ResponseStatus`). Errores de dominio/validación los traduce `GlobalExceptionHandler` (`@RestControllerAdvice`) a `ErrorResponse` con el status correcto (401/404/502/…). Se reservaría `ResponseEntity` para casos que necesiten **variar status o headers por rama** en el mismo método; hoy no hace falta.

#### ADR-07 — Documentación e idioma
- **Decision:** OpenAPI/Swagger obligatorio; README en español; código en inglés; Javadoc breve en español en clases/métodos clave.
- **Why:** Un evaluador entiende el sistema con README + swagger-ui.

#### ADR-08 — Capas, calidad y deploy
- **Decision:** Capas clásicas; SonarCloud opcional en CI; deploy en **Fly.io** con `Dockerfile` multi-stage y `fly.toml` (`internal_port` 8080).
- **Why:** Estructura familiar; calidad visible; host gratuito/hobby alineado al challenge.

#### ADR-09 — Metodología Cursor / SDD
- **Decision:** Desarrollar con Cursor en estilo SDD: plan, fases con DoD, ejecución fase a fase.
- **Why:** Alcance controlado y evidencia del proceso.

#### ADR-10 — Autoría Git
- **Decision:** Commits como GitHub user `duartegaston` (`Gaston Duarte <duartegaston07@gmail.com>`). Prohibido Cursor bot / agentes.
- **Why:** El historial debe reflejar al candidato.

#### ADR-11 — Estética UI demo
- **Decision:** UI con tipografía, colores y atmósfera Star Wars.
- **Why:** Dominio reconocible sin inflar el frontend.

---

## Metodología: Cursor + SDD

1. **Plan** — alcance, ADRs y fases.  
2. **Fases** — checklist + Definition of Done.  
3. **Ejecución** — una fase a la vez (desde Fase 2, commits directos a `main` salvo pedido de PR).

Cursor es copiloto de implementación; **no** autor de los commits.

---

## Arquitectura en capas

```
com.conexa.challenge
├── config       # Security, OpenAPI, Cache, properties
├── controller   # Auth + resources REST (DTOs directos)
├── service      # Auth, catalog, people/films/starships/vehicles, seed
├── client       # SwapiClient (RestClient), mapper, properties
├── security     # JWT filter, JwtService, UserDetails
├── model        # UserEntity, Role + dto/
├── repository   # UserRepository
└── exception    # @RestControllerAdvice + excepciones de dominio
```

UI estática: `src/main/resources/static/`.

Flujo: HTTP → JWT filter → Controller → Service → SwapiClient / UserRepository → DTO → respuesta.

---

## SWAPI + caché

- **Origen:** https://www.swapi.tech/api (configurable `swapi.base-url`)  
- **Cliente:** Spring `RestClient` (sin SDK Star Wars)  
- **Timeouts:** `swapi.connect-timeout-ms`, `swapi.read-timeout-ms`  
- **Caché:** Caffeine `swapi-lists` / `swapi-details` (TTL `cache.swapi.ttl-minutes`, default 5)  
- **Errores:** origen caído → **502 Bad Gateway**  
- Tests: WireMock / mocks — **nunca** red real a SWAPI en CI  

---

## UI demo (look Star Wars)

1. http://localhost:8080/ — login (JWT en `sessionStorage`)  
2. http://localhost:8080/app.html — tabs, filtro, paginación, detalle  
3. 401 → vuelve al login  

Tipografía Orbitron / Exo 2; fondo espacial; acentos oro crawl + azul sable.

---

## OpenAPI / Swagger

- UI: http://localhost:8080/swagger-ui.html  
- Spec: http://localhost:8080/v3/api-docs  
- Security scheme `bearerAuth` (JWT). Login es público; recursos llevan tag + `@SecurityRequirement`.  
- Flujo sugerido: `POST /api/v1/auth/login` → Authorize con el `accessToken` → probar `GET /api/v1/people`.

---

## Tests

```bash
./mvnw clean test
```

| Tipo | Ejemplos |
|------|----------|
| Unit | `JwtServiceTest`, `AuthServiceTest`, `PeopleServiceTest`, `CatalogServiceTest`, `SwapiMapperTest` |
| Slice | `@WebMvcTest` `PeopleControllerWebMvcTest`; `@DataJpaTest` `UserRepositoryTest` |
| Integration | `@SpringBootTest` + H2: auth, login→JWT→people, WireMock SWAPI, resources |

---

## Calidad / SonarCloud

Hay workflow CI en `.github/workflows/ci.yml`:

1. Job **test** — siempre corre `./mvnw clean test`.  
2. Job **sonar** — solo si existe el secret **`SONAR_TOKEN`** en el repo.

### Cómo habilitar (Gaston)

1. Crear cuenta / org en [SonarCloud](https://sonarcloud.io) e importar `duartegaston/challenge-conexa`.  
2. Generar un token y añadirlo como secret de GitHub: `Settings → Secrets → Actions → SONAR_TOKEN`.  
3. Opcional: variables `SONAR_ORGANIZATION` y `SONAR_PROJECT_KEY` (defaults en el workflow / `sonar-project.properties`).  
4. Re-push a `main` o re-run del workflow.

**Estado actual:** Action + propiedades listas; **análisis bloqueado** hasta que exista `SONAR_TOKEN` (no se inventan secretos en el repo).

Análisis local (con token exportado):

```bash
export SONAR_TOKEN=...   # no commitear
./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.organization=duartegaston \
  -Dsonar.projectKey=duartegaston_challenge-conexa \
  -Dsonar.host.url=https://sonarcloud.io
```

---

## Deploy (Fly.io)

**Host:** [Fly.io](https://fly.io) · **DB prod:** H2 file (`/data/challenge`) · **sin** Postgres managed de pago.

Artefactos en el repo: `Dockerfile` (Maven Java 21 → JRE), `fly.toml`, perfil Spring `prod`.

### Variables de entorno

| Variable | Obligatorio | Notas |
|----------|-------------|--------|
| `SPRING_PROFILES_ACTIVE` | Sí | `prod` (ya en `fly.toml` `[env]`) |
| `JWT_SECRET` | Sí | Secret largo (≥32 chars). **Solo** vía `fly secrets set` |
| `PORT` | No | Fly inyecta `8080`; la app usa `server.port=${PORT:8080}` y `0.0.0.0` |
| `H2_FILE_PATH` | No | Default `/data/challenge` |
| `SEED_USER_PASSWORD` / `SEED_ADMIN_PASSWORD` | No | Overrides del seed |

### Comandos (Gaston)

Requisitos: [flyctl](https://fly.io/docs/hands-on/install-flyctl/) + login (`fly auth login` o `FLY_API_TOKEN`).

```bash
# 1) Crear app (si el nombre challenge-conexa está tomado, elegí otro y editá fly.toml)
fly launch --no-deploy --name challenge-conexa --region iad --copy-config
# o, si la app ya existe en fly.toml:
fly apps create challenge-conexa   # solo si aún no existe

# 2) Secret JWT (NO pegar el valor en chat / issues / commits)
fly secrets set JWT_SECRET="$(openssl rand -base64 48)"

# 3) (Opcional) Volumen 1 GB para persistir H2 entre reinicios
fly volumes create challenge_data --region iad --size 1
# Luego descomentar [[mounts]] en fly.toml

# 4) Deploy
fly deploy

# 5) Abrir / status
fly status
fly open
# URL típica: https://challenge-conexa.fly.dev
```

Smoke post-deploy:

```bash
BASE=https://challenge-conexa.fly.dev   # ajustar al hostname real

curl -s "$BASE/actuator/health"

TOKEN=$(curl -s -X POST "$BASE/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"user123"}' | jq -r .accessToken)

curl -s "$BASE/api/v1/people?page=1&limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

### URL pública

Aún **no desplegada desde este agente** (no había `FLY_API_TOKEN` en el entorno). Tras `fly deploy` la URL será:

`https://<app-name>.fly.dev` (p. ej. `https://challenge-conexa.fly.dev` si el nombre está libre).

### Límites free / hobby (esperables)

- **Cold start / auto-stop:** `min_machines_running = 0` → la máquina duerme; el primer request puede tardar varios segundos.
- **RAM:** VM 512 MB compartida (ver `fly.toml`).
- **H2 sin volumen:** datos de auth en disco efímero; reinicio/redeploy = DB vacía + re-seed.
- **H2 con volumen:** users persisten; el volumen puede no estar en el plan más restringido — crear solo si el plan lo permite.
- **Sin Postgres managed:** decisión consciente (ADR-05); SWAPI sigue siendo la fuente del catálogo.
- **Egress a SWAPI:** dependencia de red externa; 502 si swapi.tech cae.

### Smoke local con perfil prod

```bash
mkdir -p data
JWT_SECRET=local-prod-smoke-secret-at-least-32-chars \
SPRING_PROFILES_ACTIVE=prod \
H2_FILE_PATH=./data/challenge \
./mvnw spring-boot:run
```

---

## Autoría Git

**Regla dura:** commits como **`Gaston Duarte <duartegaston07@gmail.com>`** (`duartegaston`). Sin identidades Cursor/bot.

```bash
git log -1 --format='%an <%ae>'
```

---

## Posibles mejoras

- Refresh tokens / rotación de secretos JWT  
- Stale-if-error en caché SWAPI  
- Observabilidad (request id, métricas)  
- Rate limiting frente a SWAPI  
