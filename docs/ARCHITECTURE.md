# Architecture

## System shape

CareerPilot AI is a modular monolith. `backend/` is a Java 21 Spring Boot 3.5.16 application organized by domain under `com.careerpilot`, with a Maven Wrapper pinned to Maven 3.9.9. `frontend/` is a JavaScript React 18 SPA served by Vite 6.4.3 in development and a static web server in deployment. MySQL 8.4 is the configured application database; a new Flyway baseline is kept separate from the preserved PostgreSQL migration history.

```text
Browser (React, Router, typed Axios client)
        | HTTPS / JSON /api/v1, bearer access token
Spring Security -> REST controllers -> domain services -> JPA repositories
                                                |             |
                                     AI provider port     MySQL/Flyway
                                  local / OpenAI-compatible
Portal UI -> authenticated simulation API -> owner-scoped submission records
Local Playwright run service -> loopback-only simulated portal UI -> persisted owner-scoped confirmation
```

## Module boundaries

`auth` handles credentials, access tokens and current-user identity; `user` owns editable profile and password changes; `resume` owns private files, parsed contact/skill hints and immutable versions; `jobs` owns seeded listings and user-provided imports; `matching` owns deterministic scoring/evidence; `ai` supplies local and optional OpenAI-compatible cover-letter providers; `applications` owns status history and persisted summary metrics; `interview` owns question bank, sessions and local rubric evaluations; `portal` owns fictional submission validation, idempotency, storage, and sanitized verification; `automation` owns consent-checked, asynchronous Playwright runs, request fingerprints, durable status, owner-scoped run history, and a real-site adapter extension contract. No external adapter is registered; see `docs/REAL_SITE_AUTOMATION.md`.

## Security and data flow

JWT access tokens are short lived and held in memory by the frontend; refresh is deliberately deferred until a secure rotation/revocation design is implemented. Passwords use BCrypt. Every user-specific query filters on authenticated owner identity. Resume uploads remain outside public directories, are size/type/content checked, and are not logged. Simulated portal submissions require authenticated ownership, explicit user consent, size/type/signature checks, owner-scoped idempotency, and sanitized confirmation lookup. The automation runner only accepts loopback frontend/API URLs, gets its inputs over stdin, performs no real-site navigation, and independently verifies the persisted owner-scoped confirmation before marking success. Interrupted runs are marked unverified on API restart. Failure screenshots are kept in ignored local `.tools` storage. External AI credentials are optional; development fallback output is labeled. User approval gates each simulated application submission.

## Runtime and failure behavior

The API is versioned at `/api/v1`, returns a common JSON error envelope, and exposes health/readiness through Actuator. MySQL is required for persisted workflows. AI credentials are optional; deterministic matching and seeded interviews remain usable. Simulated portals are local fixtures, never real employer sites. Configuration comes from environment variables; secrets are not committed.

## Initial architecture decisions

- Modular monolith rather than microservices: shared domain transactions and a portfolio-scale deployment.
- MySQL + Flyway: relational ownership and auditable schema changes.
- Cookie-free in-memory access token for the first slice: avoids long-lived secrets in browser storage; reload requires login until secure refresh is added.
- Deterministic match scoring with explicit evidence; AI only provides optional semantic explanations and drafts.
- Local private filesystem storage behind an interface; S3-compatible adapter remains a production extension.
