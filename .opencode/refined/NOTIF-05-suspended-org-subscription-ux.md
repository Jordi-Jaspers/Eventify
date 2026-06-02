---
epic: "NOTIF"
title: "Suspended Org Subscription UX"
estimate: S
status: ready
created: 2026-06-02
depends_on: ["NOTIF-04-subscription-redesign"]
labels: [fullstack, notification, subscription]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user with subscriptions to watchlists in a suspended org\
**I want** clear visual indicators that notifications are blocked and limited edit capability\
**So that** I understand why I'm not receiving alerts and can clean up stale subscriptions\

## 2. Business Context & Value

When an org is suspended, its watchlist subscriptions become inactive but remain in the user's list. Without UX treatment, users see subscriptions that silently do nothing — causing confusion and eroding trust in the notification system.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Blocked indicator on subscription list
    * Given a user with a subscription to a watchlist in a suspended org
    * When they view their subscriptions list
    * Then that subscription shows a "Blocked" badge/indicator with tooltip "Organization suspended — notifications paused"
* [ ] **Scenario 2**: Remove allowed on blocked subscription
    * Given a blocked subscription
    * When the user clicks "Remove"
    * Then the subscription is deleted (standard confirmation flow)
* [ ] **Scenario 3**: Edit disallowed on blocked subscription
    * Given a blocked subscription
    * When the user attempts to edit it
    * Then edit controls are disabled with tooltip "Cannot edit while organization is suspended"
* [ ] **Scenario 4**: Suspended org watchlists excluded from subscription search
    * Given an org in suspended state with watchlists
    * When a user searches for watchlists to subscribe to (in settings page or quick subscribe)
    * Then watchlists belonging to suspended orgs do not appear in results
* [ ] **Scenario 5**: Reactivated org — subscriptions resume
    * Given a previously suspended org that gets reactivated
    * When the next severity transition fires
    * Then subscriptions to that org's watchlists fire normally again (blocked indicator removed)
* [ ] **Edge Case**: All subscriptions blocked
    * Given a user whose only subscriptions are all to suspended orgs
    * When they view subscriptions list
    * Then they see all items with blocked indicators + empty state message for active subscriptions

## 4. Technical Requirements

* **API Changes**:
    - Subscription list response includes `blocked: boolean` + `blockedReason: string` per subscription (derived from org status)
    - Subscription search endpoint (`GET /v1/subscriptions/watchlists/search`): filter out watchlists where `organization.status = SUSPENDED`
    - `PUT /v1/subscriptions/{id}`: return 409 Conflict if subscription's org is suspended
* **Database**: N/A — uses existing `organization.status` field
* **Security**: N/A — read-only status derivation
* **Performance**: Subscription list query joins org table — ensure index on `organization.status`

## 5. Design & UI/UX

- **Blocked badge**: Orange/amber badge with lock icon + "Blocked" text on subscription card
- **Tooltip**: On hover explains "Organization suspended — notifications paused"
- **Disabled state**: Edit button grayed out, cursor not-allowed
- **Delete**: Still uses standard red destructive button (not disabled)
- **Search results**: Simply omitted — no "suspended" indicator in search (avoid confusion)

## 6. Implementation Notes

- Backend: `SubscriptionService.listForUser()` → join/fetch org status, map to DTO field `blocked`
- Backend: search endpoint already filters by org access — add `AND o.status != 'SUSPENDED'`
- Backend: update endpoint — check org status before allowing edit
- Frontend: conditional rendering in `SubscriptionCard` component based on `blocked` field
- `SeverityTransitionJob` already skips suspended orgs — no change needed there

## 7. Test Impact Analysis

### Existing tests affected by this change:

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `SubscriptionServiceTest` | list methods | Returns subscriptions without blocked field | YES | Add blocked field assertion |
| `SeverityTransitionJobTest` | skips suspended | Already skips — validates existing behavior | NO | Keep |

### Test modification policy:

- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified: SubscriptionService tests, SubscriptionController tests

### Potential files impacted by implementation

| File | Change |
|------|--------|
| `subscription/service/SubscriptionService.java` | Add blocked status derivation |
| `subscription/controller/SubscriptionController.java` | 409 on edit of blocked subscription |
| `subscription/dto/SubscriptionResponse.java` | Add blocked + blockedReason fields |
| Frontend: `SubscriptionCard` component | Blocked state rendering |
| Frontend: watchlist search | Filter already handled by backend |
