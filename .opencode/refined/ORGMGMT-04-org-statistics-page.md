---
epic: "ORGMGMT"
title: "Organization Statistics Page"
estimate: L
status: ready
created: 2026-05-22
depends_on: ["ORGMGMT-02-sidebar-restructure-route-renames"]
labels: [frontend, backend]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As an** organization admin/owner\
**I want** a statistics page showing event volume, error rates, and trends\
**So that** I can understand my organization's health and usage patterns\

## 2. Business Context & Value
Org admins currently have no visibility into usage trends. A statistics page with time-range filtering gives them data to make decisions about channels, alerting thresholds, and capacity.

## 3. Acceptance Criteria
* [ ] **Event volume chart**
    * Given an org admin viewing statistics
    * When selecting a time range
    * Then a line/bar chart shows daily event volume for that period
* [ ] **Error rate chart**
    * Given an org admin viewing statistics
    * When selecting a time range
    * Then a chart shows error rate (% of events with ERROR/CRITICAL severity) over time
* [ ] **Time range filter with quick buttons**
    * Given the statistics page
    * When the user clicks a quick button (7d, 30d, 90d, 180d) or selects custom range
    * Then charts update to show data for that period
    * And the selected range persists during the session
* [ ] **Summary stat cards**
    * Given the statistics page loads
    * Then top-level cards show: total events (period), average daily volume, current error rate, active channels count
* [ ] **Access restricted to admin/owner**
    * Given a regular org member
    * When attempting to access `/organizations/[orgId]/statistics`
    * Then access is denied (403 or hidden from nav)
* [ ] **Caching layer**
    * Given statistics are requested
    * When the same time range is requested within cache TTL
    * Then cached results are returned without re-querying
* [ ] **Custom date range**
    * Given the user selects "Custom" range
    * When they pick start and end dates
    * Then charts update to that exact range

## 4. Technical Requirements
* **API Changes**: New endpoint `GET /v1/organizations/{orgId}/statistics?days=30` (or `startDate`/`endDate` for custom) returning:
  - `eventVolume`: array of {date, count}
  - `errorRate`: array of {date, rate}
  - `summary`: {totalEvents, avgDailyVolume, currentErrorRate, activeChannels}
* **Database**: N/A — queries existing `event_timeline_hourly` or events table with aggregation
* **Security**: Requires org ADMIN or OWNER role. Enforce via existing org role check pattern.
* **Performance**: `@Cacheable` with key = `orgId + days`. Use same cache pattern as `AdminStatsService`. Consider Caffeine with 5-min TTL.

## 5. Design & UI/UX
* Layout: stat cards at top, charts below (full-width)
* Time range filter: toolbar at top-right, same pattern as monitoring `ConfigurePopover` but with day-scale quick buttons (7d, 30d, 90d, 180d) instead of hour-scale
* Charts: use existing charting library (if any) or lightweight option (Chart.js / recharts equivalent for Svelte)
* Responsive: charts stack vertically on mobile

## 6. Implementation Notes
* **Frontend route**: `client/src/routes/(authenticated)/organizations/[orgId]/statistics/` (created as placeholder in ORGMGMT-02)
* **Time range component**: Adapt pattern from `client/src/lib/components/monitor/ConfigurePopover.svelte` but with different quick ranges (days not hours)
* **Backend pattern**: Follow `AdminStatsService` — `@Cacheable` annotations, `?days` param
* **Existing data**: `event_timeline_hourly` table likely has pre-aggregated data for efficient queries
* **Role check**: Use existing org permission pattern (same as Settings page access)

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| New: `client/src/routes/(authenticated)/organizations/[orgId]/statistics/+page.svelte` | Statistics page UI |
| New: `client/src/routes/(authenticated)/organizations/[orgId]/statistics/+page.ts` | Data loading |
| New: `server/.../organization/controller/OrgStatisticsController.java` | Statistics endpoint |
| New: `server/.../organization/service/OrgStatisticsService.java` | Query + cache logic |
| New: `client/src/lib/api/organization/OrgStatisticsController.ts` | Frontend API client |
| New: `client/src/lib/components/statistics/TimeRangeToolbar.svelte` | Reusable time range filter (shared with ORGMGMT-05) |
