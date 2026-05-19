# Organization Status Change Notifications

**Completed:** 2026-05-19
**Epic:** ORGMGMT
**Source:** `.opencode/refined/ORGMGMT-02-org-status-change-notifications.md`

## Summary

Notify all organization members when org is suspended (urgent) or reactivated (non-urgent). No notification for idempotent transitions.

## Implementation

### Backend

- `OrganizationService.updateStatus()` captures oldStatus before mutation, delegates to `NotificationDispatchService.dispatchOrganizationStatusChange()`
- `NotificationDispatchService` routes to private `dispatchOrganizationSuspended()` / `dispatchOrganizationReactivated()` methods
- Audience: `NotificationAudience.organization(orgId)` — all org members
- Category: SYSTEM, actionUrl: `/organizations`, actionLabel: "View organizations"

## Files Modified

- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java` — capture oldStatus, call dispatch service
- `server/src/main/java/io/github/eventify/api/notification/service/NotificationDispatchService.java` — new dispatch methods
- `server/src/test/java/io/github/eventify/api/organization/service/OrganizationServiceTest.java` — 6 new tests
- `server/src/test/java/io/github/eventify/api/notification/service/NotificationDispatchServiceTest.java` — 4 new tests

## Tests

- 10 tests written, all passing
