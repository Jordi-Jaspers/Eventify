# Admin Stats Aggregate Optimization

**Completed:** 2026-05-29
**Epic:** PERF
**Source:** ad-hoc request (performance bug report)

## Summary

Replaced slow correlated subqueries against raw `event` hypertable with pre-computed continuous aggregates for `/api/v1/admin/stats/growth` and `/api/v1/admin/stats/event-volume` endpoints.

## Plan Approved by the user:

- Range >= 1 day → query `admin_event_stats_daily` continuous aggregate
- Range < 24h (single day) → query `event_timeline_hourly` continuous aggregate
- New `AdminEventStatsDailyRepository` for daily aggregate queries
- Rewrite `AdminStatsService` routing logic
- Update existing service tests to mock new aggregate repositories
- Same API response shape, no frontend changes

## Implementation

### Backend

- `AdminStatsService` — rewrote `getEventVolume()` with single-day vs multi-day routing, replaced `eventRepository.findDailyEventCounts()` with aggregate queries in `calculateGrowthData()`
- `AdminEventStatsDailyRepository` — new repository with native queries against `admin_event_stats_daily`
- `DailyEventStats` — new projection interface (day + totalEvents)
- PMD `CouplingBetweenObjects` threshold raised from 20 to 25

### Deviations from Plan

- Did not remove `findDailyEventCounts`/`countByTimestampAfter` from EventRepository (may be used elsewhere)
- Did not add timestamp-only index migration (deferred to separate task)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent | Analyze endpoints, queries, aggregates, tests | Complete |
| backend-testing-agent | Update service unit tests for new repos | Complete |
| backend-agent | Implement aggregate routing in service | Complete |

## Files Modified

- `server/src/main/java/.../admin/stats/service/AdminStatsService.java` — routing logic rewrite
- `server/src/main/java/.../admin/stats/repository/AdminEventStatsDailyRepository.java` — new
- `server/src/main/java/.../admin/stats/model/projection/DailyEventStats.java` — new
- `server/src/test/java/.../admin/stats/service/AdminStatsServiceDateRangeTest.java` — updated mocks
- `server/src/quality/config/pmd/pmd.xml` — threshold adjustment

## Tests

- 9 tests updated/added, all passing
- 1739 total tests, 1 pre-existing unrelated failure (NotificationDispatchServiceTest)
