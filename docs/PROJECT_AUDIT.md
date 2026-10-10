# CareerPilot AI — Repository Audit

Audit date: 2026-10-10 (Asia/Calcutta)  
Workspace: `C:\Users\Sach\Downloads\SHAMBHU\Projects\CareerPilot`  
Scope: source, tests, configuration, documentation, Git state, locally available tool versions, and saved verification evidence. No application behavior, infrastructure, dependencies, or services were changed or started during this audit.

## A. Executive summary

CareerPilot is a substantial but unfinished full-stack application. The frontend and a Java 21 / Spring Boot modular-monolith backend are present, backed by PostgreSQL migrations. Source and saved test artifacts support successful frontend and backend builds and a meaningful set of unit, mocked-browser, and historical local integration workflows. The repository contains eight Flyway migrations, an executable JAR, a built frontend, and recent test reports.

The main implemented user paths are account creation/sign-in, profile editing, resume upload and limited extraction/review, job listing/import/saving, deterministic resume-to-job skill matching, cover-letter drafting/editing, deterministic interview practice, application tracking, persisted analytics, fictional-portal submission, and an approval-gated local Playwright Automation Center. AI is only used for optional cover-letter drafting; the default is a clearly labeled deterministic template. There is no resume rewriting, AI question generation, or AI interview evaluation. No real employer integration is registered or submitted.

The project is not production-ready. Coverage is concentrated in service tests and selected workflows, rather than controller/security/database integration and full user journeys. Application tracking omits several requested fields and workflows, the resume parser is a narrow text/skill extractor, and logout only clears the in-memory browser token. Rate limiting, refresh/revocation, upload malware scanning, retention/encryption policy, production secret enforcement, deployment, and Docker verification remain gaps.

At audit time the command runner and filesystem work, Git is available, Java 21/Maven Wrapper and frontend dependencies are locally available, and PostgreSQL 18.6 binaries exist. Docker is unavailable. No listener was found on ports 8080, 8081, 4173, 4174, 5432, or 5433; `pg_ctl status` reports no PostgreSQL server. Earlier test and integration claims below are explicitly identified as prior evidence, not audit-time reruns.

## B. Specification source and requirement-by-requirement audit

I located and read the full original master prompt at `C:\Users\Sach\.codex\attachments\ae14c252-7dcc-467b-9118-78e139712b64\Pasted text.txt` (1,319 lines). It includes Parts 2A, 2B, and 2C. The audit request is at `C:\Users\Sach\.codex\attachments\3ddca1a0-4526-422e-a557-6795c6059468\Pasted text.txt`. The master specification is available; no requirements below are inferred from a missing prompt.

| Requirement | Expected behavior from master specification | Status | Relevant source | Tests / evidence | Remaining work |
|---|---|---|---|---|---|
| Repository, architecture, documentation | Modular monolith, documented plan, architecture, schema, API, security, AI, testing, deployment and status; preserve work | Partial | `docs/IMPLEMENTATION_PLAN.md`, `docs/ARCHITECTURE.md`, `docs/DATABASE_SCHEMA.md`, `docs/API_DOCUMENTATION.md`, `docs/IMPLEMENTATION_STATUS.md` | Files exist; current Git state and prior milestone evidence inspected | Bring docs into agreement with actual routes/configuration; this report records discovered mismatches |
| Java/Spring stack and API | Java 21, Maven, Spring Web/Security/JPA/Validation, REST DTOs, layered modules, health/OpenAPI | Complete (stack) / Partial (contract coverage) | `backend/pom.xml`, `backend/src/main/java/com/careerpilot/**`, `backend/src/main/resources/application.yml` | Maven Wrapper reports Maven 3.9.9 on local Microsoft JDK 21.0.12.1; latest package artifact and tests exist | Add MockMvc/controller contract and startup integration coverage; decide/document pageable endpoints consistently |
| PostgreSQL schema and migrations | Normalized schema, migrations as source of truth, constraints/indexes, seed data and persistence tests | Partial | `backend/src/main/resources/db/migration/V1__...sql` through `V8__...sql`, entities/repositories | Status records migrations 1–8 applied, Hibernate validate passed, and live persistence observed earlier on PostgreSQL 18.6 | Add repeatable migration/ownership repository tests; reconcile missing conceptual `user_skills` and `interview_feedback` entities; test supported PostgreSQL version |
| Registration, login, JWT, password security | Register/login, password hashing, protected API, ownership, logout/invalidation; secure browser token strategy | Partial | `auth/AuthController.java`, `AuthService.java`, `JwtService.java`, `JwtAuthenticationFilter.java`, `config/SecurityConfig.java`, `frontend/src/auth/AuthContext.tsx` | Auth/JWT service tests; status records HTTP registration/login, protected 401 and cross-owner 404 | Logout is client-only; no refresh/revocation; JWT fallback secret is predictable; add security/controller tests and production secret enforcement |
| Profile and settings | Read/edit profile, password changes, settings page | Partial | `user/ProfileController.java`, `ProfileService.java`, `frontend/src/pages/ProfilePage.tsx`, `frontend/src/App.tsx` | API implemented; settings route renders the profile page | Implement actual settings or remove duplicate route; add profile/password browser and ownership tests |
| Frontend design and navigation | Responsive polished UI, protected routes, forms, loading/error/empty states, accessible controls, no dead buttons | Partial | `frontend/src/App.tsx`, `components/`, `pages/`, `styles.css` | Typecheck/build and one auth validation unit test are recorded as passing; browser suite covers selected portal/automation flows | Only one unit test; inspect/fix responsive/accessibility/error boundary, form validation, empty states, and verify page controls end to end |
| Resume lifecycle | PDF/DOCX upload, robust structured extraction, review/correction, versioning, replacement/download/delete and owner checks; private storage | Partial | `resume/ResumeController.java`, `ResumeService.java`, `ResumeParser.java`, `ResumeStorage.java`, `ResumeVersion.java` | Parser tests; earlier live PDF upload returned extracted email/skills/version | Extraction currently returns text, email, phone, and matches against a fixed skill list; no education/experience/projects/certifications structure. Review only edits email/phone/skills. Add lifecycle, corrupt/encrypted/size and cross-owner integration tests; review deletion/reference safety |
| Job discovery/import | Seeded listings, detail, search filters, sorting/paging, save/unsave, import and duplicate detection | Partial | `jobs/JobController.java`, `JobService.java`, `JobRepository.java`, `frontend/src/pages/JobsPage.tsx`, migrations V1/V5 | Six seeded jobs and save/import persistence were previously observed | Search currently exposes `q`, employment type, page/size/sort; separate title/company/location/skill filters requested by spec are absent. Add UI/API coverage and resolve application duplicate edge cases |
| Explainable resume matching | Deterministic scoring plus explainable required/preferred/partial/missing skills, evidence and learning/improvement suggestions, persisted history | Partial | `matching/MatchingController.java`, `MatchingService.java`, `MatchDtos.java`, `frontend/src/pages/MatchPage.tsx`, V2 | Deterministic service tests; prior HTTP match persisted `skill-coverage-v1`, score 60 | Current scorer is deterministic skill coverage with aliases; no semantic interpretation, qualification/project assessment or learning-resource integration. Report weighting limitations and test result/persistence/ownership |
| AI resume improvement | Truth-preserving summary/bullet rewrite, tailoring, keyword suggestions, before/after, user approval, copy/download | Missing | No resume improvement provider/service/controller/page found; resume page is lifecycle UI | No test source or endpoint found | Implement through provider abstraction, with factuality safeguards, explicit approval, and local clearly labeled fallback; add provider contract and E2E coverage |
| Cover letters | Job/resume-context draft, tone/length, truthful output, editing, regeneration, history, copy/download, application association | Partial | `ai/CoverLetterController.java`, `CoverLetterService.java`, `AiProvider.java`, provider implementations, `frontend/src/pages/CoverLetterPage.tsx`, V3 | Two OpenAI-compatible HTTP stub tests and local-provider test; status records draft/edit/history persistence against DB | No regeneration/version history beyond saved drafts, download, or evidenced application association UI; remote provider never exercised with real account; improve missing-information response handling |
| AI provider abstraction | Configurable provider/model/key, timeout/limits, safe errors, validated response, deterministic tests/local fallback, private data handling | Partial | `ai/AiProvider.java`, `OpenAiCompatibleAiProvider.java`, `LocalTemplateAiProvider.java`, `UnconfiguredAiProvider.java`, `docs/AI_PROVIDER.md` | Local provider test and two HTTP-stub contract tests | Only cover letters call provider. Resume contents/name/job description leave the machine when remote provider enabled; add explicit data-consent/retention UX and remote smoke test when authorized |
| Interview preparation and mock interviews | Seeded topics/difficulties, question generation, persisted sessions/answers, rubric feedback, strengths/weaknesses, revisions and progress | Partial | `interview/InterviewController.java`, `InterviewService.java`, entities, V3/V4, `frontend/src/pages/InterviewsPage.tsx` | Service test; prior live session/answer/history and analytics persistence observed | Questions are seeded and feedback is local keyword overlap. No AI question generation, robust correctness/communication/problem-solving feedback, explicit separate feedback entity, or meaningful progression recommendations. Extend tests and seed bank |
| Application tracker | Create/update full application details, stage history, deadlines, follow-ups, notes, resume/letter associations, filters/search and duplicate controls | Partial | `applications/ApplicationController.java`, `ApplicationService.java`, entities, `frontend/src/pages/ApplicationsPage.tsx`, V1 | Prior application/status persistence and analytics count observed | API creates and changes status only; no edit notes/deadline/follow-up or resume/letter linkage in create DTO, search/filter, or detailed history endpoint/UI. `ApplicationStatus` does not fully match requested status set. Add owner and lifecycle tests |
| Three simulated portals | Three distinct labeled flows, validation, persisted submissions, confirmation IDs, safe verification, duplicate protection | Complete (simulated path; historical integration) | `portal/SimulatedPortalController.java`, `SimulatedPortalService.java`, `frontend/src/pages/SimulatedPortalPage.tsx`, V6 | Prior browser/API/DB evidence says A/B/C reached persisted verified success; mocked E2E covers UI workflows | Add controller/database tests and cleanup/fixture lifecycle; submission PII and resume bytes currently stored in database. Do not call these real employer submissions |
| Playwright automation | User-selected job/resume, supplied answers, explicit consent, resilient fill, idempotency, retries/timeouts, independent record verification, evidence | Complete (simulated path; historical integration) | `automation/AutomationRunService.java`, `ProcessPortalAutomationRunner.java`, `frontend/scripts/portal-automation.mjs`, `AutomationPage.tsx`, V7/V8 | Prior live UI workflow and A/B/C DB flows; same-key retry, changed-payload 409, cross-user 404 recorded; saved test result passed | Test artifacts on current machine are historical; add failure-path/timeout/restart integration suite and ensure setup commands configure matching ports |
| Real-site automation framework | Authorized adapter interface, approval, preview, non-bypass behavior; no fabricated submissions | Partial (extension contract only) | `automation/RealJobSiteAdapter.java`, `docs/REAL_SITE_AUTOMATION.md` | Source confirms interface; no registered adapter implementation | No external portal is automated or contacted. Implement only after an authorized site/test account is identified; current interface has no controller/service binding or persistence path |
| Analytics/dashboard | Persisted application/interview outcomes, recent activity and useful empty state; no fake metrics | Partial | `applications/ApplicationService.java` (`summary`), `ApplicationController.java`, `DashboardPage.tsx`, `AnalyticsPage.tsx` | Earlier API returned persisted application/interview counts; frontend uses Recharts | Analytics is limited to applications by status, interview totals/answers, and recent application updates; no match history/common skill gaps/follow-ups/automation outcome aggregation. Add query and cross-module tests |
| API errors, validation, ownership | Consistent safe error body, validation, status codes and per-user access | Partial | `common/ApiExceptionHandler.java`, `ApiErrorWriter.java`, DTOs, repositories/services, `SecurityConfig.java` | Service tests and historical 401/404 owner checks | No comprehensive MockMvc suite; verify every ID-based resource and malformed request; some paging parameters clamp instead of reject; confirm unexpected error messages do not leak |
| Security/privacy | Secure defaults, safe uploads, data minimization, CORS/CSRF, abuse controls and documented limits | Partial | `SecurityConfig.java`, `JwtService.java`, `ResumeParser.java`, `ResumeStorage.java`, `.gitignore`, `docs/SECURITY.md` | `.env` is ignored and not tracked; targeted source scan found no committed private-key/API-key pattern; bearer token stored in JS memory | No rate limit, refresh/revocation, production secret profile, malware scanning, at-rest encryption/retention deletion policy, or AI disclosure consent. Local JWT/database fallback credentials must never be used in deployment |
| Docker/deployment/CI | Compose with DB volume/health, app images, CI, deployment notes, tested configuration | Partial / Unverified | `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`, `.github/workflows/ci.yml`, `docs/DEPLOYMENT.md` | Files exist; Docker command unavailable | Compose, images, CI and deployment have not been executed in this audit; Docker installation is explicitly out of scope. Resolve defaults and test on an approved Docker host |
| Definition of done / final acceptance | Every requirement accepted only after relevant build, test, integration and docs evidence | Partial | This report and `docs/IMPLEMENTATION_STATUS.md` | Prior build/test/API/browser/database evidence listed below | Many requested acceptance tests are missing; use backlog and acceptance criteria in section G before calling the project complete |

## C. Feature inventory and actual behavior

| Module | What a user can actually do / frontend-to-backend flow | Backend and persistence | Logic type and remaining gaps |
|---|---|---|---|
| Authentication/profile | Register or sign in; browser holds bearer token in module memory; protected pages call typed Axios API. Edit profile fields and change password. “Settings” currently routes to the same profile page. | `/api/v1/auth/register`, `/auth/login`, `/users/me`, `/users/me/profile`, `/users/me/password`; `AuthService`, `ProfileService`; `users`, `user_profiles`. | Real BCrypt/JWT/persisted profile. Client logout clears token only; token expires after 900 seconds; no invalidation or refresh. |
| Jobs | Browse seeded/sample and user-imported listings, open job detail, search broad query/type, save/unsave; import job description. | `/api/v1/jobs`, `/jobs/{id}`, `/jobs/import`, `/jobs/{id}/saved`; `JobService`, repositories; `jobs`, `saved_jobs`. | Persisted. No external job API or scraping. Search is narrower than specification. |
| Resumes | Upload PDF/DOCX, list/view versions, review extracted email/phone/skills, download or delete. File bytes live outside public web root; DB stores private key and extracted text. | `/api/v1/resumes` multipart; `/resumes/{id}`, version `/review`, `/download`; `ResumeService`, parser, storage; `resumes`, `resume_versions`. | Apache PDFBox/POI parsing and deterministic fixed-list skill recognition. Not full structured resume extraction and no resume improvement. |
| Match | Select a resume version and job, request and view score/evidence/missing skills. | `POST /api/v1/matches`; `MatchingService`; `resume_job_matches`, `match_skills`, `skills`. | Deterministic `skill-coverage-v1`; it is not a hiring probability or semantic AI score. |
| Cover letters | Select job/resume/tone; generate a draft, edit and view saved letters. | `POST /api/v1/cover-letters/draft`, `GET /cover-letters`, `GET/PUT /cover-letters/{id}`; `CoverLetterService`; `cover_letters`. | Local mode formats name/title/company/recognized skill names into a deterministic template. `AI_PROVIDER=openai` calls compatible Chat Completions only when a key is configured. No resume/letter application association UX, download or true version history found. |
| Interviews | Choose seeded topic/difficulty, create session, receive next question, submit answer, see a score, deterministic feedback/model answer and history. | `/api/v1/interviews/questions`, `/sessions`, `/sessions/{id}`, `/sessions/{id}/next`, `/sessions/{id}/answers`; `InterviewService`; `interview_questions`, `interview_sessions`, `interview_answers`. | Deterministic word-set overlap with a small stop list. No submitted code execution, generated questions, AI assessment, separate strengths/weaknesses or communication/problem-solving measure. |
| Applications/analytics | Create application and update its status; see list and analytics with status counts and recent records. | `/api/v1/applications`, `/applications/{id}/status`, `/analytics/summary`; `ApplicationService`; `applications`, `application_status_history`. | Persisted, but tracker does not yet expose all desired deadlines, follow-up, filtering, note editing and linked resume/letter workflow. Analytics omit matching and automation data. |
| Simulated portals | Three in-app fictional workflows with consent, resume attachment and portal-specific fields; confirmation after persist; test verification endpoint. | `/api/v1/simulated-portals/{portal}/jobs`, `/.../{portal}/submissions`, `/.../submissions/{confirmationId}`; `SimulatedPortalService`; `simulated_portal_submissions`. | Real local persistence, not a real employer. Browser UI tests mock these endpoints; historical integration separately used local DB. |
| Automation Center | Authenticated user chooses fictional portal/job, an owned resume version, answers and explicit consent; polls run and sees result/history. Local Node/Playwright helper submits and verifies DB-backed API record. | `POST/GET /api/v1/automation-runs`, `GET /{id}`; `AutomationRunService`, `ProcessPortalAutomationRunner`; `automation_runs` and portal records. | Simulated-only; idempotency fingerprint is owner scoped and independent confirmation is required. No retry of uncertain real-world submissions. Evidence stored locally under ignored `.tools`. |
| Real external portals | None are currently available in product. | `RealJobSiteAdapter` Java interface exists; zero implementations are registered. | Contract only. No real website navigation, autofill, account use, or application submission. |

### Principal API routes

All routes use `/api/v1`. Auth: registration/login and public job/fictional-portal listing routes are public; other routes require a Bearer access token. Exact request fields are in backend DTOs and `docs/API_DOCUMENTATION.md`.

- `POST /auth/register`, `POST /auth/login`, `GET /users/me`
- `GET/PATCH /users/me/profile`, `POST /users/me/password`
- `GET /jobs`, `GET /jobs/{id}`, `POST /jobs/import`, `PUT/DELETE /jobs/{id}/saved`
- `POST/GET /resumes`, `GET /resumes/{id}`, `PUT /resumes/{id}/versions/{versionId}/review`, `GET /resumes/{id}/versions/{versionId}/download`, `DELETE /resumes/{id}`
- `POST /matches`
- `POST /cover-letters/draft`, `GET /cover-letters`, `GET/PUT /cover-letters/{id}`
- `GET /interviews/questions`, `POST/GET /interviews/sessions`, `GET /interviews/sessions/{id}`, `GET /interviews/sessions/{id}/next`, `POST /interviews/sessions/{id}/answers`
- `POST/GET /applications`, `PATCH /applications/{id}/status`, `GET /analytics/summary`
- `GET /simulated-portals/{portal}/jobs`, `POST /simulated-portals/{portal}/submissions`, `GET /simulated-portals/submissions/{confirmationId}`
- `POST/GET /automation-runs`, `GET /automation-runs/{id}`
- `GET /actuator/health` and `/actuator/health/readiness`; development Swagger UI `/swagger-ui.html` and OpenAPI `/v3/api-docs`.

## D. Technology stack and schema

### Verified installed/configured versions

| Area | Repository requirement/configuration | Observed in this workspace |
|---|---|---|
| Runtime | Java 21 (`backend/pom.xml`) | Microsoft OpenJDK `21.0.12.1` runs from ignored `.tools/microsoft-jdk-21/jdk-21.0.12.1+1`; direct `java -version` on this binary succeeded. The system `java` shim points to Oracle `javapath` and did not return during the first check; use the verified local JDK path. |
| Spring/Maven | Spring Boot `3.5.16`; Maven Wrapper distribution `3.9.9` | Wrapper `-v` observed Maven 3.9.9 on Java 21.0.12.1. Spring Boot 3.5.x supports Java 21; the project explicitly targets 21. |
| Backend libraries | Web, Data JPA, Security, Validation, Actuator, Flyway core/PostgreSQL, PostgreSQL JDBC, Springdoc `2.8.6`, PDFBox `3.0.4`, POI OOXML `5.4.1`, Spring Boot test/Security test | Exact versions are fixed directly or inherited from the Spring Boot parent in `backend/pom.xml`. JWT is custom HS256 built with Java crypto, not a JWT library. HTTP uses Spring `RestClient`; Spring AI SDK is not included. |
| Frontend | React `18.3.1`, React DOM `18.3.1`, TypeScript `5.7.3`, Vite `6.4.3`, Tailwind `4.3.3`, React Router `7.18.4`, Axios `1.20.0`, RHF `7.55.0`, Zod `3.24.2`, Recharts `3.0.0`, Lucide `0.468.0` | `npm ls --depth=0` from `frontend/` reported these installed versions. npm `10.8.1`, Node `20.16.0`. Lockfile is `frontend/package-lock.json`. |
| Frontend tests | Vitest `4.1.11`, Testing Library DOM `10.4.1`, React `16.2.0`, jest-dom `6.6.3`, user-event `14.6.1`, jsdom `26.0.0`, Playwright Test `1.64.0` | Installed dependencies observed via npm. One unit-test file, multiple Playwright specs. |
| Database | PostgreSQL in Compose configured as `postgres:16-alpine` | Local PostgreSQL binaries report `18.6`; data cluster `PG_VERSION` is 18. No server was running during audit. Prior live test used 18.6; project status notes Flyway warning because its tested support was through PG17. |
| Build/deploy | Dockerfiles, Compose, nginx config, GitHub Actions workflow | Docker command is absent; no container command was executed. CI/Compose behavior is unverified. |

Node.js is needed to run Vite, npm scripts, frontend tests, and the Playwright worker; the application API/backend is Java/Spring Boot and does not use Node as its backend.

### Migrations and conceptual schema

Flyway V1–V8 create `users`, `jobs`, `saved_jobs`, `applications`, `application_status_history`, `resumes`, `resume_versions`, `skills`, `resume_job_matches`, `match_skills`, `cover_letters`, `interview_questions`, `interview_sessions`, `interview_answers`, `user_profiles`, `simulated_portal_submissions`, and `automation_runs`. V8 changes `resume_versions.checksum_sha256` to `VARCHAR(64)` to match the Hibernate mapping while preserving the original applied V2 checksum. The implementation does not have separate `user_skills` or `interview_feedback` tables from the conceptual spec; feedback is stored on answers and returned as a DTO. No pgvector is used.

## E. Environment variables and external credentials

No real secrets are printed in this report. `.env` exists but is ignored by Git; `.env.example` contains placeholders. Spring Boot and Vite do **not** automatically load the repository-root `.env` file. Docker Compose reads that file automatically. For local non-Compose runs, set process environment variables in each terminal or use a deliberate safe loader.

| Variable | Component / status | Controls and example placeholder | Where/how to obtain; works without it? |
|---|---|---|---|
| `DATABASE_URL` | Backend; required for DB-backed operation | `jdbc:postgresql://127.0.0.1:5433/careerpilot` | Local PostgreSQL connection URL. Backend cannot start DB modules without reachable database. |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Backend; required; Compose maps `POSTGRES_USER/PASSWORD` into them | `<local-db-user>`, `<local-db-password>` | Set to the local DB role. Do not publish values. `.env` itself is ignored, but is not loaded by Maven automatically. |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Compose DB container; Compose-only | `careerpilot`, `careerpilot`, `<local-only-password>` | Initial cluster/database credentials. Compose unverified. |
| `JWT_SECRET` | Backend; required; local fallback exists but unsafe for production | `<random-32-bytes-or-more>` | Generate a random secret locally. Source enforces at least 32 UTF-8 bytes. Production must not use `application.yml` fallback. |
| `JWT_EXPIRATION_SECONDS` | Backend; optional, default 900 | `900` | Access-token lifetime; no refresh/revocation flow. |
| `PORT` | Backend; optional, default 8080 | `8081` | Spring server port. |
| `FRONTEND_ORIGIN` | Backend; required for browser calls, default `http://localhost:5173` | `http://127.0.0.1:5173` | Exact browser origin allowed by CORS. |
| `VITE_API_BASE_URL` | Frontend build/dev; optional fallback `http://localhost:8080/api/v1` | `http://127.0.0.1:8081/api/v1` | API base URL compiled into Vite. Must be set in frontend process or `frontend/.env`; root `.env` is not loaded by Vite. |
| `AI_PROVIDER` | Backend; optional default `local` | `local` or `openai` | Local mode works without an account/key; remote selected without key returns 503 on generation. |
| `AI_API_KEY` | Backend; optional except when `AI_PROVIDER=openai` | `<provider-secret>` | Create at OpenAI API platform for OpenAI use; store in environment/secret manager. A ChatGPT subscription does not itself include API usage credits. API calls may incur usage charges; check current provider billing/pricing before use. No real key is configured. |
| `AI_BASE_URL`, `AI_MODEL` | Backend; optional defaults `https://api.openai.com`, `gpt-4.1-mini` | Provider origin/model ID | OpenAI-compatible service endpoint/model. Do not assume the configured model/account is valid until an authorized live call. |
| `AI_TIMEOUT_SECONDS`, `AI_MAX_INPUT_CHARACTERS` | Backend; optional defaults `20`, `20000` | `20`, `20000` | Remote request timeout and text input bound. Only cover-letter generation uses this provider. |
| `UPLOAD_DIR` | Backend; optional default `./data/uploads/resumes` | `./data/uploads/resumes` | Private local resume-file storage directory; ensure backup/access/retention policy. |
| `CAREERPILOT_AUTOMATION_FRONTEND_URL`, `CAREERPILOT_AUTOMATION_API_BASE_URL` | Backend local runner; optional defaults 5173 and 8080 | Loopback frontend/API URLs | Must point at actual local services; runner enforces loopback HTTP. |
| `CAREERPILOT_AUTOMATION_NODE`, `CAREERPILOT_AUTOMATION_SCRIPT`, `CAREERPILOT_AUTOMATION_FRONTEND_DIRECTORY`, `CAREERPILOT_AUTOMATION_BROWSER`, `CAREERPILOT_AUTOMATION_TIMEOUT_SECONDS`, `CAREERPILOT_AUTOMATION_EVIDENCE` | Backend local runner; optional defaults in `application.yml` | `node`, `../frontend/scripts/portal-automation.mjs`, `../frontend`, `msedge`, `90`, `../.tools/automation-evidence` | Local Node/Playwright execution paths, browser, timeout and failure evidence directory. They work only when the runner's paths and browser installation are valid. |
| `CAREERPILOT_LIVE_E2E`, `CAREERPILOT_E2E_BASE_URL`, `PLAYWRIGHT_BROWSER`, `CI` | Frontend tests only | `1`, local Vite URL, `msedge`, CI-defined | Enables live tests, points Playwright at existing Vite, selects Edge channel, configures retry behavior. No third-party account is needed. |
| Email, job search, cloud storage, external portals | Not referenced as active integrations | None | No email provider, job aggregator API, cloud object store, or employer credential is implemented/configured. |

Remote AI sends the selected candidate name, selected job description, extracted resume text and skill list, and tone to the configured compatible endpoint. Local mode does not make a network AI call and is labeled `LOCAL_TEMPLATE`. With OpenAI selected and no key, generation fails safely with 503; other core workflows do not require the key. The optional provider was tested only against a local HTTP stub. Create an API key from the [OpenAI API keys page](https://platform.openai.com/api-keys); OpenAI says the full secret is shown only at creation, so save it securely and rotate it by creating a replacement if lost. API billing is separate from ChatGPT subscription billing; paid API use requires API billing setup and charges vary by model/token/features, so review current [API pricing](https://developers.openai.com/api/docs/pricing) and billing controls before enabling it. Do not paste the key into chat or commit it.

## F. Windows PowerShell setup and run guide

This is a reproducible recipe based on the checked-in config and locally observed tools. It has **not** been executed end-to-end during this audit. No services are running now. The ignored `.env` currently points to PostgreSQL 5432, API 8080, and frontend CORS 5173; historical integrated verification used PG 5433, API 8081, and Vite 4174. For a clean session, use the explicit ports below and keep frontend/CORS/automation URLs aligned. Do not blindly reuse the stale `.env` URL values.

### 1. Open three PowerShell terminals and set project paths

Run in each terminal:

```powershell
$ProjectRoot = 'C:\Users\Sach\Downloads\SHAMBHU\Projects\CareerPilot'
$Jdk = Join-Path $ProjectRoot '.tools\microsoft-jdk-21\jdk-21.0.12.1+1'
$env:JAVA_HOME = $Jdk
$env:PATH = "$Jdk\bin;$env:PATH"
```

Tool check (Node/npm/Git are on PATH; use the local JDK path shown):

```powershell
node --version
npm --version
git --version
& (Join-Path $env:JAVA_HOME 'bin\java.exe') -version
Set-Location (Join-Path $ProjectRoot 'backend')
.\mvnw.cmd -v
Get-Command docker -ErrorAction SilentlyContinue
& (Join-Path $ProjectRoot '.tools\postgresql\pgsql\bin\psql.exe') --version
```

Expected observations from this machine: Node `v20.16.0`, npm `10.8.1`, Git `2.46.0`, Java `21.0.12.1`, Wrapper Maven `3.9.9`, PostgreSQL client `18.6`; Docker command not found.

### 2. Start the existing local PostgreSQL cluster — Terminal 1

The PostgreSQL 18 data directory already exists under ignored `.tools`; no server was running at audit time. Its saved options indicate loopback `127.0.0.1:5433`. This command starts that existing cluster and does not create/reset it:

```powershell
Set-Location $ProjectRoot
$PgCtl = Join-Path $ProjectRoot '.tools\postgresql\pgsql\bin\pg_ctl.exe'
$PgData = Join-Path $ProjectRoot '.tools\postgresql-data'
& $PgCtl -D $PgData -l (Join-Path $ProjectRoot '.tools\logs\postgres-audit-run.log') -o '-h 127.0.0.1 -p 5433' start
```

Verify before proceeding:

```powershell
& $PgCtl -D $PgData status
& (Join-Path $ProjectRoot '.tools\postgresql\pgsql\bin\pg_isready.exe') -h 127.0.0.1 -p 5433
```

The configured cluster already has local test data/credentials; the audit did not print or verify credential values. If startup reports a stale PID or authentication/database error, inspect the named process and PostgreSQL log first. Do not delete data or kill an unidentified process. The project’s Compose path is an alternative only after Docker is installed and approved; it has not been tested here.

### 3. Start backend — Terminal 2

Set environment values in this terminal. Obtain the existing local DB password from the ignored local config through a secure local method; do not paste it into chat or a command saved in shell history. Set a random JWT secret per session:

```powershell
Set-Location $ProjectRoot
$env:PORT = '8081'
$env:DATABASE_URL = 'jdbc:postgresql://127.0.0.1:5433/careerpilot'
$env:DATABASE_USERNAME = 'careerpilot'
$passwordLine = Get-Content -LiteralPath (Join-Path $ProjectRoot '.env') | Where-Object { $_ -match '^DATABASE_PASSWORD=' } | Select-Object -First 1
if (-not $passwordLine) { throw 'Set DATABASE_PASSWORD in the ignored local .env file or enter it securely in this terminal.' }
$env:DATABASE_PASSWORD = $passwordLine.Substring('DATABASE_PASSWORD='.Length)
$env:JWT_SECRET = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:JWT_EXPIRATION_SECONDS = '900'
$env:FRONTEND_ORIGIN = 'http://127.0.0.1:5173'
$env:AI_PROVIDER = 'local'
$env:CAREERPILOT_AUTOMATION_FRONTEND_URL = 'http://127.0.0.1:5173'
$env:CAREERPILOT_AUTOMATION_API_BASE_URL = 'http://127.0.0.1:8081/api/v1'
$env:CAREERPILOT_AUTOMATION_FRONTEND_DIRECTORY = (Join-Path $ProjectRoot 'frontend')
$env:CAREERPILOT_AUTOMATION_SCRIPT = (Join-Path $ProjectRoot 'frontend\scripts\portal-automation.mjs')
$env:CAREERPILOT_AUTOMATION_EVIDENCE = (Join-Path $ProjectRoot '.tools\automation-evidence')
$env:UPLOAD_DIR = (Join-Path $ProjectRoot '.tools\uploads\resumes')
$env:JAVA_HOME = Join-Path $ProjectRoot '.tools\microsoft-jdk-21\jdk-21.0.12.1+1'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location (Join-Path $ProjectRoot 'backend')
.\mvnw.cmd -B spring-boot:run
```

Replace the password placeholder locally before running. The direct Maven/Spring process reads environment variables; it does not source root `.env`. Flyway applies migrations, then Hibernate validates. Expected API `http://127.0.0.1:8081`; check `Invoke-RestMethod http://127.0.0.1:8081/actuator/health` and expect `status` `UP`.

### 4. Start frontend — Terminal 3

```powershell
Set-Location $ProjectRoot
$env:VITE_API_BASE_URL = 'http://127.0.0.1:8081/api/v1'
Set-Location (Join-Path $ProjectRoot 'frontend')
npm run dev -- --host 127.0.0.1 --port 5173
```

Open `http://127.0.0.1:5173`. API prefix is `/api/v1`. Set backend `FRONTEND_ORIGIN` to the exact Vite origin. Vite reads `VITE_API_BASE_URL` from its own process or `frontend/.env`; the root `.env` is not automatically loaded by Vite.

### 5. Verify user workflow

1. In a fourth PowerShell window, verify `http://127.0.0.1:8081/actuator/health` reports `UP`.
2. Register in the UI with an email you control and a locally chosen password (minimum 10 characters). Sign in and open the dashboard.
3. Upload a non-sensitive, text-based PDF or DOCX. Confirm extracted details and saved version. Review/edit available fields and re-open the resume list.
4. Open Jobs, inspect sample-data source labels, search, save a job, and import a user-provided description. Confirm they remain after a page reload.
5. Create a match and inspect deterministic `skill-coverage-v1` evidence. Draft/edit a cover letter and confirm it says `LOCAL_TEMPLATE` while `AI_PROVIDER=local`.
6. Complete an interview answer and change an application status; check history/analytics reflect persisted records. Cross-user and failure scenarios should be tested using isolated test accounts, not personal data.
7. For automation, select a fictional portal/job and an owned resume version, provide the required answers, explicitly consent, then wait for `VERIFIED_SUCCESS`; open run history and verify its confirmation. This is local simulated submission only.

### 6. Test commands

Frontend terminal, from `frontend/`:

```powershell
npm run typecheck
npm test
npm run build
$env:PLAYWRIGHT_BROWSER = 'msedge'
npm run test:e2e
Remove-Item Env:PLAYWRIGHT_BROWSER
```

Default Playwright tests start Vite on 4173 unless `CAREERPILOT_E2E_BASE_URL` is set. Live UI test requires a separately running Vite/API/PostgreSQL and:

```powershell
$env:CAREERPILOT_LIVE_E2E = '1'
$env:CAREERPILOT_E2E_BASE_URL = 'http://127.0.0.1:5173'
$env:PLAYWRIGHT_BROWSER = 'msedge'
npm run test:e2e -- e2e/automation-live.spec.ts
```

Backend terminal, from `backend/` with local `JAVA_HOME` set:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B package
```

To stop Vite and Spring Boot, press `Ctrl+C` in their own terminals. To stop PostgreSQL, first confirm the local cluster’s status and use its `pg_ctl` (not Task Manager/process kill):

```powershell
& (Join-Path $ProjectRoot '.tools\postgresql\pgsql\bin\pg_ctl.exe') -D (Join-Path $ProjectRoot '.tools\postgresql-data') status
& (Join-Path $ProjectRoot '.tools\postgresql\pgsql\bin\pg_ctl.exe') -D (Join-Path $ProjectRoot '.tools\postgresql-data') -m fast stop
```

### Common errors and fixes

- **Java not found / wrong Java:** direct PATH `java` resolves to a system shim here; set `JAVA_HOME` and prepend `.tools\microsoft-jdk-21\jdk-21.0.12.1+1\bin` before calling `mvnw.cmd`.
- **Connection refused:** verify PostgreSQL status and port 5433, then compare `DATABASE_URL`; do not assume the ignored `.env` is loaded.
- **Flyway/Hibernate schema mismatch:** preserve migration history and inspect Flyway output; do not modify an already-applied migration. V8 exists specifically for the resume checksum type alignment.
- **401 in browser:** token is in memory only; sign in again after reload/15-minute expiry. Check browser calls use the expected API and CORS origin.
- **CORS:** `FRONTEND_ORIGIN` must exactly match the browser scheme/host/port.
- **Automation `FAILED` or `UNVERIFIED`:** ensure frontend/API URLs and browser runner paths point to the running local services. `UNVERIFIED` intentionally means persistence could not be confirmed; do not assume submission succeeded.
- **Port occupied:** inspect listener PID and command line first. Choose a new API `PORT`, Vite port, matching CORS origin, VITE API URL, and automation URLs together. Never kill an unidentified process.
- **OpenAI mode returns 503:** set `AI_API_KEY` in the backend process, verify billing/model/base URL outside chat; or switch to labeled `AI_PROVIDER=local`.
- **Docker command missing:** Docker/Compose was not available; this audit did not install it or test Compose.

## G. Test and verification evidence

No tests/builds/services were rerun during the audit. The following is prior evidence corroborated by repository artifacts and the implementation log, not a claim of a fresh audit-time run.

| Category | Command / evidence | Latest known result | Scope/limitation |
|---|---|---|---|
| Backend unit/service tests | `cd backend; .\mvnw.cmd -B test`; latest `backend/target/surefire-reports/TEST-*.xml` timestamps 2026-10-10 09:26; eight XML suites sum to 16 tests, 0 failures, 0 errors, 0 skipped | Passed; report files exist | Auth/JWT, parser, matching, interview rubric, local/HTTP-stub AI, simulated-portal service. Not MockMvc or real PostgreSQL repository/integration suite. |
| Backend package | `cd backend; .\mvnw.cmd -B package`; `backend/target/careerpilot-api-0.1.0-SNAPSHOT.jar` exists, timestamp 09:26:31, size 88,736,196 bytes | Prior build reported pass; artifact observed | Artifact existence is verified; no current rebuild. |
| Frontend typecheck/unit/build | `npm run typecheck`, `npm test`, `npm run build` from `frontend/` | Prior log: typecheck/build passed; 1 Vitest test passed; built output `frontend/dist/index.html` timestamp 09:23 | One unit test covers only registration form validation; no current rerun. |
| Default browser E2E | `PLAYWRIGHT_BROWSER=msedge npm run test:e2e` (PowerShell equivalent in section F) | Prior log: 4 passed; 2 conditional live tests skipped. `frontend/test-results/.last-run.json` says `passed`, `failedTests: []`. | Three mocked fictional portal flows and mocked Automation Center. Mocks do not verify backend/database persistence. |
| Live Automation Center browser test | `CAREERPILOT_LIVE_E2E=1`, base URL set, then Playwright `e2e/automation-live.spec.ts` | Prior log: separate 1 passed | Ran against UI, API, local browser worker and PostgreSQL using synthetic checked-in PDF, not during this audit. |
| Database migrations/schema | Prior log says Flyway validated/applied V1–V8 on PostgreSQL 18.6 and Hibernate validate passed | Prior integration evidence | At audit time PG binaries were 18.6 but server was stopped; PG18 exceeds Flyway’s noted tested support through 17. No migration rerun now. |
| API persistence/auth | Prior log records signup/profile, unauthorized 401, seeded jobs, resume, saved job, application/status, match, cover-letter, interview, analytics persistence and owner-scope checks | Historical observed HTTP/API integration | No servers running in audit. Historical report describes portal-run cross-owner 404 and protected routes; not a comprehensive controller test suite. |
| Portal automation | Prior API/browser/database runs report A/B/C `VERIFIED_SUCCESS`, same-key replay returned same run, modified body returned 409, foreign-owner lookup 404 | Historical end-to-end observation | Only fictional local portals; no external job site. |
| AI integration | `OpenAiCompatibleAiProviderTest` (2 tests), local provider (1) | Passed in latest Maven XML | HTTP stub only; no paid/real provider request or real AI credential. |
| Docker/Compose/CI | `docker compose config`, `docker compose up`, CI job | Not run; Docker command missing | Config files exist; images, health checks and full deployment remain unverified. |

Missing important test scenarios: MockMvc auth/validation/error contract; PostgreSQL repository and migration initialization in automated CI; IDOR matrix for every domain; resume replace/review/download/delete ownership and malformed/encrypted uploads; all job filters/pagination; application edit/status-history/duplicate/deadline workflows; cover-letter invalid AI output and owner boundaries; complete interview progression; analytics correctness across modules; portal cleanup/replay/failure/unknown outcome; automation worker timeout/restart and screenshot evidence; broader UI journeys from signup through saved job, match, letter, tracker and interview.

## H. Prioritized remaining-work checklist

### P0 — essential correctness/security before calling the product complete

- [ ] **Replace unsafe production defaults.** `application.yml` contains a predictable development JWT secret and DB password fallback. Fail startup in a production profile if secret values are missing/short; document secret manager injection. Acceptance: production config cannot start with defaults, and secret is absent from tracked files/logs.
- [ ] **Complete ownership and integration test baseline.** Add MockMvc plus PostgreSQL-backed tests for auth, uploads, profile, applications, letters, interview, simulated submissions and automation. Acceptance: each owner-scoped ID returns 404 to a second user; test workflow runs from clean migrations in CI.
- [ ] **Resolve tracker data-integrity gaps.** Define valid application status transitions and duplicate semantics for null job IDs and imports; implement required fields/associations. Acceptance: no false `SUBMITTED`; transitions/history reflect every edit and duplicate is deterministic.
- [ ] **Audit and constrain uploaded/portal data.** Scanning, MIME/signature limits, disk/database storage, access, backups, deletion and retention are undeveloped or partially implemented. Acceptance: malformed files fail safely, files stay private, deletion removes associated private bytes per policy, and tests cover size/path/type/ownership.

### P1 — missing/partial original requirements

- [ ] **Structured resume review lifecycle.** Extend extraction to reviewable education, experience, projects, certifications and contact details without inventing facts; preserve immutable version relationship and link selected version to match/application. Tests: valid/empty/encrypted/corrupt PDF/DOCX, correction roundtrip, replacement/version, owner isolation.
- [ ] **Complete job filters and discovery.** Add title/company/location/skill/employment filters and stable sort/page; label seeded sample data, maintain import attribution and dedupe. Tests: criteria, paging, boundary, ownership, save/unsave.
- [ ] **Complete application tracker.** Add deadlines, follow-ups, notes edits, resume/letter linkage, filters/search, detail/history UI and requested status model. Tests: history append-only, associations owner-safe, duplicate behavior and analytics.
- [ ] **Close interview prep specification.** Expand question seed coverage/difficulty, AI questions if provider configured, feedback dimensions, strengths/weaknesses, recommendations, history/progress and safe statement that code was not executed. Tests: deterministic fallback and invalid AI structure.
- [ ] **Broaden analytics/dashboard.** Persisted match history, skill gaps, upcoming follow-ups, automation outcomes, interview progress and activity. Tests: empty state and cross-module account-isolated aggregate correctness.
- [ ] **Finish portal automation resiliency.** Test failure screenshot evidence, timeout, API restart transitions, cleanup and duplicate submission with repeatable database fixtures. Do not retry uncertain outcomes without verification.
- [ ] **Full frontend test coverage.** Add form/navigation/protected route/loading/error tests and user journeys. Acceptance: browser test from registration through persisted career workflow and all three simulated portals.

### P2 — AI and workflow improvements

- [ ] **AI-powered resume improvement (absent).** Provider abstraction, truthful rewrite suggestions, before/after, confirm-before-apply, user edits/copy/download, no fabricated numbers. Requires optional AI account for remote mode; local fallback must be explicitly labeled. Tests: factual input constraints, prompt injection handling, output validation, consent and user approval.
- [ ] **AI-generated interview questions (absent).** Extend provider contract, topic/difficulty and validated question schema; fallback to seeds when no provider. Tests: output schema, provider failure, local flow and persistence.
- [ ] **Richer mock interview feedback (partial).** Measure correctness/completeness separately from communication/problem-solving; distinguish model opinion from measured data and persist session summary. Tests with deterministic rubric fixtures and clear UI labels.
- [ ] **Cover-letter complete workflow.** Missing-information prompts, truthful generation, regeneration/history, copy/download and link to application; ensure PII is disclosed before remote provider transmission.

### P3 — production, security, deployment and polish

- [ ] Add refresh/revocation strategy or documented short-token re-auth; production CSP/security headers, rate limiting and abuse controls; audit log that excludes secrets/PII.
- [ ] Define retention/deletion, encryption-at-rest and backups for resumes, DB portal bytes, AI transfer disclosures and account deletion.
- [ ] Test Docker Compose and CI on a Docker-capable runner; align container DB version with supported Flyway; verify health checks and frontend proxy/API path.
- [ ] Execute deployment preparation only with chosen host, secrets, database, TLS, backups and verification permissions; no deployment currently exists.
- [ ] Fix README/environment path consistency and use a clean setup guide; replace the stale root `.env` URL values only when the operator chooses to edit their local ignored configuration.

## I. Security and production-readiness findings

| Severity | Finding and evidence | Concrete remediation |
|---|---|---|
| High | `application.yml` supplies predictable JWT secret and database password defaults. JWT code rejects keys under 32 bytes, but the checked-in fallback itself is a known value. | Separate local/test from production config; require externally injected strong secret and DB credentials in production; rotate any deployed values. |
| High | JWT bearer is accessible to JavaScript (although held only in memory), and logout merely forgets the token. There is no revocation or refresh rotation; a copied token remains valid until expiry. | Keep short lifetime; implement carefully designed revocation/refresh or clearly document bearer threat and logout semantics; test expiry/logout. Avoid introducing localStorage. |
| High | Resume content is stored locally as files; simulated portal submission also persists resume bytes plus applicant name/email and answers in PostgreSQL. No encryption/retention/deletion policy is implemented. | Minimize duplicate storage; define per-user retention/deletion; configure encrypted disk/database/backups and access; add deletion integration tests. |
| High | Optional remote AI sends personal name, resume text/skills and job description to configured provider. No UI consent/data-transfer disclosure observed. | Present explicit disclosure/consent and provider choice before transmission; redact/minimize; document provider retention and user deletion expectations. |
| Medium | No rate limiting on registration/login, uploads, AI generation, or automation submissions. | Add server-side rate limits/quotas and abuse tests; cap costly generation and automation. |
| Medium | Uploads have 5 MB Spring request limits and parser/read checks, generated path keys and normalized storage root. However file type is extension/signature based and no malware scanner or isolated parser service is present; decompression/parser resource exhaustion needs review. | Verify content signatures and limits, zip-bomb bounds for DOCX, malware scanning and parser resource/time limits; test corrupt/encrypted/oversized uploads. |
| Medium | Security is stateless bearer and CSRF is disabled; CORS is one configured origin with credentials disabled. That is coherent for bearer-only APIs, but deployment must not switch to cookie auth without revisiting CSRF. | Keep strict origin allowlist; add HTTPS/CSP/security headers, TLS, secure deployment config and tests; revisit CSRF if cookie auth is introduced. |
| Medium | No complete systematic IDOR, validation, unexpected-error, controller, and data-retention tests. | Add MockMvc/database tests for all owner IDs, malformed values, database errors and safe error bodies. |
| Low | No secrets found in tracked `.env` (it is untracked/ignored); `.env.example` contains placeholders. Targeted key/private-key scan did not identify obvious credential literals. | Continue secret scanning in CI; rotate credentials if a real secret was ever committed in Git history. Do not treat a clean working-tree scan as a history audit. |
| Operational | PostgreSQL 18.6 is installed locally; prior run logs Flyway warned support tested through PG17. Docker/Compose/deployment are unverified. | Prefer the specified PostgreSQL 16 path or validate PG18 compatibility; test clean migrations, health checks and backup/restore in a controlled environment. |

No evidence indicates SQL string concatenation from user input in the inspected domain paths; repositories use JPA. This is a source review, not a formal penetration test or full Git-history secret scan.

## J. Repository state and audit-time environment

Workspace command runner and filesystem are accessible. Directory is an existing project and Git repository, branch `main`, HEAD `5d17297` (`Changes made to backend again`), tracking `origin/main`. The worktree has user changes; no commit, reset, deletion, dependency install, infrastructure edit, or application-code edit was performed.

Modified files at the start/end of this audit:

- `README.md`, `docs/API_DOCUMENTATION.md`, `docs/ARCHITECTURE.md`, `docs/DATABASE_SCHEMA.md`, `docs/IMPLEMENTATION_STATUS.md`, `docs/TESTING.md`
- `backend/src/main/java/com/careerpilot/automation/AutomationRunRepository.java`, `AutomationRunService.java`, `ProcessPortalAutomationRunner.java`, `backend/src/main/resources/application.yml`
- `frontend/playwright.config.ts`, `frontend/scripts/portal-automation.mjs`, `frontend/src/App.tsx`, `frontend/src/api/client.ts`, `frontend/src/api/types.ts`, `frontend/src/components/AppShell.tsx`

Untracked existing work:

- `backend/src/main/java/com/careerpilot/automation/RealJobSiteAdapter.java`
- `docs/REAL_SITE_AUTOMATION.md`
- `frontend/src/pages/AutomationPage.tsx`, `frontend/e2e/automation-center.spec.ts`, `frontend/e2e/automation-live.spec.ts`, `frontend/e2e/fixtures/integration-resume.pdf`

No network/install attempts were made. `git diff --check` reported no whitespace errors (only CRLF normalization warnings). Git-visible changes are the user's work and must be preserved. `.tools/`, `.env`, node_modules, build output, local DB and evidence are ignored.

## K. Recommended implementation order

1. Address production defaults and security tests before exposing the app.
2. Build repeatable PostgreSQL-backed integration tests for current vertical slices and make every owner boundary measurable.
3. Finish resume lifecycle and application tracker data relationships/flows.
4. Fill job-search filters and cross-module matching/analytics workflows.
5. Add AI resume improvement and interview generation through the provider boundary, then richer mock feedback; maintain transparent local fallbacks.
6. Extend deterministic E2E coverage through real local API/DB; add portal failure/restart/retry checks.
7. Verify Docker/Compose/CI on an approved Docker host, resolve PostgreSQL compatibility, then conduct deployment and privacy/retention review.

## Audit file edits

This report was added after inspecting the master spec and current repository. The README and implementation status were also revised only to correct run/configuration guidance and to append this audit result; application behavior was not changed. The test table above preserves the distinction between historical passing results and audit-time observations.
