# Capital Call Tracker — Backend

Kotlin / Spring Boot 3 API for the Alternative Capital Partners Capital Call Tracker.

## Stack

- Kotlin 1.9, Spring Boot 3.3, Java 21
- `spring-boot-starter-web`, `-security`, `-oauth2-resource-server`, `-data-jpa`, `-validation`, `-actuator`
- `kotlinx-coroutines-core` / `-reactor` for the async notice-generation feature
- PostgreSQL (via Docker) for local development
- JUnit 5 + MockK + springmockk for tests

## Prerequisites

- JDK 21
- No local Gradle install needed — use the wrapper (`./gradlew`, `gradlew.bat`)
- Docker, for running PostgreSQL locally (see below)

## Configuration & secrets

Database credentials and Azure IDs are **not** committed to the repo. `application.yml`
only references env var placeholders (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`,
`${AZURE_TENANT_ID}`, `${AZURE_API_CLIENT_ID}`) — no real values.

For local dev, copy the template and fill in real values:

```bash
cp .env.example .env
```

`.env` is gitignored and never pushed. `./gradlew bootRun` and `./gradlew test` load it
automatically (see the `dotenv` block in `build.gradle.kts`) and export its keys as
process environment variables for that run — nothing is written back into
`application.yml`. If `.env` is missing (e.g. in CI), the loader is a no-op.

For CI/staging/production, set the real values as actual environment variables on the
platform (CI secrets, container/orchestrator config, etc.) rather than shipping a
`.env` file — `.env` is a local-dev convenience only.

The DB values have no defaults, so the app **won't boot** until `DB_URL` / `DB_USERNAME`
/ `DB_PASSWORD` are set — that's intentional; a silent fallback to the wrong database is
worse than a fast failure.

## Database (PostgreSQL via Docker)

The app connects to PostgreSQL using `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` (see
`.env.example` and the Configuration section above). Start a local container with plain
`docker run` — no Dockerfile or compose file needed:

```bash
docker run --name capitalcall-postgres \
  -e POSTGRES_DB=capitalcall \
  -e POSTGRES_USER=capitalcall \
  -e POSTGRES_PASSWORD=capitalcall \
  -p 5432:5432 \
  -v capitalcall-postgres-data:/var/lib/postgresql/data \
  -d postgres:16
```

- `-v capitalcall-postgres-data:/var/lib/postgresql/data` persists data in a named
  volume across container restarts.
- The `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` above must match
  `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` in your `.env` file — the defaults in
  `.env.example` already match this command, so they work out of the box.

Day-to-day container management:

```bash
docker stop capitalcall-postgres     # stop
docker start capitalcall-postgres    # start again (data persists)
docker logs -f capitalcall-postgres  # tail logs
docker rm -f capitalcall-postgres    # remove container (volume survives)
docker volume rm capitalcall-postgres-data  # wipe data
```

Hibernate's `ddl-auto: update` creates/updates the schema automatically on startup,
so no manual migration step is needed for local development.

## Entra ID setup (do this first)

This API validates Microsoft Entra ID access tokens. Nothing here works against real
tokens until you complete the app registration steps below (see project spec Part 0
for the full walkthrough):

1. **Register the API app** (`CapitalCall-API`, single tenant, no redirect URI).
   - Note the **Directory (tenant) ID** and **Application (client) ID**.
   - Expose an API → default Application ID URI (`api://{api-client-id}`) → add scope
     `access_as_user`.
   - App roles → create `FundManager` and `Investor` (value must match exactly — the
     backend maps the `roles` claim directly to `ROLE_FundManager` / `ROLE_Investor`).
2. **Register the SPA app** (`CapitalCall-Web`, redirect URI `http://localhost:4200`),
   grant it delegated `access_as_user` permission with admin consent.
3. **Assign your own account** to both app roles under Enterprise applications →
   CapitalCall-API → Users and groups, so you can test both views.
4. Sign in once, decode your access token at [jwt.ms](https://jwt.ms), and copy the
   `oid` claim.

Set the following in your `.env` file (see Configuration & secrets above) before running
with real auth:

```
AZURE_TENANT_ID=<directory-tenant-id>
AZURE_API_CLIENT_ID=<capitalcall-api-application-client-id>
```

Without them the app still boots against the `.env.example` placeholders (JWKS lookup is
lazy, only happens on first token validation), but every request will be rejected with
an invalid-token error — this is expected until they're set to real values.

### Seeding your own `oid`

`DemoDataSeeder` seeds one investor with `entraObjectId = "REPLACE-WITH-YOUR-OID"`.
Replace that placeholder in
[`DemoDataSeeder.kt`](src/main/kotlin/com/swissre/capitalcall/config/DemoDataSeeder.kt)
with the `oid` you copied from jwt.ms so `GET /api/capital-calls/mine` returns data
for your signed-in account.

## Security model

- Access tokens are requested with scope `api://{api-client-id}/access_as_user`.
- The JWT decoder validates signature + issuer against the tenant, **and** audience
  against the API's client ID via a custom `AudienceValidator` — Spring's resource
  server support does not do the audience check for you.
- Roles arrive in the `roles` claim (not `scp`, not `groups`) and are mapped to
  `ROLE_FundManager` / `ROLE_Investor` Spring Security authorities by
  `RolesClaimJwtAuthenticationConverter`.
- Every controller method is annotated with `@PreAuthorize` — the filter chain alone
  only enforces "is authenticated", never role distinctions.
- `/api/capital-calls/mine` is scoped by the `oid` claim read server-side from the
  validated JWT — never by a client-supplied investor ID. Any frontend role-based UI
  hiding is cosmetic only; this backend is the actual security boundary.

## Localization

Every request should include an `Accept-Language: en` or `Accept-Language: sk` header.
`AcceptHeaderLocaleResolver` resolves the locale from that header (default `en`); both
domain error messages and Bean Validation field errors resolve from the same
`messages.properties` / `messages_sk.properties` bundle via a shared `MessageSource`.
The frontend must display `message` / `fieldErrors` verbatim — it does not translate
API error content.

## Running locally

Make sure the PostgreSQL container (above) is running first, then:

```bash
./gradlew bootRun
```

- API: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health` — permitted without auth.

## Tests

```bash
./gradlew test
```

## Swapping PostgreSQL for Azure SQL

Moving to Azure SQL is a datasource/driver + `application.yml` change only:

- Replace the `org.postgresql:postgresql` runtime dependency with the MS SQL JDBC driver.
- Update `spring.datasource.url` / `driver-class-name` / credentials.

No application code changes are required — JPA entities and repositories are
database-agnostic.

## Package structure

```
config/      security, CORS, locale resolver, validation-message wiring, demo data seeder
domain/      Fund, Investor, CapitalCall JPA entities
repository/  Spring Data JPA repositories
service/     CapitalCallService (CRUD), NoticeGenerationService (async)
controller/  FundController, CapitalCallController
dto/         request/response DTOs
web/         sealed DomainException hierarchy, @RestControllerAdvice, ErrorResponse
```
