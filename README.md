# CareerPilot AI

CareerPilot AI is a full-stack career management application built with React JavaScript, Spring Boot 3.5, Java 21, Maven, and MySQL 8.4. It includes resume parsing/versioning, job tracking and user-provided job imports, explainable skill matching, editable cover-letter drafts, interview practice, analytics, a user-approved automation center with persistent run history, and three fictional career-portal simulations.

This repository is an active, incomplete implementation. The requirement-by-requirement audit, exact environment inventory, historical test evidence, remaining work, and security findings are in [docs/PROJECT_AUDIT.md](docs/PROJECT_AUDIT.md). The PostgreSQL V1–V8 migration chain and data are preserved; the application now has an independent MySQL Flyway baseline. Database migration to MySQL has not been executed because no MySQL server or Docker is installed. See [docs/MIGRATION_PROGRESS.md](docs/MIGRATION_PROGRESS.md) for current observed state and limitations. The protected Automation Center runs consented Playwright simulations against fictional portals; it does not apply to real employers.

## Requirements

- Node.js 20+ and npm.
- Java 21 and Maven 3.9+ for the backend.
- MySQL 8.4 for local backend execution. Docker Compose is configured but remains unverified because Docker is unavailable in the current environment.
- Microsoft Edge or Playwright Chromium for browser tests.

## Local development

For the exact Windows PowerShell instructions, including the existing local JDK/PostgreSQL tools, three-terminal startup, matching CORS/API/automation ports, health checks, smoke workflows, and stop commands, follow [the setup guide in the project audit](docs/PROJECT_AUDIT.md#windows-powershell-setup-and-run-guide). The root `.env` file is read by Docker Compose; Spring Boot and Vite do not automatically load it for separate local processes. Set environment variables in each process/terminal as shown in that guide. Backend default port is 8080; Vite development default is 5173; align `PORT`, `FRONTEND_ORIGIN`, `VITE_API_BASE_URL`, and automation URLs when selecting other ports.

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
