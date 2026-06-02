# Fix Notification Dispatch Not Firing on First Severity Transition

**Completed:** 2026-05-21
**Epic:** BUGS
**Source:** .opencode/refined/BUGS-02-notification-null-severity-fix.md

## Summary

Fixed NULL-unsafe JPQL query that excluded channels with `lastNotifiedSeverity = NULL` from severity transition detection, causing notifications to never fire for newly active channels.

## Plan Approved by the user:

### Requirements Summary

- Fix JPQL query to include channels where `lastNotifiedSeverity IS NULL` (first-ever transition)
- No regression for subsequent transitions
- No duplicate notifications

### Technical Approach

- Backend: Fix `ChannelRepository.findChannelsWithSeverityChange()` JPQL query to handle NULL

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Add repository integration tests for NULL case |
| 2 | spring-backend-agent | Fix JPQL query |

## Implementation

### Backend

- Fixed JPQL: added `(c.lastNotifiedSeverity IS NULL OR ...)` condition
- No schema changes, no new endpoints

### Deviations from Plan

- None

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Repository integration tests | 5 tests (2 fail as expected) |
| spring-backend-agent | Fix JPQL query | All 5 passing |

## Files Modified

- `server/src/main/java/io/github/eventify/api/channel/repository/ChannelRepository.java` - NULL-safe JPQL query
- `server/src/test/java/io/github/eventify/api/channel/repository/ChannelRepositorySeverityChangeTest.java` - New integration tests

## Tests

- 5 tests written, 5 passing
