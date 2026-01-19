# Batch Event Ingestion API

**Epic**: Event Ingestion
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-19

## 1. User Story
**As a** system syncing historical or offline events
**I want** to send multiple events with client-provided timestamps in a single request
**So that** I can efficiently upload batches of events that occurred while offline or from log files

## 2. Business Context & Value
Some use cases require uploading events that occurred in the past:
- Offline IoT devices syncing when they regain connectivity
- Log file ingestion from external systems
- Historical data import during onboarding

This endpoint accepts client-provided timestamps and validates them against retention policies.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Successfully ingest batch of events
    *   Given a valid API key with access to channel 123
    *   When I POST to `/v1/events/batch` with 3 valid events
    *   Then I receive 201 Created with `{ "accepted": 3, "rejected": 0, "events": [...] }`
    *   And all 3 events are stored with client-provided timestamps

*   [ ] **Scenario 2**: Reject entire batch if any event has future timestamp
    *   Given a valid API key
    *   When I POST batch with one event having `"timestamp": "2030-01-01T00:00:00Z"`
    *   Then I receive 400 Bad Request
    *   And error indicates which event failed: `{ "errors": [{ "index": 1, "field": "timestamp", "message": "Timestamp cannot be in the future" }] }`
    *   And no events are stored

*   [ ] **Scenario 3**: Reject entire batch if any event exceeds retention period
    *   Given a valid API key for a user with 90-day retention
    *   When I POST batch with one event having timestamp from 100 days ago
    *   Then I receive 400 Bad Request with error "Timestamp exceeds retention period"
    *   And no events are stored

*   [ ] **Scenario 4**: Reject batch if any channel is inaccessible
    *   Given a USER API key for user A
    *   When I POST batch with one event targeting a channel owned by user B
    *   Then I receive 403 Forbidden
    *   And no events are stored

*   [ ] **Scenario 5**: Reject batch exceeding max size (100 events)
    *   Given a valid API key
    *   When I POST batch with 101 events
    *   Then I receive 400 Bad Request with error "Batch size exceeds maximum of 100 events"

*   [ ] **Scenario 6**: Require timestamp for all events
    *   Given a valid API key
    *   When I POST batch with one event missing `timestamp`
    *   Then I receive 400 Bad Request with error "timestamp is required for batch ingestion"

*   [ ] **Scenario 7**: Handle empty batch
    *   Given a valid API key
    *   When I POST to `/v1/events/batch` with `{ "events": [] }`
    *   Then I receive 400 Bad Request with error "At least one event is required"

*   [ ] **Scenario 8**: Events across multiple channels (same owner)
    *   Given a valid API key with access to channels 123 and 456
    *   When I POST batch with events targeting both channels
    *   Then I receive 201 Created and all events are stored

## 4. Technical Requirements
*   **API Endpoint**:
    *   `POST /v1/events/batch`
    *   Authentication: API Key only
    *   Content-Type: `application/json`

*   **Request Schema**:
    ```json
    {
      "events": [
        {
          "channelId": 123,
          "severity": "WARNING",
          "title": "High memory",
          "message": "Memory at 85%",
          "metadata": { "host": "server-01" },
          "timestamp": "2026-01-18T03:00:00Z"  // Required
        },
        {
          "channelId": 123,
          "severity": "CRITICAL",
          "title": "OOM killed",
          "timestamp": "2026-01-18T03:05:00Z"
        }
      ]
    }
    ```

*   **Response Schema (201 Created)**:
    ```json
    {
      "accepted": 2,
      "rejected": 0,
      "events": [
        { "index": 0, "id": 124, "timestamp": "2026-01-18T03:00:00Z" },
        { "index": 1, "id": 125, "timestamp": "2026-01-18T03:05:00Z" }
      ]
    }
    ```

*   **Error Response (400 Bad Request)**:
    ```json
    {
      "code": "ERR-0053",
      "message": "Batch validation failed",
      "errors": [
        { "index": 1, "field": "timestamp", "message": "Timestamp cannot be in the future" }
      ]
    }
    ```

*   **Validation (per event)**:
    *   Same as real-time endpoint, plus:
    *   `timestamp`: Required, not in future, not older than owner's `retention_days`

*   **Batch-level validation**:
    *   Max 100 events per batch
    *   All channels must be accessible by the API key
    *   All-or-nothing: if any event fails, reject entire batch

*   **Database**:
    *   Use single transaction for all inserts
    *   Sort events by timestamp before insert (better hypertable chunk locality)

*   **Security**:
    *   Same as real-time endpoint
    *   Validate channel access for ALL unique channel IDs in batch before inserting any

*   **Performance**:
    *   Target: < 500ms for 100-event batch
    *   Batch insert using `saveAll()` or native batch SQL

## 5. Design & UI/UX
N/A - API endpoint only

## 6. Implementation Notes / Research
*   **Controller**: Add `ingestBatch()` method to `EventIngestionController.java`
*   **Request DTO**: `CreateBatchEventRequest.java` containing `List<BatchEventItem>`
*   **Batch item DTO**: `BatchEventItem.java` - same as `CreateEventRequest` but with required `timestamp`
*   **Response DTO**: `BatchEventCreatedResponse.java`
*   **Timestamp validation**: 
    *   Look up owner's `retention_days` (from User for personal channel, Organization for org channel)
    *   `timestamp > NOW()` -> reject
    *   `timestamp < NOW() - retention_days` -> reject
*   **Transaction**: Use `@Transactional` to ensure all-or-nothing semantics
*   **Batch insert**: Consider `JdbcTemplate.batchUpdate()` for better performance than JPA `saveAll()`

## 7. Out of Scope
*   Partial success (accepting some events, rejecting others) - future enhancement
*   Async processing with 202 Accepted response
*   Quota enforcement (EVENT-004)
