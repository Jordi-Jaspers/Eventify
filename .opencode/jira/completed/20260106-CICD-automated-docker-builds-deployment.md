# CI/CD: Automated Docker Builds & Deployment Pipeline

**Date:** 2026-01-06
**Epic:** CI/CD & Deployment Pipeline
**Stories:** CICD-001, CICD-002, CICD-003, CICD-004

---

## Feature Plan (Approved)

### Requirements Summary

- Automated Docker image builds on push to develop/main branches
- Two-container architecture: separate frontend (SvelteKit/Bun) and backend (Spring Boot) images
- Push images to GitHub Container Registry (ghcr.io)
- Test environment on NUC with Watchtower for auto-updates
- Traefik routing: `/api/*` → backend, `/*` → frontend

### Technical Approach

**CI/CD Workflow:**
- Build triggered on push to develop/main or version tags
- Backend: Gradle build → Docker multi-stage build with slim JRE (jlink)
- Frontend: Bun install → Vite build → Docker multi-stage with alpine
- Push to ghcr.io with tags: `latest-dev`, `dev-<sha>`, semver for releases

**Architecture:**
- Oracle server: Traefik reverse proxy with static config
- NUC server: Docker containers with port mappings
- Routing: Oracle Traefik → NUC containers via internal network

### Implementation Workflow

1. CICD-004: GHCR authentication setup (manual step)
2. CICD-002: Create frontend Dockerfile
3. CICD-001: Update CI workflow for Docker builds
4. CICD-003: Configure test environment docker-compose + Traefik routes

---

## Actual Changelog

### Summary
Implemented complete CI/CD pipeline for automated Docker builds and deployment to test environment. Both frontend and backend images build successfully and are pushed to GitHub Container Registry.

### Changes

**Backend (server/):**
- Updated `Dockerfile`: Java 25 (eclipse-temurin:25-jdk-noble), fixed LABEL placement, corrected JAR filename pattern
- Multi-stage build with jlink for minimal JRE (~331MB final image)
- Health check on `/api/v1/public/actuator/health`

**Frontend (client/):**
- Created `Dockerfile`: Multi-stage build with Bun alpine
- Created `.dockerignore`: Excludes node_modules, .svelte-kit, build artifacts
- Updated `constants.ts`: Made `VITE_SERVER_BASE_URL` optional for same-origin routing
- Final image size: ~115MB

**CI/CD (.github/workflows/):**
- Updated `ci.yml`: Added Docker build & push jobs for both images
- Conditional builds: develop → `latest-dev`, main → `latest`, tags → semver
- GHCR authentication using `GITHUB_TOKEN`

**Server Config (separate repo - server-config/nuc/):**
- Created `applications/eventify-tst/docker-compose.yml`:
  - Two containers: eventify-server (port 8085), eventify-client (port 3005)
  - Watchtower for auto-updates with scoped polling
  - Uses `env_file` for configuration
- Created `.env.example` with all environment variables from application.yml
- Updated Oracle Traefik `config.yml`:
  - Added routers: `http-eventify-tst`, `https-eventify-tst-api`, `https-eventify-tst-ui`
  - Priority-based routing: API (100) > UI (50)
  - Added services pointing to `jordi-server:8085` and `jordi-server:3005`

### Files Modified (Eventify Repo)

| File | Action | Description |
|------|--------|-------------|
| `.github/workflows/ci.yml` | Modified | Added Docker build & push jobs |
| `server/Dockerfile` | Modified | Java 25, fixed LABEL, JAR pattern |
| `client/Dockerfile` | Created | Multi-stage Bun/SvelteKit build |
| `client/.dockerignore` | Created | Exclude build artifacts |
| `client/src/lib/config/constants.ts` | Modified | Optional API URL |

### Files Modified (Server Config Repo)

| File | Action | Description |
|------|--------|-------------|
| `nuc/applications/eventify-tst/docker-compose.yml` | Created | Two-container setup with Watchtower |
| `nuc/applications/eventify-tst/.env.example` | Created | All env vars from application.yml |
| `oracle/traefik/config.yml` | Modified | Added Eventify routes and services |

### Docker Images

| Image | Size | Base |
|-------|------|------|
| `ghcr.io/jordijaspers/eventify-server` | 331MB | ubuntu:noble + slim JRE |
| `ghcr.io/jordijaspers/eventify-client` | 115MB | oven/bun:alpine |

### Manual Steps Required (CICD-004)

1. Create GitHub PAT with `read:packages` scope
2. On NUC: `echo "<token>" | docker login ghcr.io -u jordijaspers --password-stdin`
3. Copy `.env.example` to `.env` and fill in values
4. Create database: `./scripts/database-reset.sh -n eventify -u <user> -P <pass> -C nuc_postgres -d yes`
5. Start stack: `docker-compose up -d`

### Quality Metrics

- ✅ Server Docker build: Successful
- ✅ Client Docker build: Successful
- ✅ Server image size: 331MB (optimized with jlink)
- ✅ Client image size: 115MB (alpine base)
- ✅ No Traefik route conflicts
- ✅ All existing tests passing

### Notes

- Watchtower polls every 60 seconds for new images
- Images tagged with `latest-dev` for develop branch builds
- Frontend uses relative `/api` path when `VITE_SERVER_BASE_URL` not set
- Oracle Traefik handles TLS termination with Let's Encrypt
