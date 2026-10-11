# CareerPilot deployment guide

Updated: 2026-10-11

## Target architecture

- Backend: Spring Boot 3.5.16 / Java 21 on a Render Docker web service. The Dockerfile is only for hosted deployment; local Windows development continues to use Java 21 and `backend\mvnw.cmd` without Docker.
- Database: existing Aiven MySQL 8.4 service. Do not point deployment at the local database or change the root `.env`.
- Frontend: Vite static site on Vercel, configured after the API is deployed.

The user reports that Aiven connectivity and the Flyway migrations were verified. That is not independently rechecked by this deployment preparation. No hosted service was created or deployed in this task.

## Deploy the backend on Render

1. Push the repository and connect it in Render. Create a **Web Service**, select the repository and branch, and choose the **Free** instance type.
2. Select the **Docker** runtime. Set **Root Directory** to `backend` and Dockerfile path to `Dockerfile`. Leave Docker command overrides empty; the image entrypoint starts Spring Boot.
3. Set the health check path to `/actuator/health/readiness`.
4. Add the environment variables below in the Render service dashboard. Put credentials only in Render's secret-value fields; do not add them to Git, Docker build arguments, frontend variables, or chat.

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

The Docker image runs as a non-root user. Its upload directory is writable, but Render Free has an ephemeral filesystem: resume uploads disappear when the service restarts, redeploys, or spins down. Do not treat this setup as durable storage for real resumes. Render Free web services spin down after 15 minutes without traffic and can take about a minute to wake. Render's free-tier documentation says Free instances are intended for testing/hobby use, not production. Review current [Render Free limitations](https://render.com/docs/free) before use.

### Render backend smoke checks

After Render shows the deploy as live, replace the placeholder with the assigned host:

```powershell
$api = 'https://<your-render-service>.onrender.com'
Invoke-RestMethod "$api/actuator/health/readiness"
```

Then use the frontend to register/sign in and check authenticated profile, jobs, resume, and application flows. Inspect browser Network errors and Render logs if CORS or database access fails. Never paste tokens, passwords, or full secret-bearing connection strings into an issue or chat.

## Deploy the frontend on Vercel

1. Import the same GitHub repository into Vercel.
2. Set the project **Root Directory** to `frontend`, framework preset to **Vite**, build command to `npm run build`, and output directory to `dist`. Vercel should install from `frontend/package-lock.json`.
3. Add this public API base URL in the Vercel project's environment variables for Production (and Preview only if previews should call the same API):

```text
VITE_API_BASE_URL=https://<your-render-service>.onrender.com/api/v1
```

4. Deploy the frontend. Copy its exact HTTPS origin (scheme + host, no path) into Render's `FRONTEND_ORIGIN`, then redeploy the backend. The backend CORS configuration accepts the single configured origin.
5. Test registration/login and an authenticated API request from the deployed site. Do not put `DATABASE_PASSWORD`, `JWT_SECRET`, or any AI provider key in a `VITE_*` variable; frontend build variables are public to browser users.

## Deployment boundaries

- Docker is used only by Render for building/running the backend. Local development remains Docker-free: run MySQL 8.4 as a Windows service, the API using Java 21 and `backend\mvnw.cmd`, and the UI with `npm run dev` from `frontend`.
- Docker context is the backend directory; `.dockerignore` excludes `.env`, target output, uploads, and data directories. No resume files or secrets should enter the image.
- The Automation Center is not verified for hosted operation. It spawns Node/Playwright and expects a browser plus local sibling frontend files and loopback URLs; this Java-only image does not provide that worker runtime. Do not advertise hosted automation as working.
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
