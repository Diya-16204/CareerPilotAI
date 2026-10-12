# API Documentation

Base path: `/api/v1`. JSON, UTF-8. Authenticated requests use a short-lived bearer access token. The `userId` is always derived from the principal. IDs are UUID strings. Timestamps are ISO-8601 UTC. List responses use `{items, page, size, totalItems, totalPages}`; `page` is zero-based, `size` defaults to 20 and is capped at 100. Sort is `field,asc|desc` and only documented fields are accepted.

## Error contract

`{timestamp, status, errorCode, message, path, fieldErrors?}`. Validation errors return 400; unauthenticated 401; forbidden 403; missing resources 404; conflict 409; unexpected failures 500 with a safe generic message. Stack traces and sensitive inputs are never returned.

## Initial endpoints

| Method/path | Auth | Contract |
|---|---|---|
| `POST /auth/register` | Public | `{email,password,name}` -> 201 `{user,accessToken,tokenType,expiresIn}`; validates email and password policy. |
| `POST /auth/login` | Public | `{email,password}` -> 200 token response; generic invalid-credential error. |
| `GET /users/me` | Required | -> current user/profile DTO. |
| `GET /users/me/profile` | Required | -> editable profile DTO. |
| `PATCH /users/me/profile` | Required | validated editable profile fields -> updated DTO. |
| `POST /users/me/password` | Required | `{currentPassword,newPassword}` -> 204 after password verification. |
| `GET /jobs` | Optional | query, title, company, location, skill, employmentType, page, size, sort -> page DTO. |
| `GET /jobs/{id}` | Optional | -> job detail including source/sample attribution. |
| `POST /jobs/import` | Required | title, company, location, employmentType, description, skills -> private user-provided job; duplicate title/company conflicts. |
| `PUT /jobs/{id}/saved` | Required | idempotently save job -> `{saved:true}`. |
| `DELETE /jobs/{id}/saved` | Required | idempotently unsave -> 204. |
| `POST /resumes` | Required | multipart `file`, title -> parsed resume/version summary; size/content checked. |
| `GET /resumes` | Required | paged owner's resumes. |
| `GET /resumes/{id}` | Required | owner-only structured preview and version list. |
| `POST /matches` | Required | `{resumeVersionId,jobId}` -> score, formula, matched/missing/partial evidence and recommendations. |
| `POST /applications` | Required | `{jobId?,resumeVersionId?,company,title,source}` -> 201 application and initial status history. |
| `GET /applications` | Required | paged owner records with filters. |
| `PATCH /applications/{id}/status` | Required | `{status,note?,followUpAt?}` -> updated record and append-only history. |
| `GET /analytics/summary` | Required | persisted counts/status distributions/recent activity; zero-data response is valid. |
| `GET /cover-letters` | Required | paged owner-only cover-letter drafts. |
| `POST /cover-letters/draft` | Required | `{jobId,resumeVersionId,tone}` -> persisted draft labeled `LOCAL_TEMPLATE` or `OPENAI` according to the configured provider. |
| `PUT /cover-letters/{id}` | Required | update the owner's draft body. |
| `GET /interviews/questions` | Required | topic/difficulty filters and paging. |
| `POST /interviews/sessions` | Required | role/topic/difficulty -> persisted session. |
| `POST /interviews/sessions/{id}/answers` | Required | question/answer -> persisted rubric feedback. |
| `GET /interviews/sessions` | Required | paged owner-only interview history. |
| `GET /interviews/sessions/{id}` | Required | owner-only session and answer details. |
| `GET /simulated-portals/{portal}/jobs` | Public | locally seeded fictional jobs for `portal_a`, `portal_b`, or `portal_c`. |
| `POST /simulated-portals/{portal}/submissions` | Required | `Idempotency-Key: UUID`; requires explicit consent, a validated PDF/DOCX attachment (base64, max 5 MB), applicant details, job key, and portal-specific fields (Portal A cover letter; Portal B education and experience; Portal C role-specific answers). Persists an owner-scoped simulation record and returns its confirmation ID. Replays and duplicate same-role submissions are rejected or return the existing confirmation. |
| `GET /simulated-portals/submissions/{confirmationId}` | Required | owner-only sanitized verification response; returns `PERSISTED` only for a saved record. |
| `POST /automation-runs` | Required | `Idempotency-Key: UUID`; portal/job, owned resume version, exact screening answers, portal-specific fields and explicit `consent: true` -> 202 with durable run ID/state. Runs the local Playwright workflow asynchronously and reaches `VERIFIED_SUCCESS` only after owner-scoped persistence is independently checked. Reusing a key with the same request returns the existing run; a changed request returns 409. |
| `GET /automation-runs` | Required | paged owner-only automation run history. |
| `GET /automation-runs/{id}` | Required | owner-only run status and sanitized outcome; foreign-owner lookups return 404. |
| `POST /auto-apply/sessions` | Required | `{jobId,resumeVersionId,applicationUrl?,tone?,questions?}` -> persistent per-job preparation session. Questions are copied from the application by the user; the route does not crawl employer pages. |
| `GET /auto-apply/sessions` / `GET /auto-apply/sessions/{id}` | Required | list the latest 50 or retrieve one owner-only preparation session. |
| `POST /auto-apply/sessions/{id}/prepare` | Required | retry preparation of a failed or incomplete session; confirmed answers for matching questions are retained. |
| `PUT /auto-apply/sessions/{id}/answers/{key}` | Required | save a user-entered answer, optionally confirm it and remember it for the same question. Sensitive/consequential answers cannot be remembered or AI drafted. |
| `PATCH /auto-apply/sessions/{id}/draft` | Required | update the editable cover-letter draft. |
| `POST /auto-apply/sessions/{id}/approve` | Required | explicit consent to approve the current content hash after required information is confirmed and the resume file is available. Approval does not submit anything. |
| `POST /auto-apply/sessions/{id}/continue-manually` | Required | only for an approved unchanged draft; transitions to `REQUIRES_MANUAL_ACTION`. It does not submit to an employer. |

OpenAPI is exposed through Swagger UI in development. The default portal browser suite mocks HTTP responses; a separate opt-in live Portal A test drives the UI against Spring API and PostgreSQL. The automation-run endpoint was exercised through the local Playwright browser and PostgreSQL for all three portal workflows; successful completion, idempotent retry, changed-request conflict, and owner isolation were observed. Interrupted `PENDING`/`RUNNING` records are marked `UNVERIFIED` at API startup because a previous browser result cannot safely be assumed. Portal routes are local simulations only and cannot submit to real employers.

The protected React Automation Center at `/app/automation` provides the consent, live status, and history interface for fictional portal tests. The separate `/app/auto-apply` preparation flow does not invoke this Playwright runner. External employer-site adapters are not registered; no real employer application is submitted. The safe extension contract and authorization process are documented in `docs/REAL_SITE_AUTOMATION.md`.
