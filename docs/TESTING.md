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

The Playwright suite also uses bundled Chromium by default after `npx playwright install chromium`. With installed Microsoft Edge, the default suite most recently passed 4 tests: the three portal UI flows and the mocked Automation Center flow. Two live tests are skipped by default. Mocked responses verify the UI contract only and do not prove database persistence.

From `backend/` on Windows, use Java 21 and the checked-in Maven Wrapper:

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B package
```

The latest full Maven package build passed all 16 tests and created the executable Spring Boot JAR. Tests cover auth/JWT, PDF/DOCX parsing, matching, interview scoring, local and optional AI-provider contracts, and simulated-portal consent/idempotency.

## Verification still required

Observed integration results: all eight Flyway migrations were validated/applied on PostgreSQL 18.6 and Hibernate schema validation passed. The live Automation Center test passed through React, Spring Boot, the Playwright browser worker, and PostgreSQL. Direct API/browser runs reached `VERIFIED_SUCCESS` for all three fictional portals; duplicate retry, changed-request conflict, and cross-user run isolation were checked. Docker is unavailable, so Docker Compose execution remains unverified. The optional AI provider was tested against a local HTTP stub; no real provider key/account is configured.

To run the live Automation Center test, set `CAREERPILOT_LIVE_E2E=1` and `CAREERPILOT_E2E_BASE_URL` to the already-running Vite origin. Ensure `VITE_API_BASE_URL` points to the Spring API and that API automation URL settings point to the same local frontend/API services. The test uses a synthetic, checked-in resume fixture and creates a new isolated test account.
