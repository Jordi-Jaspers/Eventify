# Stale Channel Detection & Filtering

**Completed:** 2026-02-17
**Epic:** Channel Management
**Story:** `.opencode/refined/CHANNEL-05-stale-channel-detection.md`

## Summary

Implemented stale channel detection to identify channels that haven't received events in 7+ days. Channels are automatically marked as stale by a scheduled job and cleared when new events arrive via a database trigger. Users can filter and sort channels by staleness and last activity.

## Approved Plan

### Requirements
- Track `lastEventAt` timestamp for each channel
- Mark channels as stale if no events in 7+ days
- Clear stale flag when new events arrive
- New channels (created < 7 days) exempt from staleness
- Filter channels by stale status
- Sort channels by last activity
- Display stale badge and last activity column in UI

### Technical Approach
- **Backend:** DB trigger for real-time updates + scheduled job for marking stale (belt and suspenders)
- **Frontend:** Amber "Stale" badge, "Last Activity" column with relative time, isStale filter

### Execution Order
| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Write 23 tests for staleness scenarios |
| 2 | spring-backend-agent | Implement entity, migration, trigger, job, repository |
| 3 | svelte-frontend-agent | Add stale badge, last activity column, filters |

## Implementation

### Backend

**New Files:**
- `ChannelStalenessJob.java` - Scheduled job running every 5 minutes:
  - `markChannelsAsStale()` - Marks channels with no events in 7+ days as stale
  - `clearStaleForActiveChannels()` - Safety net to clear stale flag for channels with recent activity
- `202602171400-PRD-channel-staleness-tracking.xml` - Liquibase migration with:
  - `last_event_at` and `is_stale` columns on channel table
  - Partial index for stale filtering
  - Index for last_event_at sorting
  - Performance threshold documentation (< 1000 events/sec for trigger approach)
- `update_channel_last_event.sql` - DB trigger function that:
  - Updates `last_event_at` to event timestamp on INSERT
  - Clears `is_stale` flag automatically
  - Real-time, no job dependency

**Modified Files:**
- `Channel.java` - Added `lastEventAt` (OffsetDateTime) and `isStale` (boolean) fields
- `ChannelDetailsResponse.java` - Added fields to DTO for API responses
- `ChannelMetaData.java` - Added `isStale` filter (BOOLEAN) and `lastEventAt` sort option
- `ChannelRepository.java` - Added:
  - `markChannelsAsStale(threshold)` - Bulk update for stale marking
  - `clearStaleForActiveChannels(threshold)` - Bulk update to clear stale flag

**Key Design Decisions:**
1. DB trigger handles real-time staleness clearing (immediate on event insert)
2. Scheduled job runs every 5 minutes (not daily) for faster detection
3. Safety net in job clears stale flag for active channels (handles edge cases)
4. 7-day grace period for new channels (createdAt check in query)

### Frontend

**Modified Files:**
- `channel.ts` - Added utility functions:
  - `getRelativeActivityTime()` - Formats lastEventAt as "3 days ago", "Never", etc.
  - `getStaleBadgeProps()` - Returns amber badge configuration for stale channels
- `ChannelRow.svelte` - Added:
  - Amber "Stale" badge with tooltip "No events in 7+ days"
  - "Last Activity" column with relative time display
- `channels/+page.svelte` (user) - Added:
  - `lastEventAt` column header (sortable)
  - `isStale` filter (BOOLEAN type)
  - Updated grid layout (18 columns total)
- `organizations/[orgId]/channels/+page.svelte` (org) - Same changes as user page

**Grid Layout (18 columns):**
| Column | Span | Notes |
|--------|------|-------|
| Channel name | 4 | Unchanged |
| Description | 7 | Reduced from 9 |
| Status | 2 | Unchanged |
| Last Activity | 2 | **New** |
| Created | 2 | Unchanged |
| Actions | 1 | Unchanged |

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Write 23 tests across 3 test files | ✅ Complete |
| spring-backend-agent | Implement entity, migration, trigger, job, repository | ✅ Complete |
| svelte-frontend-agent | Add UI for stale badge, last activity column, filters | ✅ Complete |

## Files Modified

### Backend (New)
- `server/src/main/java/io/github/eventify/api/channel/job/ChannelStalenessJob.java`
- `server/src/main/resources/db/changelog/changesets/202602171400-PRD-channel-staleness-tracking.xml`
- `server/src/main/resources/db/changelog/triggers/update_channel_last_event.sql`

### Backend (Modified)
- `server/src/main/java/io/github/eventify/api/channel/domain/Channel.java`
- `server/src/main/java/io/github/eventify/api/channel/dto/ChannelDetailsResponse.java`
- `server/src/main/java/io/github/eventify/api/channel/search/ChannelMetaData.java`
- `server/src/main/java/io/github/eventify/api/channel/repository/ChannelRepository.java`

### Backend (Tests)
- `server/src/test/java/io/github/eventify/api/channel/job/ChannelStalenessJobTest.java` (6 unit tests)
- `server/src/test/java/io/github/eventify/api/channel/repository/ChannelRepositoryStalenessTest.java` (11 integration tests)
- `server/src/test/java/io/github/eventify/api/channel/controller/ChannelStalenessSearchTest.java` (7 integration tests)

### Frontend
- `client/src/lib/utils/channel.ts`
- `client/src/lib/components/channels/ChannelRow.svelte`
- `client/src/routes/(authenticated)/channels/+page.svelte`
- `client/src/routes/(authenticated)/organizations/[orgId]/channels/+page.svelte`
- `client/test/resources/screenshots/channels/*.png` (26 screenshots updated)

## Tests

- 23 backend tests written (6 unit + 17 integration)
- All backend tests passing
- 26 screenshot tests updated and passing
- Frontend check and build passing

## Commits

1. `feat(channels): add stale channel detection backend` - Core implementation
2. `feat(channels): add safety net to clear stale flag for active channels` - Belt and suspenders
3. `feat(channels): add stale channel UI with last activity column` - Frontend implementation

## Notes

- Scheduled job frequency changed from daily (in spec) to every 5 minutes for faster detection
- Added safety net method `clearStaleForActiveChannels()` to handle edge cases where trigger might be missed
- Performance threshold documented in migration: switch to batch approach if > 1000 events/sec
- Pre-existing warnings in EventsList.svelte unrelated to this feature
