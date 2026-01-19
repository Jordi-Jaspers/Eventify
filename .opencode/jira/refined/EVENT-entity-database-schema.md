# Event Entity & Database Schema

**Epic**: Event Ingestion
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-19

## 1. User Story
**As a** platform developer
**I want** a well-designed event table with TimescaleDB optimizations
**So that** events can be stored efficiently with high write throughput and fast timeline queries

## 2. Business Context & Value
Events are the core data model of Eventify. External systems will send events to channels, and users will view them in timelines. The database schema must be optimized for:
- High write throughput (append-only time-series data)
- Fast timeline queries (by channel + time range)
- Efficient storage (compression for older data)
- Per-owner retention policies (cleanup by user/org retention_days)

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Event table exists with correct schema
    *   Given the application starts
    *   When Liquibase migrations run
    *   Then the `event` table exists with columns: `id`, `channel_id`, `severity`, `title`, `message`, `metadata`, `timestamp`

*   [ ] **Scenario 2**: TimescaleDB hypertable is created
    *   Given the `event` table exists
    *   When the migration completes
    *   Then `event` is converted to a hypertable partitioned by `timestamp` with 7-day chunk interval

*   [ ] **Scenario 3**: Indexes support timeline queries
    *   Given the `event` hypertable exists
    *   When querying `SELECT * FROM event WHERE channel_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC`
    *   Then the query uses the composite index `idx_event_channel_time`

*   [ ] **Scenario 4**: Compression policy is configured
    *   Given the `event` hypertable exists
    *   When chunks become older than 90 days
    *   Then TimescaleDB automatically compresses them with `channel_id` as segment key

*   [ ] **Scenario 5**: Foreign key cascades on channel deletion
    *   Given events exist for a channel
    *   When the channel is deleted
    *   Then all associated events are automatically deleted

## 4. Technical Requirements
*   **Database**:
    *   Create `event` table with columns:
        | Column | Type | Constraints |
        |--------|------|-------------|
        | `id` | `BIGSERIAL` | `PRIMARY KEY` |
        | `channel_id` | `INTEGER` | `NOT NULL`, `FK -> channel(id) ON DELETE CASCADE` |
        | `severity` | `TEXT` | `NOT NULL` |
        | `title` | `TEXT` | `NOT NULL` |
        | `message` | `TEXT` | Nullable |
        | `metadata` | `JSONB` | Nullable |
        | `timestamp` | `TIMESTAMPTZ` | `NOT NULL` |
    *   Convert to hypertable: `SELECT create_hypertable('event', 'timestamp', chunk_time_interval => INTERVAL '7 days');`
    *   Create indexes:
        *   `idx_event_channel_time` on `(channel_id, timestamp DESC)` - primary timeline query
        *   `idx_event_severity` on `(severity)` - filtering by severity
    *   Configure compression:
        ```sql
        ALTER TABLE event SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'channel_id',
            timescaledb.compress_orderby = 'timestamp DESC'
        );
        SELECT add_compression_policy('event', INTERVAL '90 days');
        ```
    *   Add table/column comments

*   **Entity**:
    *   Create `Event.java` entity with JPA annotations
    *   Create `Severity.java` enum: `OK`, `WARNING`, `CRITICAL`
    *   Create `EventRepository.java` with custom query methods

*   **Security**: N/A (schema only)
*   **Performance**: Hypertable + compression optimizes for time-series workload

## 5. Design & UI/UX
N/A - Backend infrastructure only

## 6. Implementation Notes / Research
*   **Migration file**: `server/src/main/resources/db/changelog/changesets/202601201000-PRD-event-table.xml`
*   **Entity location**: `server/src/main/java/io/github/eventify/api/event/model/Event.java`
*   **Enum location**: `server/src/main/java/io/github/eventify/api/event/model/Severity.java`
*   **Repository location**: `server/src/main/java/io/github/eventify/api/event/repository/EventRepository.java`
*   **Follow patterns from**: 
    *   `Channel.java` entity structure
    *   `202601111000-PRD-channel-table.xml` migration structure
*   **TimescaleDB docs**: Hypertable already enabled in `202411122200-PRD-initial-tables-user-management.xml`
*   **Chunk interval rationale**: 7 days balances query planning overhead vs. retention granularity
*   **Compression at 90 days**: Matches minimum user retention period, ensures inserts always go to uncompressed chunks

## 7. Out of Scope
*   Full-text search index on title/message (future story)
*   Event ingestion API (EVENT-002, EVENT-003)
*   Retention cleanup job (EVENT-005)
