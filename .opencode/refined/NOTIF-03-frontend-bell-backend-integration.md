---
epic: "NOTIF"
title: "Notification Bell Backend Integration with Polling"
estimate: M
status: ready
created: 2026-05-08
depends_on: ["NOTIF-01-dispatch-core-and-entity"]
labels: [frontend, refactor]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** logged-in user\
**I want** my notification bell to show real backend notifications (welcome, admin broadcasts, future severity alerts) and update on its own without me refreshing the page\
**So that** I can see what's happening on the platform without having to ask in Slack or refresh the page.\

## 2. Business Context & Value
NOTIF-01 built the backend; today the bell still reads localStorage/changelog. This story replaces the bell's data source with the backend API, adds 30s polling for unread count updates, and implements explicit per-item mark-as-read. After this ships, the welcome notification (NOTIF-01) becomes user-visible, admin broadcasts (NOTIF-02) reach users in real time-ish, and the bell becomes the single inbox for all backend-driven user comms. The changelog data file is no longer the bell's source — but it remains for the standalone `/changelog` page until NOTIF-04 moves it to the backend.

## 3. Acceptance Criteria
* [ ] **Scenario 1: Bell shows backend notifications**
    * Given a logged-in user with notifications in the database
    * When they click the bell icon in the sidebar
    * Then the panel shows their notifications (most recent first), each rendering category icon, title, message, action button (if `actionUrl` set), and a timestamp
* [ ] **Scenario 2: Unread badge from backend**
    * Given the user has 3 unread notifications
    * When the page loads
    * Then the bell shows the pulsing dot + a numeric badge showing "3" (capped at "9+" for ≥10)
* [ ] **Scenario 3: 30-second polling**
    * Given the user is on any authenticated page with the sidebar visible
    * When 30 seconds pass and a new notification arrives in the backend
    * Then the bell badge updates without a page reload
    * And polling is paused when `document.visibilityState === 'hidden'`, resumed on visible
* [ ] **Scenario 4: Mark single as read on click**
    * Given an unread notification in the panel
    * When the user clicks it
    * Then `POST /notifications/{id}/read` is called, the item visually transitions to read state (lower opacity, no unread dot), and the badge count decrements
    * If the notification has an `actionUrl`, the user is also navigated to that URL after marking read
* [ ] **Scenario 5: Mark all as read button**
    * Given the panel is open with ≥1 unread notification
    * When the user clicks "Mark all as read" at the top of the panel
    * Then `POST /notifications/read-all` is called, all visible items transition to read state, badge count goes to 0
* [ ] **Scenario 6: Visual category styling**
    * Given notifications of different categories
    * When rendered in the panel
    * Then each shows the correct icon + accent color per category mapping (ANNOUNCEMENT=blue/Info, UPDATE=green/Sparkles, REMINDER=amber/Clock, ALERT=red/AlertTriangle, SECURITY=red/Shield)
    * And `urgent` notifications (ALERT, SECURITY) get a left-border accent and pulsing icon
* [ ] **Scenario 7: Empty state**
    * Given the user has zero notifications
    * When they open the panel
    * Then "All caught up!" empty state shows with the existing Inbox icon
* [ ] **Scenario 8: Pagination / "Load more"**
    * Given the user has more than 20 notifications
    * When they scroll to the bottom of the panel
    * Then a "Load more" button appears; clicking fetches the next 20 via `GET /notifications?offset=20`
* [ ] **Scenario 9: Logged-out state**
    * Given the user is logged out (e.g., session expired during polling)
    * When the next poll returns 401
    * Then polling stops cleanly (no console errors), bell hides or shows in default state (existing auth handling kicks in)
* [ ] **Scenario 10: Action URL navigates**
    * Given a notification with `actionUrl="/channels"` and `actionLabel="Get started"`
    * When user clicks the action button (or the item if no separate button)
    * Then they are marked-as-read AND navigated to `/channels`
* [ ] **Edge Case: Backend error during poll**
    * Given the unread-count poll fails (5xx)
    * When the response comes back
    * Then the badge keeps the previous count (don't show "0" or error state); error logged to console; next poll retries

## 4. Technical Requirements
* **API Changes**: N/A — consumes endpoints from NOTIF-01.
* **Database**: N/A.
* **Security**: All requests use existing cookie-based auth. 401 handled by existing global middleware.
* **Performance**: Polling interval = 30s. Pause when tab hidden. Single endpoint `GET /notifications/unread-count` per poll (cheap COUNT query). Full list only fetched on panel open or "Load more".

## 5. Design & UI/UX
**Bell trigger (sidebar):**
- Bell icon (existing).
- Pulsing dot + numeric badge "N" or "9+" overlay (top-right of icon).
- Click opens the existing right-side `Sheet` panel.

**Panel header:**
- Title: "Notifications"
- "Mark all as read" button (right side, only enabled when ≥1 unread)
- Close button (existing)

**Notification item:**
- Layout: icon column (category-colored circle with icon) | content column (title + message + relative timestamp + optional action button)
- Unread state: subtle background highlight + unread dot to the left of icon
- Read state: lower opacity, no dot
- Urgent: left-border accent (4px solid red), pulsing icon
- Click anywhere on item = mark read + navigate (if actionUrl); explicit action button is redundant unless we want it visible — recommendation: action button shown only when `actionLabel` is set, and clicking it does the same thing as clicking the item

**Footer:**
- "Load more" button when more notifications available
- "View all" → future link to a full notifications page (out of scope for NOTIF-03)

**Polling lifecycle:**
- Start polling on user login / app mount
- Pause on `visibilitychange` → hidden
- Resume on visible
- Stop on logout

## 6. Implementation Notes
**Files to refactor:**
- `client/src/lib/stores/notification.svelte.ts` — replace changelog/version logic with API-backed store using Svelte 5 runes:
  - `$state` for notifications list, unreadCount
  - `$effect` for polling lifecycle (setInterval + visibility listener)
  - Methods: `fetch()`, `fetchUnreadCount()`, `markRead(id)`, `markAllRead()`, `loadMore()`
- `client/src/lib/components/notification/NotificationPanel.svelte` — render from store, not changelog data; add Mark All button, Load More button, per-item click handler
- `client/src/lib/types/notification.ts` — replace local types with API-generated types (regenerate via `bun run sync:api`)

**Files to delete:**
- `client/src/lib/stores/version.svelte.ts` (no longer needed — read state lives in backend)
- localStorage key `eventify_last_seen_version` (clean up on first load if exists)

**Files to keep:**
- `client/src/lib/data/changelog` — still feeds `/changelog` page until NOTIF-04 moves it. Bell no longer references it.

**New components:**
- `client/src/lib/components/notification/NotificationItem.svelte` — single item rendering (reusable: also used by NOTIF-02 admin preview card)
- `client/src/lib/components/notification/CategoryIcon.svelte` — small icon+color resolver based on category enum

**Polling impl:**
- `$effect(() => { const interval = setInterval(fetchUnreadCount, 30000); ... return () => clearInterval(interval); })`
- Visibility listener: `document.addEventListener('visibilitychange', ...)` — clear interval on hidden, restart on visible

**Pitfalls:**
- Don't poll when user not authenticated — gate on `authStore.user` being set.
- After marking read, optimistically update UI before server response (rollback on failure).
- "Mark all as read" should also stop the unread badge from briefly flashing back if a poll completes mid-action.
- Numeric badge: cap at "9+" — full number "127" looks bad on a sidebar icon.

## 7. Test Impact Analysis (Refactoring)
### Existing tests affected by this change:
| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| Tests for `notification.svelte.ts` (if any) | Various | Changelog-based logic | YES | Replace with API-mock-backed tests |
| `client/tests/notification-bell.spec.ts` (Playwright, if exists) | Bell open / badge / mark-read | Old changelog behavior | YES | Rewrite for new API-backed bell |

### Test modification policy:
- [x] Existing tests MAY be updated — the bell's behavior is being entirely replaced. Old changelog-based tests should be deleted or rewritten.
- [ ] Add new Playwright test: bell shows backend notification, click marks read, polling refresh, urgent visual.

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `client/src/lib/stores/notification.svelte.ts` | Full rewrite around API | all |
| `client/src/lib/components/notification/NotificationPanel.svelte` | Render from new store, add Mark All / Load More | most |
| `client/src/lib/stores/version.svelte.ts` | DELETE | all |
| `client/src/lib/types/notification.ts` | Sync with backend types | most |
| `client/src/lib/components/layout/AppSidebarUser.svelte` | Update badge to numeric (9+) | small |
