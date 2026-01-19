## [2026-01-19] - Event Entity & Database Schema

### Plan (approved)
Create the foundational Event entity and database schema with TimescaleDB optimizations for high-throughput time-series event storage.

**Requirements:**
- Event table with columns: id, channel_id, severity, title, message, metadata, timestamp
- TimescaleDB hypertable partitioned by timestamp with 7-day chunk interval
- Composite index for timeline queries (channel_id, timestamp DESC)
- Severity index for filtering
- Compression policy at 90 days with channel_id segmentby
- Foreign key cascade on channel deletion

### Actual Changes

**Backend:**
- Created `Event.java` entity with JPA annotations, JSONB metadata support via `@JdbcTypeCode(SqlTypes.JSON)`
- Created `Severity.java` enum with values: OK, WARNING, CRITICAL
- Created `EventRepository.java` with `findByChannelIdOrderByTimestampDesc` query method
- Added factory constructor `Event(CreateEventRequest, Channel)` for convenient entity creation

**Database Migration:**
- Created `202601201000-PRD-event-table.xml` with 5 changesets:
  1. Create event table with BIGSERIAL id, channel FK (CASCADE DELETE), severity, title, message, metadata (JSONB), timestamp
  2. Convert to TimescaleDB hypertable with 7-day chunk interval
  3. Create indexes: `idx_event_channel_time` (channel_id, timestamp DESC), `idx_event_severity`
  4. Configure compression: segmentby channel_id, orderby timestamp DESC, 90-day policy
  5. Add table/column/index comments

### Agents Used
- java-backend-agent: Entity, repository, and migration implementation

### Files Modified
- `server/src/main/java/io/github/eventify/api/event/model/Event.java` (created)
- `server/src/main/java/io/github/eventify/api/event/model/Severity.java` (created)
- `server/src/main/java/io/github/eventify/api/event/repository/EventRepository.java` (created)
- `server/src/main/resources/db/changelog/changesets/202601201000-PRD-event-table.xml` (created)

### Quality Metrics
- Schema follows TimescaleDB best practices
- Indexes optimized for timeline queries
- Compression configured for storage efficiency
- FK cascade ensures data integrity
