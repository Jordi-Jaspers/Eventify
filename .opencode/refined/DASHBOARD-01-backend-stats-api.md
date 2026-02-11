# Dashboard Stats API Endpoint

**Epic**: Dashboard Enhancements
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** user viewing my dashboard
**I want** to see key metrics about my channels and events
**So that** I can quickly understand the health and activity of my monitoring setup

## 2. Business Context & Value
The current dashboards are basic placeholders. Adding real-time stats provides immediate value to users by surfacing actionable insights at a glance. This is the backend foundation for the dashboard enhancement feature.

## 3. Acceptance Criteria
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

## 4. Technical Requirements
* **API Changes**:
  - `GET /api/v1/dashboard/stats` - Personal dashboard stats (requires authentication)
  - `GET /api/v1/organizations/{orgId}/dashboard/stats` - Org dashboard stats (requires org membership)
  
* **Response DTO** (`DashboardStatsResponse`):
  ```java
  public record DashboardStatsResponse(
      long eventsToday,        // Count of events in last 24 hours
      int activeChannels,      // Count of channels with status = ACTIVE
      double errorRate,        // Percentage (0.0 - 100.0)
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
  - Consider cache eviction on event ingestion (optional, can defer)

* **Security**:
  - Personal endpoint: `@PreAuthorize("isAuthenticated()")`
  - Org endpoint: `@PreAuthorize("@organizationSecurity.isMember(#orgId, principal)")`

* **Performance**:
  - All stats should be fetched in minimal DB round-trips
  - Consider a single native query that returns all metrics
  - Response time target: < 200ms

## 5. Design & UI/UX (If applicable)
N/A - Backend only

## 6. Implementation Notes / Research
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
