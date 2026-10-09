# Architecture

## System shape

CareerPilot AI is a modular monolith. `backend/` is a Java 21-targeted Spring Boot 3.5.16 application organized by domain under `com.careerpilot`; the local Java 22 compiler emits Java 21 bytecode. `frontend/` is a strict TypeScript React 18 SPA served by Vite 6.4.3 in development and a static web server in deployment. PostgreSQL is the durable source of truth; Flyway owns schema changes.

```text
Browser (React, Router, typed Axios client)
        | HTTPS / JSON /api/v1, bearer access token
Spring Security -> REST controllers -> domain services -> JPA repositories
                                                |             |
                                     AI provider port     PostgreSQL/Flyway
                                     deterministic local
Browser automation -> allowlisted simulated portal adapters -> submission records
```

## Module boundaries

`auth` handles credentials, access tokens and current-user identity; `resume` owns private files, parsed contact/skill hints and immutable versions; `jobs` owns searchable seeded listings; `matching` owns deterministic scoring/evidence; `ai` supplies a clearly labeled local template provider; `applications` owns status history and persisted summary metrics; `interview` owns question bank, sessions and local rubric evaluations. User profile editing, user imports, and automation adapters remain to be implemented. `common` holds API errors, paging and validation; `config` wires security, storage, AI, and OpenAPI.

## Security and data flow

JWT access tokens are short lived and held in memory by the frontend; refresh is deliberately deferred until a secure rotation/revocation design is implemented. Passwords use BCrypt. Every user-specific query filters on authenticated owner identity. Uploads remain outside public directories, are size/type/content checked, and are not logged. External AI and portal integrations are ports; development fallback output is labeled. User approval gates any application workflow.

## Runtime and failure behavior

The API is versioned at `/api/v1`, returns a common JSON error envelope, and exposes health/readiness through Actuator. PostgreSQL is required for persisted workflows. AI credentials are optional; deterministic matching and seeded interviews remain usable. Simulated portals are local fixtures, never real employer sites. Configuration comes from environment variables; secrets are not committed.

## Initial architecture decisions

- Modular monolith rather than microservices: shared domain transactions and a portfolio-scale deployment.
- PostgreSQL + Flyway: relational ownership and auditable schema changes.
- Cookie-free in-memory access token for the first slice: avoids long-lived secrets in browser storage; reload requires login until secure refresh is added.
- Deterministic match scoring with explicit evidence; AI only provides optional semantic explanations and drafts.
- Local private filesystem storage behind an interface; S3-compatible adapter remains a production extension.
