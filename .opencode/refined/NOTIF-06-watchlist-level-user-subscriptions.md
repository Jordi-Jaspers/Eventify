---
epic: "NOTIF"
title: "Watchlist-Level User Subscriptions"
estimate: L
status: ready
created: 2026-05-14
depends_on: [ ]
labels: [ backend, frontend, database ]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user monitoring services via watchlists\
**I want** to subscribe to severity transitions on my watchlists\
**So that** I get notified when channels change to critical or warning states without constantly watching the dashboard\

## 2. Business Context & Value
Watchlists aggregate channels into meaningful monitoring views, but users must manually check them for changes. Subscriptions close this gap by alerting users when severity transitions occur on any channel within a subscribed watchlist. This is the foundation for the entire alerting subsystem (Telegram, email adapters plug in later).

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Subscribe to a watchlist
    * Given a user viewing a watchlist monitor page
    * When they click the subscribe bell icon in the watchlist header
    * Then a subscription settings popover/sheet opens with severity checkboxes (CRITICAL, WARNING, OK) defaulting to CRITICAL only
    * And they can save to create a subscription
    * And the bell icon becomes filled/active

* [ ] **Scenario 2**: Severity transition triggers notification
    * Given a user subscribed to watchlist W targeting CRITICAL
    * And watchlist W contains channel C
    * When channel C transitions from OK → CRITICAL (via event ingestion)
    * Then the user receives an in-app notification with channel name, old severity, new severity, and a link to the watchlist
    * And the notification is marked as urgent

* [ ] **Scenario 3**: Non-matching transition does not notify
    * Given a user subscribed to watchlist W targeting CRITICAL only
    * When a channel in W transitions from OK → WARNING
    * Then no notification is dispatched to that user

* [ ] **Scenario 4**: Edit subscription settings
    * Given a user already subscribed to a watchlist
    * When they click the active bell icon
    * Then the settings popover opens with their current severity selections
    * And they can modify severities or unsubscribe

* [ ] **Scenario 5**: Unsubscribe
    * Given a user subscribed to a watchlist
    * When they click unsubscribe in the settings popover
    * Then the subscription is deleted and the bell icon returns to inactive state

* [ ] **Scenario 6**: Org watchlist — multiple subscribers
    * Given an org watchlist visible to all org members
    * When two different org members each subscribe with different severity targets
    * Then each receives notifications only for their own configured severities independently

* [ ] **Scenario 7**: Watchlist deletion cascades
    * Given a user subscribed to a watchlist
    * When the watchlist is deleted
    * Then the subscription is also deleted (FK cascade)

* [ ] **Scenario 8**: Channel removed from watchlist
    * Given a subscription on watchlist W containing channels A and B
    * When channel A is removed from the watchlist configuration
    * Then future transitions on channel A no longer trigger notifications for this subscription
    * And channel B transitions still trigger as before

* [ ] **Edge Case**: Invalid severity rejected
    * Given a user creating/updating a subscription
    * When they submit with NO_DATA as a target severity
    * Then the backend returns 400 Bad Request
    * And NO_DATA is not shown as an option in the frontend UI

* [ ] **Edge Case**: Duplicate transition suppression
    * Given channel C already at CRITICAL and a new CRITICAL event arrives
    * When the severity transition job runs
    * Then no notification fires (no transition occurred)

## 4. Technical Requirements

* **API Changes**:
    * `POST /api/v1/watchlists/{watchlistId}/subscription` — create/update subscription
        * Request: `{ targetSeverities: ["CRITICAL"], adapters: ["IN_APP"] }`
        * Response: `SubscriptionResponse` (id, watchlistId, targetSeverities, adapters, createdAt)
    * `GET /api/v1/watchlists/{watchlistId}/subscription` — get current user's subscription (404 if none)
    * `DELETE /api/v1/watchlists/{watchlistId}/subscription` — unsubscribe
    * Validation: targetSeverities must be non-empty subset of [CRITICAL, WARNING, OK]. NO_DATA rejected.
    * Validation: adapters must include IN_APP (enforced server-side, always added if missing)

* **Database**:
    * New `subscription` table: `id` (BIGSERIAL PK), `user_id` (FK→user, NOT NULL), `watchlist_id` (FK→watchlist ON DELETE CASCADE, NOT NULL), `target_severities` (JSONB, NOT NULL), `adapters` (JSONB, NOT NULL), `created_at` (TIMESTAMPTZ), `updated_at` (TIMESTAMPTZ)
    * Unique constraint on `(user_id, watchlist_id)` — one subscription per user per watchlist
    * Add `current_severity VARCHAR(20)` column to `channel` table (nullable)
    * Add `last_notified_severity VARCHAR(20)` column to `channel` table (nullable)
    * Modify existing `update_channel_last_event` trigger to also SET `current_severity = NEW.severity`
    * Index on `subscription(watchlist_id)` for the transition job's watchlist lookup

* **Severity Transition Detection** (Prometheus-style scheduled evaluation):
    * `SeverityTransitionJob` — `@Scheduled(fixedDelay = 60_000)` (1 minute)
    * Queries: `SELECT id, current_severity, last_notified_severity FROM channel WHERE current_severity IS DISTINCT FROM last_notified_severity`
    * For each changed channel:
        1. Find all watchlists containing this channel (query watchlist JSONB configs)
        2. Find all subscriptions on those watchlists where `targetSeverities` contains the new severity
        3. Dispatch via `NotificationDispatchService` with `NotificationAudience.user(subscriberId)`
        4. Update `channel.last_notified_severity = current_severity`
    * Batch update `last_notified_severity` after processing

* **Security**:
    * Personal watchlist: only owner can subscribe (enforced by existing watchlist access check)
    * Org watchlist: any org member can subscribe
    * Subscription endpoints use existing watchlist authorization

* **Performance**:
    * Zero impact on event ingestion hot path (trigger already does a channel UPDATE — adding one column is negligible)
    * Transition job runs async, 1-min interval, only processes channels with actual changes
    * Watchlist-channel lookup: consider adding a reverse index table or caching if JSONB scanning becomes slow at scale

## 5. Design & UI/UX
* **Subscribe bell icon** in watchlist monitor page header, next to watchlist name
* Inactive state: outline bell icon; Active state: filled bell icon with subtle indicator
* Click opens **subscription settings popover** (not full sheet — lightweight interaction):
    * Severity checkboxes: CRITICAL (default checked), WARNING, OK
    * Adapter toggles: hidden for now (only IN_APP, auto-included). NOTIF-07 will add Telegram toggle here.
    * Save / Unsubscribe buttons
* **Notification content**: "[Channel Name] transitioned from OK to CRITICAL" with action link to the watchlist monitor page

## 6. Implementation Notes

### Backend
| File | Purpose |
|------|---------|
| `api/subscription/model/Subscription.java` | New entity |
| `api/subscription/model/SubscriptionAdapter.java` | Enum: IN_APP, TELEGRAM (future) |
| `api/subscription/repository/SubscriptionRepository.java` | JPA repo with `findByWatchlistIdIn(List<Long>)` |
| `api/subscription/service/SubscriptionService.java` | CRUD + validation (NO_DATA rejection, IN_APP enforcement) |
| `api/subscription/controller/SubscriptionController.java` | REST endpoints nested under watchlist path |
| `api/subscription/job/SeverityTransitionJob.java` | Scheduled job — follows `ChannelStalenessJob` pattern |
| `db/changesets/202505141000-PRD-subscription-table.xml` | Migration: subscription table + channel severity columns + trigger update |

### Frontend
| File | Purpose |
|------|---------|
| Watchlist monitor page header | Add bell icon + click handler |
| `lib/components/watchlist/SubscriptionPopover.svelte` | New: severity checkboxes, save/unsubscribe |
| `lib/api/subscription/` | New: API client for subscription CRUD |

### Patterns to follow
* Scheduled job: follow `ChannelStalenessJob` pattern exactly (`@Component` + `@Scheduled` + delegate to service)
* Notification dispatch: use `NotificationDispatchService.dispatch()` with `NotificationAudience.user(userId)`
* NotificationPayload: category `ALERT`, urgent = true for CRITICAL transitions, false for WARNING/OK
* API: follow existing watchlist controller patterns for auth/access checks
* Migration: raw `<sql>` tags, `COMMENT ON` changesets, naming `202505141000-PRD-*`

### Watchlist-channel reverse lookup concern
* Currently watchlist configs store channel IDs in JSONB — finding "which watchlists contain channel X" requires scanning all watchlist configs
* For MVP: PostgreSQL JSONB containment query is acceptable at current scale
* If performance degrades: add a `watchlist_channel` junction table (denormalized) maintained on watchlist save — future optimization, not needed now

### Thread pool consideration
* Current scheduler is single-threaded — `SeverityTransitionJob` + existing jobs share one thread
* If job execution time becomes a concern, configure a `ThreadPoolTaskScheduler` — defer until measured

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `triggers/update_channel_last_event.sql` | Add `current_severity = NEW.severity` to existing UPDATE | ~1 line |
| Watchlist monitor page | Add bell icon in header | Header area |
