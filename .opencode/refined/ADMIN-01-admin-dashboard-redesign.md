---
epic: "ADMIN"
title: "Admin Dashboard Redesign — Overview & Infrastructure"
estimate: L
status: ready
created: 2025-05-15
depends_on: []
labels: [backend, frontend, admin]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform administrator\
**I want** a redesigned statistics dashboard with tabbed navigation, time range controls, and infrastructure metrics\
**So that** I can monitor platform health and growth at a glance without navigating multiple pages\

## 2. Business Context & Value
The current admin statistics page is minimal (3 stat cards + 1 growth chart + quick action buttons). Admins need a comprehensive dashboard showing platform health across multiple dimensions. Quick actions don't belong on a statistics page. This redesign introduces tabs, configurable time ranges, and infrastructure metrics (API keys, channels, storage) to make the dashboard genuinely useful for day-to-day operations.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Global time range selector
    * Given the admin is on the statistics page
    * When they select a time range (7d / 30d / 90d)
    * Then all charts and time-based metrics on the page update to reflect that range
* [ ] **Scenario 2**: Overview tab — stat cards
    * Given the admin views the Overview tab
    * When the page loads
    * Then they see stat cards for: Total Users (with active %), Total Organizations, Total Channels (with active/paused/stale breakdown), Total Events (ingested in selected period)
* [ ] **Scenario 3**: Overview tab — growth chart
    * Given the admin views the Overview tab
    * When the page loads
    * Then they see the existing org/user growth AreaChart, now respecting the global time range selector
* [ ] **Scenario 4**: Overview tab — best growth day records
    * Given the admin views the Overview tab
    * When the page loads
    * Then they see a "Records" section showing: best day for new users (date + count), best day for new orgs (date + count), best day for events ingested (date + count)
* [ ] **Scenario 5**: Infrastructure tab — API key stats
    * Given the admin navigates to the Infrastructure tab
    * When the tab loads
    * Then they see: total keys (user vs org split), created this week/month, revoked this month, expiring in 30 days, never-used keys, top 5 keys by usage
* [ ] **Scenario 6**: Infrastructure tab — channel health
    * Given the admin navigates to the Infrastructure tab
    * When the tab loads
    * Then they see: total channels, active count, paused count, stale count, pending deletion count
* [ ] **Scenario 7**: Infrastructure tab — storage snapshot
    * Given the admin navigates to the Infrastructure tab
    * When the tab loads
    * Then they see current DB table sizes for: event, notification, channel, organization, user (formatted as human-readable sizes like "1.2 GB")
* [ ] **Scenario 8**: Quick actions removed
    * Given the admin views any tab
    * Then there are no "Quick Actions" buttons on the page
* [ ] **Scenario 9**: Tab navigation
    * Given the admin is on the statistics page
    * When they click between Overview / Infrastructure tabs
    * Then the tab content switches without page reload, and the selected tab is reflected in the URL (query param or hash)

## 4. Technical Requirements
* **API Changes**:
  - `GET /api/v1/admin/stats` — extend `AdminStatsResponse` with: `totalChannels`, `activeChannels`, `pausedChannels`, `staleChannels`, `pendingDeletionChannels`, `totalEventsInPeriod`, `bestGrowthDayUsers` (date + count), `bestGrowthDayOrganizations` (date + count), `bestGrowthDayEvents` (date + count)
  - Add query param `?days=7|30|90` (default 30) to control growth data range
  - `GET /api/v1/admin/stats/storage` — new endpoint returning table sizes via `pg_total_relation_size` for key tables. Response: `List<TableSizeEntry>` with `tableName` + `sizeBytes` + `sizeFormatted`
  - Existing `GET /api/v1/admin/api-keys/stats` already returns everything needed for Infrastructure tab
* **Database**: N/A — no schema changes. New repository count queries only.
* **Security**: All endpoints require `VIEW_PLATFORM_STATS` permission. Storage endpoint uses native query — ensure no SQL injection (hardcoded table names only).
* **Performance**: Storage query hits `pg_total_relation_size` — fast (catalog lookup). Channel counts are simple `COUNT` queries. Cache stats response for 60s if needed (optional optimization).

## 5. Design & UI/UX
- **Layout**: Page header "Platform Statistics" + global time range toggle (7d/30d/90d pill buttons, right-aligned) + tab bar (Overview | Infrastructure)
- **Overview tab**:
  - Row of 4 stat cards (Users, Orgs, Channels, Events) with sparkline or delta badge
  - Growth chart (existing AreaChart, now time-range-aware)
  - "Records" section: 3 small highlight cards showing best growth day per metric
- **Infrastructure tab**:
  - API Keys section: stat cards row + "Top Keys" mini-table
  - Channel Health section: donut/pie chart or stat cards showing status distribution
  - Storage section: horizontal bar chart or table showing table sizes
- **No quick actions anywhere**
- **Tab state in URL**: `?tab=overview` / `?tab=infrastructure` for shareability

## 6. Implementation Notes
- **Backend files to modify:**
  - `AdminStatsService.java` — add channel count queries, event count for period, best growth day calculations
  - `AdminStatsResponse.java` — extend with new fields
  - `AdminDashboardController.java` — add `days` query param, add `/stats/storage` endpoint
  - `ChannelRepository.java` — add `countByStatus(ChannelStatus)` or similar count methods
  - `EventRepository.java` — add `countByTimestampAfter(OffsetDateTime)` for total events in period
- **Frontend files to modify:**
  - `client/src/routes/(authenticated)/admin/statistics/+page.svelte` — full rewrite with tab structure
  - `client/src/lib/api/admin/AdminDashboardController.ts` — add `days` param, add storage endpoint call
- **Patterns to follow:**
  - Existing `ApiKeyStatsResponse` pattern for stats DTOs
  - `layerchart` AreaChart for growth chart (already used)
  - shadcn `Tabs` component for tab navigation
  - Existing stat card pattern (inline in current page — extract to reusable component)
- **Storage query pattern:**
  ```sql
  SELECT relname AS table_name, pg_total_relation_size(c.oid) AS size_bytes
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public' AND relname IN ('event','notification','channel','organization','user')
  ```

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../admin/service/AdminStatsService.java` | Add channel counts, event count, best growth day logic, accept days param |
| `server/.../admin/model/response/AdminStatsResponse.java` | Extend with new fields |
| `server/.../admin/controller/AdminDashboardController.java` | Add days param + storage endpoint |
| `server/.../channel/repository/ChannelRepository.java` | Add count-by-status methods |
| `server/.../event/repository/EventRepository.java` | Add countByTimestampAfter |
| `client/src/routes/(authenticated)/admin/statistics/+page.svelte` | Full redesign with tabs |
| `client/src/lib/api/admin/AdminDashboardController.ts` | Add days param + storage API call |
