# Dashboard Stats Cards - Full Stack Implementation

**Epic**: Dashboard Enhancements
**Status**: Ready for Dev
**Estimate**: L
**Created Date**: 2026-02-11
**Depends On**: DASHBOARD-01-statcard-shared-component.md

## 1. User Story
**As a** user viewing my dashboard
**I want** to see stat cards showing Events Today, Active Channels, Error Rate, and Last Event
**So that** I can quickly understand the health and activity of my monitoring setup at a glance

## 2. Business Context & Value
Visual stats cards transform the dashboard from a basic placeholder into a valuable monitoring hub. Users get immediate insight into their system health without navigating to individual channels. The error rate card with color thresholds provides at-a-glance health status.

## 3. Acceptance Criteria

### Backend
* [ ] **Scenario 1**: User requests personal dashboard stats
    * Given I am an authenticated user
    * When I call `GET /api/v1/dashboard/stats`
    * Then I receive stats for my personal channels only (org channels excluded)
    * And the response includes: eventsToday, activeChannels, errorRate, lastEventAt

* [ ] **Scenario 2**: User requests organization dashboard stats
    * Given I am an authenticated member of organization 123
    * When I call `GET /api/v1/organizations/123/dashboard/stats`
    * Then I receive stats for that organization's channels only
    * And the response includes: eventsToday, activeChannels, errorRate, lastEventAt

* [ ] **Scenario 3**: Error rate calculation
    * Given I have 10 active channels
    * And 2 channels have their most recent event with severity = CRITICAL
    * When I request dashboard stats
    * Then errorRate = 20.0 (percentage)

* [ ] **Scenario 4**: Empty state handling
    * Given I have no channels
    * When I request dashboard stats
    * Then eventsToday = 0, activeChannels = 0, errorRate = 0.0, lastEventAt = null

* [ ] **Scenario 5**: Caching
    * Given stats were fetched 10 seconds ago
    * When I request dashboard stats again
    * Then the cached response is returned (within TTL)

* [ ] **Scenario 6**: Organization access control
    * Given I am NOT a member of organization 456
    * When I call `GET /api/v1/organizations/456/dashboard/stats`
    * Then I receive 403 Forbidden

### Frontend
* [ ] **Scenario 7**: Stats cards display on User Dashboard
    * Given I am on the User Dashboard (`/dashboard`)
    * When the page loads
    * Then I see 4 stat cards above the organizations section
    * And they show: Events Today, Active Channels, Error Rate, Last Event

* [ ] **Scenario 8**: Stats cards display on Organization Dashboard
    * Given I am on an Organization Dashboard (`/organizations/123/dashboard`)
    * When the page loads
    * Then I see 4 stat cards above the organization details
    * And they show stats for that organization's channels only

* [ ] **Scenario 9**: Error Rate color thresholds
    * Given the Error Rate stat card is displayed
    * When error rate < 1%, Then the card uses `green` variant
    * When error rate >= 1% and < 5%, Then the card uses `yellow` variant
    * When error rate >= 5%, Then the card uses `red` variant

* [ ] **Scenario 10**: Last Event relative time
    * Given the last event was 5 minutes ago
    * When I view the Last Event card
    * Then it shows "5 minutes ago" (relative time format)

* [ ] **Scenario 11**: Loading state
    * Given the dashboard is fetching stats
    * When the API call is in progress
    * Then all 4 stat cards show loading skeleton state

* [ ] **Scenario 12**: Responsive layout
    * Given I am viewing the dashboard
    * When on desktop (>=1024px), Then cards display in 4 columns
    * When on tablet (>=768px, <1024px), Then cards display in 2x2 grid
    * When on mobile (<768px), Then cards stack vertically

* [ ] **Scenario 13**: Error handling
    * Given the stats API fails
    * When I view the dashboard
    * Then an error message is shown with a retry option

## 4. Technical Requirements

### Backend

* **API Changes**:
  - `GET /api/v1/dashboard/stats` - Personal dashboard stats (requires authentication)
  - `GET /api/v1/organizations/{orgId}/dashboard/stats` - Org dashboard stats (requires org membership)
  
* **Response DTO** (`DashboardStatsResponse`):
  ```java
  public record DashboardStatsResponse(
      long eventsToday,           // Count of events in last 24 hours
      int activeChannels,         // Count of channels with status = ACTIVE
      double errorRate,           // Percentage (0.0 - 100.0)
      OffsetDateTime lastEventAt  // Timestamp of most recent event, nullable
  ) {}
  ```

* **Database Queries Needed**:
  - Count events where `timestamp >= NOW() - 24 hours` for user's/org's channels
  - Count channels where `status = ACTIVE` for user/org
  - For each active channel, find the most recent event's severity
  - Count how many have `severity = CRITICAL`
  - Find the MAX timestamp across all events

* **Caching**: 
  - Use `@Cacheable` with ~30 second TTL
  - Cache key should include user ID (personal) or org ID (org)

* **Security**:
  - Personal endpoint: `@PreAuthorize("isAuthenticated()")`
  - Org endpoint: `@PreAuthorize("@organizationSecurity.isMember(#orgId, principal)")`

* **Performance**:
  - All stats should be fetched in minimal DB round-trips
  - Consider a single native query that returns all metrics
  - Response time target: < 200ms

### Frontend

* **Frontend Service**: Create `$lib/services/dashboard.service.ts`
  ```typescript
  export async function getDashboardStats(): Promise<DashboardStatsResponse> { ... }
  export async function getOrgDashboardStats(orgId: number): Promise<DashboardStatsResponse> { ... }
  ```

* **Responsive Grid Classes**:
  ```html
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
  ```

* **Icons** (from lucide-svelte):
  - Events Today: `Activity` or `Zap`
  - Active Channels: `Radio` or `Layers`
  - Error Rate: `AlertTriangle` or `ShieldAlert`
  - Last Event: `Clock`

* **Relative Time**: Use existing `formatDate` utility or add relative time helper

* **Error Rate Variant Logic**:
  ```typescript
  function getErrorRateVariant(rate: number): 'green' | 'yellow' | 'red' {
      if (rate < 1) return 'green';
      if (rate < 5) return 'yellow';
      return 'red';
  }
  ```

## 5. Design & UI/UX
* **Layout**: Stats cards appear at the top of each dashboard, above existing content
* **Card Icons**: Each card has a distinct icon matching its purpose
* **Error Rate**: 
  - Shows percentage with 1 decimal (e.g., "2.5%")
  - Card border/gradient changes color based on threshold
* **Last Event**: 
  - Shows relative time (e.g., "5 minutes ago", "2 hours ago")
  - Subtitle shows actual timestamp on hover or below value
* **Glassmorphism**: Cards use existing StatCard styling (matches design system)

### Visual Reference
```
+-----------------------------------------------------------------------------+
|  +--------------+  +--------------+  +--------------+  +--------------+    |
|  | Events Today |  | Active       |  | Error Rate   |  | Last Event   |    |
|  |     1,234    |  | Channels     |  |    2.5%      |  | 5 min ago    |    |
|  |              |  |     12       |  |   (green)    |  |              |    |
|  +--------------+  +--------------+  +--------------+  +--------------+    |
|                                                                             |
|  [Rest of dashboard content...]                                             |
+-----------------------------------------------------------------------------+
```

## 6. Implementation Notes / Research

### Backend
* **Existing patterns to follow**:
  - `AdminDashboardController` + `AdminStatsService` for controller/service structure
  - `AdminStatsResponse` for response DTO pattern
  - `EventRepository` for native query patterns

* **File locations**:
  - Controller: `api/dashboard/controller/DashboardController.java` (new package)
  - Service: `api/dashboard/service/DashboardStatsService.java`
  - Response: `api/dashboard/model/response/DashboardStatsResponse.java`
  - For org endpoint, could add to existing `OrganizationController` or create dedicated controller

* **Error rate query approach** (suggested):
  ```sql
  WITH channel_last_events AS (
      SELECT DISTINCT ON (c.id) c.id as channel_id, e.severity
      FROM channel c
      LEFT JOIN event e ON e.channel_id = c.id
      WHERE c.user_id = :userId AND c.organization_id IS NULL AND c.status = 'ACTIVE'
      ORDER BY c.id, e.timestamp DESC
  )
  SELECT COUNT(*) FILTER (WHERE severity = 'CRITICAL') as critical_count,
         COUNT(*) as total_count
  FROM channel_last_events
  ```

* **Paths constant**: Add to `io.github.eventify.api.Paths`:
  - `DASHBOARD_STATS_PATH = "/api/v1/dashboard/stats"`
  - `ORG_DASHBOARD_STATS_PATH = "/api/v1/organizations/{orgId}/dashboard/stats"`

### Frontend
* **User Dashboard location**: `src/routes/(authenticated)/dashboard/+page.svelte`
* **Org Dashboard location**: `src/routes/(authenticated)/organizations/[orgId]/dashboard/+page.svelte`
* **StatCard import**: `import { StatCard } from '$lib/components/ui/stat-card'`
* **Existing pattern**: See admin dashboard for StatCard usage with loading states and trailing snippets
* **API types**: Run `bun run sync:api` after backend is complete to generate TypeScript types
