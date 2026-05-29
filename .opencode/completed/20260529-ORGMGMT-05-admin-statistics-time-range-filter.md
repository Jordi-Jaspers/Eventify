# Admin Statistics Time Range Filter

**Completed:** 2026-05-29
**Epic:** ORGMGMT
**Source:** .opencode/refined/ORGMGMT-05-admin-statistics-time-range-filter.md

## Summary

Upgraded admin statistics page with flexible time range filtering (quick ranges + custom date picker) and switched affected endpoints from GET+query params to POST+JSON body.

## Plan Approved by the user:

### Requirements Summary

- Quick range buttons (7d/30d/90d/180d) on admin stats
- Custom date range via TimeRangePopover
- Reuses shared components (TimeRangePopover, computeDaysFromRange)
- Backend supports both days and explicit startDate/endDate (mutual exclusivity)

### Technical Approach

- Backend: POST endpoints with AdminStatsRequest body (days XOR startDate+endDate)
- Frontend: Replace PillToggle days selector with TimeRangePopover, minimal border-bottom tab nav
- Caching: Only days-based calls cached; custom ranges uncached

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | backend-testing-agent | Create date range test suite |
| 2.2 | backend-agent | Implement POST+body, validator, service overloads |
| 2.3 | backend-testing-agent | Update old tests (SplitTest, EventStatsControllerTest) |
| 2.4 | backend-optimizer-agent | Remove dead code, fix caching, reduce duplication |
| 3.1 | frontend-agent | Implement TimeRangePopover integration + POST API |
| 3.2 | frontend-optimizer-agent | Simplify patterns, extract helpers |

## Implementation

### Backend

- Endpoints: POST /v1/admin/stats/growth, /event-volume, /events (body: AdminStatsRequest)
- GET unchanged: /counts, /storage
- Validator: mutual exclusivity (days XOR dates), days 1-365, start<=end, end<=today
- Caching: @Cacheable on days-based overloads only; custom ranges uncached
- Routing logic in controller (avoids Spring proxy self-invocation)

### Frontend

- AdminController.ts: switched to POST+body
- +page.svelte: TimeRangePopover with custom date support, minimal tab nav
- OverviewTab.svelte: graph toggle left-aligned, removed title

### Deviations from Plan

- Changed from GET+query params to POST+body per user feedback
- Removed dead getAdminStats() method and AdminStatsResponse class

## Files Modified

- `server/.../controller/AdminDashboardController.java` — POST endpoints with request body routing
- `server/.../service/AdminStatsService.java` — date-range overloads, delegation pattern
- `server/.../service/AdminEventStatsService.java` — date-range overload
- `server/.../model/request/AdminStatsRequest.java` — new request record
- `server/.../model/validator/AdminStatsValidator.java` — mutual exclusivity validation
- `server/.../model/response/AdminStatsResponse.java` — deleted (dead code)
- `server/.../controller/AdminDashboardControllerDateRangeTest.java` — new (21 tests)
- `server/.../validator/AdminStatsValidatorDateRangeTest.java` — new (13 tests)
- `server/.../service/AdminStatsServiceDateRangeTest.java` — new (10 tests)
- `server/.../controller/AdminDashboardControllerSplitTest.java` — updated to POST
- `server/.../controller/AdminEventStatsControllerTest.java` — updated to POST
- `server/.../service/AdminStatsServiceTest.java` — deleted (dead code)
- `client/src/lib/api/admin/AdminController.ts` — POST+body
- `client/src/lib/api/models.ts` — added AdminStatsRequest
- `client/src/routes/(authenticated)/admin/statistics/+page.svelte` — TimeRangePopover, minimal tab nav
- `client/src/lib/components/admin/statistics/OverviewTab.svelte` — graph toggle left-aligned

## Tests

- 44+ backend tests written/updated, all passing
