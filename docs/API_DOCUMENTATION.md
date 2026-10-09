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
| `POST /cover-letters/draft` | Required | `{jobId,resumeVersionId,tone}` -> explicitly labeled local template draft. |
| `PUT /cover-letters/{id}` | Required | update the owner's draft body. |
| `GET /interviews/questions` | Required | topic/difficulty filters and paging. |
| `POST /interviews/sessions` | Required | role/topic/difficulty -> persisted session. |
| `POST /interviews/sessions/{id}/answers` | Required | question/answer -> persisted rubric feedback. |
| `GET /interviews/sessions` | Required | paged owner-only interview history. |
| `GET /interviews/sessions/{id}` | Required | owner-only session and answer details. |
| `GET /simulated-portals/{portal}/jobs` | Public | locally seeded fictional jobs for `portal_a`, `portal_b`, or `portal_c`. |
| `POST /simulated-portals/{portal}/submissions` | Required | `Idempotency-Key: UUID`; requires explicit consent, a validated PDF/DOCX attachment (base64, max 5 MB), applicant details, job key, and answers. Persists an owner-scoped simulation record and returns its confirmation ID. |
| `GET /simulated-portals/submissions/{confirmationId}` | Required | owner-only sanitized verification response; returns `PERSISTED` only for a saved record. |

OpenAPI is exposed through Swagger UI in development. Portal browser tests currently mock HTTP responses; actual migration and persistence verification require PostgreSQL and have not run. Portal routes are local simulations only and cannot submit to real employers. The Playwright suite exercises each form and independently checks the confirmation response, but it does not yet drive the UI against the live backend/database.
