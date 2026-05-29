---
name: eventify-architecture
description: Eventify architecture, folder structure, and system overview. Use when understanding where code belongs or navigating the codebase. Trigger keywords: where does X go, project structure, architecture, new module, new domain, new page, new component.
metadata:
  skill-type: architecture
  language: java, typescript
  framework: spring-boot, sveltekit
  project-type: fullstack
---

# Eventify Architecture

## System Overview

Eventify is an event notification and monitoring platform. It is a fullstack monorepo:

- **Backend:** Spring Boot 4.1 (Java 25), Gradle Kotlin DSL, Lombok, MapStruct
- **Frontend:** SvelteKit (Svelte 5), TypeScript, Tailwind CSS v4, Bun, shadcn-svelte
- **Database:** PostgreSQL 17 + TimescaleDB (time-series hypertables)
- **API contract:** OpenAPI spec auto-generated from server → typed TS client via `openapi-fetch`

## Project Structure

```
eventify/
├── server/                   # Spring Boot backend (port 8080)
│   ├── src/main/java/io/github/eventify/
│   │   ├── common/           # Cross-cutting: config, security, audit, util, email, exception, constant
│   │   └── api/              # Domain modules (one folder per domain)
│   ├── src/main/resources/
│   │   └── db/changelog/     # Liquibase migrations
│   └── build.gradle.kts
├── client/                   # SvelteKit frontend (port 3000 / dev: 5173)
│   └── src/
│       ├── routes/
│       │   ├── (authenticated)/  # Protected pages
│       │   └── (public)/         # Login, register, etc.
│       └── lib/
│           ├── api/          # Generated + hand-written API controllers (TS)
│           ├── components/   # Feature components + shadcn-svelte ui/ primitives
│           ├── config/
│           ├── hooks/
│           ├── stores/
│           ├── types/        # api.d.ts (generated), domain types
│           └── utils/
└── scripts/                  # openapi-sync.sh, database-reset.sh, common.sh
```

## Backend Architecture

**Base package:** `io.github.eventify`

### `common/`
Shared infrastructure. Do **not** put business logic here.

| Sub-package | Contents |
|-------------|----------|
| `config` | Spring beans, CORS, security config, JPA config |
| `security` | JWT, OAuth2, SpEL security service beans |
| `audit` | `AuditLog`, audit service, event listeners |
| `util` | Stateless helpers |
| `constant` | Enums, string constants |
| `exception` | Global `@ControllerAdvice`, domain exceptions |
| `email` | Email service, Thymeleaf templates |

### `api/` — Domain Modules

Each domain is self-contained:

```
api/{domain}/
├── controller/        # REST controllers
├── service/           # Business logic
├── repository/        # Spring Data JPA repositories
├── model/
│   ├── {Domain}.java          # JPA entity
│   ├── request/               # Create/Update DTOs
│   ├── response/              # Response DTOs
│   ├── mapper/                # MapStruct mappers
│   └── validator/             # Fluent DSL validators (NOT JSR-303)
├── job/               # (optional) Scheduled tasks
└── cache/             # (optional) Cache keys / eviction logic
```

**Controller naming convention:** `{Context}{Domain}Controller.java`
- `User*` — actions performed by an authenticated user
- `Organization*` — actions scoped to an org
- `Admin*` — admin-only actions
- `Api*` — API key authenticated actions

**Known domains:** `organization`, `token`, `monitor`, `bootstrap`, `changelog`, `notification`, `admin`, `user`, `subscription`, `channel`, `dashboard`, `apikey`, `quota`, `authentication`, `event`, `watchlist`, `session`

## Frontend Architecture

### Routes

```
src/routes/
├── (authenticated)/   # Requires valid session
│   └── {route}/
│       ├── +page.svelte
│       └── +page.ts
└── (public)/          # No auth required
    └── {route}/
```

### `src/lib/`

| Folder | Contents |
|--------|----------|
| `api/` | `{Domain}Controller.ts` — typed wrappers around generated `openapi-fetch` client |
| `components/` | Feature folders (e.g., `monitor/`, `channel/`) + `ui/` (shadcn-svelte primitives) |
| `config/` | App-wide constants, env helpers |
| `hooks/` | Svelte hooks / SvelteKit handle functions |
| `stores/` | `{domain}.svelte.ts` — Svelte 5 rune-based stores |
| `types/` | `api.d.ts` (generated), hand-written domain types |
| `utils/` | Pure functions |

## Where to Put New Code

| I need to create... | Location |
|---------------------|----------|
| New domain module | `server/src/main/java/io/github/eventify/api/{domain}/` |
| Controller | `api/{domain}/controller/{Context}{Domain}Controller.java` |
| Service | `api/{domain}/service/{Domain}Service.java` |
| Entity | `api/{domain}/model/{Domain}.java` |
| Request DTO | `api/{domain}/model/request/Create{Domain}Request.java` |
| Response DTO | `api/{domain}/model/response/{Domain}Response.java` |
| Mapper | `api/{domain}/model/mapper/{Domain}Mapper.java` |
| Validator | `api/{domain}/model/validator/{Domain}Validator.java` |
| DB migration | `server/src/main/resources/db/changelog/changesets/{YYYYMMDDHHMM}-PRD-{desc}.xml` |
| New page | `client/src/routes/(authenticated)/{route}/+page.svelte` |
| Feature component | `client/src/lib/components/{feature}/{ComponentName}.svelte` |
| API controller (TS) | `client/src/lib/api/{Domain}Controller.ts` |
| Store / service | `client/src/lib/stores/{domain}.svelte.ts` |

## Key Patterns

| Pattern | Detail |
|---------|--------|
| Domain modules | Self-contained under `api/`; no cross-domain imports except via service interfaces |
| Multi-tenancy | Personal = `user_id` only; org-scoped = `user_id + organization_id`. Partial indexes enforce isolation |
| OpenAPI sync | Server generates spec → `scripts/openapi-sync.sh` → `client/src/lib/types/api.d.ts` |
| Custom validation | Fluent DSL validators in `model/validator/`. **No JSR-303 annotations** |
| Security services | Named Spring beans used in SpEL expressions inside `@PreAuthorize` |
| TimescaleDB | `event` and `audit_log` are hypertables; continuous aggregates for dashboards |
| Soft delete | Only `organization` uses `deleted_at / deleted_by`; all others are hard delete |
| Audit columns | `created_at TIMESTAMPTZ` on all tables; `updated_at` on mutable tables |
| PK convention | Old: `SERIAL`; new: `BIGINT GENERATED ALWAYS AS IDENTITY`; hypertables: composite with timestamp |

## Database Conventions

- Liquibase XML with raw `<sql>` tags — **never** Liquibase XML schema tags like `<createTable>`
- File naming: `{YYYYMMDDHHMM}-PRD-{description}.xml` (use `TST` for test data only)
- Author: `jordi.jaspers` on all changesets
- Every table changeset gets a follow-up changeset with `COMMENT ON TABLE/COLUMN` SQL
- Index naming: `idx_{table}_{columns}`, unique: `uq_{table}_{description}`
- Timestamps always UTC: `TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')`

## External Services (Dev)

| Service | Purpose | Default |
|---------|---------|---------|
| TimescaleDB (pg17) | Primary database | `localhost:5432`, db: `tst_eventify` |
| Jaeger | Distributed tracing (OTLP) | `localhost:4318` (HTTP) / `16686` (UI) |
| Inbucket | Local SMTP sink | `localhost:2500` / `9000` (UI) |
| Google OAuth2 | Social login | env: `OAUTH2_GOOGLE_CLIENT_ID/SECRET` |
| GitHub OAuth2 | Social login | env: `OAUTH2_GITHUB_CLIENT_ID/SECRET` |

## Quick Reference

```bash
# Dev infrastructure
docker compose up -d                  # from repo root — starts DB, Jaeger, Inbucket

# Backend (from server/)
./gradlew bootRun                     # run with dev profile
./gradlew clean build                 # full build + tests + quality checks
./gradlew test                        # tests only
./gradlew spotlessApply               # format code

# Frontend (from client/)
bun run dev                           # dev server (port 5173)
bun run build                         # production build
bun run check                         # svelte-check type checking
bun run test                          # vitest
bun run sync:api                      # regenerate API types from server spec

# Scripts (from repo root)
scripts/database-reset.sh             # drop + recreate tst_eventify
scripts/openapi-sync.sh               # full DB reset → start server → sync types
```

## CI/CD

- **All branches/PRs:** `./gradlew clean build` + `bun run check && bun run build`
- **`develop` branch:** Docker images pushed to GHCR as `latest-dev` + `dev-<sha>`
- **Semver tags (`x.y.z`):** Release images (`x.y.z`, `x.y`, `latest`) + GitHub Release with changelog
- **Registry:** `ghcr.io/jordi-jaspers/eventify-{server,client}`

## Spring Profiles

| Profile | Liquibase context | Notes |
|---------|------------------|-------|
| `dev` | `tst` | Default active; secure-cookies disabled |
| `prd` | `prd` | SpringDoc disabled |
| `console` | — | Included by default for local console output |
