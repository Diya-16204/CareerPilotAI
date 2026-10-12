# CareerPilot AI

CareerPilot AI is a full-stack career management application built with React JavaScript, Spring Boot 3.5, Java 21, Maven, and MySQL 8.4. It includes resume parsing/versioning, job tracking and user-provided job imports, explainable skill matching, editable cover-letter drafts, interview practice, analytics, a user-approved automation center with persistent run history, and three fictional career-portal simulations.

The protected **Auto Apply** workflow (`/app/auto-apply`) prepares one job at a time using a selected reviewed resume, existing cover-letter generation, optional LinkedIn/Instagram referral drafts, application questions, and persisted preparation history. The optional [Chrome helper](docs/AUTO_APPLY_BROWSER_AUTOFILL.md) captures visible employer-form labels/types/options/required flags into the saved session and can fill supported empty fields after approval. Users review and save answers on Auto Apply before a distinct Final Apply page. Its Apply Now action records only a fictional CareerPilot demo submission; real employer applications must be reviewed and submitted by the user. It never fills sensitive answers, attaches a file, or bypasses CAPTCHA/MFA. The separate Playwright Automation Center is limited to fictional test portals.

This repository is an active, incomplete implementation. The requirement-by-requirement audit, environment inventory, historical test evidence, remaining work, and security findings are in [docs/PROJECT_AUDIT.md](docs/PROJECT_AUDIT.md). The PostgreSQL migration history and data are preserved; the application uses an independent MySQL Flyway baseline. Apply the MySQL migrations only after configuring and verifying the intended local MySQL database. See [docs/MIGRATION_PROGRESS.md](docs/MIGRATION_PROGRESS.md) for observed state and limitations. The protected Automation Center runs consented Playwright simulations against fictional portals; it does not apply to real employers.

## Live deployment

- Frontend: [https://career-pilot-ai-blond-seven.vercel.app](https://career-pilot-ai-blond-seven.vercel.app/)
- Backend readiness: [https://careerpilotai-he15.onrender.com/actuator/health/readiness](https://careerpilotai-he15.onrender.com/actuator/health/readiness)
- Database: Aiven MySQL 8.4 (connection and migrations reported verified by the project owner).

The live frontend and backend were checked on 2026-10-11: the frontend homepage returned HTTP 200, backend readiness returned `UP`, and the public jobs API returned HTTP 200 with CORS allowing the Vercel origin. Warm request times in that check were 0.9 seconds for the homepage and about 2.3–2.4 seconds for the API; this does not measure Render's idle cold start. Client-side navigation to sign-in and registration rendered; direct URL refresh at `/login` returned Vercel 404. `frontend/vercel.json` configures the SPA fallback, but that fix is not live until Vercel deploys a commit containing it. The referral saved-jobs lookup and slow-start handling correction are present in this repository; whether they are live has not been rechecked, and the backend must deploy the matching route before the live UI can use it. Authenticated workflows have not been verified in the deployed environment. Render Free sleeps when idle and can take around 50 seconds or more to wake; its filesystem is ephemeral, so resume uploads are not durable. The hosted Automation Center lacks its Node/Playwright worker. See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for setup and limitations.

### Known hosted problems

- **Resume file unavailable:** A user-provided Automation Center screenshot shows the resume name/version in the form but the backend returns `Resume file is unavailable.` Resume metadata is stored in MySQL, while the PDF/DOCX bytes are stored under `UPLOAD_DIR` on the backend filesystem. Render Free's filesystem is ephemeral; after a restart, spin-down, or redeploy, metadata can remain while the file is gone. Re-uploading the resume to the deployed site can restore it temporarily. Durable use requires private persistent/object storage; do not assume a displayed resume entry means its file is present.
- **Hosted automation worker missing:** [`backend/Dockerfile`](backend/Dockerfile) builds and runs Spring Boot only. It does not package Node.js, Playwright, a browser, or the sibling frontend files expected by `ProcessPortalAutomationRunner`. The worker is configured for loopback URLs and a local fictional portal. Hosted browser automation is not operational just because the backend is running; resume bytes are also ephemeral on the current Render Free setup.
- **Simulation only:** Automation Center's portals are fictional CareerPilot test portals. Real employer-site auto-application adapters are not implemented. No real employer receives an application from this feature.
- **Auto Apply limits:** Employer-form discovery is an optional unpacked Chrome extension action: open each visible form step and capture it while the matching Auto Apply session remains open. Hidden/custom widgets and resume uploads may need manual handling. The feature does not submit to real employers. Local AI mode produces a labeled cover-letter template; AI answer drafting requires a configured private `AI_API_KEY`. V3 and V4 add the Auto Apply session/form persistence schema; V4 has been added but was not applied to any database during this implementation. Deploy and verify the backend migration before using this new flow online.
- **Other deployment limits:** Render Free can sleep while idle. Authenticated end-to-end workflows have not been verified against the deployed account/database. See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) and [docs/PROJECT_AUDIT.md](docs/PROJECT_AUDIT.md) for evidence and remaining work.

## Requirements

- Node.js 20+ and npm.
- Java 21 and Maven 3.9+ for the backend.
- MySQL 8.4 for local backend execution.
- Microsoft Edge or Playwright Chromium for browser tests.

## Local development

Run the backend directly with Java 21 and the Maven Wrapper, and run the frontend with npm. Spring Boot and Vite do not automatically load the root `.env`; set required environment variables in each process. The PowerShell commands below use the configured local MySQL target. Backend default port is 8080; Vite development default is 5173. Keep `FRONTEND_ORIGIN`, `VITE_API_BASE_URL`, and automation URLs aligned if you change ports.

Quick start in two PowerShell terminals (configure the MySQL database and supply real local credentials first; Spring Boot startup runs Flyway migrations):

```powershell
# Terminal 1 — backend
$env:DATABASE_URL = 'jdbc:mysql://localhost:3306/careerpilot?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=UTC'
$env:DATABASE_USERNAME = '<your-local-mysql-user>'
$env:DATABASE_PASSWORD = '<your-local-mysql-password>'
$env:JWT_SECRET = '<a private random secret of at least 32 bytes>'
$env:FRONTEND_ORIGIN = 'http://localhost:5173'
Set-Location backend
.\mvnw.cmd spring-boot:run

# Terminal 2 — frontend (run from the repository root)
Set-Location frontend
npm ci
npm run dev -- --host 127.0.0.1
```

Use a Java 21 `JAVA_HOME` before starting the backend. Do not put credentials in tracked files or overwrite the existing root `.env`.

The application applies Flyway migrations at backend startup and validates the Hibernate model against the migrated schema. Local deterministic matching and interview evaluation work without an AI credential. Cover-letter drafts use an explicitly labeled local template by default. An optional OpenAI-compatible provider is available through `AI_PROVIDER=openai` and `AI_API_KEY`; see [docs/AI_PROVIDER.md](docs/AI_PROVIDER.md). No paid remote account was used for verification.

## Verification

Run from `frontend/`:

```powershell
npm test
npm run build
npm run test:e2e
```

Playwright's default suite tests the three simulation flows and the mocked Automation Center. Set `$env:PLAYWRIGHT_BROWSER = 'msedge'` when Microsoft Edge is installed. Live E2E requires a running MySQL database, API, and Vite; enable it only with `$env:CAREERPILOT_LIVE_E2E = '1'`. Backend tests run from `backend/` using `.\mvnw.cmd -B test` on Windows or `./mvnw -B test` on macOS/Linux.

See [docs/TESTING.md](docs/TESTING.md), [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md), [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md), and [docs/REAL_SITE_AUTOMATION.md](docs/REAL_SITE_AUTOMATION.md) for details. Historical test results are not a claim that services are currently running. Simulated portal applications are test-only and never contact real employers.




























































































































































































































































