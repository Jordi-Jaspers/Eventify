## [2026-01-08] - User Event Quota

### Plan (approved)
Implement monthly event quota tracking for users:
- Backend endpoint `GET /v1/user/quota` to return quota status
- Database table to track event usage per user per billing period
- Frontend integration to display real quota data in Developer Settings page
- Scheduled job to reset quotas monthly (like TokenService pattern)

### Actual Changes

**Backend:**
- Created `UserEventQuota` entity for tracking monthly event usage per user
- Created `UserEventQuotaRepository` with:
  - `findByUserId()` - Find quota by user
  - `resetAllQuotas()` - Bulk reset all quotas (for scheduled job)
- Created `UserQuotaService` with methods:
  - `getQuotaStatus(userId)` - Returns current quota status
  - `canSendEvent(userId)` - Checks if user can send more events
  - `incrementUsage(userId)` - Increments usage counter
  - `resetMonthlyQuotas()` - `@Scheduled` job that runs at midnight on 1st of every month
- Created `UserQuotaResponse` DTO with fields: used, limit, remaining, periodStart, periodEnd, percentUsed
- Created `UserQuotaController` with `GET /v1/user/quota` endpoint
- Added `USER_QUOTA_PATH` constant to `Paths.java`
- Added `QUOTA_EXCEEDED` error code to `ApiErrorCode.java`
- Created Liquibase migration for `user_event_quota` table with indexes

**Frontend:**
- Added `UserQuotaResponse` type export to `models.ts`
- Added `getQuota()` function to `UserController.ts`
- Updated Developer Settings page (`+page.svelte`) to:
  - Fetch real quota data from backend on mount
  - Display actual usage values in QuotaProgressBar
  - Handle loading and error states gracefully

**UI Validation:**
- Created Playwright test suite with 9 test cases
- Generated 9 screenshots validating:
  - Page layout with quota bar and empty state
  - Create API key sheet and success modal
  - Keys list and revoke confirmation dialog
  - All glassmorphism styling and design standards

**Testing:**
- 8 unit tests for `UserQuotaService` covering:
  - Quota status calculation
  - Scheduled monthly reset
  - Increment operations
  - Under/over quota checks
  - Percent calculation edge cases
- 4 integration tests for `UserQuotaController` covering:
  - Authenticated quota retrieval
  - Unauthorized access
  - Response format validation
  - New user zero usage

### Refactor: Scheduled Quota Reset

**Problem with initial implementation:**
- Every service method called `resetIfNewPeriod()` which checked and potentially reset quotas
- Added latency to every quota check
- Potential race conditions with concurrent requests

**Solution (following TokenService pattern):**
- Added `@Scheduled(cron = "0 0 0 1 * *")` job `resetMonthlyQuotas()`
- Runs at midnight on 1st of every month
- Bulk UPDATE resets all quotas in single transaction
- Removed `resetIfNewPeriod()` and `isCurrentPeriod()` from entity
- Removed per-request period checking from service methods

**Benefits:**
- Eliminated per-request overhead
- Removed race conditions
- Simplified service code (~30 lines removed)
- Better performance (one bulk UPDATE monthly vs. checks on every request)

### Agents Used
- **java-testing-agent**: Created TDD test suite
- **java-backend-agent**: Implemented backend feature + refactor
- **sveltekit-frontend-agent**: Wired frontend to backend API + UI validation

### Files Modified

**Backend (New):**
- `server/src/main/java/io/github/eventify/api/quota/model/UserEventQuota.java`
- `server/src/main/java/io/github/eventify/api/quota/model/response/UserQuotaResponse.java`
- `server/src/main/java/io/github/eventify/api/quota/repository/UserEventQuotaRepository.java`
- `server/src/main/java/io/github/eventify/api/quota/service/UserQuotaService.java`
- `server/src/main/java/io/github/eventify/api/quota/controller/UserQuotaController.java`
- `server/src/main/resources/db/changelog/changesets/202601081000-PRD-user-event-quota-table.xml`

**Backend (Modified):**
- `server/src/main/java/io/github/eventify/api/Paths.java` (added USER_QUOTA_PATH)
- `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java` (added QUOTA_EXCEEDED)

**Backend Tests:**
- `server/src/test/java/io/github/eventify/api/quota/service/UserQuotaServiceTest.java`
- `server/src/test/java/io/github/eventify/api/quota/controller/UserQuotaControllerTest.java`

**Frontend (Modified):**
- `client/src/lib/api/models.ts` (added UserQuotaResponse type)
- `client/src/lib/api/user/UserController.ts` (added getQuota function)
- `client/src/routes/(authenticated)/developer/+page.svelte` (wired quota to real API)

**UI Tests (New):**
- `client/test/components/developer.spec.ts` (9 test cases)
- `client/test/resources/screenshots/developer/` (9 screenshots)

### Quality Metrics
- Unit tests: 8 written, 8 passing
- Integration tests: 4 written, 4 passing
- UI tests: 9 written, 9 passing
- Frontend check: `bun run check` passes (0 errors)
- Build: Successful (373 total tests passing)

### API Endpoint

```
GET /v1/user/quota
Authorization: Bearer {jwt}

Response 200:
{
  "used": 342,
  "limit": 1000,
  "remaining": 658,
  "periodStart": "2026-01-01",
  "periodEnd": "2026-02-01",
  "percentUsed": 34.2
}

Response 401: Unauthorized
```

### Scheduled Job

```java
@Scheduled(cron = "0 0 0 1 * *")  // Midnight on 1st of every month
@Transactional
public void resetMonthlyQuotas() {
    final OffsetDateTime periodStart = OffsetDateTime.now(UTC)
        .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    final int resetCount = quotaRepository.resetAllQuotas(periodStart);
    log.info("Monthly quota reset: {} user quotas reset to 0", resetCount);
}
```

### Database Schema

```sql
CREATE TABLE user_event_quota (
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER NOT NULL UNIQUE,
    event_count     BIGINT NOT NULL DEFAULT 0,
    period_start    TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_event_quota_user ON user_event_quota(user_id);
CREATE INDEX idx_user_event_quota_period ON user_event_quota(period_start);
```
