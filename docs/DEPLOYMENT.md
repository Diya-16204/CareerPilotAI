# CareerPilot deployment guide

Updated: 2026-10-11

## Target architecture

- Backend: Spring Boot 3.5.16 / Java 21 on the existing Render Docker web service, built from the tracked `backend/Dockerfile`. Local Windows development uses Java 21 and `backend\mvnw.cmd` without Docker Compose.
- Database: existing Aiven MySQL 8.4 service. Do not point deployment at the local database or change the root `.env`.
- Frontend: Vite static site on Vercel: https://career-pilot-ai-blond-seven.vercel.app/.
- Backend: https://careerpilotai-he15.onrender.com.

The owner reports that Aiven connectivity and Flyway migrations were verified. On 2026-10-11, the deployed frontend homepage returned HTTP 200, backend readiness returned `UP`, and `GET /api/v1/jobs` returned HTTP 200 with `Access-Control-Allow-Origin` set to the Vercel origin. Authenticated application flows have not been verified. Direct navigation to `/login` returned Vercel `404`; `frontend/vercel.json` now adds a SPA fallback, but that fix is not live until Vercel rebuilds the latest commit.

## Deploy the backend on Render

1. Use the existing Render Web Service and repository branch. Select the Docker runtime, set **Root Directory** to `backend`, and use `Dockerfile`.
2. Set `/actuator/health/readiness` for the health check.
3. Configure the environment variables below in the Render dashboard. Put credentials only in secret-value fields; do not add them to Git, build arguments, frontend variables, or chat.

| Variable | Value |
| --- | --- |
| `DATABASE_URL` | `jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/careerpilot?sslMode=REQUIRED&connectionTimeZone=UTC` (use the host, port, and database shown in Aiven; keep TLS enabled) |
| `DATABASE_USERNAME` | Aiven database username |
| `DATABASE_PASSWORD` | Aiven database password (secret) |
| `JWT_SECRET` | A newly generated random value with at least 32 bytes of entropy (secret; keep stable across redeploys) |
| `JWT_EXPIRATION_SECONDS` | `900` |
| `FRONTEND_ORIGIN` | The exact Vercel site origin after the frontend is deployed, e.g. `https://your-site.vercel.app`; initially it may remain unset/default until Vercel exists, then update it and redeploy |
| `UPLOAD_DIR` | `/tmp/careerpilot/uploads` |
| `AI_PROVIDER` | `local` unless an AI provider has been deliberately configured |

The application binds to Render's injected `PORT` (defaulting to 8080 locally), runs Flyway and Hibernate validation on startup, and uses the configured MySQL URL. Check the deploy logs for successful startup and database validation; readiness alone does not prove every application workflow works. If the migrations already ran on this Aiven database, Flyway should validate the recorded history and apply only any pending version; do not reset or drop the database.

The frontend API client allows up to 70 seconds for requests because a sleeping Render Free service can take close to a minute to wake. Referral jobs use the authenticated `GET /api/v1/jobs/saved` endpoint; deploy the latest backend commit for this route. A timeout or API error must be shown as a retryable load error, not as an empty saved-jobs list.

The configured upload path is local filesystem storage. Render Free's filesystem is ephemeral: resume uploads can disappear when the service restarts, redeploys, or spins down. Do not treat this setup as durable storage for real resumes. Render Free web services spin down after inactivity and can take around a minute to wake. Review current [Render Free limitations](https://render.com/docs/free) before use.

### Render backend smoke checks

After Render shows the deploy as live, check its readiness:

```powershell
$api = 'https://careerpilotai-he15.onrender.com'
Invoke-RestMethod "$api/actuator/health/readiness"
```

Then use the frontend to register/sign in and check authenticated profile, jobs, resume, and application flows. Inspect browser Network errors and Render logs if CORS or database access fails. Never paste tokens, passwords, or full secret-bearing connection strings into an issue or chat.

## Deploy the frontend on Vercel

1. Import the same GitHub repository into Vercel.
2. Set the project **Root Directory** to `frontend`, framework preset to **Vite**, build command to `npm run build`, and output directory to `dist`. Vercel should install from `frontend/package-lock.json`.
3. Add this public API base URL in the Vercel project's environment variables for Production (and Preview only if previews should call the same API):

```text
VITE_API_BASE_URL=https://careerpilotai-he15.onrender.com/api/v1
```

4. Deploy the frontend. Copy its exact HTTPS origin (scheme + host, no path) into Render's `FRONTEND_ORIGIN`, then redeploy the backend. The backend CORS configuration accepts the single configured origin.
5. Test registration/login and an authenticated API request from the deployed site. Do not put `DATABASE_PASSWORD`, `JWT_SECRET`, or any AI provider key in a `VITE_*` variable; frontend build variables are public to browser users.

## Deployment boundaries

- Docker is used for the existing Render backend image only; local development runs MySQL 8.4 as a Windows service, the API using Java 21 and `backend\mvnw.cmd`, and the UI with `npm run dev` from `frontend`. No Docker Compose is needed locally.
- **Hosted Automation Center is currently blocked.** A user-provided screenshot shows `Resume file is unavailable.` while a resume record/version remains selectable. `ResumeStorage.resolve()` reads the stored bytes from `UPLOAD_DIR`; the resume name, version, and parsed fields are in MySQL. Render Free's filesystem is ephemeral, so a restart, spin-down, or redeploy can remove bytes without removing their MySQL metadata. Re-uploading to the deployed site can restore the file temporarily, but does not make storage durable.
- **The hosted browser worker is absent from the image.** `backend/Dockerfile` includes only the Spring Boot JAR in its runtime stage. `ProcessPortalAutomationRunner` needs Node, `frontend/scripts/portal-automation.mjs`, Playwright, a browser, and a reachable fictional portal. The defaults also use loopback endpoints and `msedge`, which is not supplied by the Linux image. A production worker design and end-to-end deployment verification are still required.
- Automation Center is simulation-only: its portals are fictional and no real employer-site adapters are implemented. Do not advertise it as applying to real jobs or as working on the hosted service.
- The new per-job preparation workflow adds Flyway migration `V3__auto_apply_preparation_sessions.sql`. This task did not run it against the hosted Aiven database or deploy the new backend. The Auto Apply API is not ready on the live site until a reviewed deployment applies this additive migration and authenticated behavior is verified.
- Render Free's ephemeral storage is unsuitable for durable resume uploads. A persistent storage design or private object storage is required before relying on hosted uploads.
- No PostgreSQL data import is performed by deployment. Do not import a PostgreSQL dump directly into MySQL without a reviewed transformation and reconciliation.
- Keep the existing root `.env` and local database settings unchanged. Do not run migrations manually as a deployment fix; startup Flyway is the mechanism, and a failed startup must first be diagnosed from logs.

## Local Windows commands (unchanged)

Start the installed MySQL service in an elevated PowerShell only if it is stopped, then in separate terminals:

```powershell
# Backend (from repository root; set DATABASE_URL, DATABASE_USERNAME,
# DATABASE_PASSWORD, and a 32+ byte JWT_SECRET in this terminal first)
Set-Location .\backend
.\mvnw.cmd spring-boot:run
```

```powershell
# Frontend
Set-Location .\frontend
npm run dev
```

The frontend defaults to `http://localhost:8080/api/v1` when `VITE_API_BASE_URL` is not set.
