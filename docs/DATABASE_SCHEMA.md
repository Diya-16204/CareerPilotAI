# Database Schema

PostgreSQL is the source of truth and Flyway migrations are authoritative. UUID primary keys and UTC `timestamptz` audit fields are used. User-owned records have a non-null `user_id`; APIs derive it from the authenticated principal rather than accepting it from request bodies.

| Table | Relationships and constraints |
|---|---|
| `users` | Unique normalized email; BCrypt hash; role; created/updated timestamps. |
| `user_profiles` | Unique FK to user; cascade only with deliberate account deletion; optimistic `version`. |
| `skills` | Unique normalized name and optional canonical category. |
| `user_skills` | FKs to user and skill; unique `(user_id, skill_id)`, proficiency and source. |
| `resumes` | FK owner; title, active version pointer, timestamps. |
| `resume_versions` | FK resume; unique `(resume_id, version_number)`; private storage key, extracted text/JSON, parser state, checksum. |
| `jobs` | Source and source key; unique `(source, source_key)` when key is present; search indexes for title/company/location; sample marker. |
| `saved_jobs` | FKs owner/job; unique `(user_id, job_id)`. |
| `resume_job_matches` | FKs owner/version/job; deterministic score, formula version, explainable JSON result and timestamp; index by owner/date. |
| `match_skills` | FK match and skill; classification (matched/partial/missing), evidence; unique per match/skill. |
| `cover_letters` | FKs owner/job/resume version; editable body, generation source label, created/updated timestamps. |
| `applications` | FKs owner, optional job/resume version; status, applied/follow-up dates, notes; index by owner/status/update. |
| `application_status_history` | FK application; from/to status, note, actor, timestamp; append-only. |
| `interview_questions` | Topic, difficulty, prompt, model answer, rubric; unique seed key. |
| `interview_sessions` | FK owner; role/topic/difficulty/state, score summary and start/end timestamps. |
| `interview_answers` | FKs session/question; answer and rubric feedback; unique `(session_id, ordinal)`. |
| `automation_runs` | FK owner and optional application; portal, consent timestamp, state, sanitized outcome and timestamps. |
| `simulated_portal_submissions` | FK run/application; unique portal idempotency key; unique confirmation ID; verified status and timestamp. |

Delete policy: avoid cascading loss of application and audit history. Account deletion should be an explicit service transaction with a documented retention policy. Resume replacement creates a new immutable version. Status and automation history is append-only. Repositories must avoid loading large extracted text in list queries; DTO projections/page queries prevent N+1 behavior.

## Migration order

1. Users, profiles, roles.
2. Skills and user skills.
3. Resumes and versions.
4. Jobs and saved jobs.
5. Matches and match skills.
6. Cover letters and applications/status history.
7. Questions, interview sessions and answers.
8. Automation runs and simulated submissions.
9. Seeded development jobs and interview content using stable keys.
