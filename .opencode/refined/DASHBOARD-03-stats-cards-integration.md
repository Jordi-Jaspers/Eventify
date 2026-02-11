# Dashboard Stats Cards Integration

**Epic**: Dashboard Enhancements
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-02-11
**Depends On**: DASHBOARD-01-backend-stats-api.md, DASHBOARD-02-statcard-shared-component.md

## 1. User Story
**As a** user viewing my dashboard
**I want** to see stat cards showing Events Today, Active Channels, Error Rate, and Last Event
**So that** I can quickly understand the health and activity of my monitoring setup at a glance

## 2. Business Context & Value
Visual stats cards transform the dashboard from a basic placeholder into a valuable monitoring hub. Users get immediate insight into their system health without navigating to individual channels. The error rate card with color thresholds provides at-a-glance health status.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Stats cards display on User Dashboard
    * Given I am on the User Dashboard (`/dashboard`)
    * When the page loads
    * Then I see 4 stat cards above the organizations section
    * And they show: Events Today, Active Channels, Error Rate, Last Event

* [ ] **Scenario 2**: Stats cards display on Organization Dashboard
    * Given I am on an Organization Dashboard (`/organizations/123/dashboard`)
    * When the page loads
    * Then I see 4 stat cards above the organization details
    * And they show stats for that organization's channels only

* [ ] **Scenario 3**: Error Rate color thresholds
    * Given the Error Rate stat card is displayed
    * When error rate < 1%, Then the card uses `green` variant
    * When error rate >= 1% and < 5%, Then the card uses `yellow` variant
    * When error rate >= 5%, Then the card uses `red` variant

* [ ] **Scenario 4**: Last Event relative time
    * Given the last event was 5 minutes ago
    * When I view the Last Event card
    * Then it shows "5 minutes ago" (relative time format)

* [ ] **Scenario 5**: Loading state
    * Given the dashboard is fetching stats
    * When the API call is in progress
    * Then all 4 stat cards show loading skeleton state

* [ ] **Scenario 6**: Empty state
    * Given I have no channels
    * When I view the dashboard
    * Then Events Today = 0, Active Channels = 0, Error Rate = 0%, Last Event = "No events"

* [ ] **Scenario 7**: Responsive layout
    * Given I am viewing the dashboard
    * When on desktop (>=1024px), Then cards display in 4 columns
    * When on tablet (>=768px, <1024px), Then cards display in 2x2 grid
    * When on mobile (<768px), Then cards stack vertically

* [ ] **Scenario 8**: Error handling
    * Given the stats API fails
    * When I view the dashboard
    * Then an error message is shown with a retry option

## 4. Technical Requirements
* **Frontend Service**: Create `$lib/services/dashboard.service.ts`
  ```typescript
  export async function getDashboardStats(): Promise<DashboardStatsResponse> { ... }
  export async function getOrgDashboardStats(orgId: number): Promise<DashboardStatsResponse> { ... }
  ```

* **API Client**: Add to `$lib/api/` (or use generated client after OpenAPI sync)

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
  - Consider: `date-fns` `formatDistanceToNow` if not already available

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
* **User Dashboard location**: `src/routes/(authenticated)/dashboard/+page.svelte`
* **Org Dashboard location**: `src/routes/(authenticated)/organizations/[orgId]/dashboard/+page.svelte`
* **StatCard import**: `import { StatCard } from '$lib/components/ui/stat-card'`
* **Existing pattern**: See admin dashboard for StatCard usage with loading states and trailing snippets
* **API types**: Run `bun run sync:api` after backend is complete to generate TypeScript types
* **Relative time options**:
  - `date-fns` is likely available (check package.json)
  - Or use simple custom function for "X minutes/hours/days ago"
