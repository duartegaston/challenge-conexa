# Challenge Conexa — API Star Wars (SWAPI)

API REST en **Java 21 / Spring Boot** que integra [SWAPI](https://www.swapi.tech/) con autenticación JWT + roles, UI de demo con estética Star Wars, OpenAPI, tests y path de deploy en host gratuito.

Repo: https://github.com/duartegaston/challenge-conexa  
Autor: [duartegaston](https://github.com/duartegaston) (Gaston Duarte)

---

## Deploy (Fly.io)

**URL:** https://challenge-conexa.fly.dev

Está en el plan free de Fly.io: la máquina puede apagarse por inactividad (**cold start**). El primer request puede tardar varios segundos en responder.

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

Perfil por defecto: `local` (H2 en memoria). Prod (Fly) usa **H2 file** en `/data`. Variables: `.env.example`.

---

## Credenciales

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

Cada fila resume una decisión tomada.

| Decisión                                                                                         | Por qué                                                                                                                                                                                              |
| ------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **REST API**                                                                                     | REST API, Simplicidad y menor sobrecarga.                                                                                                                                                            |
| **JWT Bearer + Spring Security + roles.**                                                        |                                                                                                                                                                                                      |
| **Spring** `RestClient` **→ https://www.swapi.tech + timeouts + Caffeine**                       | SWAPI es la fuente de verdad; RestClient nativo del stack MVC; Caffeine mitiga latencia; no dependencias de terceros de dominio SW.                                                                  |
| **Controllers sin** `ResponseEntity` **envolviendo cada respuesta**                              | Controllers thin: el tipo de retorno + `@ResponseStatus` / defaults MVC fijan el status; errores vía `@RestControllerAdvice`. `ResponseEntity` solo si hiciera falta variar status/headers por rama. |
| **Paginación normalizada (**`PageResponse`**)**                                                  | Contrato estable hacia la UI; page/limit reenviados a SWAPI.                                                                                                                                         |
| **H2 en prod (Fly) solo para** `UserEntity` **(auth/seed) + Flyway en caso de usar PostgreSQL.** | El challenge no exige persistir datos de SWAPI → sin Postgres para menor complejidad. Con H2 alcanza en este caso.                                                                                   |
| **Maven + Java 21 + capas clásicas MVC**                                                         | Packages `config`/`controller`/`service`/`model`/`repository`/`security`/`client`/`exception`.                                                                                                       |
| **SonarCloud vía GitHub Action**                                                                 | Calidad visible cuando hay `SONAR_TOKEN`; no bloquea el MVP.                                                                                                                                         |
| **Deploy Fly.io + Docker multi-stage (Java 21)**                                                 | Free/hobby path; `JWT_SECRET` vía Fly secrets.                                                                                                                                                       |
| **Cursor + SDD (plan → fases → ejecución)**                                                      | Alcance controlado y entregas incrementales.                                                                                                                                     

---

## Metodología: Cursor + SDD

Utilice Cursor para el desarrollo, con una metodologia Spec Driven Development. Primero le di el contexto con todas las especificaciones, luego genere un plan con distintas fases de implementacion puliendo detalles y tomando distintas deciciones. Finalmente luego de implementar cada fase realice una review de lo desarrollado y por ultimo la ejecucion y pruebas.

1. **Plan** — alcance, ADRs y fases.
2. **Fases** — checklist + Definition of Done.
3. **Ejecución** — una fase a la vez.

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
2. Job **sonar**.

---

## Posibles mejoras a futuro

- Refresh tokens / rotación de secretos JWT  
- Stale-if-error en caché SWAPI  
- Observabilidad (request id, métricas)  
- Rate limiting frente a SWAPI
