# CareerPilot AI Implementation Plan

## Initial scope and dependencies

Implement a modular monolith (Spring Boot + PostgreSQL/Flyway) with a Vite React/TypeScript client. Keep the deterministic core usable without an AI key. Data ownership, migrations, and documented API contracts underpin all user workflows. Portal automation depends on application persistence and is always opt-in and simulated.

## Build order and acceptance criteria

| Phase | Work | Depends on | Acceptance evidence |
|---|---|---|---|
| 0 Inspect | Inspect files, Git, and toolchain; record baseline | None | `IMPLEMENTATION_STATUS.md` contains observed state |
| 1 Plan | Architecture, relational model, API/error contract, gates | Phase 0 | Architecture/API/schema docs agree |
| 2 Setup | React/Vite/TypeScript/Tailwind, Spring Boot/Maven, env, Compose | Phase 1 | Frontend build and backend package succeed |
| 3 Common/data | DTOs, validation, error handling, Flyway, entities/repositories | Phase 2 | Migration initializes; persistence tests pass |
| 4 Auth | Registration/login/current-user, JWT, ownership | Phase 3 | Auth and cross-user isolation tests pass |
| 5 Frontend | Design system, navigation, auth, dashboard, typed API | Phases 2–4 | Type check/build; API-backed empty states |
| 6 Resume | Secure PDF/DOCX upload, parsing/review/versioning | Phases 3–5 | Upload/parse/ownership tests pass |
| 7 Jobs/apps | Seeded/imported jobs, search/save, application tracker/history | Phases 3–5 | Search and persisted status workflow verified |
| 8 AI/matching | Explainable deterministic matching and safe provider fallback | Phases 6–7 | Known-input scoring and no-key behavior verified |
| 9 Interview | Seeded question bank, sessions, answers, feedback/history | Phases 3–5 | Session persistence and evaluation verified |
| 10 Portals | Three isolated simulated portals, durable confirmation records | Phase 7 | Each portal produces verifiable deduplicated records |
| 11 Playwright | Consent-aware browser adapter and E2E flows | Phase 10 | Browser workflows and database records verified |
| 12 Integration | Real dashboard analytics and cross-module workflows | Phases 6–11 | UI values reconcile with persisted API data |
| 13 QA/security | Unit, MVC, integration, frontend, E2E and ownership checks | All feature phases | Actual command outputs recorded |
| 14 Deployment | Docker, health checks, CI, local and deployment docs | Phases 2–13 | Config checks/build evidence where tools allow |
| 15 Audit | Requirement-by-requirement evidence and remaining defects | All | Status checklist distinguishes implemented/tested/blocked |

## Initial vertical-slice API contract

Prefix: `/api/v1`. JSON requests/responses; ISO-8601 UTC timestamps. Protected routes require `Authorization: Bearer <access-token>`. IDs are opaque UUIDs. List routes use `page` (0-based), `size` (default 20, max 100), `sort` (`field,asc|desc`). Errors use the documented `ApiError` shape. Initial slice: `POST /auth/register`, `POST /auth/login`, `GET /users/me`, `GET /jobs`, `POST /jobs/{jobId}/save`, `POST /applications`, `GET /applications`, `PATCH /applications/{id}/status`, `POST /resumes`, `POST /matches`, and `GET /analytics/summary`.

## Verification commands

Commands depend on installed tools. Expected commands: `npm ci`, `npm run typecheck`, `npm test`, `npm run build`; `mvnw test`, `mvnw verify`; and `docker compose config` / `docker compose up --build`. Record only commands actually run and their observed results in the status file. Current inspection found npm and Git, but no Maven or Docker; Java version remains to be checked.

## Data relationships

`User` owns `UserProfile`, `Resume` and `ResumeVersion`, `UserSkill`, `SavedJob`, `ResumeJobMatch`, `CoverLetter`, `Application` and status history, `InterviewSession` and its answers, and automation records. `Job` is global/sample or user-imported. `ResumeJobMatch` references one owner, resume version, and job. `Application` may reference a saved job and resume version. Portal submissions reference an application and a portal key and have a unique idempotency key. Full constraints are in `DATABASE_SCHEMA.md`.

## Decision gates

Do not claim production readiness, external job submissions, live deployment, real AI generation, file parsing, database persistence, or browser verification until the relevant implementation has run and been observed. Missing dependencies are recorded as blockers; independent work continues.
