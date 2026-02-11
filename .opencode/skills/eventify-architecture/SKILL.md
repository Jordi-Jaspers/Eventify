---
name: eventify-architecture
description: Eventify architecture, folder structure, and system overview. Use when understanding where code belongs or navigating the codebase.
metadata:
  skill-type: architecture
  language: Java 25, TypeScript 5.x
  framework: Spring Boot 4.0.1, SvelteKit 2.x, Svelte 5
  project-type: Monorepo (backend + frontend)
---

# Eventify Architecture

## System Overview

Eventify is a real-time event ingestion and monitoring platform designed for high-performance time-series data handling. It provides:

- **Event Ingestion:** High-volume REST API with batching and API Key authentication
- **Data Organization:** Multi-level hierarchy (Organization → Channel → Event)
- **Monitoring:** Real-time visualization and monitoring dashboards
- **Access Control:** Granular permissions, organization memberships, API key management
- **Security:** Multi-modal authentication (OAuth2, JWT, API Keys)

## Project Structure

```
eventify/
├── client/                 # SvelteKit frontend (Svelte 5, Bun)
│   ├── src/
│   │   ├── lib/            # Shared logic
│   │   │   ├── api/        # Generated API client (openapi-fetch)
│   │   │   ├── components/ # UI Components (organized by feature + /ui)
│   │   │   ├── services/   # Business logic / Complex API orchestrators
│   │   │   ├── stores/     # State management (Stores & Runes)
│   │   │   └── types/      # TypeScript definitions & API types
│   │   └── routes/         # SvelteKit file-based routing
│   │       ├── (authenticated)/  # Protected routes
│   │       └── (public)/         # Landing, login, registration
│   └── tests/              # Playwright E2E tests
│
├── server/                 # Spring Boot backend (Java 25, Gradle)
│   ├── src/
│   │   ├── main/java/io/github/eventify/
│   │   │   ├── api/        # Feature-based packages (event, user, channel, etc.)
│   │   │   └── common/     # Cross-cutting concerns (security, email, config)
│   │   └── main/resources/
│   │       └── db/         # Liquibase migrations & SQL triggers
│   └── build.gradle.kts
│
├── scripts/                # Shared automation (DB reset, CI/CD helpers)
├── docker-compose.yml      # Local development services
└── .opencode/              # Project configuration & documentation
```

## Backend Architecture

### Layer Architecture

```
Controller → Validator → Service → Repository → Entity
     ↓           ↓           ↓           ↓          ↓
  REST API   Validation  Business   Data Access   JPA Model
  Security                Logic
```

### Package Structure (Feature-First)

The backend uses **feature-first packaging** under `io.github.eventify.api`:

| Code Type | Location | Responsibilities |
|-----------|----------|------------------|
| **Controllers** | `api/<feature>/controller` | REST endpoints, `@PreAuthorize` security, call Validators |
| **Services** | `api/<feature>/service` | Business logic, `@Transactional` |
| **Repositories** | `api/<feature>/repository` | Spring Data JPA interfaces |
| **Entities** | `api/<feature>/model` | JPA Entities with Lombok |
| **DTOs** | `api/<feature>/model/request` & `/response` | Immutable records for API |
| **Mappers** | `api/<feature>/model/mapper` | MapStruct interfaces |
| **Validators** | `api/<feature>/model/validator` | Request validation logic |

### Where to Put Backend Code

| I need to create... | Package |
|---------------------|---------|
| New REST endpoint | `api/<feature>/controller/<Feature>Controller.java` |
| Business logic | `api/<feature>/service/<Feature>Service.java` |
| Database query | `api/<feature>/repository/<Feature>Repository.java` |
| JPA entity | `api/<feature>/model/<Entity>.java` |
| Request DTO | `api/<feature>/model/request/<Action>Request.java` |
| Response DTO | `api/<feature>/model/response/<Entity>Response.java` |
| Entity↔DTO mapping | `api/<feature>/model/mapper/<Feature>Mapper.java` |
| Validation rules | `api/<feature>/model/validator/<Feature>Validator.java` |
| Database migration | `server/src/main/resources/db/changelog/` |
| Cross-cutting concern | `common/<concern>/` (security, email, config) |

### Backend Key Patterns

| Pattern | Usage | Example |
|---------|-------|---------|
| Method Security | Controller authorization | `@PreAuthorize("@channelSecurity.canAccess(#id, principal)")` |
| MapStruct | Entity↔DTO conversion | `EventMapper.toCreatedResponse(event)` |
| Lombok | Reduce boilerplate | `@RequiredArgsConstructor`, `@Getter`, `@Builder` |
| Custom Validators | Request validation | `EventValidator.validateAndThrow(request)` |
| Global Exception Handler | Centralized errors | `GlobalExceptionHandler` in `common.exception.handler` |

## Frontend Architecture

### Route Structure

Routes use SvelteKit's file-based routing with layout groups:

```
routes/
├── (authenticated)/           # Requires login
│   ├── dashboard/
│   ├── channels/
│   ├── organizations/[orgId]/
│   │   ├── dashboard/
│   │   ├── channels/
│   │   ├── watchlists/
│   │   └── settings/
│   ├── watchlists/
│   ├── profile/
│   └── admin/                 # Admin-only
└── (public)/                  # No auth required
    ├── login/
    ├── register/
    └── landing/
```

### Component Organization

| Code Type | Location | Pattern |
|-----------|----------|---------|
| **Routes/Pages** | `src/routes/` | SvelteKit file-based routing |
| **Generic UI** | `src/lib/components/ui/` | shadcn-svelte (Radix primitives) |
| **Feature Components** | `src/lib/components/<feature>/` | Domain-specific (e.g., `monitor/Timeline.svelte`) |
| **Layout Components** | `src/lib/components/layout/` | AppSidebar, AppNavbar, AppLogo |
| **Services** | `src/lib/services/` | Business logic, complex API orchestration |
| **Stores** | `src/lib/stores/` | Global state (legacy stores + Svelte 5 runes) |
| **API Client** | `src/lib/api/` | openapi-fetch client |
| **Types** | `src/lib/types/` | TypeScript definitions, generated API types |

### Where to Put Frontend Code

| I need to create... | Location |
|---------------------|----------|
| New page | `src/routes/(authenticated)/<path>/+page.svelte` |
| Page data loader | `src/routes/<path>/+page.ts` or `+page.server.ts` |
| Reusable UI component | `src/lib/components/ui/<Component>.svelte` |
| Feature-specific component | `src/lib/components/<feature>/<Component>.svelte` |
| API call logic | `src/lib/services/<feature>Service.ts` |
| Global state | `src/lib/stores/<store>.ts` |
| Type definition | `src/lib/types/<types>.ts` |

### Frontend Key Patterns

| Pattern | Usage | Example |
|---------|-------|---------|
| Svelte 5 Runes | Reactivity | `let count = $state(0);` |
| OpenAPI Fetch | Type-safe API calls | `client.POST('/api/v1/events', { body })` |
| shadcn-svelte | UI components | `<Button variant="outline">` |
| Route Groups | Auth separation | `(authenticated)/`, `(public)/` |

## Database Layer

- **Technology:** TimescaleDB (PostgreSQL extension for time-series)
- **Migrations:** Liquibase with raw SQL changesets
- **Key Entities:**

| Entity | Purpose | Notes |
|--------|---------|-------|
| `User` | Identity and profile | OAuth2 linked |
| `Organization` | Multi-tenancy root | Users can belong to multiple |
| `Channel` | Logical event stream | Belongs to User or Organization |
| `Event` | Time-series data | **Timescale Hypertable** (partitioned) |
| `ApiKey` | Ingestion authentication | Scoped to User/Organization |
| `Watchlist` | Grouped channels for monitoring | Personal or org-level |

## External Integrations

| Integration | Technology | Notes |
|-------------|------------|-------|
| OAuth2 | Google, GitHub | Social login |
| JWT | HTTP-only cookies | Session management |
| Email | Spring Mail | `ConsoleEmailService` for dev, `DefaultEmailService` for prod |
| OpenAPI | Auto-generated spec | Frontend types generated from `openapi.json` |
| Observability | JFrame OTLP | Tracing and metrics |

## Quick Reference

### Commands

| Task | Command | Location |
|------|---------|----------|
| Start Backend | `./gradlew bootRun` | Root |
| Start Frontend | `bun run dev` | `client/` |
| Run Java Tests | `./gradlew test` | Root |
| Run E2E Tests | `./scripts/playwright-test.sh` | Root |
| Sync API Types | `bun run sync:api` | `client/` |
| Reset Database | `./scripts/database-reset.sh` | Root |
| Format Code | `./gradlew spotlessApply` | Root |

### Key Files

| Purpose | Path |
|---------|------|
| Backend config | `server/src/main/resources/application.yml` |
| Frontend config | `client/svelte.config.js` |
| API types | `client/src/lib/types/api.d.ts` |
| OpenAPI spec | `server/openapi.json` |
| Styling guide | `.opencode/STYLING-GUIDE.md` |
| Database migrations | `server/src/main/resources/db/changelog/` |

### Development Notes

- After backend API changes, run `bun run sync:api` from `client/` to regenerate TypeScript types
- Frontend uses glassmorphism design with dark-mode-first aesthetic
- UI components follow shadcn-svelte patterns
- Use `@PreAuthorize` annotations for endpoint security
- All database changes require Liquibase migrations
