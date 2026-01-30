## [2026-01-19] - Event Retention Cleanup Job

### Plan (approved)
Scheduled job to automatically delete events based on owner's retention policy (user's retention_days for personal channels, org's retention_days for org channels).

**Requirements:**
- Delete events older than retention period
- Run daily at 3 AM UTC
- Batch deletion (10,000 per batch) to avoid long transactions
- Log deletion metrics

### Actual Changes

**Backend:**
- Created `EventRetentionCleanupJob.java` - Scheduled job with `@Scheduled(cron = "0 0 3 * * *", zone = "UTC")`
- Created `EventRetentionCleanupService.java` - Orchestrates batch deletion for personal and org channels
- Extended `EventRepository.java` - Added native query methods with `@Modifying` + `@Query(nativeQuery=true)`

**SQL Queries:**
```sql
-- Personal channels: use user's retention_days
DELETE FROM event WHERE id IN (
    SELECT e.id FROM event e
    JOIN channel c ON e.channel_id = c.id
    JOIN "user" u ON c.user_id = u.id
    WHERE c.organization_id IS NULL
      AND e.timestamp < NOW() - (u.retention_days || ' days')::INTERVAL
    LIMIT 10000
)

-- Organization channels: use org's retention_days
DELETE FROM event WHERE id IN (
    SELECT e.id FROM event e
    JOIN channel c ON e.channel_id = c.id
    JOIN organization o ON c.organization_id = o.id
    WHERE e.timestamp < NOW() - (o.retention_days || ' days')::INTERVAL
    LIMIT 10000
)
```

**Test Infrastructure:**
- Added `aValidEvent(Channel, int)` helper to `IntegrationTest` parent class
- Added `EventRepository` to `TestContextInitializer`

### Agents Used
- java-testing-agent: Created test suite (25 tests)
- java-backend-agent: Initial implementation
- Orchestrator: Refactored to use repository pattern, cleanup per code review

### Files Modified
- `server/src/main/java/io/github/eventify/api/event/job/EventRetentionCleanupJob.java` (created)
- `server/src/main/java/io/github/eventify/api/event/service/EventRetentionCleanupService.java` (created)
- `server/src/main/java/io/github/eventify/api/event/repository/EventRepository.java` (modified)
- `server/src/test/java/io/github/eventify/api/event/job/EventRetentionCleanupJobTest.java` (created)
- `server/src/test/java/io/github/eventify/api/event/service/EventRetentionCleanupServiceTest.java` (created)
- `server/src/test/java/io/github/eventify/api/event/service/EventRetentionCleanupServiceIntegrationTest.java` (created)
- `server/src/test/java/io/github/eventify/support/IntegrationTest.java` (modified)
- `server/src/test/java/io/github/eventify/support/util/TestContextInitializer.java` (modified)

### Quality Metrics
- ✅ Unit tests: 14 passing
- ✅ Integration tests: 11 written (require Docker)
- ✅ Follows existing Job+Service pattern
- ✅ Uses repository with native queries (consistent with codebase)
- ✅ Batch deletion prevents long transactions
