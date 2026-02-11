# Channel Search Status Filter NPE Fix

**Completed:** 2026-02-11
**Story:** `.opencode/refined/BUG-01-channel-search-status-filter-npe.md`

## Summary

Fixed NullPointerException when filtering channels by status in search endpoints. The bug prevented users from filtering channel lists by ACTIVE, PAUSED, or PENDING_DELETION status.

## Root Cause

In `ChannelMetaData.java` line 49, the `addField()` call for STATUS used `SearchType.ENUM` but omitted the enum class parameter:

```java
// BROKEN:
addField(STATUS, STATUS, SearchType.ENUM, true);

// FIXED:
addField(STATUS, STATUS, SearchType.ENUM, ChannelStatus.class, true);
```

The `SearchType.ENUM` requires the enum class to call `getEnumConstants()` for validation, causing NPE when absent.

## Agents Used

| Agent | Task |
|-------|------|
| spring-testing-agent | Added 3 failing tests for status filter |
| orchestrator | Applied one-line fix |

## Files Modified

- `server/src/main/java/io/github/eventify/api/channel/model/ChannelMetaData.java` - Added `ChannelStatus.class` parameter
- `server/src/test/java/io/github/eventify/api/channel/controller/SearchOrgChannelControllerTest.java` - Added 2 status filter tests
- `server/src/test/java/io/github/eventify/api/channel/controller/UserChannelControllerTest.java` - Added 1 status filter test

## Tests

- 3 tests written
- 3 tests passing
- All existing channel tests still passing

## Notes

- XS effort bug fix (one-line change)
- TDD approach: tests written first, confirmed NPE failure, then fix applied
- Similar pattern exists in other MetaData classes (OrganizationMetaData, AdminApiKeyMetaData) for reference
