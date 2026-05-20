# Events Analytics Tab

**Completed:** 2026-05-19
**Epic:** ADMIN
**Source:** .opencode/refined/ADMIN-02-events-analytics-tab.md

## Summary

Added Events analytics tab (3rd) to admin statistics page. Backend aggregates from TimescaleDB `event_timeline_hourly` continuous aggregate + `user_event_quota`. Frontend shows daily ingestion chart, severity breakdown, quota stats, and top channels table.

## Plan Approved by the user:

### Requirements Summary

- `GET /api/v1/admin/stats/events?days=30` secured by `VIEW_PLATFORM_STATS`
- Returns: dailyIngestion[], topChannels[] (top 10), severityBreakdown, quotaStats
- New "Events" tab on admin statistics page
- Time range respects existing `?days=` global selector (7/30/90)

### Technical Approach

- Backend: New endpoint, service, repository (native SQL on continuous aggregate), domain/mapper/response pattern
- Frontend: 3rd tab with lazy-load, AreaChartCard for ingestion, StatCards for severity, table for top channels

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Backend tests (26 tests) |
| 2.2 | spring-backend-agent | Implement service + endpoint |
| 2.3 | — | Backend review gate |
| 2.4 | backend-optimizer-agent | Refactor (JPA repos, merged mapper, domain split) |
| 3.2 | svelte-frontend-agent | Build Events tab UI |
| 3.3 | — | Frontend review gate |
| 3.4 | frontend-optimizer-agent | Extract tab components |

## Implementation

### Backend

- Endpoint: `GET /api/v1/admin/stats/events?days={7|30|90}`
- Service: `AdminEventStatsService` — queries `event_timeline_hourly` + `user_event_quota`
- Repository: `EventTimelineRepository` (JPA interface, native queries)
- Domain: `EventStats`, `DailyIngestionData`, `TopChannelInfo`, `SeverityBreakdownData`, `QuotaStatsData`
- Mapper: `AdminStatsMapper` (merged, 5 methods)
- Also refactored: `AdminStorageRepository` → JPA, `StorageStats` domain model

### Frontend

- Page: `admin/statistics/+page.svelte` (thin orchestrator, ~160 lines)
- Extracted: `OverviewTab.svelte`, `InfrastructureTab.svelte`, `EventsTab.svelte`, `statistics-helpers.ts`
- API: `getEventStats(days)` in `AdminController.ts` via openapi-fetch

### Deviations from Plan

- Added LEFT JOIN for top channels query (channels without org)
- Extracted all 3 tabs into components (optimizer phase), not just events
- Merged EventStatsMapper + StorageStatsMapper into single AdminStatsMapper
- Converted both repositories to JPA interfaces (user request)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Backend tests | 26 tests created |
| spring-backend-agent | Implementation + refactoring (5 sessions) | All tests passing |
| svelte-frontend-agent | Events tab UI | Built + type-checked |
| frontend-optimizer-agent | Extract components | 780→160 lines main file |

## Files Modified

- `server/.../api/Paths.java` — added ADMIN_STATS_EVENTS_PATH
- `server/.../admin/controller/AdminDashboardController.java` — added getEventStats endpoint
- `server/.../admin/service/AdminEventStatsService.java` — new service
- `server/.../admin/service/AdminStatsService.java` — refactored to use mapper
- `server/.../admin/repository/EventTimelineRepository.java` — new JPA repo
- `server/.../admin/repository/AdminStorageRepository.java` — converted to JPA
- `server/.../admin/model/` — 6 domain classes
- `server/.../admin/model/response/` — 5 response DTOs
- `server/.../admin/model/projection/` — 3 projections
- `server/.../admin/model/mapper/AdminStatsMapper.java` — merged mapper
- `server/.../quota/repository/UserEventQuotaRepository.java` — added quota queries
- `server/src/test/.../AdminEventStatsControllerTest.java` — 14 tests
- `server/src/test/.../AdminEventStatsServiceTest.java` — 12 tests
- `server/src/test/.../AdminStatsServiceTest.java` — updated for mapper
- `client/src/lib/api/admin/AdminController.ts` — added getEventStats
- `client/src/lib/types/api.d.ts` — regenerated from OpenAPI
- `client/src/routes/(authenticated)/admin/statistics/+page.svelte` — refactored
- `client/src/lib/components/admin/statistics/` — 3 tab components + helpers

## Tests

- 26 backend tests written, all passing
- Frontend: type-check passes (skip_frontend_tests: true)
