# Stale Channel Detection & Filtering

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-02-16
**Depends On**: None

## 1. User Story
**As a** platform administrator or channel owner
**I want** to see which channels haven't received events recently
**So that** I can identify unused channels and clean them up

## 2. Business Context & Value
Over time, channels accumulate but may no longer be used:
- Deprecated services still have channels
- Test channels from past experiments
- Renamed/migrated applications

Stale detection helps users:
- Keep their channel list clean and relevant
- Identify forgotten integrations
- Reduce clutter in the UI

## 3. Acceptance Criteria

### Backend
* [ ] **Scenario 1**: Track last event timestamp
    * Given a channel
    * When an event is ingested to that channel
    * Then `last_event_at` is updated to current timestamp

* [ ] **Scenario 2**: Stale flag set by scheduled job
    * Given a channel with `last_event_at` older than 7 days
    * When the staleness job runs
    * Then `is_stale` is set to `true`

* [ ] **Scenario 3**: Stale flag cleared on new event
    * Given a stale channel
    * When a new event is ingested
    * Then `is_stale` is set to `false`
    * And `last_event_at` is updated

* [ ] **Scenario 4**: New channels not marked stale
    * Given a newly created channel with no events
    * When checking staleness
    * Then `is_stale` is `false` (grace period: channels created within last 7 days are exempt)

* [ ] **Scenario 5**: Filter by stale status
    * Given the channel search API
    * When I filter by `isStale=true`
    * Then only stale channels are returned

* [ ] **Scenario 6**: Sort by last activity
    * Given the channel search API
    * When I sort by `lastEventAt`
    * Then channels are ordered by last event timestamp

### Frontend
* [ ] **Scenario 7**: Stale badge in UI
    * Given a stale channel in the table
    * When viewing the channels table
    * Then a "Stale" badge is displayed (muted/warning style)

* [ ] **Scenario 8**: Filter dropdown includes stale option
    * Given the channel table filters
    * When I open the status filter
    * Then I can filter by "Stale" channels

* [ ] **Scenario 9**: Last activity column
    * Given the channels table
    * When viewing channels
    * Then I see "Last Activity" column with relative time (e.g., "3 days ago", "2 weeks ago")

* [ ] **Scenario 10**: Never-used channels display
    * Given a channel that has never received events
    * When viewing the table
    * Then "Last Activity" shows "Never" or "No activity"

## 4. Technical Requirements

### Database Changes
```sql
-- Add tracking columns
ALTER TABLE channel ADD COLUMN last_event_at TIMESTAMPTZ;
ALTER TABLE channel ADD COLUMN is_stale BOOLEAN NOT NULL DEFAULT false;

-- Index for stale filtering
CREATE INDEX idx_channel_is_stale ON channel (is_stale) WHERE is_stale = true;

-- Index for last_event_at sorting/filtering
CREATE INDEX idx_channel_last_event_at ON channel (last_event_at);
```

### Database Trigger: Update last_event_at on event insert
```sql
CREATE OR REPLACE FUNCTION update_channel_last_event()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE channel 
    SET last_event_at = NEW.timestamp,
        is_stale = false
    WHERE id = NEW.channel_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_channel_last_event
AFTER INSERT ON event
FOR EACH ROW
EXECUTE FUNCTION update_channel_last_event();
```

### Scheduled Job: Mark stale channels
```java
@Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
public void markStaleChannels() {
    int updated = channelRepository.markChannelsAsStale(
        OffsetDateTime.now().minusDays(7)
    );
    log.info("Marked {} channels as stale", updated);
}
```

### Repository Methods
```java
@Modifying
@Query("""
    UPDATE Channel c 
    SET c.isStale = true 
    WHERE c.lastEventAt < :threshold 
      AND c.isStale = false
      AND c.createdAt < :threshold
    """)
int markChannelsAsStale(@Param("threshold") OffsetDateTime threshold);
```

### API Changes
| Change | Details |
|--------|---------|
| `ChannelDetailsResponse` | Add `isStale: boolean` and `lastEventAt: OffsetDateTime` fields |
| Channel search | Add `isStale` filter parameter |
| Channel search | Add `lastEventAt` sort option |

### Entity Changes
```java
@Column(name = "last_event_at")
private OffsetDateTime lastEventAt;

@Column(name = "is_stale", nullable = false)
private boolean isStale = false;
```

## 5. Design & UI/UX

### Stale Badge
- Style: Muted amber/orange badge, distinct from "Paused"
- Text: "Stale"
- Tooltip: "No events received in over 7 days"
- Position: After status badge (Active/Paused)

### Last Activity Column
- Header: "Last Activity"
- Display: Relative time format
  - Recent: "5 min ago", "2 hours ago"
  - Older: "3 days ago", "2 weeks ago", "1 month ago"
  - Never: "Never" or "No activity" (muted text)
- Sortable: Yes (ascending/descending)

### Filter Options
Extend existing status filter:
- All
- Active
- Paused
- Stale

Or add separate "Activity" filter:
- All
- Active (events in last 7 days)
- Stale (no events in 7+ days)

## 6. Implementation Notes / Research

### Grace Period Logic
Channels created within the last 7 days should NOT be marked stale, even if `last_event_at` is null. The scheduled job query includes:
```sql
AND c.createdAt < :threshold
```

### Existing Patterns
- `ChannelMetaData` handles search specifications - extend with `isStale` filter
- `ChannelStatus` enum is for Active/Paused/PendingDeletion - stale is a **separate boolean**, not a status
- This allows a channel to be both "Active" (accepting events) and "Stale" (no recent activity)

### Performance Considerations
- Trigger runs on every event insert - query is simple (single row update by PK)
- For very high volume, consider batching: update every N events or via async queue
- Partial index on `is_stale = true` keeps index small

### Files to Modify

**Backend**
| File | Change |
|------|--------|
| `Channel.java` | Add `lastEventAt` and `isStale` fields |
| `ChannelDetailsResponse.java` | Add fields to response |
| `ChannelMetaData.java` | Add `isStale` filter specification |
| `ChannelRepository.java` | Add `markChannelsAsStale()` method |
| New: `ChannelStaleJob.java` | Scheduled job for marking stale channels |

**Frontend**
| File | Change |
|------|--------|
| `ChannelRow.svelte` | Add stale badge, last activity column |
| Channel table config | Add isStale filter, lastEventAt sort |
| `channel.ts` utils | Add stale badge helper |

### Migration File
Create new Liquibase changeset: `YYYYMMDDHHMM-channel-stale-tracking.xml`

### Test Scenarios
- Event ingestion updates `last_event_at` and clears `is_stale`
- Scheduled job marks old channels as stale
- New channels (created < 7 days ago) not marked stale
- Filter by stale returns correct channels
- Sort by lastEventAt works correctly
