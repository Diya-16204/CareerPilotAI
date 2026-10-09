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
| `jobs` | Public seeded/sample jobs and owner-scoped user imports; source and sample marker; search indexes for title/company/location. |
| `saved_jobs` | FKs owner/job; unique `(user_id, job_id)`. |
| `resume_job_matches` | FKs owner/version/job; deterministic score, formula version, explainable JSON result and timestamp; index by owner/date. |
| `match_skills` | FK match and skill; classification (matched/partial/missing), evidence; unique per match/skill. |
| `cover_letters` | FKs owner/job/resume version; editable body, generation source label, created/updated timestamps. |
| `applications` | FKs owner, optional job/resume version; status, applied/follow-up dates, notes; index by owner/status/update. |
| `application_status_history` | FK application; from/to status, note, actor, timestamp; append-only. |
| `interview_questions` | Topic, difficulty, prompt, model answer, rubric; unique seed key. |
| `interview_sessions` | FK owner; role/topic/difficulty/state, score summary and start/end timestamps. |
| `interview_answers` | FKs session/question; answer and rubric feedback; unique `(session_id, ordinal)`. |
| `simulated_portal_submissions` | V6 table with FK owner; fictional portal/job identifiers, applicant, education/experience/cover-letter and screening data, PDF/DOCX bytes, owner-scoped idempotency, unique confirmation ID, and creation time. Unique identity index prevents repeat submissions for the same owner/portal/job/email. |
| `automation_runs` | Planned; no migration/entity/API currently implements Playwright run state, retry outcome, or evidence history. |

Delete policy: avoid cascading loss of application and audit history. Account deletion should be an explicit service transaction with a documented retention policy. Resume replacement creates a new immutable version. Status and automation history is append-only. Repositories must avoid loading large extracted text in list queries; DTO projections/page queries prevent N+1 behavior.

## Migration order

1. V1 creates users, jobs, saved jobs, applications, analytics, and development job seeds.
2. V2 creates resumes/versions and matching records.
3. V3 creates cover letters and interview questions/sessions/answers.
4. V4 adds interview question seeds.
5. V5 adds profiles and owner-scoped job imports.
6. V6 adds owner-scoped simulated portal submissions.

Migration application and Hibernate validation have not yet been run against PostgreSQL in the current environment.
