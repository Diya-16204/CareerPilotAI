# CareerPilot AI

CareerPilot AI is a full-stack career management application built with React, TypeScript, Spring Boot, and PostgreSQL. It includes resume parsing/versioning, job tracking and user-provided job imports, explainable skill matching, editable cover-letter drafts, interview practice, analytics, and three fictional career-portal simulations.

This repository is an active implementation. Tested features, blockers, and unmet specification criteria are tracked in [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md). PostgreSQL migrations and core REST persistence flows have been integration-tested locally. Docker Compose remains unverified, and durable Playwright run orchestration/history is still outstanding.

## Requirements

- Node.js 20+ and npm.
- Java 21 and Maven 3.9+ for the backend.
- PostgreSQL 16 for local backend execution. Docker Compose is the intended convenience path, but Docker must be installed separately.
- Microsoft Edge or Playwright Chromium for browser tests.

## Local development

1. Configure PostgreSQL and environment values from `.env.example`. The API expects a database named `careerpilot` by default.
2. Start the API from `backend/` with the Maven Wrapper: `.\mvnw.cmd -B spring-boot:run` on Windows or `./mvnw -B spring-boot:run` on macOS/Linux. Java 21 is required.
3. In another terminal, run `npm install` and `npm run dev` from `frontend/`.
4. Open the Vite URL shown in the terminal. `VITE_API_BASE_URL` defaults to `http://localhost:8080/api/v1`.

The application applies Flyway migrations at backend startup and validates the Hibernate model against the migrated schema. Local deterministic matching and interview evaluation work without an AI credential. Cover-letter drafts use an explicitly labeled local template by default. An optional OpenAI-compatible provider is available through `AI_PROVIDER=openai` and `AI_API_KEY`; see [docs/AI_PROVIDER.md](docs/AI_PROVIDER.md). No paid remote account was used for verification.

## Verification

Run from `frontend/`:

```powershell
npm run typecheck
npm test
npm run build
npm run test:e2e
```

Playwright's default suite tests the three simulation flows with mocked API responses. Install Playwright Chromium with `npx playwright install chromium`, or set `$env:PLAYWRIGHT_BROWSER = 'msedge'` when Microsoft Edge is installed. A separate live Portal A test exercises registration, the React portal UI, Spring API, and PostgreSQL: start the backend/database, set `$env:CAREERPILOT_LIVE_E2E = '1'`, then run `npm run test:e2e -- e2e/live-api.spec.ts`. Backend tests run from `backend/` using `.\mvnw.cmd -B test` on Windows or `./mvnw -B test` on macOS/Linux.

See [docs/TESTING.md](docs/TESTING.md), [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md), and [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) for details. Simulated portal applications are explicitly test-only and never contact real employers.
