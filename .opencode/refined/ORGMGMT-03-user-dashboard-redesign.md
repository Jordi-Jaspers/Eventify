---
epic: "ORGMGMT"
title: "User Dashboard Redesign"
estimate: M
status: ready
created: 2026-05-22
depends_on: ["ORGMGMT-02-sidebar-restructure-route-renames"]
labels: [frontend, backend]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user\
**I want** my dashboard to show actionable operational information\
**So that** I can start my day knowing what needs attention\

## 2. Business Context & Value
Current dashboard shows generic stats and a "Coming Soon" placeholder. Redesign turns it into a "start your day" view: what's broken, what happened recently, and org health at a glance.

## 3. Acceptance Criteria
* [ ] **Watchlist health section — non-OK only**
    * Given the user has watchlists with channels in WARNING or CRITICAL
    * When viewing the dashboard
    * Then only non-OK watchlists are shown with their current severity and affected channel count
    * And if all watchlists are OK, show a "All clear" message
* [ ] **Recent notifications section**
    * Given the user has received notifications in the last 24h
    * When viewing the dashboard
    * Then the most recent notifications are listed (max 10) with timestamp, watchlist name, severity transition
    * And clicking a notification navigates to the relevant watchlist/monitor
* [ ] **Organization status cards**
    * Given the user belongs to multiple organizations
    * When viewing the dashboard
    * Then each org shows: name, status badge (ACTIVE/SUSPENDED), event volume (today), channels in alert count
    * And clicking an org card navigates to that org's monitor page
* [ ] **Suspended org visual indicator**
    * Given the user belongs to a suspended org
    * When viewing the dashboard
    * Then that org card shows a SUSPENDED badge with muted styling
    * And the card is still clickable (navigates to org monitor — will show 403 or suspension message)
* [ ] **Empty states**
    * Given a new user with no orgs/watchlists
    * When viewing the dashboard
    * Then helpful onboarding prompts are shown (create org, create watchlist)

## 4. Technical Requirements
* **API Changes**: New endpoint `GET /v1/user/dashboard` returning:
  - `watchlistHealth`: list of non-OK watchlists with severity + channel count
  - `recentNotifications`: last 10 notifications (last 24h)
  - `organizations`: list with id, name, status, eventVolumeToday, channelsInAlertCount
* **Database**: N/A — aggregates existing data
* **Security**: User sees only their own watchlists/orgs/notifications
* **Performance**: Single endpoint aggregating multiple queries. Consider caching with short TTL (1-2 min).

## 5. Design & UI/UX
* Layout: 2-column on desktop, single column on mobile
* Top: Watchlist health alerts (attention-grabbing, severity-colored)
* Middle: Recent notifications (compact list)
* Bottom: Org cards grid (similar to current but with more info)
* Remove: welcome card, generic stat cards, "Coming Soon" placeholder

## 6. Implementation Notes
* **Frontend**: Rewrite `client/src/routes/(authenticated)/dashboard/+page.svelte`
* **Backend**: New controller + service for dashboard aggregation
* **Pattern**: Follow `AdminStatsService` pattern with `@Cacheable` (short TTL via Caffeine if added, or simple cache)
* **Watchlist health**: Query from existing monitor data (channel severities per watchlist)
* **Notifications**: Query from existing notification table filtered by user + last 24h

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `client/src/routes/(authenticated)/dashboard/+page.svelte` | Complete rewrite |
| `client/src/routes/(authenticated)/dashboard/+page.ts` | Update data loading |
| New: `server/.../dashboard/controller/UserDashboardController.java` | New endpoint |
| New: `server/.../dashboard/service/UserDashboardService.java` | Aggregation logic |
| New: `client/src/lib/api/dashboard/UserDashboardController.ts` | Frontend API client |
