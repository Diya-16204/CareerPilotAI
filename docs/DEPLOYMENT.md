# CareerPilot deployment guide

Updated: 2026-10-11 (Asia/Calcutta)

## Environment recheck (2026-10-11)

- The local Microsoft JDK 21.0.12.1 remains available at `.tools/microsoft-jdk-21/jdk-21.0.12.1+1`; the global `java` command resolves to Oracle Java 22.0.2, so select the local JDK explicitly for the Java 21 backend. Node.js 20.16.0 and npm 10.8.1 are available.
- Docker/Docker Compose and MySQL 8.4 are still unavailable: their commands and common install paths were absent, no matching Docker/MySQL services were listed, and no listener was observed on ports 3306/33060. Port 5432 has a PostgreSQL listener; it was not stopped or modified.
- `gh`, AWS/Azure/GCP CLIs, Wrangler, Vercel, Netlify, Railway, and Fly CLIs were not found. No related deployment credential environment-variable names were present, and the available Codex browser sessions had no tabs, so provider authentication could not be established. No deployment or provider changes were made.
- At the start of the recheck Git was on `main` at `49cf9fc0ac61ca85ce6a842661babcccfcd76881`. A subsequent observation showed `main` and `origin/main` at `b95da60664122ffd616a56c59cfa98552513f0ef` (`Commiting`); this existing commit was not created by this continuation. Its work was preserved, and the following Compose/upload-volume corrections remain as local changes. No commit or push was made by this continuation.
- Compose was inspected but cannot be validated by `docker compose config`, built, or started without Docker. The MySQL Flyway migration was inspected but not applied; no MySQL schema or Hibernate validation result is claimed.

## Deployment state

CareerPilot is **not deployed**. This guide records a low-cost target architecture and the configuration needed to prepare a deployment. No provider account was connected to this task, no hosting service was created, and no charge was incurred. No public URL exists.

The recommended architecture is Cloudflare Pages for the Vite frontend and Railway for the Spring Boot API, MySQL 8.4, and a persistent upload volume. This is a cost-oriented recommendation based on the current app, not a claim that the application is deployment-ready. Railway Hobby currently starts at $5/month and includes $5 of usage; usage beyond the included amount and storage/egress can add charges. Cloudflare Pages has a Free plan suitable for static frontend hosting at this project's current scale. Recheck provider pricing before creating services. Sources: [Railway pricing](https://docs.railway.com/pricing/plans), [Cloudflare Pages limits](https://developers.cloudflare.com/pages/platform/limits/).

## Verified prerequisites and blockers

- Java 21, Node.js/npm, Git, the Maven Wrapper, and local Maven are available in the development environment. Docker, MySQL server/client, and deployment CLIs for the examined providers were not found.
- No hosting credential environment variables were found; provider authentication could not be established through the available CLIs or open browser sessions. The repository's public GitHub remote does not grant deployment access.
- The existing root `.env` is user-owned and contains PostgreSQL configuration. Do not replace or edit it for this deployment. MySQL is the active configured backend target, but no MySQL runtime is available to verify migrations, schema validation, data import, or API integration.
- Existing PostgreSQL data and separately stored resume files have local backups documented in `IMPLEMENTATION_STATUS.md` and `MIGRATION_PROGRESS.md`. Those backups are not encrypted and are not a migration. Do not point a new service at the PostgreSQL source or treat an empty MySQL database as a completed migration.
- The backend container includes only the Java runtime. The Automation Center launches a Node/Playwright worker and currently defaults to local Edge/browser and loopback URLs. That workflow is not deployable in the current backend image and must be redesigned and verified before representing hosted automation as available.
- An earlier Windows service enumeration was denied by the OS. The 2026-10-11 service query found no matching Docker/MySQL services; command/path and port checks also found no MySQL installation or listener. This does not prove that a database service cannot exist under another name or on another host.

## Provider setup (manual dashboard steps)

These steps require the owner to authorize the repository with Cloudflare and Railway and accept provider billing. No deployment or spending is authorized by this document. Enter secrets directly in provider dashboards; never paste them into chat, commit them, or place them in frontend build variables.

### 1. Create MySQL and backend services in Railway

Create a private MySQL service using the official MySQL 8.4 image (or another explicitly verified MySQL 8.4 deployment), with a persistent volume mounted at `/var/lib/mysql`. Railway documents its MySQL variables and private service networking at [Railway MySQL](https://docs.railway.com/databases/mysql). Confirm the actual image version and persistent-volume mount before applying migrations.

Create a backend service from the connected repository with root directory `backend` and its `Dockerfile`. The app listens on Railway's injected `PORT`; configure readiness path `/actuator/health/readiness` ([health checks](https://docs.railway.com/deployments/healthchecks)). Mount a persistent volume at `/data`, then set `UPLOAD_DIR=/data/uploads/resumes`; confirm the mount is writable by the non-root container user. Review [Railway volume behavior and backups](https://docs.railway.com/volumes/reference) and [volume backups](https://docs.railway.com/volumes/backups).

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

After the first deployment, update the Railway `FRONTEND_ORIGIN` to the exact Pages origin, redeploy the API, and verify browser CORS requests. The direct API URL is required because `frontend/nginx.conf` proxies only within a Compose network. See [Cloudflare Pages build configuration](https://developers.cloudflare.com/pages/configuration/build-configuration/).

## Data migration gate

Do not import the PostgreSQL dump directly into MySQL. PostgreSQL and MySQL schemas, migration histories, types, and SQL syntax differ. First provision an isolated MySQL 8.4 database, run the MySQL Flyway baseline, validate the Hibernate schema, and implement a reviewed, repeatable transformation that preserves IDs, ownership, constraints, timestamps, and related file references. Import a copy of the verified PostgreSQL backup and resume-file backup, then reconcile source and target row counts and resume checksums. Run the API and user-isolation smoke checks against that target before any cutover. Keep the original PostgreSQL source and backups intact until the owner verifies the migrated application.

Current blocker: no MySQL server or Docker is available here, so the migration has not been run and no compatible importer has been validated.

## Local verification and public smoke checks

The existing root `.env` is PostgreSQL-oriented. Leave it intact. For Compose, copy `.env.compose.example` to the ignored `.env.compose.local`, replace the placeholder database passwords and JWT secret locally, and explicitly pass that file so Compose does not load `.env`:

```powershell
Copy-Item .env.compose.example .env.compose.local
docker compose --env-file .env.compose.local config --quiet
docker compose --env-file .env.compose.local up --build -d
docker compose --env-file .env.compose.local ps
Invoke-RestMethod http://localhost:3000/actuator/health/readiness
npm ci --prefix frontend
npm test --prefix frontend
npm run build --prefix frontend
$env:JAVA_HOME = (Resolve-Path .tools\microsoft-jdk-21\jdk-21.0.12.1+1).Path
Push-Location backend
.\mvnw.cmd -B clean verify
Pop-Location
```

Compose shutdown, when desired, is `docker compose --env-file .env.compose.local down`; do not add `-v`, which would remove Compose-managed volumes. Docker/MySQL are not installed in the current environment, so these commands are reproducible instructions only and were not run here.

For a deployed instance, verify `GET https://<api-host>/actuator/health/readiness` returns healthy, registration/login and authenticated API calls succeed from the exact Pages origin, resume upload survives an API restart, and unauthorized/cross-account access is rejected. Verify the deployed build hash and TLS behavior in both provider dashboards. Do not announce a public URL until those checks have actually passed.

The current frontend unit test and backend Maven verification results are recorded in `IMPLEMENTATION_STATUS.md`; repeat backend verification outside a restrictive sandbox if local HTTP stub tests are blocked from opening loopback sockets.

## Known deployment limitations

- Public hosting, TLS, database version, persistent volume, migration, and cross-origin API behavior have not been verified.
- The resume storage volume needs an explicit backup and restore drill; a volume alone is not a backup.
- The Playwright automation worker has no hosted runtime design in the current Java-only image. It is a Node subprocess using `frontend/scripts/portal-automation.mjs`, a Playwright browser, sibling frontend files, local Edge defaults, and loopback-only frontend/API URL checks. In Compose/Railway the backend container's loopback does not address the frontend service; the current runner also rejects service DNS/HTTPS URLs. Resume files live on the API's configured upload storage and are not automatically available in a separate worker. Work runs on an in-process executor, so persisted statuses recover interrupted work as `UNVERIFIED` rather than queueing it for another worker.
- Before hosted automation, design and verify a separately isolated worker/browser image with a durable queue and leases, bounded concurrency/timeouts, pinned Playwright/browser versions, non-root/read-only runtime, and strict outbound hostname allowlists. Replace loopback-only URL assumptions with explicit trusted service origins and narrowly scoped worker authentication; avoid sharing the user's bearer token with arbitrary page code, stdout, or diagnostics. Provide the selected resume to the worker through a controlled private file-transfer or object-storage mechanism, and protect/redact screenshots and job payloads with private storage, retention limits, and access logging. Keep the browser limited to CareerPilot's own simulation origins; no real employer submissions are implemented or authorized.
- The app has no demonstrated production MySQL migration of existing user data, no real AI provider configured, and no verified hosted error monitoring or restore procedure.
- The app has no demonstrated production MySQL migration of existing user data, no real AI provider configured, and no verified hosted error monitoring or restore procedure.
