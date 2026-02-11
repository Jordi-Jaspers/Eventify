# Channel Search Status Filter NPE

**Epic**: Bug Fixes
**Status**: Ready for Dev
**Estimate**: XS
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** user or organization admin
**I want** to filter channels by status in the search endpoint
**So that** I can quickly find active, paused, or pending deletion channels

## 2. Business Context & Value
Currently, attempting to filter channels by `status` in either the organization channel search (`/api/v1/org/{orgId}/channels`) or user channel search (`/api/v1/user/channels`) throws a NullPointerException:

```
Cannot invoke "java.lang.Class.getEnumConstants()" because "this.enumClass" is null
```

This prevents users from filtering their channel lists by status, forcing them to manually scan through potentially large lists of channels.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Filter organization channels by ACTIVE status
    * Given an organization with channels in various statuses (ACTIVE, PAUSED, PENDING_DELETION)
    * When I search with filter `status:ACTIVE`
    * Then only channels with status ACTIVE are returned

* [ ] **Scenario 2**: Filter organization channels by PAUSED status
    * Given an organization with channels in various statuses
    * When I search with filter `status:PAUSED`
    * Then only channels with status PAUSED are returned

* [ ] **Scenario 3**: Filter user channels by status
    * Given a user with access to channels in various statuses
    * When I search my channels with filter `status:ACTIVE`
    * Then only my channels with status ACTIVE are returned

* [ ] **Scenario 4**: No NPE on status filter
    * Given any channel search endpoint
    * When I apply a status filter
    * Then no NullPointerException is thrown

## 4. Technical Requirements
* **API Changes**: None - existing endpoints, fixing broken functionality
* **Database**: None
* **Security**: N/A
* **Performance**: N/A

### Root Cause
In `ChannelMetaData.java` line 49, the `addField()` call for STATUS uses `SearchType.ENUM` but does not provide the enum class:

```java
// BROKEN (current):
addField(STATUS, STATUS, SearchType.ENUM, true);

// FIXED (should be):
addField(STATUS, STATUS, SearchType.ENUM, ChannelStatus.class, true);
```

The `SearchType.ENUM` requires the enum class to call `getEnumConstants()` for validation.

### The Fix
Change line 49 in `server/src/main/java/io/github/eventify/api/channel/model/ChannelMetaData.java`:

```java
addField(STATUS, STATUS, SearchType.ENUM, ChannelStatus.class, true);
```

## 5. Design & UI/UX
N/A - Backend bug fix only

## 6. Implementation Notes / Research

### TDD Approach (Required)
Per backlog requirements, this must be solved using TDD:

1. **Write failing tests FIRST** in:
   - `server/src/test/java/io/github/eventify/api/channel/controller/SearchOrgChannelControllerTest.java`
   - `server/src/test/java/io/github/eventify/api/channel/controller/UserChannelControllerTest.java`

2. **Test pattern to follow** (from existing tests in `SearchOrgChannelControllerTest`):
   ```java
   @Test
   void searchByStatus_shouldReturnOnlyMatchingChannels() {
       // Given: Create channels with different statuses
       // When: Search with status:ACTIVE filter
       // Then: Only ACTIVE channels returned
   }
   ```

3. **Run tests** - they should fail with NPE

4. **Apply the one-line fix** to `ChannelMetaData.java`

5. **Run tests again** - they should pass

### Reference Implementation
Other MetaData classes correctly pass enum class:
- `OrganizationMetaData.java`: `addField(STATUS, STATUS, SearchType.MULTI_ENUM, OrganizationStatus.class, true);`
- `AdminApiKeyMetaData.java`: `addField(SCOPE, SCOPE, SearchType.MULTI_ENUM, ApiKeyScope.class, true);`

### Files to Modify
| File | Change |
|------|--------|
| `ChannelMetaData.java` | Add `ChannelStatus.class` parameter to line 49 |
| `SearchOrgChannelControllerTest.java` | Add status filter test |
| `UserChannelControllerTest.java` | Add status filter test |
