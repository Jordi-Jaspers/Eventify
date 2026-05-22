# Block Watchlist Notifications for Suspended Organizations

**Completed:** 2026-05-22
**Epic:** ORGMGMT
**Source:** .opencode/refined/ORGMGMT-01-suspended-org-notification-blocking.md

## Summary

SeverityTransitionJob now filters out channels belonging to SUSPENDED organizations before dispatching watchlist notifications. Batch org status lookup prevents N+1 queries.

## Plan Approved by the user:

### Requirements Summary

- Skip notification dispatch for channels belonging to SUSPENDED orgs
- Personal channels (null org) continue to be processed normally
- Batch lookup org statuses (single `findAllById` call)
- Log skipped channels at DEBUG level

### Technical Approach

- Backend: Add `OrganizationRepository` to `SeverityTransitionJob`, batch-collect org IDs, filter before processing
- Frontend: N/A
- Database: No migrations needed

### Execution Order

| Phase | Agent                 | Task                          |
|-------|-----------------------|-------------------------------|
| 2.1   | spring-testing-agent  | Write 7 new unit tests        |
| 2.2   | spring-backend-agent  | Implement org status filtering |

## Implementation

### Backend

- `SeverityTransitionJob`: Added `OrganizationRepository` field, batch org ID collection from channels, `findAllById` call, `isChannelAllowed` filter method, DEBUG logging for skipped channels
- No new endpoints, no migrations

### Deviations from Plan

- None

## Agents Used

| Agent                | Task                           | Result   |
|----------------------|--------------------------------|----------|
| deep-research-agent  | Research job + test infra      | Complete |
| spring-testing-agent | Write org-filtering tests      | Complete |
| spring-backend-agent | Implement filtering logic      | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/subscription/job/SeverityTransitionJob.java` - Added org status filtering
- `server/src/test/java/io/github/eventify/api/subscription/job/SeverityTransitionJobTest.java` - 7 new tests

## Tests

- 15 tests total (8 existing + 7 new), all passing
