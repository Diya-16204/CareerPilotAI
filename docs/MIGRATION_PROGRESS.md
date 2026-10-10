# CareerPilot JavaScript + MySQL Completion Progress

Started: 2026-10-10 (Asia/Calcutta)  
Workspace: `C:\Users\Sach\Downloads\SHAMBHU\Projects\CareerPilot`

This checklist follows the new migration prompt and the original CareerPilot master specification. Each item is evidence-gated; code presence alone does not mean complete. The new prompt's Java 21/Spring Boot requirement is retained, while JavaScript/JSX and MySQL supersede the older TypeScript/PostgreSQL stack references.

## Stage 0 — Baseline and traceability

### Baseline Git and workspace state

- Workspace command runner and filesystem responded.
- Branch `main`, HEAD `3d67d4cd3d4a22607790ffe47c85d766c5f29c65` (`Changes are made`), upstream `origin/main`; initially clean worktree, no staged/unstaged/untracked changes.
- No commits, pushes, history rewrites, remote changes, or destructive Git commands made.
- Original master prompt and Parts 2A–2C were previously read in full; prior audit `docs/PROJECT_AUDIT.md` is available and has been read. Current source is being rechecked because the audit predates the current commit.
- Current source inventory at baseline: React/Vite application `.tsx`/`.ts`, API client/types, tests, Vite config and Playwright config; 8 PostgreSQL-specific migrations; Spring Boot 3.5.16 and Maven Wrapper 3.9.9; existing automation and test modules. Conversion/configuration changes below preserve all PostgreSQL migration files.
- Tools observed: Node 20.16.0, npm 10.8.1, Git 2.46.0, local Microsoft Java 21.0.12.1 and Maven Wrapper Maven 3.9.9. MySQL client/server, a MySQL Windows service, Docker and Docker Compose were not found; no listener on 3306/33060. PostgreSQL 18.6 is available.
- Existing listeners at inspection: Java Spring processes on 8080/8081, Vite on 4173/4174, PostgreSQL on 5432 and 5433. Process identities were inspected; no process was stopped or altered. The server on 5433 is the ignored local CareerPilot PostgreSQL data directory.
- A read-only query of the project database on 5433 verified Flyway V1–V8 succeeded and the following counts: users 14, jobs 6, resumes 12, applications 0, automation runs 11, simulated portal submissions 8. No private row values were printed.
- A custom-format `pg_dump` was created at `.tools/backups/careerpilot-postgresql-5433-20261010T102725.dump`; `pg_restore --list` successfully read its 117 catalog entries (63,618 bytes). It is a local, untracked, unencrypted backup under ignored `.tools`. Separately stored resume blobs were found in `backend/data/uploads/resumes` (13 files, 10,283 bytes); a separate ignored backup at `.tools/backups/careerpilot-resume-files-20261010T1053` has identical relative paths and SHA-256 hashes for all 13 files. The PostgreSQL source and upload source are unchanged.
- The root `.env` is user-owned/ignored and was not changed. Its database settings are PostgreSQL and must not be overwritten during the MySQL migration.

### Requirement traceability checklist

| Requirement / gap | Planned safe change | Acceptance evidence |
|---|---|---|
| Keep the original product and architecture | Retain React/Vite UI and Java 21/Spring Boot REST API; no frontend/backend rewrite | Existing page/API behavior retained; full frontend build and backend package succeed |
| Convert frontend TypeScript application to JavaScript/JSX | Convert `.tsx` to `.jsx`, `.ts` app/config/tests to `.js` where applicable; remove TypeScript-only tooling/config only after reference search; keep Zod and runtime validation | App/config/test `.ts`/`.tsx` removed; fresh Vite build, Vitest and mocked Playwright pass; no active TypeScript tooling references |
| Use MySQL instead of PostgreSQL for the app | Add MySQL Connector/J and MySQL Flyway module; env-based MySQL datasource; preserve existing PostgreSQL files/data | MySQL driver/config/source and separate baseline implemented; clean isolated MySQL migration + Hibernate validation + API smoke tests blocked because MySQL service is absent |
| Preserve Flyway/PostgreSQL history | Leave applied PostgreSQL V1–V8 unchanged; create a separate MySQL migration location/baseline; never clean/drop either DB | SHA/checksum/source unchanged for PG migrations; MySQL Flyway history is separate; PG V1–V8 still recorded successful |
| Preserve existing PostgreSQL data | Keep server and DB intact; verified custom backup and separately stored resume files | PG backup catalog verified; 13/13 separately stored resume blobs match source SHA-256; future import and row relationship checks remain |
| Authentication, authorization and safe configuration | Remove predictable signing fallback; validate key size/lifetime; preserve JWT memory-only policy; test ownership/security | JWT service now rejects missing/weak secret and non-positive lifetime; fresh unit tests cover these cases. Broader owner-scope and production integration checks remain |
| Resume upload and privacy | Preserve behavior; improve invalid-content/size/ownership tests; ensure file storage is private and retention documented | PDF/DOCX workflows and failure cases pass; storage path remains outside static assets; test user isolation |
| Jobs/search/matching | Preserve existing deterministic matcher and interfaces while filling required filters and workflow gaps from audit | API/UI tests cover title/company/location/skills/type, sorting/pagination, import/save, ownership and deterministic match history |
| Applications and analytics | Add missing edits, deadline/follow-up, associations/history/filtering and persisted analytics where feasible | MySQL-backed workflow tests verify state and owner-isolated aggregates (blocked on live MySQL) |
| Cover letters and AI | Extend existing provider abstraction; deterministic local mode; validate provider response; require explicit consent before personal data leaves app | Unit/provider-stub tests cover local, valid/malformed, timeout, 429, missing-key; no paid live call without separate explicit authorization |
| Interview preparation/evaluation | Add question generation behind provider port with local seeded fallback; improve truthful rubric feedback/progress | Tests cover session progress/history, schema failures, deterministic mode and ownership |
| Simulated portals and Playwright | Retain fictional-only simulations, explicit consent, persisted confirmations/idempotency; test failure/restart/unknown outcomes | Fresh mocks pass; live E2E against MySQL only after safe DB is available; no real employer contact |
| API/docs/CI/deployment | Update manifests, environment example, Compose/CI and docs for JS/MySQL; distinguish verified vs blocked | Docs match source; CI config inspected; container integration reported unverified without Docker |

Status terms used: **Freshly verified**, **Implemented/unverified**, **Partial**, **Missing**, **Blocked**, or **Not applicable**. MySQL live integration is currently **Blocked** because the environment has no MySQL service/client and the prompt prohibits installing system software without authorization. No database data is being discarded to work around this.

## Stages

| Stage | State | Evidence / blocker |
|---|---|---|
| 0. Baseline and traceability | Freshly verified | Current source, Git, tools, live processes, PostgreSQL schema history/aggregate counts and safe DB backup observed. |
| 1. TypeScript to JavaScript | Freshly verified | All application/config/test TS/TSX files converted or removed; Vitest, production build, and mocked Edge suite freshly passed. |
| 2. PostgreSQL to MySQL | Implemented/unverified | Driver, datasource, Flyway MySQL location and baseline, Compose/CI and docs configured. No MySQL runtime exists to test schema or migrate data. PostgreSQL source and file data remain intact and backed up. |
| 3. Security/data integrity | Partial | Removed predictable JWT secret fallback and added secret/lifetime guard tests. Full ownership, revocation and data lifecycle controls remain. |
| 4–10. Resume, jobs/matching, applications, AI, interview, simulations, analytics/UI | Partial | Reuse existing implementation and target audited gaps. |
| 11. DB/API/docs quality | Partial | MySQL/JavaScript references updated; backend unit/package verify and frontend checks passed; MySQL database tests remain blocked. |
| 12. Regression and final audit | In progress | Re-ran build/test suites; need final reference/status audit and identify remaining required feature gaps. |

## Work log

- 2026-10-10: inspected workspace and clean Git baseline; confirmed Java 21/Maven wrapper and frontend dependency tree from prior session; found active existing Spring/Vite/PostgreSQL processes without stopping them. MySQL/Docker missing. Read Flyway history and aggregate table counts read-only; made and verified local custom-format PostgreSQL backup.
- 2026-10-10: converted the React app/tests/config from TypeScript to JavaScript/JSX and removed TypeScript compiler packages/config. Fresh Vite build passed, Vitest 1/1 passed, and isolated Edge E2E 4/4 mocked tests passed with 2 live opt-in cases skipped. A first E2E attempt reused a pre-existing stale Vite server and failed; it was not stopped. The isolated rerun used agent-started port 4175 and that owned server was stopped afterward.
- 2026-10-10: configured Spring Boot for MySQL Connector/J and `flyway-mysql`, added a separate MySQL baseline containing the current entities and seed rows, and updated Compose/CI/current docs. PostgreSQL V1–V8 files were left unchanged. Fresh Java 21 `mvnw -B verify` passed 16/16 tests and packaged the executable JAR. This is not live MySQL verification.
- 2026-10-10: located 13 existing uploaded-resume files (10,283 bytes) and copied them into ignored backup storage. All 13 backup file paths and SHA-256 hashes match the originals. No source DB or upload files were changed. MySQL data import remains blocked pending availability of a MySQL runtime.
- 2026-10-10: removed the predictable JWT signing-secret default; startup now requires an environment-provided value of at least 32 bytes, and non-positive token lifetime is rejected. Added unit coverage for weak/missing secrets and invalid lifetime. A clean Java 21 `mvnw -B clean verify` freshly compiled 83 main + 8 test source files, passed 18 tests (0 failures/errors/skips), and packaged the Spring Boot JAR. Fresh final frontend rerun passed Vitest 1/1 and production Vite build (2,187 modules).

## Immediate next actions

1. Verify and refine the MySQL baseline against a provisioned isolated MySQL 8.4 instance; don't point at or mutate the PostgreSQL source.
2. Design and test a checksum/row-count/file-aware PostgreSQL-to-MySQL import; source dump and resume-file backup are ready, but no import has been performed.
3. Continue audited product/security gaps and add tests that do not require a live DB; then complete full MySQL-backed owner-isolation and integration tests once the runtime exists.
