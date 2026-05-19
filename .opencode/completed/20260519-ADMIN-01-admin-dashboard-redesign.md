# Admin Dashboard Redesign — Overview & Infrastructure

**Completed:** 2026-05-19
**Epic:** ADMIN
**Source:** `.opencode/refined/ADMIN-01-admin-dashboard-redesign.md`

## Summary

Redesigned admin statistics page with tabbed navigation (Overview/Infrastructure), global time range selector (7d/30d/90d), channel/event stats, best growth day records, API key stats, channel health, and storage snapshot. Removed quick actions.

## Plan Approved by the user:

### Requirements Summary

- Global time range selector (7d/30d/90d) for all time-based metrics
- Overview tab: 4 stat cards + growth chart (toggled Users&Orgs / Events) + Records section
- Infrastructure tab: API key stats + channel health + storage snapshot
- Remove quick actions
- Tab state in URL: `?tab=overview|infrastructure`
- All endpoints require `VIEW_PLATFORM_STATS`

### Technical Approach

- Backend: Extended `GET /v1/admin/stats?days=` + new `GET /v1/admin/stats/storage`
- Frontend: Full rewrite of statistics page with pill tabs and chart toggle

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Backend test suite |
| 2 | spring-backend-agent | Implementation |
| 3 | backend-optimizer-agent | Refactor |
| 4 | svelte-frontend-agent | Frontend implementation |
| 5 | frontend-optimizer-agent | Frontend refactor |

## Implementation

### Backend

- `AdminStatsService` — extended with channel counts, event counts, best growth days, configurable days param
- `AdminDashboardController` — added `days` query param + `/storage` endpoint
- `AdminStatsValidator` — validates days (7/30/90)
- `AdminStorageRepository` — native query for pg_total_relation_size
- `AdminStatsResponse` — added channel/event/bestGrowthDay fields
- `GrowthDataPoint` — added newEvents, newEventsGrowthPercentage
- `TableSizeEntry` — new response class
- `ChannelRepository` — added countByIsStaleTrue()
- `EventRepository` — added countByTimestampAfter(), findDailyEventCounts()

### Frontend

- Full rewrite of `+page.svelte` with pill tab nav, time range selector, chart toggle
- Updated `AdminController.ts` with days param + getStorageStats()
- Added `TableSizeEntry` export to models.ts

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Backend tests | 70 tests (41 service + 29 controller) |
| spring-backend-agent | Implementation + 2 review rounds | Complete |
| backend-optimizer-agent | Refactor service | -8% lines |
| svelte-frontend-agent | Frontend implementation | Complete |
| frontend-optimizer-agent | Frontend refactor | -5% page, -29% controller |

## Files Modified

- `server/src/main/java/.../admin/service/AdminStatsService.java`
- `server/src/main/java/.../admin/controller/AdminDashboardController.java`
- `server/src/main/java/.../admin/model/validator/AdminStatsValidator.java` (new)
- `server/src/main/java/.../admin/repository/AdminStorageRepository.java` (new)
- `server/src/main/java/.../admin/model/response/AdminStatsResponse.java`
- `server/src/main/java/.../admin/model/response/GrowthDataPoint.java`
- `server/src/main/java/.../admin/model/response/TableSizeEntry.java` (new)
- `server/src/main/java/.../channel/model/ChannelStatus.java`
- `server/src/main/java/.../channel/repository/ChannelRepository.java`
- `server/src/main/java/.../event/repository/EventRepository.java`
- `server/src/main/java/.../Paths.java`
- `server/src/test/.../admin/service/AdminStatsServiceTest.java`
- `server/src/test/.../admin/controller/AdminDashboardControllerTest.java`
- `client/src/routes/(authenticated)/admin/statistics/+page.svelte`
- `client/src/lib/api/admin/AdminController.ts`
- `client/src/lib/api/models.ts`

## Tests

- 70 backend tests written, all passing
