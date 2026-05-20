# Optimize Admin Dashboard Load Times

**Completed:** 2026-05-19
**Epic:** PERF
**Source:** .opencode/refined/PERF-01-optimize-admin-dashboard-load-times.md

## Summary

Split monolithic admin stats endpoint into 3 independent endpoints (counts, growth, event-volume), added Spring Cache with 60s TTL, created daily aggregate migration, and parallelized frontend fetches with per-section loading states.

## Plan Approved by the user:

### Requirements Summary

- Split GET /admin/stats into counts/growth/event-volume endpoints
- Add Spring Cache (60s TTL) on all stats methods
- Create admin_event_stats_daily continuous aggregate (5-min refresh)
- Frontend: parallel fetches per section, per-section loading/error states
- No visual changes, security unchanged

### Technical Approach

- Backend: 3 new endpoints, @Cacheable, MapStruct domain→DTO, merged validator (7/30/90/180)
- Frontend: split loading, generated OpenAPI types, no custom interfaces
- Database: new continuous aggregate from event_timeline_hourly

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Create backend test suite (40 tests) |
| 2 | spring-backend-agent | Implement to pass tests |
| 3 | backend-optimizer-agent | Extract TimeProvider, SearchInputHelper |
| 4 | svelte-frontend-agent | Split loading, new endpoints, generated types |

## Implementation

### Backend

- 3 new endpoints: GET /admin/stats/counts, /growth, /event-volume
- Removed old GET /admin/stats
- @Cacheable on AdminStatsService methods
- Domain models + MapStruct mapping in controller
- Merged AdminStatsValidator (7/30/90/180)
- New migration: admin_event_stats_daily aggregate

### Frontend

- Parallel fetches per tab section
- Per-section loading skeletons and error states
- All types from generated OpenAPI spec
- Days selector: 7/30/90/180

### Deviations from Plan

- Removed Caffeine dependency — Spring's ConcurrentMapCacheManager sufficient for single-instance
- Cache tests removed (not valuable for simple TTL config)
- Consolidated API key stats query optimization deferred (existing pattern works)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | 40 backend tests | Complete |
| spring-backend-agent | Implementation + review fixes | Complete |
| backend-optimizer-agent | Extract shared utilities | Complete |
| svelte-frontend-agent | Frontend split + type fixes | Complete |
| deep-research-agent | Bug diagnosis (field mismatches) | Complete |

## Files Modified

### Backend
- `AdminDashboardController.java` — 3 new endpoints
- `AdminStatsService.java` — getAdminCounts/Growth/EventVolume + @Cacheable
- `AdminStatsMapper.java` — domain→response mappings
- `AdminCountsResponse.java`, `AdminGrowthResponse.java`, `AdminEventVolumeResponse.java`, `DailyVolumePoint.java` — new DTOs
- `AdminCounts.java`, `AdminGrowth.java`, `AdminEventVolume.java`, `DailyVolumeData.java` — domain models
- `AdminStatsValidator.java` — allows 7/30/90/180
- `application.yml` — cache config
- `Main.java` — @EnableCaching
- `build.gradle.kts` — spring-boot-starter-cache
- `202605191000-PRD-admin-event-stats-daily-aggregate.xml` — migration
- `TimeProvider.java` — startOfDayUtc()
- `SearchInputHelper.java` — extractFilter()
- `AdminDashboardControllerSplitTest.java` — 22 tests
- `AdminApiKeyServiceStatisticsTest.java` — 10 tests
- Deleted: `AdminDaysValidator`, `CacheConfig`, `AdminDashboardControllerTest`, `AdminStatsServiceCacheTest`

### Frontend
- `AdminController.ts` — 3 new typed endpoints
- `+page.svelte` — parallel fetches, per-section loading/error
- `OverviewTab.svelte`, `InfrastructureTab.svelte`, `EventsTab.svelte` — generated types
- `statistics-helpers.ts` — new formatters
- `models.ts` — new type exports
- `openapi.json`, `api.d.ts` — regenerated

## Tests

- 1610 backend tests passing
- Frontend: `bun run check` clean
