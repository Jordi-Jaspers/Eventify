# Channel Access Validation via API Key

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-11

## 1. User Story
**As an** API user sending events
**I want** the system to validate that my API key can access the specified channel
**So that** I cannot accidentally or maliciously send events to channels I don't own

## 2. Business Context & Value
Security is critical for a multi-tenant event platform. API keys must only be able to write to channels within their scope. A personal API key should only access that user's channels. An organization API key should only access that organization's channels. This prevents data leakage and unauthorized access.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Personal API key accesses user's channel - allowed
    *   Given I have a personal API key (scope: USER)
    *   And I own a channel with ID 123
    *   When I send an event with channelId: 123
    *   Then the request is accepted

*   [ ] **Scenario 2**: Personal API key accesses another user's channel - denied
    *   Given I have a personal API key
    *   And channel ID 456 belongs to another user
    *   When I send an event with channelId: 456
    *   Then I receive a 403 Forbidden error

*   [ ] **Scenario 3**: Personal API key accesses org channel - denied
    *   Given I have a personal API key (even if I'm a member of an org)
    *   And channel ID 789 belongs to an organization
    *   When I send an event with channelId: 789
    *   Then I receive a 403 Forbidden error

*   [ ] **Scenario 4**: Org API key accesses org's channel - allowed
    *   Given I have an organization API key for Org A
    *   And channel ID 100 belongs to Org A
    *   When I send an event with channelId: 100
    *   Then the request is accepted

*   [ ] **Scenario 5**: Org API key accesses different org's channel - denied
    *   Given I have an organization API key for Org A
    *   And channel ID 200 belongs to Org B
    *   When I send an event with channelId: 200
    *   Then I receive a 403 Forbidden error

*   [ ] **Scenario 6**: Channel does not exist - 404
    *   Given any valid API key
    *   When I send an event with a non-existent channelId
    *   Then I receive a 404 Not Found error

*   [ ] **Scenario 7**: Channel is paused - rejected
    *   Given a valid API key and a paused channel
    *   When I send an event to that channel
    *   Then I receive a 422 Unprocessable Entity with message "Channel is paused"

*   [ ] **Scenario 8**: Channel is pending deletion - rejected
    *   Given a valid API key and a channel with status PENDING_DELETION
    *   When I send an event to that channel
    *   Then I receive a 404 Not Found (treat as if it doesn't exist)

## 4. Technical Requirements
*   **New Service Method**: `ChannelAccessService.validateAccess(ApiKey apiKey, Long channelId)`
*   **Validation Logic**:
    ```
    1. Fetch channel by ID (throw 404 if not found or PENDING_DELETION)
    2. If channel.status == PAUSED, throw 422
    3. If apiKey.scope == USER:
       - channel.scope must be USER
       - channel.ownerId must equal apiKey.userId
    4. If apiKey.scope == ORGANIZATION:
       - channel.scope must be ORGANIZATION
       - channel.organizationId must equal apiKey.organizationId
    5. If validation fails, throw 403
    ```
*   **Integration Point**: This validation will be called by the Event Ingestion endpoint (future story)
*   **Caching Consideration**: Channel lookups may benefit from caching for high-throughput scenarios

## 5. Design & UI/UX
N/A - Backend security infrastructure

## 6. Implementation Notes / Research
*   **ApiKey model**: Already has `scope`, `user`, and `organization` fields
*   **Channel model**: Has `scope`, `ownerId`, and `organizationId` fields
*   **Error responses**: Use existing exception handling patterns (`JFrameException` subtypes)
*   **Testing**: Comprehensive unit tests for all access scenarios
*   **Future**: This service will be used by `POST /v1/events` endpoint
