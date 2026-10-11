# CareerPilot AI

CareerPilot AI is a full-stack career management application built with React JavaScript, Spring Boot 3.5, Java 21, Maven, and MySQL 8.4. It includes resume parsing/versioning, job tracking and user-provided job imports, explainable skill matching, editable cover-letter drafts, interview practice, analytics, a user-approved automation center with persistent run history, and three fictional career-portal simulations.

This repository is an active, incomplete implementation. The requirement-by-requirement audit, environment inventory, historical test evidence, remaining work, and security findings are in [docs/PROJECT_AUDIT.md](docs/PROJECT_AUDIT.md). The PostgreSQL migration history and data are preserved; the application uses an independent MySQL Flyway baseline. Apply the MySQL migrations only after configuring and verifying the intended local MySQL database. See [docs/MIGRATION_PROGRESS.md](docs/MIGRATION_PROGRESS.md) for observed state and limitations. The protected Automation Center runs consented Playwright simulations against fictional portals; it does not apply to real employers.

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
