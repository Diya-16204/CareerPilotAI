# CareerPilot deployment guide

Updated: 2026-10-11 (Asia/Calcutta)

## Environment recheck (2026-10-11)

- The local Microsoft JDK 21.0.12.1 remains available at `.tools/microsoft-jdk-21/jdk-21.0.12.1+1`; the global `java` command resolves to Oracle Java 22.0.2, so select the local JDK explicitly for the Java 21 backend. Node.js 20.16.0 and npm 10.8.1 are available.
- MySQL 8.4 was not available during the 2026-10-11 environment check. Port 5432 had a PostgreSQL listener; it was not stopped or modified.
- `gh`, AWS/Azure/GCP CLIs, Wrangler, Vercel, Netlify, Railway, and Fly CLIs were not found. No related deployment credential environment-variable names were present, and the available Codex browser sessions had no tabs, so provider authentication could not be established. No deployment or provider changes were made.
- At the start of the recheck Git was on `main` at `49cf9fc0ac61ca85ce6a842661babcccfcd76881`. A subsequent observation showed `main` and `origin/main` at `b95da60664122ffd616a56c59cfa98552513f0ef` (`Commiting`); this existing commit was not created by that continuation. Its work was preserved. No commit or push was made by that continuation.
- The MySQL Flyway migration was inspected but not applied; no MySQL schema or Hibernate validation result is claimed.

## Deployment state

CareerPilot is **not deployed**. This guide records a low-cost target architecture and the configuration needed to prepare a deployment. No provider account was connected to this task, no hosting service was created, and no charge was incurred. No public URL exists.

The recommended architecture is Cloudflare Pages for the Vite frontend and Railway for the Spring Boot API, MySQL 8.4, and a persistent upload volume. This is a cost-oriented recommendation based on the current app, not a claim that the application is deployment-ready. Railway Hobby currently starts at $5/month and includes $5 of usage; usage beyond the included amount and storage/egress can add charges. Cloudflare Pages has a Free plan suitable for static frontend hosting at this project's current scale. Recheck provider pricing before creating services. Sources: [Railway pricing](https://docs.railway.com/pricing/plans), [Cloudflare Pages limits](https://developers.cloudflare.com/pages/platform/limits/).

## Verified prerequisites and blockers

- Java 21, Node.js/npm, Git, the Maven Wrapper, and local Maven are available in the development environment. MySQL server/client and deployment CLIs for the examined providers were not found in that check.
- No hosting credential environment variables were found; provider authentication could not be established through the available CLIs or open browser sessions. The repository's public GitHub remote does not grant deployment access.
- The existing root `.env` is user-owned and contains PostgreSQL configuration. Do not replace or edit it for this deployment. MySQL is the active configured backend target, but no MySQL runtime is available to verify migrations, schema validation, data import, or API integration.
- Existing PostgreSQL data and separately stored resume files have local backups documented in `IMPLEMENTATION_STATUS.md` and `MIGRATION_PROGRESS.md`. Those backups are not encrypted and are not a migration. Do not point a new service at the PostgreSQL source or treat an empty MySQL database as a completed migration.
- The Automation Center launches a Node/Playwright worker and currently defaults to local Edge/browser and loopback URLs. Hosted automation must be redesigned and verified before representing it as available.
- An earlier Windows service enumeration was denied by the OS. The 2026-10-11 service query and command/path and port checks found no MySQL installation or listener. This does not prove that a database service cannot exist under another name or on another host.

## Provider setup (manual dashboard steps)

These steps require the owner to authorize the repository with Cloudflare and Railway and accept provider billing. No deployment or spending is authorized by this document. Enter secrets directly in provider dashboards; never paste them into chat, commit them, or place them in frontend build variables.

### 1. Create MySQL and backend services in Railway

Create a private MySQL service using the official MySQL 8.4 image (or another explicitly verified MySQL 8.4 deployment), with a persistent volume mounted at `/var/lib/mysql`. Railway documents its MySQL variables and private service networking at [Railway MySQL](https://docs.railway.com/databases/mysql). Confirm the actual image version and persistent-volume mount before applying migrations.

Create a backend service from the connected repository using a Java 21 build environment. Configure its build to run `bash ./mvnw -B package` from `backend/` and its start command to run `java -jar target/careerpilot-api-0.1.0-SNAPSHOT.jar`. Confirm the platform provides Java 21, supports the Maven Wrapper, injects its `PORT`, and provides persistent upload storage before deployment. Configure readiness path `/actuator/health/readiness` ([health checks](https://docs.railway.com/deployments/healthchecks)); set `UPLOAD_DIR` to the platform's persistent upload path only after verifying it. Review [Railway volume behavior and backups](https://docs.railway.com/volumes/reference) and [volume backups](https://docs.railway.com/volumes/backups). These platform settings have not been tested.

Set backend variables in Railway (reference syntax should be confirmed in the dashboard):

```text
DATABASE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&connectionTimeZone=UTC
DATABASE_USERNAME=${{MySQL.MYSQLUSER}}
DATABASE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=<random secret with at least 32 bytes of entropy>
JWT_EXPIRATION_SECONDS=900
FRONTEND_ORIGIN=https://<actual-pages-hostname>
UPLOAD_DIR=/data/uploads/resumes
AI_PROVIDER=local
```

The app runs Flyway migrations at startup and Hibernate schema validation; startup success must be checked in logs and through readiness. Use a secret manager-generated JWT value. Keep AI in local mode unless the owner separately configures a provider and accepts its data handling and cost terms. Do not define API keys as `VITE_*` variables.

### 2. Create the Cloudflare Pages frontend

Connect the repository in Cloudflare Pages. Set root directory `frontend`, build command `npm ci && npm run build`, output directory `dist`, and Node 20 or a compatible current runtime. Configure this build-time public variable:

```text
VITE_API_BASE_URL=https://<actual-railway-api-hostname>/api/v1
```

After the first deployment, update the Railway `FRONTEND_ORIGIN` to the exact Pages origin, redeploy the API, and verify browser CORS requests. The frontend calls the API directly using `VITE_API_BASE_URL`. See [Cloudflare Pages build configuration](https://developers.cloudflare.com/pages/configuration/build-configuration/).

## Data migration gate

Do not import the PostgreSQL dump directly into MySQL. PostgreSQL and MySQL schemas, migration histories, types, and SQL syntax differ. First provision an isolated MySQL 8.4 database, run the MySQL Flyway baseline, validate the Hibernate schema, and implement a reviewed, repeatable transformation that preserves IDs, ownership, constraints, timestamps, and related file references. Import a copy of the verified PostgreSQL backup and resume-file backup, then reconcile source and target row counts and resume checksums. Run the API and user-isolation smoke checks against that target before any cutover. Keep the original PostgreSQL source and backups intact until the owner verifies the migrated application.

Current blocker at the 2026-10-11 check: no MySQL server was available, so the migration has not been run and no compatible importer has been validated.

## Local verification and public smoke checks

The existing root `.env` is PostgreSQL-oriented. Leave it intact. Run the services directly on Windows. Set backend environment variables in the process that launches Spring Boot, and set `VITE_API_BASE_URL` in the frontend process if it differs from the default. Never copy credentials into tracked files.

```powershell
npm ci --prefix frontend
npm test --prefix frontend
npm run build --prefix frontend
$env:JAVA_HOME = (Resolve-Path .tools\microsoft-jdk-21\jdk-21.0.12.1+1).Path
Push-Location backend
.\mvnw.cmd -B clean verify
Pop-Location
```

These are local build/test commands only; they do not start the backend or run Flyway migrations. Configure and confirm the target MySQL database before starting Spring Boot, because application startup applies Flyway migrations.

For a deployed instance, verify `GET https://<api-host>/actuator/health/readiness` returns healthy, registration/login and authenticated API calls succeed from the exact Pages origin, resume upload survives an API restart, and unauthorized/cross-account access is rejected. Verify the deployed build hash and TLS behavior in both provider dashboards. Do not announce a public URL until those checks have actually passed.

The current frontend unit test and backend Maven verification results are recorded in `IMPLEMENTATION_STATUS.md`; repeat backend verification outside a restrictive sandbox if local HTTP stub tests are blocked from opening loopback sockets.

## Known deployment limitations

- Public hosting, TLS, database version, persistent volume, migration, and cross-origin API behavior have not been verified.
- The resume storage volume needs an explicit backup and restore drill; a volume alone is not a backup.
- The Playwright automation worker has no verified hosted runtime design. It is a Node subprocess using `frontend/scripts/portal-automation.mjs`, a Playwright browser, sibling frontend files, local Edge defaults, and loopback-only frontend/API URL checks. Resume files live on the API's configured upload storage and are not automatically available in a separate worker. Work runs on an in-process executor, so persisted statuses recover interrupted work as `UNVERIFIED` rather than queueing it for another worker.
- Before hosted automation, design and verify a separately isolated worker/browser image with a durable queue and leases, bounded concurrency/timeouts, pinned Playwright/browser versions, non-root/read-only runtime, and strict outbound hostname allowlists. Replace loopback-only URL assumptions with explicit trusted service origins and narrowly scoped worker authentication; avoid sharing the user's bearer token with arbitrary page code, stdout, or diagnostics. Provide the selected resume to the worker through a controlled private file-transfer or object-storage mechanism, and protect/redact screenshots and job payloads with private storage, retention limits, and access logging. Keep the browser limited to CareerPilot's own simulation origins; no real employer submissions are implemented or authorized.
- The app has no demonstrated production MySQL migration of existing user data, no real AI provider configured, and no verified hosted error monitoring or restore procedure.
- The app has no demonstrated production MySQL migration of existing user data, no real AI provider configured, and no verified hosted error monitoring or restore procedure.
