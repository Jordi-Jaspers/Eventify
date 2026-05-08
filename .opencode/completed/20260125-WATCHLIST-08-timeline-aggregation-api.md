# WATCHLIST-08: Timeline Aggregation API

**Completed:** 2026-01-25
**Branch:** `epic/watchlist`
**Commit:** `aad3ea45`

## Summary

Implemented the Timeline Aggregation API for real-time watchlist monitoring. This includes two POST endpoints (`/v1/user/monitor` and `/v1/organization/{orgId}/monitor`) that return consolidated timeline data showing severity states over time for watchlist channels and groups.

## Key Features

### Timeline Building & Consolidation
- **TimelineBuilder**: Creates severity timelines from events using a sweep-line algorithm
  - Handles prior events (before range start) for initial state
  - Merges consecutive events with same severity
  - Extends last duration to range end in live mode
- **TimelineConsolidator**: Merges multiple timelines picking worst severity at each point
  - Uses sweep-line algorithm with time points
  - Handles overlapping and non-overlapping timelines
  - Works with any `TimelineSource` (Channel, ChannelGroup, WatchlistConfiguration)

### Channel Groups
- **ChannelGroup**: Groups related channels with consolidated timeline
  - Implements `TimelineSource` interface
  - Serializes to JSONB with `channelIds` array
  - Enriched at runtime with full channel data
  - `getCurrentSeverity()` for sorting by worst severity

### Response Structure (1:1 with domain)
```
MonitorResponse
├── watchlistId, watchlistName
├── rangeStart, rangeEnd, live
├── configuration: ConfigurationResponse
│   ├── dashboard: Timeline          ← consolidated from all sources
│   ├── channels: List<ChannelResponse>
│   └── groups: List<ChannelGroupResponse>
└── filters: MonitorFilters
```

### Filter Application Rules
| Filter | Grouped View | Ungrouped View |
|--------|--------------|----------------|
| `onlyCritical` | ❌ Not applied | ✅ Applied |
| `sortBySeverity` | ✅ Groups + channels | ✅ All channels |
| `groupedView=false` | N/A | Flatmaps groups → channels |

## Architecture Decisions

1. **WatchlistConfiguration implements TimelineSource**: Enables the configuration itself to provide the consolidated dashboard timeline
2. **MonitorResult.configuration**: Enriched configuration with timelines, no stored dashboard field (derived via `getTimeline()`)
3. **MapStruct auto-mapping**: All DTOs including nested `ChannelGroupResponse` mapped automatically
4. **@FunctionalInterface on TimelineSource**: Single abstract method interface for clean lambda usage

## Agents Used

| Agent | Task |
|-------|------|
| testing-agent | Write tests for TimelineBuilder, TimelineConsolidator, MonitorService, controllers |
| backend-agent | Implement monitor API, channel groups, response DTOs, mappers |

## Files Created

### Main Source (server/src/main/java/io/github/eventify/api/monitor/)
- `controller/UserMonitorController.java`
- `controller/OrganizationMonitorController.java`
- `model/MonitorFilters.java`
- `model/MonitorResult.java`
- `model/TimeRange.java` (enum: LAST_1H, LAST_24H, LAST_7D, LAST_30D, CUSTOM)
- `model/TimeSpan.java` (start, end, isLive)
- `model/Timeline.java`
- `model/TimelineSource.java` (@FunctionalInterface)
- `model/TimePoint.java` (for sweep-line algorithm)
- `model/request/MonitorRequest.java`
- `model/response/MonitorResponse.java`
- `model/response/ConfigurationResponse.java`
- `model/response/ChannelResponse.java`
- `model/response/ChannelGroupResponse.java`
- `model/response/TimelineDuration.java`
- `model/mapper/MonitorMapper.java`
- `service/MonitorService.java`
- `util/TimelineBuilder.java`
- `util/TimelineConsolidator.java`
- `validator/MonitorValidator.java`

### Watchlist Extensions
- `model/ChannelGroup.java`
- `model/request/ChannelGroupRequest.java`
- `service/WatchlistSecurityService.java`

### Test Data
- `db/changelog/changesets/202601251000-TST-watchlist-groups-test-data.xml`
  - Production Monitoring: 'Critical Services' group
  - All Channels Overview: 'Application' and 'Operations' groups
  - E-commerce Operations: 'Customer Experience' group
  - Payment Dashboard: 'Financial' group

## Files Modified

- `api/Paths.java` - Added monitor endpoint paths
- `api/channel/model/Channel.java` - Added transient timeline fields, implements TimelineSource
- `api/channel/repository/ChannelRepository.java` - Simplified queries
- `api/event/model/Severity.java` - Added priority for sorting, NO_DATA sentinel
- `api/event/repository/EventRepository.java` - Added timeline query methods
- `api/watchlist/model/Watchlist.java` - Added helper methods
- `api/watchlist/model/WatchlistConfiguration.java` - Implements TimelineSource, groups support
- `api/watchlist/model/WatchlistFilters.java` - Added groupedView filter
- `api/watchlist/model/mapper/WatchlistMapper.java` - Channel/group mapping
- `api/watchlist/service/WatchlistService.java` - Refactored channel validation
- `common/exception/ApiErrorCode.java` - Added monitor-related error codes

## Tests

- **80+ tests** covering:
  - TimelineBuilder (14 tests)
  - TimelineConsolidator (12 tests)
  - MonitorService (10 tests including channel groups)
  - MonitorValidator (20 tests)
  - UserMonitorController (12 integration tests)
  - OrganizationMonitorController (12 integration tests)
  - ChannelGroup (unit tests)
  - WatchlistSecurityService (unit tests)

## Session Progression

1. **Sessions 1-4**: Built core Timeline API (endpoints, TimelineBuilder, TimelineConsolidator)
2. **Session 5**: Fixed integration test infrastructure
3. **Session 6**: Major refactoring (groupedView filter, WatchlistConfiguration as TimelineSource)
4. **Session 7**: Redundancy cleanup:
   - Removed `isLive` parameter from `TimelineBuilder.fromEvents()` (derived from range)
   - Changed `MonitorResult.dashboard` to derived method
   - Created `ConfigurationResponse` to mirror domain structure
   - Moved `dashboard` into `ConfigurationResponse` (1:1 with domain)
   - Simplified `toChannelGroupResponse()` to use MapStruct auto-mapping
   - Added test data with channel groups

## Code Quality Fixes

- Added `@FunctionalInterface` annotation to `TimelineSource`
- Refactored `TimelineBuilder.fromEvents()` to reduce return count (max 2)
- Fixed brace formatting in `WatchlistService`
- Removed unused imports
