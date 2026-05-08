# WATCHLIST - 03 CRUD API Organization

**Completed:** 2026-01-28
**Vibe Kanban Task:** WATCHLIST - 03 CRUD API Organization
**Task ID:** c40666aa-8f65-4c4c-a2a5-9259bade3a03

## Summary

Extended the existing user watchlist backend with organization-level CRUD endpoints. Created organization watchlist service and controller with role-based access control. Refactored common logic into AbstractWatchlistService base class for DRY code.

## Agents Used

| Agent | Task |
|-------|------|
| testing-agent | Created 27 tests (15 controller + 12 service) |
| backend-agent | Implemented service, controller, repository extensions |
| orchestrator | Refactored services to use AbstractWatchlistService |

## API Endpoints

| Method | Path | Auth |
|--------|------|------|
| POST | `/v1/organization/{orgId}/watchlist` | OWNER/ADMIN |
| POST | `/v1/organization/{orgId}/watchlist/search` | Any member |
| GET | `/v1/organization/{orgId}/watchlist/{id}` | Any member |
| PUT | `/v1/organization/{orgId}/watchlist/{id}` | OWNER/ADMIN |
| DELETE | `/v1/organization/{orgId}/watchlist/{id}` | OWNER/ADMIN |

## Files Created

- `server/src/main/java/io/github/eventify/api/watchlist/service/AbstractWatchlistService.java`
- `server/src/main/java/io/github/eventify/api/watchlist/service/OrganizationWatchlistService.java`
- `server/src/main/java/io/github/eventify/api/watchlist/controller/OrganizationWatchlistController.java`
- `server/src/test/java/io/github/eventify/api/watchlist/controller/OrganizationWatchlistControllerTest.java`
- `server/src/test/java/io/github/eventify/api/watchlist/service/OrganizationWatchlistServiceTest.java`

## Files Modified

- `server/src/main/java/io/github/eventify/api/watchlist/service/WatchlistService.java` - Extends AbstractWatchlistService
- `server/src/main/java/io/github/eventify/api/watchlist/repository/WatchlistRepository.java` - Added org queries
- `server/src/main/java/io/github/eventify/api/channel/repository/ChannelRepository.java` - Added org channel batch query
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistMetaData.java` - Added org specification
- `server/src/main/java/io/github/eventify/api/Paths.java` - Added org watchlist path constants
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java` - Added getOrganization alias

## Key Features

- **RBAC**: OWNER/ADMIN can create/update/delete, any member can view/search
- **Global Admin Bypass**: MANAGE_ORGANIZATIONS authority grants full access
- **Channel Validation**: Only organization channels accepted (personal/other-org rejected)
- **Duplicate Name Check**: Scoped to organization
- **AbstractWatchlistService**: Common logic extracted (defaults, updates, pagination)

## Tests

- 27 new tests written (15 integration + 12 unit)
- All watchlist tests passing (45+ total)
- Full test suite passing

## Notes

- Backend-only feature (no frontend in this task)
- Follows same patterns as OrganizationChannelService/Controller
- Security annotations match existing org endpoints
