---
epic: "ORGMGMT"
title: "Admin Statistics Time Range Filter Upgrade"
estimate: S
status: ready
created: 2026-05-22
depends_on: ["ORGMGMT-04-org-statistics-page"]
labels: [frontend, backend]
priority: P3
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform admin\
**I want** time range filtering on the admin statistics page with quick buttons and custom range\
**So that** I can analyze platform trends over specific periods\

## 2. Business Context & Value
Admin statistics currently use a hardcoded `?days=30` param. Adding the same time range filter as the org statistics page gives admins flexible analysis. Reuses the `TimeRangeToolbar` component from ORGMGMT-04.

## 3. Acceptance Criteria
* [ ] **Quick range buttons on admin stats**
    * Given an admin on `/admin/statistics`
    * When clicking 7d, 30d, 90d, or 180d
    * Then all charts/stats refresh for that period
* [ ] **Custom date range**
    * Given an admin selects "Custom"
    * When picking start and end dates
    * Then stats refresh for that exact range
* [ ] **Reuses shared component**
    * Given the `TimeRangeToolbar` component from ORGMGMT-04
    * When integrated into admin statistics
    * Then it behaves identically (same UX, same quick buttons)
* [ ] **Backend supports date range params**
    * Given existing admin stats endpoints accept `?days=N`
    * When custom range is selected
    * Then frontend sends `?startDate=X&endDate=Y` (or equivalent)
    * And backend handles both param styles

## 4. Technical Requirements
* **API Changes**: Extend existing admin stats endpoints to accept `startDate`/`endDate` query params as alternative to `?days`. Backward compatible — `?days` still works.
* **Database**: N/A
* **Security**: N/A — same `VIEW_PLATFORM_STATS` authority
* **Performance**: Cache keys must include the date range. Existing `@Cacheable` keys use `#days` — extend to include date params.

## 5. Design & UI/UX
* Add `TimeRangeToolbar` component to top of admin statistics page
* Same position/styling as org statistics page
* Default selection: 30d (matches current behavior)

## 6. Implementation Notes
* **Reuse**: `TimeRangeToolbar.svelte` from ORGMGMT-04
* **Backend**: `AdminDashboardController` endpoints already accept `?days` — add optional `startDate`/`endDate` params
* **Cache**: Update `@Cacheable` keys in `AdminStatsService` to include date range
* **Frontend**: `client/src/routes/(authenticated)/admin/statistics/+page.svelte` — add toolbar, wire to API calls

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `client/src/routes/(authenticated)/admin/statistics/+page.svelte` | Add TimeRangeToolbar, wire to API |
| `server/.../admin/controller/AdminDashboardController.java` | Add startDate/endDate params |
| `server/.../admin/service/AdminStatsService.java` | Update cache keys, support date range |
| `client/src/lib/components/statistics/TimeRangeToolbar.svelte` | Already created in ORGMGMT-04 |
