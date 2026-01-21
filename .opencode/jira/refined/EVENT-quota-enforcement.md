# Quota Enforcement & Usage Tracking

**Epic**: Event Ingestion
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-19

## 1. User Story
**As a** platform operator
**I want** to enforce a monthly event quota of 1000 events per user
**So that** the platform remains sustainable and users upgrade for higher limits

## 2. Business Context & Value
Free-tier users have a limit of 1000 events per month. This prevents abuse, controls infrastructure costs, and creates an upgrade path for power users. The quota resets on the 1st of each month.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Allow event when under quota
    *   Given user has sent 500 events this month (quota: 1000)
    *   When user sends a new event
    *   Then event is accepted (201 Created)
    *   And `event_count` is incremented to 501

*   [ ] **Scenario 2**: Block event when quota reached
    *   Given user has sent 1000 events this month (quota: 1000)
    *   When user sends a new event
    *   Then event is rejected with 429 Too Many Requests
    *   And response body: `{ "code": "ERR-0054", "message": "Monthly event quota exceeded (1000/1000)" }`
    *   And event is NOT stored

*   [ ] **Scenario 3**: Block batch when it would exceed quota
    *   Given user has sent 995 events this month
    *   When user sends a batch of 10 events
    *   Then batch is rejected with 429 Too Many Requests
    *   And response: `{ "code": "ERR-0054", "message": "Batch would exceed monthly quota (995 + 10 > 1000)" }`
    *   And no events are stored

*   [ ] **Scenario 4**: Quota resets on 1st of month
    *   Given user reached quota (1000/1000) in January
    *   When February 1st arrives
    *   And user sends an event
    *   Then event is accepted
    *   And `event_count` is reset to 1
    *   And `period_start` is updated to February 1st

*   [ ] **Scenario 5**: Create quota record on first event
    *   Given user has never sent an event (no quota record exists)
    *   When user sends their first event
    *   Then a `user_event_quota` record is created with `event_count = 1`

*   [ ] **Scenario 6**: Organization events count against user's quota
    *   Given user A creates an org API key
    *   And user A has sent 900 events this month
    *   When user A sends 150 events via the org API key
    *   Then first 100 events succeed
    *   And event 101+ are rejected (quota exceeded)

*   [ ] **Scenario 7**: Include quota info in 429 response headers
    *   Given user has reached quota
    *   When event is rejected
    *   Then response includes headers:
        *   `X-RateLimit-Limit: 1000`
        *   `X-RateLimit-Remaining: 0`
        *   `X-RateLimit-Reset: 2026-02-01T00:00:00Z`

## 4. Technical Requirements
*   **Quota Check** (on every ingestion request):
    ```java
    UserEventQuota quota = quotaRepository.findByUserId(apiKey.getUser().getId())
        .orElseGet(() -> createNewQuota(apiKey.getUser()));
    
    // Reset if new month
    if (quota.getPeriodStart().getMonth() != OffsetDateTime.now().getMonth() 
        || quota.getPeriodStart().getYear() != OffsetDateTime.now().getYear()) {
        quota.setEventCount(0);
        quota.setPeriodStart(firstDayOfCurrentMonth());
    }
    
    // Check limit
    int eventCount = isBatch ? batchSize : 1;
    if (quota.getEventCount() + eventCount > 1000) {
        throw new QuotaExceededException(...);
    }
    
    // After successful insert
    quota.setEventCount(quota.getEventCount() + eventCount);
    quotaRepository.save(quota);
    ```

*   **Exception**: Create `QuotaExceededException` extending `ApiException`
*   **Error Code**: Add `QUOTA_EXCEEDED` (ERR-0054) to `ApiErrorCode`

*   **Response Headers** (on 429):
    ```
    X-RateLimit-Limit: 1000
    X-RateLimit-Remaining: 0
    X-RateLimit-Reset: 2026-02-01T00:00:00Z (ISO 8601)
    ```

*   **Database**: 
    *   Uses existing `user_event_quota` table
    *   No schema changes needed

*   **Concurrency**: 
    *   Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` on quota lookup to prevent race conditions
    *   Or use optimistic locking with `@Version` column

*   **Performance**:
    *   Quota check adds ~5ms to request latency
    *   Consider caching quota in Redis for high-throughput scenarios (future)

## 5. Design & UI/UX
N/A - API behavior only. Dashboard quota display is a separate story in Timeline epic.

## 6. Implementation Notes / Research
*   **Existing table**: `user_event_quota` already exists with `event_count`, `period_start`, `updated_at`
*   **Service**: Add quota logic to `EventIngestionService.java`
*   **Exception**: `server/src/main/java/io/github/eventify/common/exception/QuotaExceededException.java`
*   **Integration points**:
    *   Real-time endpoint (`POST /v1/events`) - check quota before insert
    *   Batch endpoint (`POST /v1/events/batch`) - check quota for full batch size before insert, otherwise reject entire batch.
*   **Edge case**: Batch that would partially exceed quota is fully rejected (all-or-nothing)
*   **Quota attribution**: Events sent via org API key count against the API key creator's (user's) personal quota

## 7. Out of Scope
*   Configurable quota limits per user (future story - see backlog)
*   Organization-level quotas (future - tied to billing)
*   Dashboard UI showing usage vs. limit
*   Warning notifications at 80% usage
