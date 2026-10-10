# External site automation extension

The backend defines `RealJobSiteAdapter` as an extension contract for a specifically authorized external job site. No external adapters are registered. The current Playwright runner remains restricted to local fictional portals; it does not navigate to third-party sites.

To add a site adapter:

1. Confirm that the site permits the proposed integration and that the user has authorized use of their account and application data.
2. Implement one adapter for the documented site workflow. Match fields only to user-approved data and answers.
3. Build a preview containing the source URL, visible form values, and capture time. Require a fresh `ExplicitApproval` before any final submission.
4. Use owner-scoped idempotency and site-specific duplicate handling. Never retry an uncertain submission automatically.
5. Verify any confirmation against an authoritative site result when available. Return `UNVERIFIED` when persistence cannot be confirmed.
6. Store a safe evidence reference and sanitized outcome; do not store credentials or log tokens, full resumes, or private answers.
7. Keep a manual completion path. Do not bypass CAPTCHA, MFA, access controls, anti-bot measures, or site restrictions.
8. Add contract, ownership, approval, duplicate-submission, and end-to-end tests using an authorized test environment.

An adapter must not be enabled by merely adding a hostname. It needs a documented authorization basis, explicit preview/approval, and a tested site-specific implementation.
