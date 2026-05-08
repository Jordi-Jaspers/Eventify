---
epic: "NOTIF"
title: "Move Changelog Source to Backend"
estimate: S
status: ready
created: 2026-05-08
depends_on: []
labels: [backend, frontend, refactor]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** developer releasing a new version of Eventify\
**I want** the changelog to live in the backend codebase\
**So that** the same source of truth feeds both the public `/changelog` page and the automated release-notification dispatch (NOTIF-05), eliminating the chance of releasing a version with no notification because the frontend changelog was updated but the notification was forgotten.\

## 2. Business Context & Value
The changelog today lives in `client/src/lib/data/changelog` (frontend, static TS file). It feeds the `/changelog` page. NOTIF-05 will dispatch release notifications automatically based on the current version — but the notification needs content (title, summary, action URL). Putting changelog data in the backend creates one source of truth: dev updates `changelog.json` once, both the page and the auto-dispatch use it. If dev forgets, both break visibly together (good — failure is loud).

## 3. Acceptance Criteria
* [ ] **Scenario 1: Backend serves changelog**
    * Given the backend has loaded `changelog.json`
    * When a client calls `GET /api/v1/changelog`
    * Then the response is an array of changelog entries: `{ version, releaseDate, title, description, highlights: [string], category }` ordered by `releaseDate DESC`
* [ ] **Scenario 2: Single-version lookup**
    * Given the backend changelog
    * When a client calls `GET /api/v1/changelog/{version}` (e.g., `GET /api/v1/changelog/1.1.0`)
    * Then the matching entry is returned, or 404 if version doesn't exist
* [ ] **Scenario 3: Frontend `/changelog` page reads from API**
    * Given a user visits `/changelog`
    * When the page loads
    * Then the changelog data is fetched from `GET /api/v1/changelog` (not from a local TS file) and rendered identically to today
* [ ] **Scenario 4: Old frontend changelog data deleted**
    * Given the new backend source is live
    * When the codebase is searched
    * Then `client/src/lib/data/changelog/*` no longer exists; no imports reference it
* [ ] **Scenario 5: Public endpoint (no auth required)**
    * Given an unauthenticated visitor
    * When they call `GET /api/v1/changelog`
    * Then they receive the changelog (this is public information — same as a marketing page)
* [ ] **Scenario 6: Migration of existing entries**
    * Given the existing entries in `client/src/lib/data/changelog/*`
    * When this story is implemented
    * Then all entries are migrated 1:1 into `server/src/main/resources/changelog.json` with no content loss
* [ ] **Edge Case: Malformed changelog.json**
    * Given an invalid JSON in `changelog.json`
    * When the app starts
    * Then app fails to start with a clear error message (fail-fast — better than silently serving empty changelog)

## 4. Technical Requirements
* **API Changes**:
    * `GET /api/v1/changelog` → `[{ version: string, releaseDate: ISO date, title: string, description: string, highlights: string[], category: string }]`
    * `GET /api/v1/changelog/{version}` → single entry or 404
* **Database**: N/A — changelog stored as a static JSON file in the backend resources directory, loaded once at startup. No DB table.
* **Security**: Both endpoints permitted unauthenticated. Add to security config allowlist (similar to `/api/v1/health` or marketing endpoints).
* **Performance**: Cached in memory at startup (one-time JSON parse, ~ms). No re-read after startup. New release = redeploy.

## 5. Design & UI/UX
N/A for this story — `/changelog` page rendering is unchanged, only the data source changes. Page layout, styling, and interactions stay the same.

## 6. Implementation Notes
**Backend:**
- File: `server/src/main/resources/changelog.json` — JSON array
- `api/changelog/model/ChangelogEntry.java` — record matching JSON structure
- `api/changelog/service/ChangelogService.java` — `@PostConstruct` loads + parses + caches the JSON; methods: `getAll()`, `getByVersion(String)`
- `api/changelog/controller/ChangelogController.java` — two endpoints
- Security config (`common/security/SecurityConfig.java` or similar) — permit `/api/v1/changelog/**` unauthenticated

**Frontend:**
- `client/src/routes/changelog/+page.ts` (or `+page.server.ts`) — load via API client instead of importing TS data
- `client/src/lib/data/changelog/` — DELETE entire directory
- Run `bun run sync:api` to regenerate types

**Migration of content:**
- Read existing `client/src/lib/data/changelog/*.ts` files
- Convert each entry to JSON
- Result: single `changelog.json` array with all historical entries
- Verify rendered `/changelog` page is identical before/after

**Skill update:**
- Update `.opencode/skills/eventify-whats-new/SKILL.md` to point at `server/src/main/resources/changelog.json` instead of frontend file. The skill's instructions for "how to add a What's New entry" change from "edit the TS file" to "add a JSON object to changelog.json".

**Pitfalls:**
- The `/changelog` page is currently statically-rendered (likely SSR-friendly via local import). After the change, it depends on a backend call — make sure SSR works via `+page.server.ts` (no client-only API call).
- Don't lose any existing entries in migration — diff before/after.

## 7. Test Impact Analysis (Refactoring)
### Existing tests affected by this change:
| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| Tests importing `client/src/lib/data/changelog` | various | Static TS data shape | YES | Replace import with API mock |
| Playwright `/changelog` page test (if exists) | Page renders entries | Frontend data source | NO | Should still pass since rendered output is identical |

### Test modification policy:
- [x] Existing tests MAY be updated where they import the old static file (file is being deleted)
- [x] Add backend test: `ChangelogService` loads JSON correctly, throws on malformed file
- [x] Add controller integration test: `GET /changelog` returns entries, `GET /changelog/{version}` works + 404

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `server/src/main/resources/changelog.json` | NEW — migrate all existing entries | all |
| `client/src/lib/data/changelog/` | DELETE entire directory | all |
| `client/src/routes/changelog/+page.{ts,server.ts}` | Fetch from API | most |
| `client/src/routes/changelog/+page.svelte` | Update import / data prop | small |
| `.opencode/skills/eventify-whats-new/SKILL.md` | Update path references | small |
