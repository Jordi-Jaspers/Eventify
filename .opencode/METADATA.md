# Project: eventify

**Initialized:** 2026-05-29

## Configuration

| Setting             | Value | Description                                                           |
|---------------------|-------|-----------------------------------------------------------------------|
| skip_frontend_tests | true  | Skip frontend tests during the TDD-workflow (only 2 test files exist) |
| skip_backend_tests  | false | Skip backend tests during the TDD-workflow                            |

## Tech Stack

| Type                 | Technology                         | Version       |
|----------------------|------------------------------------|---------------|
| Backend Language     | Java                               | 25            |
| Backend Framework    | Spring Boot                        | 4.1.0-RC1     |
| Build Tool           | Gradle (Kotlin DSL)                | latest        |
| Frontend Framework   | SvelteKit                          | 2.59          |
| Frontend Language    | TypeScript / Svelte 5              | —             |
| CSS                  | Tailwind CSS                       | v4            |
| UI Components        | shadcn-svelte                      | —             |
| Database             | PostgreSQL + TimescaleDB           | pg17          |
| Migrations           | Liquibase                          | XML + raw SQL |
| Package Manager (FE) | Bun                                | ≥1.3.0        |
| API Contract         | OpenAPI 3 + openapi-fetch          | —             |
| Mapping              | MapStruct                          | 1.6.3         |
| Testing (BE)         | JUnit 5 + Mockito + Testcontainers | —             |
| Testing (FE)         | Vitest                             | —             |

## Agents

| Agent                 | Path                                        | Purpose                         |
|-----------------------|---------------------------------------------|---------------------------------|
| backend-agent         | `.opencode/agents/backend-agent.md`         | Implements Spring Boot features |
| backend-testing-agent | `.opencode/agents/backend-testing-agent.md` | Writes JUnit 5 tests            |
| frontend-agent        | `.opencode/agents/frontend-agent.md`        | Builds SvelteKit UI             |

## Skills

| Skill                             | Path                                                          | Purpose                                    |
|-----------------------------------|---------------------------------------------------------------|--------------------------------------------|
| eventify-architecture             | `.opencode/skills/eventify-architecture/SKILL.md`             | Project structure, where to put code       |
| eventify-backend-patterns         | `.opencode/skills/eventify-backend-patterns/SKILL.md`         | Java code patterns, naming, structure      |
| eventify-backend-testing-patterns | `.opencode/skills/eventify-backend-testing-patterns/SKILL.md` | Test patterns, base classes, fixtures      |
| eventify-frontend-patterns        | `.opencode/skills/eventify-frontend-patterns/SKILL.md`        | Svelte component, store, API patterns      |
| eventify-whats-new                | `.opencode/skills/eventify-whats-new/SKILL.md`                | User-facing changelog update guidelines    |
| eventify-release-process          | `.opencode/skills/eventify-release-process/SKILL.md`          | Git flow release conventions               |
| jframe-search-pagination          | `.opencode/skills/jframe-search-pagination/SKILL.md`          | JFrame search/pagination endpoint patterns |

## Architecture

Eventify is an event notification and monitoring platform. It is a fullstack monorepo:

- **Backend:** Spring Boot 4.1 (Java 25), Gradle Kotlin DSL, Lombok, MapStruct
- **Frontend:** SvelteKit (Svelte 5), TypeScript, Tailwind CSS v4, Bun, shadcn-svelte
- **Database:** PostgreSQL 17 + TimescaleDB (time-series hypertables)
- **API contract:** OpenAPI spec auto-generated from server → typed TS client via `openapi-fetch`

### Project Structure

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

### Backend Domain Modules

Each domain under `api/{domain}/`:

```
controller/  → REST controllers ({Context}{Domain}Controller)
service/     → Business logic (split by responsibility)
repository/  → Spring Data JPA + JpaSpecificationExecutor
model/
  {Domain}.java       → JPA entity
  request/            → Inbound DTOs
  response/           → Outbound DTOs (@Schema annotated)
  mapper/             → MapStruct mappers
  validator/          → Fluent DSL validators (NOT JSR-303)
job/          → (optional) Scheduled tasks
cache/        → (optional) Cache logic
```

**Domains:** organization, token, monitor, bootstrap, changelog, notification, admin, user, subscription, channel,
dashboard, apikey, quota, authentication, event, watchlist, session

### Where to Put New Code

| I need to create... | Location                                                                          |
|---------------------|-----------------------------------------------------------------------------------|
| New domain module   | `server/src/main/java/io/github/eventify/api/{domain}/`                           |
| Controller          | `api/{domain}/controller/{Context}{Domain}Controller.java`                        |
| Service             | `api/{domain}/service/{Domain}Service.java`                                       |
| Entity              | `api/{domain}/model/{Domain}.java`                                                |
| Request DTO         | `api/{domain}/model/request/Create{Domain}Request.java`                           |
| Response DTO        | `api/{domain}/model/response/{Domain}Response.java`                               |
| Mapper              | `api/{domain}/model/mapper/{Domain}Mapper.java`                                   |
| Validator           | `api/{domain}/model/validator/{Domain}Validator.java`                             |
| DB migration        | `server/src/main/resources/db/changelog/changesets/{YYYYMMDDHHMM}-PRD-{desc}.xml` |
| New page            | `client/src/routes/(authenticated)/{route}/+page.svelte`                          |
| Feature component   | `client/src/lib/components/{feature}/{ComponentName}.svelte`                      |
| API controller (TS) | `client/src/lib/api/{Domain}Controller.ts`                                        |
| Store / service     | `client/src/lib/stores/{domain}.svelte.ts`                                        |

### Key Patterns

| Pattern           | Detail                                                                             |
|-------------------|------------------------------------------------------------------------------------|
| Domain modules    | Self-contained under `api/`; no cross-domain imports except via service interfaces |
| Multi-tenancy     | Personal = `user_id` only; org-scoped = `user_id + organization_id`                |
| OpenAPI sync      | Server generates spec → `scripts/openapi-sync.sh` → typed client                   |
| Custom validation | Fluent DSL validators. **No JSR-303**                                              |
| Security services | Named beans for SpEL in `@PreAuthorize`                                            |
| TimescaleDB       | `event` and `audit_log` are hypertables; continuous aggregates for dashboards      |

### Quick Reference

```bash
# Dev infrastructure
docker compose up -d                  # starts DB, Jaeger, Inbucket

# Backend (from server/)
./gradlew bootRun                     # run with dev profile
./gradlew clean build                 # full build + tests + quality
./gradlew test                        # tests only
./gradlew spotlessApply               # format code

# Frontend (from client/)
bun run dev                           # dev server (port 5173)
bun run build                         # production build
bun run check                         # type checking
bun run test                          # vitest
bun run sync:api                      # regenerate API types from server spec

# Scripts
scripts/database-reset.sh             # drop + recreate tst_eventify
scripts/openapi-sync.sh               # full sync pipeline
```

## Notes

- After adding/changing backend endpoints, run `bun run sync:api` from client/ to regenerate types
- Validators use fluent DSL, NOT JSR-303 annotations — no `@Valid` anywhere
- All path constants centralized in `api/Paths.java`
- Error codes in `common/exception/ApiErrorCode.java` — add new ones sequentially (ERR-0063, etc.)
- Frontend has minimal tests (2 files) — skip_frontend_tests=true
- Dev stack: `docker compose up -d` from root for TimescaleDB + Jaeger + Inbucket
