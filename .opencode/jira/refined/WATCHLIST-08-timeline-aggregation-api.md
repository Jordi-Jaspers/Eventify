# Timeline Aggregation API

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: L
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-01-entity-database-schema.md

## 1. User Story
**As a** user or organization member
**I want** to fetch aggregated timeline data for a watchlist
**So that** the monitor page can display duration-based severity segments

## 2. Business Context & Value
This is the core data aggregation logic that powers the monitoring visualization. It transforms raw events into duration segments, handles NO_DATA states, computes the consolidated dashboard timeline, and applies sorting/filtering.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Basic timeline data retrieval
    *   Given a watchlist with channels that have events
    *   When they POST to `/v1/user/monitor` with watchlist ID
    *   Then they receive timeline data for each channel

*   [ ] **Scenario 2**: Duration segments computed correctly
    *   Given a channel with events: OK@10:00, OK@10:05, CRITICAL@10:15, OK@10:25
    *   When fetching timeline data
    *   Then durations are: [OK: 10:00-10:05], [CRITICAL: 10:15-10:15], [OK: 10:25-now]

*   [ ] **Scenario 3**: NO_DATA segments for gaps
    *   Given a time range of last 24h and first event at 12h ago
    *   When fetching timeline data
    *   Then a NO_DATA segment spans from 24h ago to first event

*   [ ] **Scenario 4**: NO_DATA between last event and now (live mode)
    *   Given last event was 2 hours ago and we're in live mode
    *   When fetching timeline data
    *   Then the last segment extends to "now" (current time)

*   [ ] **Scenario 5**: Consolidated dashboard timeline
    *   Given multiple channels with different severities at same time
    *   When fetching timeline data
    *   Then the dashboard timeline shows worst severity at each point

*   [ ] **Scenario 6**: Sorting by severity and recency
    *   Given channels with different last-critical timestamps
    *   When sortBySeverity is true
    *   Then channels are grouped by current severity, then sorted by recency

*   [ ] **Scenario 7**: Only critical filter
    *   Given channels with various current severities
    *   When onlyCritical filter is true
    *   Then only channels with current CRITICAL status are returned

*   [ ] **Scenario 8**: Paused channels excluded
    *   Given a channel with PAUSED status
    *   When fetching timeline data
    *   Then that channel shows as paused (greyed out, no timeline data)

*   [ ] **Scenario 9**: Time range filtering
    *   Given time range of "24h"
    *   When fetching timeline data
    *   Then only events within last 24 hours are considered

*   [ ] **Scenario 10**: Custom date range
    *   Given explicit start and end timestamps
    *   When fetching timeline data
    *   Then only events within that range are considered

*   [ ] **Scenario 11**: Watchlist defaults applied when no filters provided
    *   Given a watchlist with default timeRange="7d" and onlyCritical=true
    *   When fetching without filter parameters
    *   Then the watchlist defaults are applied

## 4. Technical Requirements

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/user/monitor` | Get timeline data for user watchlist |
| POST | `/v1/organization/{orgId}/monitor` | Get timeline data for org watchlist |

### Request Model

**MonitorRequest**
```java
public record MonitorRequest(
    @NotNull Long watchlistId,
    String timeRange,           // "24h", "7d", "30d" - null uses watchlist default
    OffsetDateTime startTime,   // For custom range
    OffsetDateTime endTime,     // For custom range
    Boolean onlyCritical,       // null uses watchlist default
    Boolean sortBySeverity      // null uses watchlist default
) {}
```

### Response Model

**MonitorResponse**
```java
public record MonitorResponse(
    Long watchlistId,
    String watchlistName,
    OffsetDateTime rangeStart,
    OffsetDateTime rangeEnd,
    boolean isLive,
    DashboardTimeline dashboard,
    List<ChannelTimeline> channels,
    AppliedFilters appliedFilters
) {}
```

**DashboardTimeline** (consolidated)
```java
public record DashboardTimeline(
    List<TimelineSegment> segments
) {}
```

**ChannelTimeline**
```java
public record ChannelTimeline(
    Long channelId,
    String channelName,
    String channelStatus,       // ACTIVE, PAUSED
    Severity currentSeverity,   // Last known severity
    OffsetDateTime lastEventAt, // Timestamp of last event
    List<TimelineSegment> segments
) {}
```

**TimelineSegment**
```java
public record TimelineSegment(
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    String severity  // "OK", "WARNING", "CRITICAL", "NO_DATA"
) {}
```

**AppliedFilters** (so UI knows what was applied)
```java
public record AppliedFilters(
    String timeRange,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    boolean onlyCritical,
    boolean sortBySeverity
) {}
```

### Path Constants
Add to `Paths.java`:
```java
public static final String MONITOR_PART = "/monitor";

public static final String USER_MONITOR_PATH = USERS_PATH + MONITOR_PART;
public static final String ORGANIZATION_MONITOR_PATH = ORGANIZATION_PATH + MONITOR_PART;
```

### Duration Aggregation Algorithm

```
Input: List<Event> events (sorted by timestamp ASC), timeRange
Output: List<TimelineSegment>

1. If no events in range:
   - Return single NO_DATA segment for entire range

2. Initialize segments = []
3. Set previousEvent = null
4. Set rangeStart = calculateRangeStart(timeRange)

5. Get last event BEFORE rangeStart (for initial state)
   - If exists: initialSeverity = that event's severity
   - Else: initialSeverity = NO_DATA

6. If first event is after rangeStart:
   - Add segment(rangeStart, firstEvent.timestamp, initialSeverity or NO_DATA)

7. For each event in range:
   - If previousEvent exists AND event.severity != previousEvent.severity:
     - Close previous segment at this event's timestamp
     - Start new segment with event.severity
   - Else if previousEvent exists AND same severity:
     - Extend current segment's endTime to event.timestamp
   - Else (first event):
     - Start new segment with event.severity
   - Set previousEvent = event

8. Close final segment:
   - If live mode: endTime = now
   - If historical: endTime = rangeEnd

9. Return segments
```

### Consolidated Dashboard Timeline Algorithm

```
Input: List<ChannelTimeline> channelTimelines
Output: DashboardTimeline

1. Collect all unique timestamps (segment boundaries) from all channels
2. Sort timestamps
3. For each time interval between consecutive timestamps:
   - Find severity of each channel at that time
   - Dashboard severity = max(all channel severities) using ordinal
   - Create segment with that severity
4. Merge consecutive segments with same severity
5. Return DashboardTimeline
```

### Sorting Algorithm

```
Input: List<ChannelTimeline> channels, sortBySeverity flag
Output: Sorted List<ChannelTimeline>

If sortBySeverity:
  1. Group by currentSeverity (CRITICAL, WARNING, OK, NO_DATA)
  2. Within each group, sort by lastEventAt DESC
  3. Concatenate groups in severity order (CRITICAL first)
Else:
  - Return in watchlist channel order (position)
```

### Database Query

For efficiency, fetch events with a single query:
```sql
SELECT e.*, c.id as channel_id, c.name as channel_name, c.status
FROM event e
JOIN channel c ON e.channel_id = c.id
JOIN watchlist_channel wc ON wc.channel_id = c.id
WHERE wc.watchlist_id = :watchlistId
  AND e.timestamp >= :rangeStart
  AND e.timestamp <= :rangeEnd
ORDER BY c.id, e.timestamp ASC
```

Also fetch the last event before range start for initial state:
```sql
SELECT DISTINCT ON (e.channel_id) e.*
FROM event e
JOIN watchlist_channel wc ON wc.channel_id = e.channel_id
WHERE wc.watchlist_id = :watchlistId
  AND e.timestamp < :rangeStart
ORDER BY e.channel_id, e.timestamp DESC
```

### Security
- User monitor: Validate user owns the watchlist
- Org monitor: Validate user is member of the organization

### Performance
- TimescaleDB is optimized for time-range queries
- Use indexes on `event(channel_id, timestamp)`
- Consider caching for frequently accessed watchlists
- Limit to reasonable time ranges (max 30 days for now)

## 5. Design & UI/UX
- N/A (API only)

## 6. Implementation Notes / Research

### File Locations
- Controller: `server/src/main/java/io/github/eventify/api/monitor/controller/UserMonitorController.java`
- Controller: `server/src/main/java/io/github/eventify/api/monitor/controller/OrganizationMonitorController.java`
- Service: `server/src/main/java/io/github/eventify/api/monitor/service/MonitorService.java`
- Service: `server/src/main/java/io/github/eventify/api/monitor/service/TimelineAggregationService.java`
- Models: `server/src/main/java/io/github/eventify/api/monitor/model/`

### Patterns to Follow
- Follow existing controller patterns
- Use `@Transactional(readOnly = true)` for read-only operations
- Consider using native queries for complex aggregations

### TimescaleDB Considerations
- The `event` table is a TimescaleDB hypertable
- Time-range queries are highly optimized
- Consider using TimescaleDB's `time_bucket` function for future chart aggregations

### Edge Cases
- Channel with no events ever → all NO_DATA
- Channel with events only outside range → NO_DATA, but show last known severity
- All channels paused → return all as paused, no timeline data
- Empty watchlist (no channels) → return empty channels list

### Test Cases
- Single channel, single event
- Single channel, multiple events (same severity)
- Single channel, multiple events (severity changes)
- Multiple channels, overlapping severities
- NO_DATA at start of range
- NO_DATA at end of range (historical mode)
- Live mode extends to now
- Paused channel handling
- Only critical filter
- Sort by severity vs original order
- Custom date range
- Time range presets (24h, 7d, 30d)
