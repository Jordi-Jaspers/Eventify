# User Dashboard Redesign

**Completed:** 2026-05-22
**Epic:** ORGMGMT
**Source:** .opencode/refined/ORGMGMT-03-user-dashboard-redesign.md

## Summary

Replaced generic stats dashboard with actionable "start your day" view: watchlist health (non-OK only), recent notifications (24h), and organization status grid with event volume and alert counts.

## Plan Approved by the user:

### Requirements Summary

- Watchlist health section showing only non-OK watchlists with severity + channel count
- Recent notifications (max 10, last 24h) with urgency indicators
- Organization status grid with event volume today + channels in alert
- Welcome card with user info and dynamic system status
- Quick stats row (notifications, channels in alert, organizations)

### Technical Approach

- Backend: `GET /v1/user/dashboard` → `UserDashboardResponse` (aggregates watchlists, notifications, org memberships)
- Frontend: Full rewrite of dashboard page with Card-based layout, StatCard components, responsive grid

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Backend test suite (11 unit + 9 integration) |
| 2 | spring-backend-agent | Implement service, controller, domain models, mapper |
| 3 | backend-optimizer-agent | Lombok, streams, @Schema annotations (-55% lines) |
| 4 | svelte-frontend-agent | Dashboard page rewrite + API controller |
| 5 | frontend-optimizer-agent | $derived constants, inline handlers |

## Implementation

### Backend

- `GET /v1/user/dashboard` → `UserDashboardResponse`
- `UserDashboardService` — aggregates watchlist health, recent notifications, org status
- Domain models: `UserDashboard`, `WatchlistHealth`, `RecentNotification`, `OrganizationDashboardSummary`
- Response DTOs: `UserDashboardResponse`, `WatchlistHealthResponse`, `RecentNotificationResponse`, `OrganizationStatusResponse`
- `UserDashboardMapper` (MapStruct)
- No database migrations (aggregates existing data)

### Frontend

- Full rewrite of `+page.svelte` (305→303 lines)
- `UserDashboardController.ts` using openapi-fetch `client.GET`
- Welcome card, quick stats row (StatCard), watchlist health, notifications, org grid
- Responsive: 2-col desktop, single-col mobile

### Deviations from Plan

- Added welcome card with user info + dynamic status (user request during review)
- Added quick stats row with StatCard components (user request)
- Used openapi-fetch instead of native fetch after API sync

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Backend tests | 11 unit + 9 integration tests |
| spring-backend-agent | Implementation (5 rounds) | All tests passing |
| backend-optimizer-agent | Refactor | 720→321 lines (-55%) |
| svelte-frontend-agent | Dashboard page (8 rounds) | Type check passing |
| frontend-optimizer-agent | Cleanup | $derived, inlined handlers |

## Files Modified

- `server/src/main/java/io/github/eventify/api/dashboard/` — entire package (new)
- `server/src/main/java/io/github/eventify/api/Paths.java` — added USER_DASHBOARD_PATH
- `server/src/main/java/io/github/eventify/api/watchlist/repository/WatchlistRepository.java` — added findAllByUserId
- `server/src/main/java/io/github/eventify/api/notification/repository/NotificationRepository.java` — added findRecentByUserId
- `server/src/test/java/io/github/eventify/api/dashboard/` — test package (new)
- `client/src/routes/(authenticated)/dashboard/+page.svelte` — full rewrite
- `client/src/lib/api/dashboard/UserDashboardController.ts` — new
- `client/src/lib/api/models.ts` — added type aliases
- `client/src/lib/types/api.d.ts` — regenerated from OpenAPI spec
- `server/openapi.json` — updated with new endpoint

## Tests

- 20 tests written (11 unit + 9 integration), all passing
