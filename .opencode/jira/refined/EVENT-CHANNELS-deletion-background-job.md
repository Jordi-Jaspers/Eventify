# Channel Deletion Background Job

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-01-11

## 1. User Story
**As a** system operator
**I want** channels marked for deletion to be cleaned up asynchronously
**So that** deletion is performant and doesn't timeout for channels with many events

## 2. Business Context & Value
Channels can accumulate millions of events. Synchronous deletion would cause request timeouts and database locks. An async cleanup job ensures smooth user experience while guaranteeing eventual cleanup. This pattern is industry standard for large-scale data deletion.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Job processes PENDING_DELETION channels
    *   Given there are channels with status PENDING_DELETION
    *   When the scheduled job runs
    *   Then it processes each pending channel

*   [ ] **Scenario 2**: Events are deleted in batches
    *   Given a channel with 100,000 events pending deletion
    *   When the job processes this channel
    *   Then events are deleted in configurable batches (e.g., 10,000 per iteration)
    *   And the job continues until all events are deleted

*   [ ] **Scenario 3**: Channel is deleted after all events removed
    *   Given a channel whose events have all been deleted
    *   When the job runs next
    *   Then the channel record itself is deleted (hard delete)

*   [ ] **Scenario 4**: Job is idempotent
    *   Given the job is interrupted mid-process
    *   When it runs again
    *   Then it resumes from where it left off without errors

*   [ ] **Scenario 5**: Job logs progress
    *   Given the job is processing deletions
    *   Then it logs: channel ID, events deleted, time taken
    *   And errors are logged with full context

*   [ ] **Scenario 6**: Job handles empty channels
    *   Given a channel with no events pending deletion
    *   When the job runs
    *   Then the channel is deleted immediately

## 4. Technical Requirements
*   **Scheduled Job**: Spring `@Scheduled` with configurable cron (default: every 5 minutes)
*   **Batch Size**: Configurable property `eventify.channel.deletion.batch-size=10000`
*   **Job Logic**:
    ```
    1. Find channels WHERE status = 'PENDING_DELETION'
    2. For each channel:
       a. Delete up to BATCH_SIZE events for this channel
       b. If no events remain, delete the channel record
       c. Log progress
    3. If more channels remain, they'll be processed on next run
    ```
*   **Transaction**: Each batch deletion in its own transaction to avoid long locks
*   **Index**: Ensure `event` table has index on `channel_id` for efficient deletion

## 5. Design & UI/UX
N/A - Background infrastructure

## 6. Implementation Notes / Research
*   **Service**: `ChannelDeletionService` or method in `ChannelService`
*   **Job Class**: `ChannelDeletionJob` with `@Scheduled` annotation
*   **Configuration**: Add properties to `application.yml`
*   **Note**: Event table/entity doesn't exist yet - this job will be fully functional once Event Ingestion epic is complete. Implement the channel deletion logic; event deletion can be added when events exist.
*   **Metrics**: Consider adding Micrometer metrics for observability
*   **Testing**: Unit test with mocked repository; integration test with test containers
*   **Ownership model**: This job deletes channels regardless of ownership type (personal or organization) - it only cares about the PENDING_DELETION status
