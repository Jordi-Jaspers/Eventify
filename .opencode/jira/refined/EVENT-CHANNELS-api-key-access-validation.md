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
Security is critical for a multi-tenant event platform. API keys must only be able to write to channels within their ownership scope:
- A **personal API key** (organization_id IS NULL) can only access **personal channels** owned by the same user
- An **organization API key** (organization_id IS NOT NULL) can only access **organization channels** belonging to the same organization

Even if a user is a member of an organization, they cannot use their personal API key to send events to organization channels. They must use an organization API key for that.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Personal API key accesses user's own channel - allowed
    *   Given I have a personal API key (organization_id IS NULL)
    *   And I own a personal channel with ID 123 (my user_id, organization_id IS NULL)
    *   When I send an event with channelId: 123
    *   Then the request is accepted

*   [ ] **Scenario 2**: Personal API key accesses another user's channel - denied
    *   Given I have a personal API key
    *   And channel ID 456 is a personal channel belonging to another user
    *   When I send an event with channelId: 456
    *   Then I receive a 403 Forbidden error

*   [ ] **Scenario 3**: Personal API key accesses org channel - denied
    *   Given I have a personal API key (even if I'm a member of the org)
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

*   [ ] **Scenario 6**: Org API key accesses personal channel - denied
    *   Given I have an organization API key for Org A
    *   And channel ID 300 is a personal channel (organization_id IS NULL)
    *   When I send an event with channelId: 300
    *   Then I receive a 403 Forbidden error

*   [ ] **Scenario 7**: Channel does not exist - 404
    *   Given any valid API key
    *   When I send an event with a non-existent channelId
    *   Then I receive a 404 Not Found error

*   [ ] **Scenario 8**: Channel is paused - rejected
    *   Given a valid API key and a paused channel
    *   When I send an event to that channel
    *   Then I receive a 422 Unprocessable Entity with message "Channel is paused"

*   [ ] **Scenario 9**: Channel is pending deletion - rejected
    *   Given a valid API key and a channel with status PENDING_DELETION
    *   When I send an event to that channel
    *   Then I receive a 404 Not Found (treat as if it doesn't exist)

## 4. Technical Requirements
*   **New Service Method**: `ChannelAccessService.validateAccess(ApiKey apiKey, Long channelId)`
*   **Validation Logic**:
    ```
    1. Fetch channel by ID
       - If not found OR status == PENDING_DELETION → throw 404
    2. If channel.status == PAUSED → throw 422 "Channel is paused"
    3. Determine channel ownership type:
       - Personal channel: channel.organizationId IS NULL
       - Org channel: channel.organizationId IS NOT NULL
    4. Determine API key ownership type:
       - Personal key: apiKey.organizationId IS NULL
       - Org key: apiKey.organizationId IS NOT NULL
    5. Validate matching ownership:
       - If personal key + personal channel:
         → apiKey.userId must equal channel.userId
       - If org key + org channel:
         → apiKey.organizationId must equal channel.organizationId
       - Any other combination → throw 403
    6. If validation fails → throw 403 Forbidden
    ```
*   **Integration Point**: This validation will be called by the Event Ingestion endpoint (future story)
*   **Caching Consideration**: Channel lookups may benefit from caching for high-throughput scenarios

## 5. Design & UI/UX
N/A - Backend security infrastructure

## 6. Implementation Notes / Research
*   **ApiKey model**: Has `user` (always set) and `organization` (null for personal keys)
*   **Channel model**: Has `userId` (always set) and `organizationId` (null for personal channels)
*   **Ownership matching**: Both entities follow the same pattern - compare the nullable organization fields
*   **Error responses**: Use existing exception handling patterns (`JFrameException` subtypes)
*   **Testing**: Comprehensive unit tests for all 6 access scenarios (2 allowed, 4 denied)
*   **Future**: This service will be used by `POST /v1/events` endpoint
*   **Dependency**: This story can be implemented and unit tested, but full integration testing requires Event Ingestion story
