# Project: eventify

**Initialized:** 2026-01-23
**Last Updated:** 2026-02-11

## Configuration

| Setting | Value | Description |
|---------|-------|-------------|
| `screenshot_tests_enabled` | `true` | Enable screenshot tests for UI validation |
| `update_whats_new` | `true` | Add completed stories to What's New changelog (`client/src/lib/data/changelog.ts`) |

## Project Files

All project-specific files are in `.opencode/`:

| File | Purpose |
|------|---------|
| `BACKLOG.md` | Raw ideas, future work (needs refinement) |
| `CHANGELOG.md` | Completed features index |
| `STYLING-GUIDE.md` | UI design system reference |
| `refined/` | Refined stories ready for development |
| `completed/` | Audit copies of completed work |
| `agents/` | Project-specific agent overrides |
| `skills/` | Project-specific skill files |

## Tech Stack

| Type | Technology | Version |
|------|------------|---------|
| Language | Java | 25 |
| Language | TypeScript | 5.x |
| Framework | Spring Boot | 4.0.1 |
| Framework | SvelteKit | 2.x |
| Framework | Svelte | 5 |
| Database | TimescaleDB (PostgreSQL) | 16 |
| Build Tool | Gradle | 8.x |
| Package Manager | Bun | 1.x |
| CSS | TailwindCSS | 4 |

## Architecture

### System Overview

Eventify is a real-time event ingestion and monitoring platform designed for high-performance time-series data handling. It provides:

- **Event Ingestion:** High-volume REST API with batching and API Key authentication
- **Data Organization:** Multi-level hierarchy (Organization → Channel → Event)
- **Monitoring:** Real-time visualization and monitoring dashboards
- **Access Control:** Granular permissions, organization memberships, API key management
- **Security:** Multi-modal authentication (OAuth2, JWT, API Keys)

### Project Structure

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

### Backend Layer Architecture

```
Controller → Validator → Service → Repository → Entity
     ↓           ↓           ↓           ↓          ↓
  REST API   Validation  Business   Data Access   JPA Model
  Security                Logic
```

The backend uses **feature-first packaging** under `io.github.eventify.api`:

| Code Type | Location | Responsibilities |
|-----------|----------|------------------|
| **Controllers** | `api/<feature>/controller` | REST endpoints, `@PreAuthorize` security |
| **Services** | `api/<feature>/service` | Business logic, `@Transactional` |
| **Repositories** | `api/<feature>/repository` | Spring Data JPA interfaces |
| **Entities** | `api/<feature>/model` | JPA Entities with Lombok |
| **DTOs** | `api/<feature>/model/request` & `/response` | Immutable records for API |
| **Mappers** | `api/<feature>/model/mapper` | MapStruct interfaces |
| **Validators** | `api/<feature>/model/validator` | Request validation logic |

### Frontend Architecture

Routes use SvelteKit's file-based routing with layout groups:

| Code Type | Location | Pattern |
|-----------|----------|---------|
| **Routes/Pages** | `src/routes/` | File-based with `(authenticated)/`, `(public)/` groups |
| **Generic UI** | `src/lib/components/ui/` | shadcn-svelte (Radix primitives) |
| **Feature Components** | `src/lib/components/<feature>/` | Domain-specific components |
| **Services** | `src/lib/services/` | Business logic, API orchestration |
| **Stores** | `src/lib/stores/` | Global state (Svelte 5 runes) |
| **API Client** | `src/lib/api/` | openapi-fetch client |
| **Types** | `src/lib/types/` | TypeScript definitions |

### Database Layer

- **Technology:** TimescaleDB (PostgreSQL extension for time-series)
- **Migrations:** Liquibase with raw SQL changesets
- **Key Entities:** User, Organization, Channel, Event (Hypertable), ApiKey, Watchlist

### Key Patterns

| Pattern | Where Used | Example |
|---------|------------|---------|
| Method Security | Backend Controllers | `@PreAuthorize("@channelSecurity.canAccess(#id, principal)")` |
| MapStruct | Entity↔DTO mapping | `EventMapper.toCreatedResponse(event)` |
| Lombok | Reduce boilerplate | `@RequiredArgsConstructor`, `@Getter`, `@Builder` |
| Svelte 5 Runes | Frontend reactivity | `let count = $state(0);` |
| OpenAPI Fetch | Type-safe API calls | `client.POST('/api/v1/events', { body })` |

### Quick Reference

| Task | Command | Location |
|------|---------|----------|
| Start Backend | `./gradlew bootRun` | Root |
| Start Frontend | `bun run dev` | `client/` |
| Run Java Tests | `./gradlew test` | Root |
| Run E2E Tests | `./scripts/playwright-test.sh` | Root |
| Sync API Types | `bun run sync:api` | `client/` |
| Reset Database | `./scripts/database-reset.sh` | Root |
| Format Code | `./gradlew spotlessApply` | Root |

## Agents

### Project-Specific Agents (in `.opencode/agents/`)

| Agent | Purpose |
|-------|---------|
| spring-testing-agent | Write Java/JUnit tests (TDD first) |
| spring-backend-agent | Implement Spring Boot backend |
| svelte-frontend-agent | Build SvelteKit frontend |

**Note:** These agents contain eventify-specific patterns (test infrastructure, coding standards). 
They override the global agents of the same name when working on this project.

### Global Agents (in `~/.config/opencode/agent/`)

| Agent | Purpose |
|-------|---------|
| ui-validator | Polish UI appearance |
| deep-research-agent | Explore codebase, find patterns |
| frontend-optimizer-agent | Refactor frontend for maintainability |
| github-actions-agent | CI/CD workflows |
| email-composer-agent | MJML email templates |

## Skills

### Project-Specific Skills (in `.opencode/skills/`)

| Skill | Purpose |
|-------|---------|
| eventify-architecture | Project structure, layer architecture, where code belongs (REQUIRED) |
| eventify-spring-standards | Spring Boot patterns: JFrame search/pagination, entities, services, controllers, tests |
| eventify-svelte-standards | SvelteKit patterns: Controller→Service→Page, API client, DataTable, reusable components |

**Note:** Project-specific skills contain patterns tailored to this codebase. Agents should load these first.

### Global Skills (in `~/.config/opencode/skill/`)

| Skill | Purpose |
|-------|---------|
| liquibase-migrations-standards | Database migration patterns with raw SQL |
| ui-validation | Playwright UI validation with ui-polish-loop |
| screenshot-tests | Playwright screenshot test creation |
| gradle-test-reports | Analyze Gradle test failures from reports |
| spring-security-best-practices | JWT, authentication, authorization patterns |
| api-design-best-practices | REST API design patterns |
| svelte-best-practices | Svelte 5 runes, reactivity patterns |
| reflect | Session analysis and skill improvement |

## Bounded Contexts

- Authentication & User Management
- Event Management (Channels, Events, Watchlists)
- Team Management (Organizations, Memberships)
- Notification System

## Security Patterns

- Input validation with custom validators
- Rate limiting for sensitive operations
- Token hashing before storage
- Audit logging for security events
- JWT with RSA signing, refresh token rotation

## Notes

- UI polish uses `~/.config/opencode/scripts/ui-polish-loop.sh`
- Styling guide: `.opencode/STYLING-GUIDE.md`
- After backend API changes, run `bun run sync:api` from `client/` to regenerate TypeScript types
- Frontend uses glassmorphism design with dark-mode-first aesthetic
