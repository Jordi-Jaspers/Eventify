---
epic: "ORGMGMT"
title: "Block Watchlist Notifications for Suspended Organizations"
estimate: S
status: ready
created: 2026-05-22
depends_on: []
labels: [backend, security]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform operator\
**I want** watchlist notifications to be silently blocked when an organization is suspended\
**So that** suspended org members don't receive misleading alerts for orgs they can't access\

## 2. Business Context & Value
The `SeverityTransitionJob` dispatches notifications every 60s without checking org status. Members of suspended orgs currently receive watchlist severity alerts even though the 403 filter blocks all access. This creates confusion and undermines the suspension mechanism.

## 3. Acceptance Criteria
* [ ] **Notifications blocked for suspended orgs**
    * Given an organization with status `SUSPENDED`
    * When `SeverityTransitionJob` finds severity transitions on channels belonging to that org
    * Then no notifications are dispatched for those channels
* [ ] **Notifications resume on reactivation**
    * Given an organization that was `SUSPENDED` and is now `ACTIVE`
    * When `SeverityTransitionJob` runs and finds severity transitions
    * Then notifications are dispatched normally
* [ ] **No catch-up notifications**
    * Given an organization that was suspended for 3 days and just reactivated
    * When the job runs after reactivation
    * Then only NEW transitions (after reactivation) trigger notifications — no backfill
* [ ] **Silent skip — no error logging**
    * Given a suspended org with active subscriptions
    * When the job skips notifications
    * Then it logs at DEBUG level (not WARN/ERROR)

## 4. Technical Requirements
* **API Changes**: N/A — no new endpoints
* **Database**: N/A — no schema changes. Org status already exists.
* **Security**: Enforces suspension semantics at notification layer (complements HTTP filter)
* **Performance**: Add org status to the existing query or filter in-memory. Must not add N+1 queries — batch lookup org statuses for all affected channels.

## 5. Design & UI/UX
N/A — backend-only change, no UI impact.

## 6. Implementation Notes
* **File to modify**: `server/src/main/java/io/github/eventify/api/subscription/job/SeverityTransitionJob.java`
* **Pattern**: The job already queries channels → watchlists → subscriptions. Add org status check early in the pipeline (after finding channels with transitions, filter out those belonging to suspended orgs).
* **Lookup**: Use existing `OrganizationRepository` or join on org status in the channel query.
* **Avoid**: Don't check per-subscription — check per-org once and filter the channel set.

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/src/main/java/io/github/eventify/api/subscription/job/SeverityTransitionJob.java` | Add org status filter before dispatching notifications |
