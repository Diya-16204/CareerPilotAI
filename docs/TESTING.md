# Testing

## Verified commands

From `frontend/`:

```powershell
npm test
npm run build
$env:PLAYWRIGHT_BROWSER = 'msedge'
npm run test:e2e
```

The Playwright suite also uses bundled Chromium by default after `npx playwright install chromium`. With installed Microsoft Edge, the default suite most recently passed 4 tests: the three portal UI flows and the mocked Automation Center flow. Two live tests are skipped by default. Mocked responses verify the UI contract only and do not prove database persistence.

From `backend/` on Windows, use Java 21 and the checked-in Maven Wrapper:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B package
```

The latest Java 21 Maven `verify` build passed all 16 tests and created the executable Spring Boot JAR. Tests cover auth/JWT, PDF/DOCX parsing, matching, interview scoring, local and optional AI-provider contracts, and simulated-portal consent/idempotency.

## Verification still required

Historical PostgreSQL integration evidence: all eight Flyway migrations were validated/applied on PostgreSQL 18.6 and Hibernate schema validation passed. Current fresh frontend verification after the JavaScript conversion: Vitest 1 passed; Vite build passed; isolated Microsoft Edge Playwright run on Vite port 4175 passed 4 mocked tests with 2 opt-in live tests skipped. Current fresh Java 21 Maven `verify`: 16 tests passed, 0 failures/errors/skips. The MySQL baseline has not been run against a server, so MySQL schema validation and live DB/API integration remain unverified. The optional AI provider was tested against a local HTTP stub; no real provider key/account is configured.

To run the live Automation Center test, first provision an isolated MySQL database and start the API with `CAREERPILOT_LIVE_E2E=1`; set `CAREERPILOT_E2E_BASE_URL` to the already-running Vite origin. Ensure `VITE_API_BASE_URL` points to the Spring API and API automation URL settings point to the same local frontend/API services. The test uses a synthetic, checked-in resume fixture and creates a new isolated test account. Do not point it at the preserved PostgreSQL source database.
