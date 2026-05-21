---
epic: "BUGS"
title: "Fix notification dispatch not firing on first severity transition"
estimate: S
status: ready
created: 2026-05-21
depends_on: []
labels: [backend, bugfix, notifications]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user subscribed to watchlist severity transitions\
**I want** notifications to fire on the first transition of a channel\
**So that** I don't miss critical events on newly active channels\

## 2. Business Context & Value
Confirmed production bug: subscriptions never fire for channels that have not previously triggered a notification. The NULL-unsafe JPQL query silently excludes these channels, making the entire notification system appear broken for new subscriptions.

## 3. Acceptance Criteria
* [ ] **First transition fires notification**
    * Given a channel with `lastNotifiedSeverity = NULL` (never notified before)
    * When an event arrives that sets `currentSeverity` to CRITICAL
    * Then the severity transition job detects this channel and dispatches notifications to matching subscribers
* [ ] **Subsequent transitions still work**
    * Given a channel with `lastNotifiedSeverity = WARNING`
    * When `currentSeverity` changes to CRITICAL
    * Then notifications dispatch as before (no regression)
* [ ] **No duplicate notifications**
    * Given a channel that has already been notified for its current severity
    * When the job runs again
    * Then no duplicate notification is sent (`lastNotifiedSeverity` is updated after dispatch)

## 4. Technical Requirements
* **API Changes**: N/A
* **Database**: N/A — no schema changes, just query fix
* **Security**: N/A
* **Performance**: N/A — same query, just NULL-safe

## 5. Design & UI/UX
N/A — backend-only fix.

## 6. Implementation Notes

### Root Cause
**File:** `server/src/main/java/.../channel/repository/ChannelRepository.java` (~line 292)

```java
// CURRENT (broken): <> with NULL = UNKNOWN → channel excluded
@Query("SELECT c FROM Channel c WHERE c.currentSeverity IS NOT NULL AND c.currentSeverity <> c.lastNotifiedSeverity")
```

### Fix
```java
@Query("SELECT c FROM Channel c WHERE c.currentSeverity IS NOT NULL AND (c.lastNotifiedSeverity IS NULL OR c.currentSeverity <> c.lastNotifiedSeverity)")
```

This handles the NULL case: when `lastNotifiedSeverity` is NULL, the channel IS included (first-ever transition).

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../channel/repository/ChannelRepository.java` | Fix JPQL NULL comparison (~line 292) |

### Notes
- The `adapters` field on Subscription is intentionally ignored for now — IN_APP always dispatches. Adapter filtering will be added with NOTIF-07 (Telegram).
