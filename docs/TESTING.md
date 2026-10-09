# Testing Strategy

Backend: JUnit 5 and Mockito for deterministic services; MockMvc for validation, errors, JWT, and ownership; PostgreSQL-backed integration tests for migrations/repositories/transactions; fixtures with no external AI calls. Frontend: Vitest/Testing Library for forms, navigation, protected routes, loading/errors, upload, job filters, applications, and interview flows; TypeScript check and production build. End-to-end: Playwright against local services and three simulated portals, verifying durable confirmation records, duplicate prevention, and cross-user denial.

Run only tests supported by the available environment and record exact command/results in `IMPLEMENTATION_STATUS.md`. A missing Docker, PostgreSQL service, browser, or dependency is a blocker, not a pass. Never use a fake provider for tests that claim real provider behavior.
