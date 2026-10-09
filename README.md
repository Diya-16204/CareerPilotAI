# CareerPilot AI

Your AI-powered career companion. A modular full-stack career platform built with React/TypeScript, Spring Boot, and PostgreSQL.

## Status

This repository is being initialized. Verified progress, tested commands, and blockers are tracked in [docs/IMPLEMENTATION_STATUS.md](docs/IMPLEMENTATION_STATUS.md). Architecture and intended API contracts are in `docs/`.

## Prerequisites

Node.js/npm for the frontend; Java 21 and Maven (or the checked-in Maven wrapper) for the backend; Docker Compose for PostgreSQL/local orchestration. See `.env.example` and [deployment guide](docs/DEPLOYMENT.md). Do not use real credentials in source control.

## Project layout

- `frontend/` React + TypeScript SPA
- `backend/` Spring Boot modular monolith
- `simulated-portals/` safe local job portal fixtures
- `automation/` browser automation adapters
- `tests/` cross-module/end-to-end verification
- `docs/` plans, contracts, security, operations, and evidence

Run commands will be documented here after the corresponding skeleton and checks are verified.
