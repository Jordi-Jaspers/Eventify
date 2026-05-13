# Changelog Backend Source

**Completed:** 2026-05-13
**Epic:** NOTIF
**Source:** .opencode/refined/NOTIF-04-changelog-backend-source.md

## Summary

Moved changelog data from static frontend TypeScript to backend JSON served via public REST API.

## Plan Approved by the user:

### Requirements Summary

- Serve changelog entries from backend API (public, no auth)
- GET all entries (newest first) + GET by version (404 if not found)
- Frontend loads via +page.server.ts instead of static import
- Delete static data file

### Technical Approach

- Backend: Public REST endpoints, service with @PostConstruct JSON loading, ResourceLoaderUtil
- Frontend: +page.server.ts fetch, data prop in page component

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Create backend test suite |
| 2 | spring-backend-agent | Implement API |
| 3 | backend-optimizer-agent | Refactor |
| 4 | svelte-frontend-agent | Update frontend to use API |
| 5 | frontend-optimizer-agent | Refactor |

## Implementation

### Backend

- GET /v1/public/changelog → List<ChangelogEntry>
- GET /v1/public/changelog/{version} → ChangelogEntry or 404
- ChangelogService loads+caches from classpath:changelog.json
- ResourceLoaderUtil reusable utility created
- CouldNotLoadResourceException (ERR-0062), CHANGELOG_NOT_FOUND (ERR-0061)

### Frontend

- +page.server.ts fetches from API
- +page.svelte uses data prop with categoryConfig loop
- Deleted $lib/data/changelog.ts

### Deviations from Plan

- ChangelogEntry is a class (not record) per user preference
- ResourceLoaderUtil extracted as reusable utility (not in original plan)
- Frontend page refactored from 3 copy-pasted sections to single loop

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Backend tests (14) | Complete |
| spring-backend-agent | API implementation | Complete |
| backend-optimizer-agent | Refactor backend | -12.5% lines |
| svelte-frontend-agent | Frontend API integration | Complete |
| frontend-optimizer-agent | Refactor frontend | -32% lines |

## Files Modified

- `server/src/main/java/io/github/eventify/api/Paths.java` - added PUBLIC_CHANGELOG_PATH constants
- `server/src/main/java/io/github/eventify/api/changelog/model/ChangelogEntry.java` - new
- `server/src/main/java/io/github/eventify/api/changelog/service/ChangelogService.java` - new
- `server/src/main/java/io/github/eventify/api/changelog/controller/ChangelogController.java` - new
- `server/src/main/resources/changelog.json` - new (migrated data)
- `server/src/main/java/io/github/eventify/common/util/ResourceLoaderUtil.java` - new
- `server/src/main/java/io/github/eventify/common/exception/CouldNotLoadResourceException.java` - new
- `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java` - added 2 codes
- `server/src/test/java/io/github/eventify/api/changelog/service/ChangelogServiceTest.java` - new
- `server/src/test/java/io/github/eventify/api/changelog/controller/ChangelogControllerTest.java` - new
- `server/src/test/resources/changelog.json` - new (test data)
- `client/src/routes/(authenticated)/changelog/+page.server.ts` - new
- `client/src/routes/(authenticated)/changelog/+page.svelte` - updated
- `client/src/lib/data/changelog.ts` - deleted
- `.opencode/skills/eventify-whats-new/SKILL.md` - updated reference

## Tests

- 14 backend tests written, 14 passing
