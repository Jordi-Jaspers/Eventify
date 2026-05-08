# Fix Monthly Quota Incorrectly Applied to Organization Channels

**Completed:** 2026-02-13
**Story:** `.opencode/refined/BUG-02-organization-quota-bypass.md`

## Summary

Fixed bug where organization API keys incorrectly enforced personal monthly quota (1000 events). Organization channels now have unlimited usage as intended.

## Approved Plan

### Requirements Summary
- Personal API keys: Enforce 1000 events/month quota
- Organization API keys: Bypass quota entirely (unlimited)

### Technical Approach
- Backend only - 2 locations in `EventIngestionController.java`
- Add conditional check: only enforce quota if `principal.isUserKey()`

### Execution Order
| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Update/add tests for correct behavior |
| 2 | spring-backend-agent | Fix controller to pass tests |

## Implementation

### Backend
- Modified `ingestEvent()` and `ingestBatch()` methods in `EventIngestionController`
- Used existing `ApiKeyPrincipal.isUserKey()` method for cleaner conditional
- Organization API keys (`isOrganizationKey() == true`) now skip quota check entirely

### Tests Updated
1. **`ingestEventWithOrgKeyDoesNotCountAgainstUserQuota`** - Renamed from incorrect test, verifies quota NOT incremented
2. **`ingestEventWithOrgKeySucceedsWhenUserQuotaExhausted`** - Renamed, verifies org key succeeds when personal quota exhausted
3. **`ingestBatchWithOrgKeyBypassesQuota`** - NEW, verifies batch events also bypass quota

### Deviations from Plan
- Used `principal.isUserKey()` instead of `principal.getOrganizationId() == null` for better readability (existing API)
- Fixed flaky test: used `TimeProvider.now()` instead of `OffsetDateTime.now()` for PostgreSQL microsecond precision

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Update tests for correct behavior | ✅ Complete |
| spring-backend-agent | Fix controller logic | ✅ Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/event/controller/EventIngestionController.java` - Added conditional quota bypass for org API keys
- `server/src/test/java/io/github/eventify/api/event/controller/EventIngestionControllerTest.java` - Updated 2 tests, added 1 new batch test, added TimeProvider import

## Tests

- 3 tests updated/added for quota bypass
- 25 total EventIngestionControllerTest tests passing
- Full `./gradlew check` passes

## Notes

- Used `principal.isUserKey()` (checks `ApiKeyScope.USER` enum) rather than null-checking `organizationId` - more semantic and robust
- Test uses `TimeProvider.now()` for microsecond precision compatibility with PostgreSQL TIMESTAMPTZ
