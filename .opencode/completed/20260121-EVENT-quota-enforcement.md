## [2026-01-21] - Quota Enforcement in Event Ingestion

### Plan (approved)
Enforce monthly event quota (1000 events/user) during event ingestion with:
- Check quota before accepting events (single or batch)
- Reject with 429 + rate limit headers when exceeded
- All-or-nothing batch semantics
- Auto-reset on new month
- Org API key events count against key creator's quota

### Actual Changes

**Backend:**
- Integrated quota enforcement into `EventIngestionController` 
- Added `checkAndIncrementOrThrow(userId, eventCount)` to `UserQuotaService` - atomic check+increment with pessimistic lock
- Added `findByUserIdWithLock()` to `UserEventQuotaRepository` with `@Lock(PESSIMISTIC_WRITE)`
- Auto-reset quota when month changes in `getOrCreateQuotaWithLock()`
- Created `QuotaExceptionHandler` for custom 429 response with rate limit headers
- Enhanced `QuotaExceededException` to carry limit/remaining/resetDate info
- Removed dead code methods: `canSendEvent()`, `canSendBatch()`, `incrementUsage()` (only existed for tests)

**Rate Limit Headers on 429:**
- `X-RateLimit-Limit: 1000`
- `X-RateLimit-Remaining: 0`
- `X-RateLimit-Reset: 2026-02-01T00:00:00Z`

**Frontend:** N/A - API behavior only

**UI Polish:** N/A - Backend only feature

**Testing:**
- 11 unit tests in `UserQuotaServiceTest` (rewritten to test actual production methods)
- 10 integration tests in `EventIngestionControllerTest` for quota scenarios
- All 35 quota-related tests passing

### Agents Used
- java-testing-agent: Created initial test suite
- java-backend-agent: Implemented quota integration
- Orchestrator: Fixed dead code, rewrote tests, created exception handler

### Files Modified
- `server/src/main/java/io/github/eventify/api/quota/service/UserQuotaService.java` - Removed dead methods, kept only used ones
- `server/src/main/java/io/github/eventify/api/quota/repository/UserEventQuotaRepository.java` - Added pessimistic lock query
- `server/src/main/java/io/github/eventify/api/event/controller/EventIngestionController.java` - Integrated quota check
- `server/src/main/java/io/github/eventify/common/exception/QuotaExceededException.java` - Added rate limit fields
- `server/src/main/java/io/github/eventify/common/exception/handler/QuotaExceptionHandler.java` - NEW: Custom handler with headers
- `server/src/test/java/io/github/eventify/api/quota/service/UserQuotaServiceTest.java` - Rewritten to test production methods
- `server/src/test/java/io/github/eventify/api/event/controller/EventIngestionControllerTest.java` - Added quota integration tests

### Quality Metrics
- ✅ Tests: 35 quota tests passing
- ✅ Build: Successful
- ✅ No dead code: Removed unused methods
- ✅ Concurrency: Pessimistic locking prevents race conditions

### Acceptance Criteria Covered
- ✅ Scenario 1: Allow event when under quota (500/1000 → accepted)
- ✅ Scenario 2: Block event when quota reached (1000/1000 → 429 + ERR-0045)
- ✅ Scenario 3: Block batch when it would exceed quota (all-or-nothing)
- ✅ Scenario 4: Quota resets on 1st of new month (auto-reset logic)
- ✅ Scenario 5: Create quota record on first event
- ✅ Scenario 6: Org API key events count against user's quota
- ✅ Scenario 7: Rate limit headers on 429 response
