# Capital Call Tracker — Backend

Kotlin / Spring Boot 3 API for the Alternative Capital Partners Capital Call Tracker.

## Stack

- Kotlin 1.9, Spring Boot 3.3, Java 21
- `spring-boot-starter-web`, `-security`, `-oauth2-resource-server`, `-data-jpa`, `-validation`, `-actuator`
- `kotlinx-coroutines-core` / `-reactor` for the async notice-generation feature
- H2 in-memory database for local development
- JUnit 5 + MockK + springmockk for tests

## Prerequisites

- JDK 21
- No local Gradle install needed — use the wrapper (`./gradlew`, `gradlew.bat`)

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

Set the following environment variables before running with real auth:

```
AZURE_TENANT_ID=<directory-tenant-id>
AZURE_API_CLIENT_ID=<capitalcall-api-application-client-id>
```

Without them the app still boots (JWKS lookup is lazy, only happens on first token
validation), but every request will be rejected with an invalid-token error — this is
expected until the env vars are set to real values.

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

```bash
./gradlew bootRun
```

- API: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:capitalcall`,
  user `sa`, no password) — permitted without auth, local only.
- Health check: `http://localhost:8080/actuator/health` — permitted without auth.

## Tests

```bash
./gradlew test
```

## Swapping H2 for Azure SQL

Local development uses H2 in-memory so the app runs with zero external dependencies.
Moving to Azure SQL is a datasource/driver + `application.yml` change only:

- Replace the `com.h2database:h2` runtime dependency with the MS SQL JDBC driver.
- Update `spring.datasource.url` / `driver-class-name` / credentials.
- Remove/disable the H2 console config.

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
