# Security and Privacy

## Implemented baseline

The current implementation status is authoritative; this document is a design target until corresponding code and tests are recorded. Planned controls include BCrypt password hashing, short-lived JWT access tokens, server-side owner filtering, validated DTOs, centralized safe errors, upload size/content checks, private file storage, restrictive CORS, rate limits, environment-only secrets, and sanitized structured logs.

## Constraints and hardening

Access tokens will be memory-only in the browser; there is no long-lived localStorage token. Refresh-token rotation/revocation must be designed before production. Configure HTTPS, secure headers, origin allowlists, request limits, secret rotation, database least privilege, encrypted backups, malware scanning, and retention/deletion policy in deployment. Never log tokens, passwords, full resume contents, or provider credentials. Resume text and job descriptions are untrusted prompt inputs. Real applications require user review and explicit approval; only simulated portals are automated in this project.

Do not describe this project as production-ready until ownership, upload, auth, and deployment controls have been verified.
