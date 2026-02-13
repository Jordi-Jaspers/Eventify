# Fix Monthly Quota Incorrectly Applied to Organization Channels

**Epic**: Bugs & Technical Debt
**Status**: Ready for Dev
**Estimate**: XS
**Created Date**: 2026-02-13
**Depends On**: None

## 1. User Story
**As a** user with an organization API key
**I want** to send unlimited events to organization channels
**So that** my organization's event ingestion is not limited by personal quota restrictions

## 2. Business Context & Value
Currently, ALL events count against the user's personal monthly quota (1000 events), regardless of whether they're sent to a personal channel or an organization channel.

**Business Rule**: 
- Personal channels: 1000 events/month limit per user
- Organization channels: **No limit** (organizations pay for unlimited usage)

**Impact**: Users on TST environment reported that their organization API keys are being blocked after hitting the personal quota limit, even though organization channels should be unlimited.

**Note**: Future TRIAL organization limitations will be handled in a separate story ("TRIAL account limitations").

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Organization API key bypasses quota
    * Given a user has an organization API key
    * And the user has reached their personal quota (1000/1000 events)
    * When they send an event using the org API key to an org channel
    * Then the event is accepted (201 Created)
    * And no quota is checked or incremented

* [ ] **Scenario 2**: Personal API key still enforces quota
    * Given a user has a personal API key
    * And the user has reached their personal quota (1000/1000 events)
    * When they send an event using the personal API key
    * Then the event is rejected with 429 "Monthly event quota exceeded"

* [ ] **Scenario 3**: Batch events with org API key bypass quota
    * Given a user has an organization API key
    * And the user has 999/1000 personal quota used
    * When they send a batch of 100 events using the org API key
    * Then all 100 events are accepted (201 Created)
    * And the personal quota remains at 999 (unchanged)

* [ ] **Scenario 4**: Personal quota still tracks personal events
    * Given a user with 0/1000 quota used
    * When they send 5 events using their personal API key
    * Then the events are accepted
    * And the personal quota is incremented to 5/1000

## 4. Technical Requirements

### Backend Changes

**Modified Files**:
| File | Change |
|------|--------|
| `server/src/main/java/io/github/eventify/api/event/controller/EventIngestionController.java` | Conditionally skip quota check for org API keys |

### Implementation

**Current Code** (`EventIngestionController.java:64`):
```java
userQuotaService.checkAndIncrementOrThrow(principal.getUserId(), 1);
```

**Updated Code**:
```java
// Only enforce quota for personal API keys (not organization API keys)
if (principal.getOrganizationId() == null) {
    userQuotaService.checkAndIncrementOrThrow(principal.getUserId(), 1);
}
```

**Same change for batch endpoint** (`EventIngestionController.java:83`):
```java
// Only enforce quota for personal API keys
if (principal.getOrganizationId() == null) {
    userQuotaService.checkAndIncrementOrThrow(principal.getUserId(), request.getEvents().size());
}
```

### API Changes
None - same endpoints, same request/response format.

### Database
No changes required.

### Security
No changes - authorization still handled by `@PreAuthorize` annotations.

## 5. Design & UI/UX
N/A - Backend-only change.

## 6. Implementation Notes / Research

### Existing Code References
- **Controller**: `server/src/main/java/io/github/eventify/api/event/controller/EventIngestionController.java`
- **ApiKeyPrincipal**: Has `getOrganizationId()` method - returns `null` for personal API keys
- **Quota Service**: `server/src/main/java/io/github/eventify/api/quota/service/UserQuotaService.java`

### Key Point: ApiKeyPrincipal Already Has What We Need
The `ApiKeyPrincipal` already distinguishes between personal and org API keys:
- `principal.getOrganizationId() == null` → Personal API key
- `principal.getOrganizationId() != null` → Organization API key

### Test Updates Required
Existing test `EventIngestionControllerTest.java` has tests for quota. Need to add/modify:
- Test: Org API key events do NOT count against user quota
- Test: Org API key events succeed even when user quota is exhausted
- Update existing test "Should count org API key events against user quota" (line 733) - this test asserts incorrect behavior

### Future Considerations (not in scope)
- Organization-level quota tracking for analytics (add to backlog)
- TRIAL organization limitations (separate story exists)
