# Retention Cleanup Job

**Epic**: Event Ingestion
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-01-19

## 1. User Story
**As a** platform operator
**I want** events to be automatically deleted based on each owner's retention policy
**So that** storage costs are managed and data governance requirements are met

## 2. Business Context & Value
Users and organizations configure their retention period (90-1825 days). Events older than this period should be automatically deleted. This:
- Manages storage costs (TimescaleDB storage isn't free)
- Complies with data governance requirements
- Respects user preferences for data retention

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Delete events for personal channels based on user retention
    *   Given user A has `retention_days = 90`
    *   And user A's channel has events from 100 days ago
    *   When the cleanup job runs
    *   Then events older than 90 days are deleted
    *   And events from the last 90 days are retained

*   [ ] **Scenario 2**: Delete events for org channels based on org retention
    *   Given organization X has `retention_days = 365`
    *   And org X's channel has events from 400 days ago
    *   When the cleanup job runs
    *   Then events older than 365 days are deleted
    *   And events from the last 365 days are retained

*   [ ] **Scenario 3**: Respect different retention per user
    *   Given user A has `retention_days = 90`
    *   And user B has `retention_days = 365`
    *   And both have events from 100 days ago
    *   When the cleanup job runs
    *   Then user A's 100-day-old events are deleted
    *   And user B's 100-day-old events are retained

*   [ ] **Scenario 4**: Job runs daily at 3 AM UTC
    *   Given the application is running
    *   When the clock reaches 3:00 AM UTC
    *   Then the cleanup job executes

*   [ ] **Scenario 5**: Job logs deletion metrics
    *   Given the cleanup job runs
    *   When events are deleted
    *   Then logs show: "Retention cleanup completed. Deleted X events for personal channels, Y events for org channels"

*   [ ] **Scenario 6**: Job handles no events to delete
    *   Given all events are within retention period
    *   When the cleanup job runs
    *   Then job completes successfully with "Deleted 0 events"

## 4. Technical Requirements
*   **Scheduled Job**:
    ```java
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")  // Daily at 3 AM UTC
    public void cleanupExpiredEvents() { ... }
    ```

*   **Cleanup Logic**:
    ```sql
    -- Personal channels: use user's retention_days
    DELETE FROM event e
    USING channel c, "user" u
    WHERE e.channel_id = c.id
      AND c.organization_id IS NULL
      AND c.user_id = u.id
      AND e.timestamp < NOW() - (u.retention_days || ' days')::INTERVAL;
    
    -- Organization channels: use org's retention_days
    DELETE FROM event e
    USING channel c, organization o
    WHERE e.channel_id = c.id
      AND c.organization_id = o.id
      AND e.timestamp < NOW() - (o.retention_days || ' days')::INTERVAL;
    ```

*   **Batch Deletion**: 
    *   TimescaleDB efficiently drops entire chunks when all data in chunk is expired
    *   For partial chunk cleanup, use `DELETE` with `LIMIT` to avoid long-running transactions:
    ```java
    int deleted;
    do {
        deleted = jdbcTemplate.update(DELETE_PERSONAL_CHANNELS + " LIMIT 10000");
        totalDeleted += deleted;
    } while (deleted > 0);
    ```

*   **Logging**:
    ```java
    log.info("Retention cleanup completed. Deleted {} events for personal channels, {} for org channels",
             personalDeleted, orgDeleted);
    ```

*   **Metrics** (optional):
    *   `eventify.retention.deleted.personal` - counter
    *   `eventify.retention.deleted.organization` - counter
    *   `eventify.retention.duration.seconds` - timer

*   **Error Handling**:
    *   Job should not fail silently
    *   Log errors with full stack trace
    *   Consider alerting if job fails repeatedly

## 5. Design & UI/UX
N/A - Background job only

## 6. Implementation Notes / Research
*   **Job class**: `server/src/main/java/io/github/eventify/api/event/job/EventRetentionCleanupJob.java`
*   **Service class**: `server/src/main/java/io/github/eventify/api/event/service/EventRetentionService.java`
*   **Follow pattern from**: `ChannelCleanupJob.java` (existing cleanup job for PENDING_DELETION channels)
*   **TimescaleDB optimization**: 
    *   After running, consider `SELECT reorder_chunk()` on affected chunks
    *   Compression policy (from EVENT-001) handles old chunks automatically
*   **Testing**:
    *   Unit test with mocked repository
    *   Integration test with test data from different dates

## 7. Out of Scope
*   Admin UI for viewing cleanup stats
*   Warning notifications before deletion
*   Manual trigger via admin API
*   Per-channel retention override (future consideration in backlog)
