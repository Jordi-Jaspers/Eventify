# Backend LOD Query Layer

**Completed:** 2026-04-04
**Epic:** MON
**Source:** `.opencode/refined/MON-02-backend-lod-query-layer.md`

## Summary

Backend LOD (Level of Detail) query layer that selects appropriate bucket sizes based on time range, queries TimescaleDB continuous aggregates for longer ranges, and preserves raw event queries for short ranges. Reduces payload from ~11MB to ~160KB for 30-day views.

## Approved Plan

### Requirements Summary

- LOD bucket selection: ≤4h → raw events, ≤24h → PT30M, ≤7d → PT2H, >7d → PT4H
- Aggregate queries on `event_timeline_hourly` continuous aggregate
- Raw event path preserved for ≤4h
- Live mode stitching: aggregate for historical + raw events for last hour
- `bucketSize` field (BucketSize enum) on MonitorResponse
- CUSTOM timeRange zoom support
- Prior-event semantics preserved in aggregate path

### Technical Approach

- Backend: BucketSize enum, LodSelector, AggregateTimelineBuilder, TimelineBucket projection, TimelineAggregateRepository (JPA interface with native queries); modified MonitorService with LOD routing, MonitorResult/MonitorResponse with BucketSize type
- Frontend: N/A (backend-only story)

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent (project) | Create LodSelectorTest, AggregateTimelineBuilderTest, TimelineBuilderTest, MonitorServiceTest LOD routing tests |
| 2 | spring-backend-agent (project) | Implement all new classes, modify existing ones, pass all tests |
| 3 | backend-optimizer-agent (global) | Fix SQL bugs, add @Repository, convert SQL to text blocks, remove redundant static modifiers |
| 4 | Orchestrator (review) | Extract Severity.fromString(), flatten LodSelector, proper SQL indentation |

## Implementation

### Backend

- **BucketSize** — Enum with PT30M, PT2H, PT4H values, `@JsonValue`/`@JsonCreator`, carries Duration
- **LodSelector** — Static utility selecting BucketSize from TimeSpan effective range (single method, if/else chain)
- **TimelineBucket** — Projection interface for aggregate query results (Spring Data auto-maps column aliases)
- **TimelineAggregateRepository** — JPA interface extending `Repository<Event, Long>` with 6 `@Query(nativeQuery=true)` methods (one per BucketSize × 2 query types) + 2 default dispatch methods. Text block SQL with proper indentation
- **AggregateTimelineBuilder** — Builds timeline from aggregate buckets with gap filling, prior-event semantics, live extension. Uses `Severity.fromString()` for parsing
- **MonitorService** — LOD routing: raw events (≤4h), aggregate (≥12h non-live), aggregate+stitch (≥12h live). Live stitching: historical aggregates + last hour raw events
- **Severity.fromString()** — Shared null-safe severity parser (replaces duplicate parseSeverity/parseSeverityValue in builders)
- **MonitorResult.bucketSize** / **MonitorResponse.bucketSize** — `BucketSize` enum type (not String), null for raw event ranges

### Deviations from Plan

- Initially implemented TimelineAggregateRepository as EntityManager class with dynamic SQL — refactored to JPA interface with static @Query methods
- Initially had `mixed` boolean on TimelineDuration — removed during refactoring (at 4h buckets nearly every bucket is mixed, making it useless)
- Initially had fromBuckets() in TimelineBuilder — extracted to separate AggregateTimelineBuilder
- Initially had `String bucketSize` on MonitorResult/MonitorResponse — changed to `BucketSize` enum type
- Added `Severity.fromString()` to eliminate duplicate severity parsing across builders

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Create initial 28 tests, then update for refactoring (BucketSize, AggregateTimelineBuilder, remove mixed) | Complete |
| spring-backend-agent | Implement LOD query layer + refactoring (BucketSize enum, JPA repo, AggregateTimelineBuilder) | Complete |
| backend-optimizer-agent | Fix SQL bugs, add @Repository, text block SQL, remove redundant static | Complete |
| deep-research-agent | Research project repository patterns for JPA interface conversion | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/event/model/Severity.java` — added `fromString()` shared parser
- `server/src/main/java/io/github/eventify/api/monitor/model/BucketSize.java` — **NEW** enum with PT30M, PT2H, PT4H
- `server/src/main/java/io/github/eventify/api/monitor/model/TimelineBucket.java` — **NEW** projection interface
- `server/src/main/java/io/github/eventify/api/monitor/model/TimelineDuration.java` — no `mixed` field (removed)
- `server/src/main/java/io/github/eventify/api/monitor/model/MonitorResult.java` — added `BucketSize bucketSize` field
- `server/src/main/java/io/github/eventify/api/monitor/model/response/MonitorResponse.java` — added `BucketSize bucketSize` field
- `server/src/main/java/io/github/eventify/api/monitor/repository/TimelineAggregateRepository.java` — **NEW** JPA interface with native queries
- `server/src/main/java/io/github/eventify/api/monitor/service/MonitorService.java` — LOD routing, live stitching
- `server/src/main/java/io/github/eventify/api/monitor/util/AggregateTimelineBuilder.java` — **NEW** aggregate timeline builder
- `server/src/main/java/io/github/eventify/api/monitor/util/LodSelector.java` — **NEW** bucket selection utility
- `server/src/main/java/io/github/eventify/api/monitor/util/TimelineBuilder.java` — raw-events-only (fromBuckets removed)
- `server/src/test/java/io/github/eventify/api/monitor/util/LodSelectorTest.java` — **NEW** 10 tests
- `server/src/test/java/io/github/eventify/api/monitor/util/AggregateTimelineBuilderTest.java` — **NEW** 8 tests
- `server/src/test/java/io/github/eventify/api/monitor/util/TimelineBuilderTest.java` — 13 tests (fromBuckets removed)
- `server/src/test/java/io/github/eventify/api/monitor/service/MonitorServiceTest.java` — +7 LOD routing tests

## Tests

- 31 new tests written, 60 total monitor tests passing
- All quality checks passing (Checkstyle, PMD, SpotBugs, Spotless)
