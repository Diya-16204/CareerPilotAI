# CareerPilot AI

CareerPilot AI is a full-stack career management application built with React, TypeScript, Spring Boot, and PostgreSQL. It includes resume parsing/versioning, job tracking and user-provided job imports, explainable skill matching, editable local cover-letter drafts, interview practice, analytics, and three fictional career-portal simulations.

This repository is an active implementation. Tested features, blockers, and unmet specification criteria are tracked in [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md). It is not yet a production-ready deployment: PostgreSQL migrations, Docker Compose, and durable portal submissions have not been integration-tested in this environment.

## Requirements

- Node.js 20+ and npm.
- Java 21 and Maven 3.9+ for the backend.
- PostgreSQL 16 for local backend execution. Docker Compose is the intended convenience path, but Docker must be installed separately.
- Microsoft Edge or Playwright Chromium for browser tests.

## Local development

1. Configure PostgreSQL and environment values from `.env.example`. The API expects a database named `careerpilot` by default.
2. Start the API from `backend/` with `mvn spring-boot:run`.
3. In another terminal, run `npm install` and `npm run dev` from `frontend/`.
4. Open the Vite URL shown in the terminal. `VITE_API_BASE_URL` defaults to `http://localhost:8080/api/v1`.

The application applies Flyway migrations at backend startup and validates the Hibernate model against the migrated schema. Local deterministic matching and interview evaluation work without an AI credential. Cover-letter drafts are clearly labeled local templates; a live LLM provider is not configured.

## Verification

Run from `frontend/`:

```powershell
npm run typecheck
npm test
npm run build
npm run test:e2e
```

Playwright tests run against the three simulation flows and currently mock their API responses. Install Playwright Chromium with `npx playwright install chromium`, or set `$env:PLAYWRIGHT_BROWSER = 'msedge'` when Microsoft Edge is installed. Backend tests run from `backend/` using `mvn test`.

See [docs/TESTING.md](docs/TESTING.md), [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md), and [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) for details. Simulated portal applications are explicitly test-only and never contact real employers.
