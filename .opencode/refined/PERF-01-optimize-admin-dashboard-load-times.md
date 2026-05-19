---
epic: "PERF"
title: "Optimize Admin Dashboard Load Times"
estimate: L
status: ready
created: 2026-05-19
depends_on: [ ]
labels: [ backend, frontend, performance, timescaledb ]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As an** admin\
**I want** the admin dashboard to load quickly\
**So that** I can monitor platform health without waiting 20+ seconds\

## 2. Business Context & Value
The admin statistics pages (`/admin/statistics` with Overview/Infrastructure/Events tabs) take ~20s on TST with production-scale data. Root causes: `AdminStatsService.getAdminStats()` fires 9+ sequential count queries, `AdminApiKeyService.getStatistics()` fires 8 sequential queries, event count queries scan the raw `event` hypertable (millions of rows/day), and the frontend loads all data in one blocking call per tab.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Independent component loading
    * Given an admin opens the Overview tab
    * When the page loads
    * Then stat cards, growth data, and graphs load as independent parallel requests with separate loading states (skeletons)
* [ ] **Scenario 2**: Event stats use continuous aggregate
    * Given an admin opens the Events tab
    * When event volume/severity stats are requested
    * Then they are served from a TimescaleDB continuous aggregate (not raw hypertable scan)
* [ ] **Scenario 3**: Consolidated API key stats
    * Given an admin opens the Infrastructure tab
    * When API key stats load
    * Then the 8 count queries are consolidated into fewer native SQL queries
* [ ] **Scenario 4**: Cache hit on repeated access
    * Given any admin stats endpoint is called twice within 60s
    * When the second request arrives
    * Then it is served from cache (`@Cacheable`) without hitting the database
* [ ] **Scenario 5**: Acceptable load time
    * Given the TST environment with production-scale data
    * When any admin stats page loads
    * Then total load time is under 3 seconds
* [ ] **Scenario 6**: Acceptable data freshness
    * Given the continuous aggregate refresh policy
    * When stats are displayed
    * Then data may lag by up to 5 minutes (acceptable for admin dashboard)

## 4. Technical Requirements
* **API Changes**:
    - Split `GET /admin/stats` → `GET /admin/stats/counts`, `GET /admin/stats/growth?days=N`, `GET /admin/stats/event-volume?days=N`
    - `GET /admin/api-keys/stats` → consolidate into single native query
    - `GET /admin/stats/events` → rewrite to use continuous aggregate
    - `GET /admin/stats/storage` → add `@Cacheable`
* **Database**: New TimescaleDB continuous aggregate for admin-level event stats (total event count per day, severity breakdown per day). Liquibase migration required.
* **Security**: No changes — existing `@PreAuthorize` admin checks remain on all endpoints
* **Performance**: Target <3s total page load on TST. `@Cacheable` with 60s TTL on all admin stats service methods. Continuous aggregate refresh policy every 5 minutes.

## 5. Design & UI/UX
- Each tab fires multiple parallel API calls (one per UI component: card group, chart, table)
- Each UI section has its own loading skeleton
- Errors per section don't block other sections from rendering
- No visual design changes — same layout, just progressive loading

## 6. Implementation Notes

### Backend
- **Continuous aggregate migration**: Create aggregate over `event_timeline_hourly` or raw `event` table for admin stats (total count per day, severity breakdown per day)
- **Consolidate count queries**: Replace sequential `repository.count()` calls with single native SQL using `SELECT COUNT(*) FILTER (WHERE ...)` pattern
- **`@Cacheable` on service methods**: Add to `AdminStatsService`, `AdminEventStatsService`, `AdminApiKeyService` stats methods with 60s TTL
- **Split controller endpoints**: Break `getAdminStats()` into `getCounts()`, `getGrowth()`, `getEventVolume()`

### Frontend
- **Split load functions**: Each tab's data loading becomes multiple parallel fetches
- **Independent loading states**: Each card/chart/table section shows skeleton independently
- **Error isolation**: One failed fetch doesn't prevent other sections from rendering

### Key files:
- `server/src/main/java/io/github/eventify/api/admin/controller/AdminDashboardController.java`
- `server/src/main/java/io/github/eventify/api/admin/service/AdminStatsService.java`
- `server/src/main/java/io/github/eventify/api/admin/service/AdminEventStatsService.java`
- `server/src/main/java/io/github/eventify/api/admin/service/AdminApiKeyService.java`
- `client/src/routes/(authenticated)/admin/statistics/`
- `server/src/main/resources/db/` (new migration)

### Cache strategy:
- `@Cacheable` with 60s TTL on each stats service method
- No explicit eviction needed — TTL-based expiry sufficient
- Acceptable trade-off: admin performs action then sees stale count for up to 60s

## 7. Out of Scope
- Admin sub-package restructuring (separate backlog item)
- Real-time WebSocket updates for stats
- Historical stats archiving
- Audit log page optimization (already paginated, no stats bottleneck)
