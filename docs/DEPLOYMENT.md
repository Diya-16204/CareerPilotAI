# CareerPilot deployment guide

Updated: 2026-10-10 (Asia/Calcutta)

## Deployment state

CareerPilot is **not deployed**. This guide records a low-cost target architecture and the configuration needed to prepare a deployment. No provider account was connected, no credentials were available, no hosting service was created, and no charge was incurred. No public URL exists.

The recommended architecture is Cloudflare Pages for the Vite frontend and Railway for the Spring Boot API, MySQL 8.4, and a persistent upload volume. This is a cost-oriented recommendation based on the current app, not a claim that the application is deployment-ready. Railway Hobby currently starts at $5/month and includes $5 of usage; usage beyond the included amount and storage/egress can add charges. Cloudflare Pages has a Free plan suitable for static frontend hosting at this project's current scale. Recheck provider pricing before creating services. Sources: [Railway pricing](https://docs.railway.com/pricing/plans), [Cloudflare Pages limits](https://developers.cloudflare.com/pages/platform/limits/).

## Verified prerequisites and blockers

- Java 21, Node.js/npm, Git, the Maven Wrapper, and local Maven are available in the development environment. Docker, MySQL server/client, and deployment CLIs for the examined providers were not found.
- No relevant hosting credentials or provider environment variables were present. The repository's public GitHub remote does not grant deployment access.
- The existing root `.env` is user-owned and contains PostgreSQL configuration. Do not replace or edit it for this deployment. MySQL is the active configured backend target, but no MySQL runtime is available to verify migrations, schema validation, data import, or API integration.
- Existing PostgreSQL data and separately stored resume files have local backups documented in `IMPLEMENTATION_STATUS.md` and `MIGRATION_PROGRESS.md`. Those backups are not encrypted and are not a migration. Do not point a new service at the PostgreSQL source or treat an empty MySQL database as a completed migration.
- The backend container includes only the Java runtime. The Automation Center launches a Node/Playwright worker and currently defaults to local Edge/browser and loopback URLs. That workflow is not deployable in the current backend image and must be redesigned and verified before representing hosted automation as available.
- Windows service enumeration was denied by the OS. The observed command-line checks found no MySQL binary and no listener on port 3306; this is not proof that no database service exists in every environment.

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

From the repository root, with Java 21 and frontend dependencies installed:

```powershell
npm ci --prefix frontend
npm test --prefix frontend
npm run build --prefix frontend
Set-Location backend
..\.tools\microsoft-jdk-21\jdk-21.0.12.1+1\bin\java.exe -version
.\mvnw.cmd -B clean verify
```

For a deployed instance, verify `GET https://<api-host>/actuator/health/readiness` returns healthy, registration/login and authenticated API calls succeed from the exact Pages origin, resume upload survives an API restart, and unauthorized/cross-account access is rejected. Verify the deployed build hash and TLS behavior in both provider dashboards. Do not announce a public URL until those checks have actually passed.

The current frontend unit test and backend Maven verification results are recorded in `IMPLEMENTATION_STATUS.md`; repeat backend verification outside a restrictive sandbox if local HTTP stub tests are blocked from opening loopback sockets.

## Known deployment limitations

- Public hosting, TLS, database version, persistent volume, migration, and cross-origin API behavior have not been verified.
- The resume storage volume needs an explicit backup and restore drill; a volume alone is not a backup.
- The Playwright automation worker has no hosted runtime design in the current Java-only image. Do not advertise hosted portal automation until it has a separately secured browser worker, deployment-safe API/frontend origins, persistence/retry behavior, and end-to-end evidence.
- The app has no demonstrated production MySQL migration of existing user data, no real AI provider configured, and no verified hosted error monitoring or restore procedure.
