# Testing

## Verified commands

From `frontend/`:

```powershell
npm run typecheck
npm test
npm run build
$env:PLAYWRIGHT_BROWSER = 'msedge'
npm run test:e2e
Remove-Item Env:PLAYWRIGHT_BROWSER
```

The Playwright suite also uses bundled Chromium by default after `npx playwright install chromium`. The three portal browser tests passed against installed Microsoft Edge. Their API responses are mocked: they verify the user-visible portal flow, explicit consent, payload formation, confirmation display, and independent verification request, but do not prove PostgreSQL persistence.

From `backend/`:

```powershell
mvn test
```

The Maven test suite passed 14 tests. It covers auth/JWT, resume parsing, matching, interview scoring, local cover-letter generation, simulated-portal consent, and idempotent retry logic.

## Verification still required

PostgreSQL-backed migration/repository tests, Spring MVC authentication and cross-user isolation tests, end-to-end browser tests against live API and database services, and Docker Compose execution remain unverified. They need PostgreSQL/Docker availability. Never count mocked API responses as durable persistence evidence, and never use a fake AI provider to claim live provider behavior.
