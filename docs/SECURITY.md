# Security and Privacy

## Implemented baseline

Implemented controls include BCrypt password hashing, short-lived JWT access tokens, server-side owner filtering, validated DTOs, centralized safe errors, resume size/content checks, private generated storage keys, restrictive CORS, and environment-provided secrets. No rate limiter or production-grade structured audit logging is implemented yet. The simulated portal API requires authentication to submit and verify; public job listings contain fictional data only. Verification responses omit applicant details. Portal submissions store applicant contact fields and PDF/DOCX bytes in PostgreSQL for the signed-in owner; configure access controls, encryption at rest, and retention/deletion before production use.

## Constraints and hardening

Access tokens will be memory-only in the browser; there is no long-lived localStorage token. Refresh-token rotation/revocation must be designed before production. Configure HTTPS, secure headers, origin allowlists, request limits, secret rotation, database least privilege, encrypted backups, malware scanning, and retention/deletion policy in deployment. Never log tokens, passwords, full resume contents, or provider credentials. Resume text and job descriptions are untrusted prompt inputs. Real applications require user review and explicit approval; only simulated portals are automated in this project.

Do not describe this project as production-ready until ownership, upload, auth, and deployment controls have been verified.
