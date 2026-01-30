# Project: eventify

**Initialized:** 2026-01-23
**Last Updated:** 2026-01-30

## Configuration

| Setting | Value | Description |
|---------|-------|-------------|
| `screenshot_tests_enabled` | `true` | Enable screenshot tests for UI validation |

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

- **Backend:** DDD, layered (Controller -> Service -> Repository -> Entity)
- **Testing:** TDD (tests before implementation)
- **API:** RESTful with OpenAPI spec

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

## Skills (Global)

All skills are in the global config at `~/.config/opencode/skill/`:

| Skill | Purpose |
|-------|---------|
| springboot-standards | Spring Boot code standards, entities, services, controllers |
| liquibase-migrations-standards | Database migration patterns with raw SQL |
| sveltekit-standards | SvelteKit/Svelte 5 frontend conventions |
| ui-validation | Playwright UI validation with ui-polish-loop |
| screenshot-tests | Playwright screenshot test creation |
| gradle-test-reports | Analyze Gradle test failures from reports |
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
- After backend changes, run `bun run sync:api` to regenerate TypeScript types
- Frontend uses glassmorphism design with gradients
