# Real-time Event Ingestion API

**Epic**: Event Ingestion
**Status**: Ready for Dev
**Estimate**: L
**Created Date**: 2026-01-19

## 1. User Story
**As a** system/application sending events
**I want** to send real-time events to a channel via API
**So that** I can monitor my application's health and activity in Eventify

## 2. Business Context & Value
This is the primary ingestion endpoint for real-time monitoring. External systems (applications, IoT devices, CI/CD pipelines) will call this endpoint to log events as they happen. The server assigns the timestamp to ensure accurate, monotonic ordering in the timeline.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Successfully ingest event with all fields
    *   Given a valid API key with access to channel 123
    *   When I POST to `/v1/events` with `{ "channelId": 123, "severity": "CRITICAL", "title": "Error", "message": "Details", "metadata": {"key": "value"} }`
    *   Then I receive 201 Created with `{ "id": 12345, "timestamp": "2026-01-19T10:30:00.123Z" }`
    *   And the event is stored in the database with server-assigned timestamp

*   [ ] **Scenario 2**: Successfully ingest event with minimal fields
    *   Given a valid API key with access to channel 123
    *   When I POST to `/v1/events` with `{ "channelId": 123, "severity": "OK", "title": "Health check passed" }`
    *   Then I receive 201 Created with event ID and timestamp
    *   And `message` and `metadata` are stored as NULL

*   [ ] **Scenario 3**: Reject request with missing required fields
    *   Given a valid API key
    *   When I POST to `/v1/events` with `{ "channelId": 123 }` (missing severity and title)
    *   Then I receive 400 Bad Request with validation errors

*   [ ] **Scenario 4**: Reject request with invalid severity
    *   Given a valid API key
    *   When I POST to `/v1/events` with `{ "channelId": 123, "severity": "INVALID", "title": "Test" }`
    *   Then I receive 400 Bad Request with error "Invalid severity. Must be one of: OK, WARNING, CRITICAL"

*   [ ] **Scenario 5**: Reject request for non-existent channel
    *   Given a valid API key
    *   When I POST to `/v1/events` with `{ "channelId": 999999, "severity": "OK", "title": "Test" }`
    *   Then I receive 404 Not Found with error "Channel not found"

*   [ ] **Scenario 6**: Reject request for channel not owned by API key
    *   Given a USER API key for user A
    *   And channel 123 belongs to user B
    *   When I POST to `/v1/events` with `{ "channelId": 123, ... }`
    *   Then I receive 403 Forbidden with error "Channel access denied"

*   [ ] **Scenario 7**: Reject request for paused channel
    *   Given a valid API key
    *   And channel 123 has status PAUSED
    *   When I POST to `/v1/events` with `{ "channelId": 123, ... }`
    *   Then I receive 422 Unprocessable Entity with error "Channel is paused"

*   [ ] **Scenario 8**: Reject request without API key
    *   Given no Authorization header
    *   When I POST to `/v1/events`
    *   Then I receive 401 Unauthorized

*   [ ] **Scenario 9**: Reject request with expired API key
    *   Given an expired API key
    *   When I POST to `/v1/events`
    *   Then I receive 401 Unauthorized with error "API key expired"

*   [ ] **Scenario 10**: Ignore timestamp field if provided
    *   Given a valid API key
    *   When I POST to `/v1/events` with `{ ..., "timestamp": "2020-01-01T00:00:00Z" }`
    *   Then the event is stored with server-assigned timestamp (timestamp field is ignored)

## 4. Technical Requirements
*   **API Endpoint**:
    *   `POST /v1/events`
    *   Authentication: API Key only (Bearer token in Authorization header)
    *   Content-Type: `application/json`

*   **Request Schema**:
    ```json
    {
      "channelId": 123,           // Required, Long
      "severity": "CRITICAL",     // Required, Enum: OK, WARNING, CRITICAL
      "title": "Error occurred",  // Required, String, max 255 chars
      "message": "Details...",    // Optional, String, max 10KB
      "metadata": { ... }         // Optional, JSON object, max 10KB
    }
    ```

*   **Response Schema (201 Created)**:
    ```json
    {
      "id": 12345,
      "timestamp": "2026-01-19T10:30:00.123Z"
    }
    ```

*   **Error Responses**:
    | Status | Code | Condition |
    |--------|------|-----------|
    | 400 | ERR-0051 | Validation error (missing fields, invalid severity, payload too large) |
    | 401 | ERR-0040 | Missing or invalid API key |
    | 401 | ERR-0041 | Expired API key |
    | 403 | ERR-0048 | Channel access denied (ownership mismatch) |
    | 404 | ERR-0001 | Channel not found |
    | 422 | ERR-0049 | Channel is paused |

*   **Validation**:
    *   `channelId`: Required, must exist, must be accessible by API key (use `ChannelAccessService`)
    *   `severity`: Required, must be valid enum value
    *   `title`: Required, 1-255 characters
    *   `message`: Optional, max 10,240 bytes (10KB)
    *   `metadata`: Optional, valid JSON, max 10,240 bytes (10KB)
    *   Any `timestamp` field in request is **ignored** (server assigns)

*   **Security**:
    *   API key authentication via `ApiKeyAuthenticationFilter`
    *   Channel access validation via `ChannelAccessService.validateAccess()`
    *   Use `@PreAuthorize("@channelSecurity.canAccess(#request.channelId, principal)")` pattern

*   **Performance**:
    *   Target: < 50ms P99 latency
    *   Single synchronous DB insert

## 5. Design & UI/UX
N/A - API endpoint only

## 6. Implementation Notes / Research
*   **Controller**: `server/src/main/java/io/github/eventify/api/event/controller/EventIngestionController.java`
*   **Service**: `server/src/main/java/io/github/eventify/api/event/service/EventIngestionService.java`
*   **Request DTO**: `server/src/main/java/io/github/eventify/api/event/model/request/CreateEventRequest.java`
*   **Response DTO**: `server/src/main/java/io/github/eventify/api/event/model/response/EventCreatedResponse.java`
*   **Validator**: `server/src/main/java/io/github/eventify/api/event/model/validator/CreateEventValidator.java`
*   **Existing patterns to follow**:
    *   `ChannelAccessService` for ownership validation
    *   `ApiKeyAuthenticationFilter` for API key auth
    *   `ChannelValidator` for jframe Validator pattern
*   **Endpoint path constant**: Add `EVENTS = "/v1/events"` to `Paths.java`
*   **Note**: Quota enforcement is handled in EVENT-004 (separate story)

## 7. Out of Scope
*   Quota enforcement (EVENT-004)
*   Batch ingestion (EVENT-003)
*   WebSocket/SSE real-time push to UI (future story)
