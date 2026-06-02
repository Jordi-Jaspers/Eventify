---
epic: "NOTIF"
title: "Subscription Redesign — Personal & Org-Shared"
estimate: XL
status: ready
created: 2026-06-02
depends_on: ["NOTIF-01-adapter-infrastructure", "NOTIF-03-notification-settings-ui"]
labels: [fullstack, notification, subscription]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user or org admin\
**I want** to manage personal and org-shared subscriptions with adapter selection\
**So that** I control which watchlists notify me, on which severity transitions, via which channels\

## 2. Business Context & Value

The existing subscription model stores adapter preferences but never enforces them. This story redesigns subscriptions into two scopes (personal + org-shared), adds dedicated management pages, and introduces the quick-subscribe modal on watchlists — completing the notification configuration loop.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Personal subscription settings page
    * Given an authenticated user navigating to profile → "Subscriptions" tab
    * When the page loads
    * Then they see a searchable, paginated list of their personal subscriptions showing watchlist name, severities, and adapter badges
* [ ] **Scenario 2**: Add personal subscription from settings page
    * Given a user on the Subscriptions settings page
    * When they click "Add Subscription" and search for a watchlist
    * Then they can select a watchlist, choose target severities, select adapters (from their linked configs), and save
* [ ] **Scenario 3**: Quick subscribe modal on watchlist
    * Given a user viewing a watchlist detail page
    * When they click the "Subscribe" button
    * Then a modal opens with: severity checkboxes (CRITICAL, WARNING, etc.) + adapter multi-select (from their linked adapters)
* [ ] **Scenario 4**: Quick subscribe — no adapters linked
    * Given a user with zero adapter configs
    * When they open the quick subscribe modal
    * Then IN_APP is pre-selected (always available) + a CTA "Link more adapters in Settings" is shown
* [ ] **Scenario 5**: Org-shared subscription management
    * Given an org owner/admin on org settings → "Subscriptions" tab
    * When they create a subscription for an org watchlist
    * Then it fires to the org's adapter configs AND always creates in-app notifications for all org members
* [ ] **Scenario 6**: Org-shared subscription disclaimer
    * Given an org admin creating an org-shared subscription
    * When the form is displayed
    * Then a disclaimer states: "This subscription will notify ALL organization members via in-app + selected org adapters"
* [ ] **Scenario 7**: Quick subscribe button — personal only
    * Given a user clicking Subscribe on a watchlist belonging to their org
    * When the modal opens
    * Then it creates a personal subscription only + shows note: "For org-wide notifications, configure in Org Settings"
* [ ] **Scenario 8**: Edit existing subscription
    * Given a user with an existing subscription
    * When they edit it (change severities or adapters)
    * Then the subscription is updated and future notifications reflect the new config
* [ ] **Scenario 9**: Remove subscription
    * Given a user with an existing subscription
    * When they click remove and confirm
    * Then the subscription is deleted and no further notifications fire for that watchlist
* [ ] **Scenario 10**: Subscription stores selected adapter config IDs
    * Given a user subscribing with adapters [Mattermost "Prod Alerts", Email]
    * When the subscription is saved
    * Then the subscription references specific `adapter_config` IDs (not just type strings)
* [ ] **Edge Case**: Deleted adapter config referenced by subscription
    * Given a subscription referencing adapter_config ID X
    * When adapter config X is deleted
    * Then the subscription still fires to remaining adapters; orphaned reference is cleaned up on next edit

## 4. Technical Requirements

* **API Changes**:
    - `GET /v1/subscriptions` — list user's personal subscriptions (paginated, searchable by watchlist name)
    - `POST /v1/subscriptions` — create personal subscription (body: watchlistId, targetSeverities, adapterConfigIds)
    - `PUT /v1/subscriptions/{id}` — update subscription
    - `DELETE /v1/subscriptions/{id}` — remove subscription
    - `GET /v1/organizations/{orgId}/subscriptions` — list org-shared subscriptions
    - `POST /v1/organizations/{orgId}/subscriptions` — create org-shared subscription
    - `PUT /v1/organizations/{orgId}/subscriptions/{id}` — update org-shared
    - `DELETE /v1/organizations/{orgId}/subscriptions/{id}` — remove org-shared
* **Database**:
    - Alter `subscription` table:
      - Add `organization_id UUID NULL REFERENCES organization(id)` — null = personal, set = org-shared
      - Change `adapters` jsonb from type strings to adapter_config UUIDs: `adapter_config_ids UUID[]` or keep jsonb with IDs
      - Add index on `organization_id`
    - Migration: existing subscriptions get `organization_id = NULL` (all personal)
* **Security**:
    - Personal subscriptions: user can only manage their own
    - Org subscriptions: OWNER/ADMIN only
    - Cannot subscribe to watchlists of suspended orgs (enforced server-side)
* **Performance**:
    - Subscription search: index on `(user_id)` and `(organization_id)` with watchlist join for name search

## 5. Design & UI/UX

- **Profile → Subscriptions tab**: Table/list with columns: Watchlist, Severities (badges), Adapters (icons), Actions (edit/delete)
- **Org Settings → Subscriptions tab**: Same layout, org context, disclaimer banner at top
- **Quick Subscribe Modal**: Compact modal — severity checkboxes in a row, adapter multi-select with config labels, Save/Cancel
- **Search**: Autocomplete watchlist search when adding from settings page (excludes suspended org watchlists)
- **Empty state**: "No subscriptions yet. Subscribe to a watchlist to receive alerts."

## 6. Implementation Notes

- Extend existing `Subscription` entity: add `organization` field, change `adapters` from `List<String>` to `List<UUID>` referencing adapter_config IDs
- `SubscriptionService`: split into personal vs org methods, add search/pagination via jFrame patterns
- `SeverityTransitionJob`: update dispatch logic — resolve adapter_config IDs → load configs → pass to dispatch with adapter identifiers
- Org-shared subscription dispatch: resolve all org members → dispatch IN_APP to all + dispatch to org adapter configs
- Frontend: new routes `/profile/subscriptions`, `/org/[orgId]/settings/subscriptions`
- Quick subscribe modal: new component mounted on watchlist detail page, calls `POST /v1/subscriptions`
- Watchlist detail page: add Subscribe/Unsubscribe toggle button

## 7. Test Impact Analysis

### Existing tests affected by this change:

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `SubscriptionServiceTest` | subscribe/unsubscribe | Current upsert logic | YES | Update for new fields (org_id, adapter_config_ids) |
| `SeverityTransitionJobTest` | dispatch per subscription | Dispatches to all adapters | YES | Update to resolve adapter configs from subscription |
| `SubscriptionControllerTest` | CRUD endpoints | Current request/response shape | YES | Update for new API contract |

### Test modification policy:

- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified: SubscriptionService tests, SeverityTransitionJob tests, SubscriptionController tests

### Potential files impacted by implementation

| File | Change |
|------|--------|
| `subscription/model/Subscription.java` | Add organization field, change adapters to config IDs |
| `subscription/service/SubscriptionService.java` | Personal + org methods, search/pagination |
| `subscription/controller/SubscriptionController.java` | New endpoints, org endpoints |
| `subscription/job/SeverityTransitionJob.java` | Resolve adapter configs before dispatch |
| Liquibase changelog | Alter subscription table |
| New frontend: subscription settings pages + quick subscribe modal | Full UI implementation |
