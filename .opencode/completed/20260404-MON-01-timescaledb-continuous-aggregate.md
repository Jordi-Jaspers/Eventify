# TimescaleDB Continuous Aggregate for Event Timeline

**Completed:** 2026-04-04
**Epic:** MON
**Source:** .opencode/refined/MON-01-timescaledb-continuous-aggregate.md

## Summary

Created a TimescaleDB continuous aggregate `event_timeline_hourly` that pre-computes hourly severity buckets from the `event` hypertable, enabling future monitor queries to read hundreds of rows instead of millions.

## Approved Plan

### Requirements Summary

- Create `event_timeline_hourly` continuous aggregate with hourly time buckets
- Add automatic hourly refresh policy (7-day lookback, 1-hour end offset)
- Configure compression on aggregate data older than 30 days
- Create composite index on `(channel_id, bucket DESC)`
- No application code changes

### Technical Approach

- Database: Single Liquibase migration with 4 changesets using TimescaleDB functions
- Backend: N/A
- Frontend: N/A

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-backend-agent | Create Liquibase migration file with 4 changesets |

## Implementation

### Database

- **Continuous aggregate** `event_timeline_hourly`: hourly buckets with `channel_id`, `bucket`, `first_severity`, `last_severity`, `event_count`, `first_event_time`, `last_event_time`
- **Refresh policy**: every 1 hour, 7-day start offset, 1-hour end offset
- **Compression**: segmented by `channel_id`, applied after 30 days
- **Index**: `idx_timeline_hourly_channel_bucket` on `(channel_id, bucket DESC)`

### Deviations from Plan

- Added `runInTransaction="false"` to changesets 1-3 (continuous aggregate operations cannot run inside a transaction block)
- Created `TimescaleLiquibaseConfiguration` test config to pre-drop continuous aggregates before Liquibase's `dropAll` (which incorrectly uses `DROP VIEW` instead of `DROP MATERIALIZED VIEW CASCADE`)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-backend-agent | Create Liquibase migration | Complete |
| spring-backend-agent | Fix dropAll incompatibility with continuous aggregates | Complete |

## Files Modified

- `server/src/main/resources/db/changelog/changesets/202604041000-PRD-event-timeline-aggregate.xml` - NEW: Liquibase migration with 4 changesets (changesets 1-3 use `runInTransaction="false"`)
- `server/src/test/java/io/github/eventify/support/config/TimescaleLiquibaseConfiguration.java` - NEW: Custom SpringLiquibase that drops continuous aggregates before `dropAll`
- `server/src/test/java/io/github/eventify/support/util/TestContextInitializer.java` - Added TimescaleLiquibaseConfiguration import

## Tests

- No new tests — database infrastructure only
- All 1252 existing tests pass (0 failures)
