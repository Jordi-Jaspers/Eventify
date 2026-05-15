---
epic: "ADMIN"
title: "Admin Dashboard — Events Analytics Tab"
estimate: M
status: ready
created: 2025-05-15
depends_on: ["ADMIN-01-admin-dashboard-redesign"]
labels: [backend, frontend, admin]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform administrator\
**I want** an Events tab on the admin dashboard showing ingestion rates, top channels, and severity distribution\
**So that** I can understand event volume patterns and identify high-traffic or problematic channels\

## 2. Business Context & Value
Event ingestion is the core value driver of the platform. Admins need visibility into how much data is flowing through the system, which channels generate the most traffic, and what severity distribution looks like — all essential for capacity planning and identifying anomalies.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Daily ingestion rate chart
    * Given the admin navigates to the Events tab
    * When the tab loads
    * Then they see an area/bar chart showing daily event count for the selected time range (7d/30d/90d)
* [ ] **Scenario 2**: Top channels by volume
    * Given the admin views the Events tab
    * When the tab loads
    * Then they see a table of top 10 channels by event count in the selected period, showing: channel name, owner (user or org), event count, percentage of total
* [ ] **Scenario 3**: Severity breakdown
    * Given the admin views the Events tab
    * When the tab loads
    * Then they see a breakdown (donut chart or stat cards) of events by severity (CRITICAL/WARNING/OK) for the selected period
* [ ] **Scenario 4**: Quota usage overview
    * Given the admin views the Events tab
    * When the tab loads
    * Then they see: users near quota limit (>80% used), users who hit quota this month, average quota utilization percentage
* [ ] **Scenario 5**: Time range respects global selector
    * Given the admin changes the global time range on the dashboard
    * When they are on the Events tab
    * Then all event metrics update to reflect the new range

## 4. Technical Requirements
* **API Changes**:
  - `GET /api/v1/admin/stats/events?days=30` — new endpoint returning:
    ```json
    {
      "dailyIngestion": [{ "date": "2025-05-14", "count": 1523 }],
      "topChannels": [{ "channelId": 1, "channelName": "...", "ownerName": "...", "ownerType": "USER|ORGANIZATION", "eventCount": 500, "percentage": 12.5 }],
      "severityBreakdown": { "CRITICAL": 120, "WARNING": 450, "OK": 3200 },
      "quotaStats": { "usersNearLimit": 3, "usersAtLimit": 1, "averageUtilization": 23.5 }
    }
    ```
* **Database**: N/A — no schema changes. Queries against `event` table (with timestamp filter) and `user_event_quota` table.
* **Security**: Requires `VIEW_PLATFORM_STATS` permission.
* **Performance**: Daily ingestion query should use `event_timeline_hourly` continuous aggregate (already has `event_count` per hour per channel — sum by day). Top channels query needs index on `(channel_id, timestamp)` — already exists via TimescaleDB hypertable. Quota stats query is simple aggregation on small `user_event_quota` table.

## 5. Design & UI/UX
- **Events tab** (third tab after Overview | Infrastructure):
  - Daily ingestion chart: AreaChart (layerchart) with single series, filled area, tooltip showing exact count per day
  - Top channels: table with rank, channel name (link to channel), owner, count, bar indicator for percentage
  - Severity breakdown: colored donut chart or 3 stat cards (CRITICAL=red, WARNING=amber, OK=green) with counts + percentages
  - Quota section: small alert-style cards showing users near/at limit

## 6. Implementation Notes
- **Backend files to create/modify:**
  - New `AdminEventStatsService.java` — daily ingestion via `event_timeline_hourly` aggregate (SUM event_count GROUP BY date), top channels, severity counts, quota stats
  - New `AdminEventStatsResponse.java` — response DTO
  - `AdminDashboardController.java` — add `/stats/events` endpoint
- **Queries:**
  - Daily ingestion: `SELECT bucket::date, SUM(event_count) FROM event_timeline_hourly WHERE bucket >= :start GROUP BY bucket::date ORDER BY bucket::date`
  - Top channels: `SELECT channel_id, COUNT(*) FROM event WHERE timestamp >= :start GROUP BY channel_id ORDER BY COUNT(*) DESC LIMIT 10` (join channel for name)
  - Severity: `SELECT severity, COUNT(*) FROM event WHERE timestamp >= :start GROUP BY severity`
  - Quota: `SELECT COUNT(*) FILTER (WHERE event_count > 800) as near_limit, COUNT(*) FILTER (WHERE event_count >= 1000) as at_limit, AVG(event_count::float / 1000 * 100) as avg_util FROM user_event_quota`
- **Frontend:**
  - Add "Events" tab to the redesigned statistics page
  - New API client function `getAdminEventStats(days)`
  - Reuse `layerchart` AreaChart for ingestion chart

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../admin/controller/AdminDashboardController.java` | Add `/stats/events` endpoint |
| `server/.../admin/service/` | New `AdminEventStatsService.java` |
| `server/.../admin/model/response/` | New `AdminEventStatsResponse.java` + sub-DTOs |
| `client/src/routes/(authenticated)/admin/statistics/+page.svelte` | Add Events tab content |
| `client/src/lib/api/admin/AdminDashboardController.ts` | Add `getAdminEventStats()` |
