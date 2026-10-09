# Deployment Preparation

The intended local stack is PostgreSQL, Spring Boot API, and a static React frontend configured through environment variables. Docker Compose should use named database volume, readiness health checks, Flyway migration on startup, and non-root app images. `.env.example` contains placeholders only. For production, terminate TLS at a trusted proxy/host, configure strict CORS, managed PostgreSQL backups, secret management, monitoring, retention, and migration rollback procedures.

No deployment is currently verified. Docker is unavailable in the initial environment inspection. A live environment requires external hosting, DNS, TLS, database, and secret configuration and must be independently verified before being called deployed.
