# Org Statistics POST+Body + Shared Validator Extraction

**Completed:** 2026-05-29
**Epic:** USER_CHANGE_REQUEST
**Source:** ad-hoc request

## Summary

Switched org statistics endpoints to POST+body (matching admin stats pattern), extracted shared StatsRequest/StatsRequestValidator to common package, added custom date range support to org stats.

## Plan Approved by the user:

### Requirements Summary

- Switch org stats timeline/summary from GET+query param to POST+body
- Extract validator to shared common package (StatsRequestValidator)
- Add custom date range support (startDate/endDate) to org stats
- Frontend sends dates directly for custom ranges instead of converting to days

### Technical Approach

- Backend: shared StatsRequest + StatsRequestValidator in common/model/, update both admin and org controllers
- Frontend: OrganizationStatisticsController.ts → POST+body, page sends dates directly

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | backend-testing-agent | Update tests (GET→POST, shared imports) + new date-range tests |
| 2.2 | backend-agent | Extract shared validator/request, update controllers/services |
| 2.4 | backend-optimizer-agent | Delete dead OrgStatsValidator + cleanup |
| 3.1 | frontend-agent | Update API controller to POST+body with dates |

## Implementation

### Backend

- Created `common/model/request/StatsRequest.java` — shared request class
- Created `common/model/validator/StatsRequestValidator.java` — shared validation
- Updated `OrgStatisticsController` — timeline/summary → POST+@RequestBody
- Updated `OrgStatsService` — added date-range overloads (uncached)
- Updated `AdminDashboardController` — uses shared StatsRequest/StatsRequestValidator
- Deleted `AdminStatsRequest`, `AdminStatsValidator`, `OrgStatsValidator`, `OrgStatsValidatorTest`

### Frontend

- `OrganizationStatisticsController.ts` → POST+body with OrgStatsRequest
- `+page.svelte` → buildStatsRequest() sends dates directly for custom ranges
- Added OrgStatsRequest type to api.d.ts + models.ts

### Deviations from Plan

- OrgStatsService days→dates delegation not applied (different semantics: rolling window vs day boundary)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| backend-testing-agent | Update tests + new date-range tests | Complete |
| backend-agent | Shared extraction + POST migration | Complete |
| backend-optimizer-agent | Dead code removal | Complete |
| frontend-agent | API + page update | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/common/model/request/StatsRequest.java` (new)
- `server/src/main/java/io/github/eventify/common/model/validator/StatsRequestValidator.java` (new)
- `server/src/main/java/io/github/eventify/api/admin/stats/controller/AdminDashboardController.java`
- `server/src/main/java/io/github/eventify/api/organization/controller/OrgStatisticsController.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrgStatsService.java`
- `server/src/main/java/io/github/eventify/api/admin/stats/model/request/AdminStatsRequest.java` (deleted)
- `server/src/main/java/io/github/eventify/api/admin/stats/model/validator/AdminStatsValidator.java` (deleted)
- `server/src/main/java/io/github/eventify/api/organization/model/validator/OrgStatsValidator.java` (deleted)
- `server/src/test/java/io/github/eventify/api/organization/model/validator/OrgStatsValidatorTest.java` (deleted)
- `server/src/test/java/io/github/eventify/api/organization/controller/OrgStatisticsControllerTest.java`
- `server/src/test/java/io/github/eventify/api/admin/stats/model/validator/AdminStatsValidatorDateRangeTest.java`
- `server/src/test/java/io/github/eventify/api/admin/stats/controller/AdminDashboardControllerDateRangeTest.java`
- `server/src/test/java/io/github/eventify/api/admin/stats/controller/AdminDashboardControllerSplitTest.java`
- `server/src/test/java/io/github/eventify/api/admin/stats/controller/AdminEventStatsControllerTest.java`
- `client/src/lib/types/api.d.ts`
- `client/src/lib/api/models.ts`
- `client/src/lib/api/organization/OrganizationStatisticsController.ts`
- `client/src/routes/(authenticated)/organizations/[orgId]/statistics/+page.svelte`

## Tests

- 14 new date-range tests for org stats (timeline + summary)
- All existing admin + org stats tests updated and passing
