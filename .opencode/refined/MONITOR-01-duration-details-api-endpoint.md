# Duration Details API Endpoint

**Epic**: Monitor
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** user viewing the monitor dashboard
**I want** to see accurate duration times when I click on a timeline segment
**So that** I can understand the true extent of each severity period, even when it extends beyond the visible monitor window

## 2. Business Context & Value
Currently, when a user clicks a duration segment on the monitor timeline, the `DurationDetailsModal` shows **clamped data** from the visible window (e.g., 1 hour). If a CRITICAL duration actually started 2 hours ago, the modal incorrectly shows it starting at the window boundary.

This causes confusion when:
- Investigating incident timelines ("When did this actually start?")
- Understanding duration patterns ("Was this a 5-minute blip or a 3-hour outage?")
- Navigating between adjacent durations

The new endpoint provides **actual duration boundaries** from the database, enabling accurate timeline visualization in the modal.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Basic duration details retrieval
    * Given a channel with events creating multiple durations
    * When I request duration details for a specific timestamp
    * Then I receive the duration containing that timestamp with actual start/end times
    * And I receive adjacent durations to fill a minimum 15-minute window
    * And I receive `hasPrevious`/`hasNext` flags indicating if more durations exist

* [ ] **Scenario 2**: Duration extends beyond visible window (cutoff visualization)
    * Given a duration that started 2 hours before the monitor's visible window
    * When the modal displays this duration
    * Then a cutoff line indicates the duration extends beyond the display
    * And an annotation shows the actual start time (e.g., "← Started 8:00")
    * And the "Previous" button navigates to the preceding duration

* [ ] **Scenario 3**: Multiple small durations (flapping channel)
    * Given a channel with rapid severity changes (e.g., 1-2 minute durations)
    * When I request duration details
    * Then the response includes enough durations to fill ~15 minutes
    * And no more than 10 durations are returned
    * And all durations have accurate start/end times

* [ ] **Scenario 4**: Very long previous duration (collapsed display)
    * Given a selected duration preceded by a 41-day OK period
    * When the modal displays the timeline
    * Then the long duration is collapsed to a narrow segment
    * And labeled appropriately (e.g., "41 days" or "Since Jan 1")

* [ ] **Scenario 5**: First duration in channel history
    * Given I select the very first duration (NO_DATA from channel creation)
    * When I view the duration details
    * Then `hasPrevious` is `false`
    * And the "Previous" button is disabled

* [ ] **Scenario 6**: Live/ongoing duration
    * Given the selected duration has no end time (still in progress)
    * When I view the duration details
    * Then `endTime` is `null` for the ongoing duration
    * And the UI shows a pulsing/live indicator

* [ ] **Scenario 7**: Cursor-based navigation at window edge
    * Given I'm viewing a window of durations with `hasNext: true`
    * When I navigate to the last duration and click "Next"
    * Then the frontend requests a new window starting from that duration's end time
    * And I receive the next set of durations

* [ ] **Scenario 8**: Security - User can only access own channels
    * Given I request duration details for a channel I don't own
    * When the API processes my request
    * Then I receive a 403 Forbidden response

* [ ] **Scenario 9**: Security - Org member can access org channels
    * Given I'm a member of an organization
    * When I request duration details for an org channel
    * Then I receive the duration data (respecting org permissions)

## 4. Technical Requirements

### API Design

**New Endpoints**:
```
GET /api/v1/channels/{channelId}/durations
GET /api/v1/organizations/{orgId}/channels/{channelId}/durations
```

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `timestamp` | ISO-8601 | Yes | The timestamp to center the window around |
| `direction` | `around` \| `before` \| `after` | No (default: `around`) | Navigation direction for cursor-based pagination |

**Response Schema**:
```json
{
  "durations": [
    {
      "severity": "OK",
      "startTime": "2026-02-11T08:00:00Z",
      "endTime": "2026-02-11T10:30:00Z"
    },
    {
      "severity": "CRITICAL", 
      "startTime": "2026-02-11T10:30:00Z",
      "endTime": "2026-02-11T10:45:00Z"
    },
    {
      "severity": "WARNING",
      "startTime": "2026-02-11T10:45:00Z",
      "endTime": null
    }
  ],
  "selectedIndex": 1,
  "hasPrevious": true,
  "hasNext": false
}
```

### Database Query Strategy

The query should:
1. Find the duration containing the given timestamp (by finding severity-change boundaries)
2. Expand the window to include ~15 minutes of coverage OR up to 10 durations
3. Determine `hasPrevious` by checking if events exist before the window
4. Determine `hasNext` by checking if events exist after the window (or if last duration is ongoing)

**Note**: This does NOT require scanning the entire channel history. The query should use indexed timestamp lookups with `LIMIT` clauses.

### Backend Components

| Component | Location | Responsibility |
|-----------|----------|----------------|
| `DurationController` | `api/channel/controller/` | REST endpoints with security |
| `DurationService` | `api/channel/service/` | Business logic, window calculation |
| `DurationDetailsResponse` | `api/channel/model/response/` | Response DTO |
| Reuse `TimelineDuration` | `api/monitor/model/` | Duration structure (already exists) |

### Security
- User channels: `@PreAuthorize("@channelSecurity.canAccess(#channelId, principal)")`
- Org channels: `@PreAuthorize("@channelSecurity.canAccessOrgChannel(#channelId, #orgId, principal)")`

### Frontend Changes

| Component | Changes |
|-----------|---------|
| `DurationDetailsModal.svelte` | Call new API on open, pass data to MiniTimeline |
| `MiniTimeline.svelte` | Implement cutoff lines, collapsed segments, cursor navigation |
| `duration-service.ts` | New service for API calls |

### Constants
```java
Duration MINIMUM_DISPLAY_WINDOW = Duration.ofMinutes(15);
int MAXIMUM_DURATIONS = 10;
```

## 5. Design & UI/UX

### MiniTimeline with Cutoff Line
```
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  ┊  [======== OK (selected) ========][---- CRITICAL ----]      │
│  ┊                                                              │
│  ┊← Started 8:00              10:30                  11:00      │
└─────────────────────────────────────────────────────────────────┘
```

### MiniTimeline with Collapsed Long Duration
```
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  [OK ⋯][========= CRITICAL (selected) =========][  WARNING  ]  │
│  "41d"  10:15                             10:45     11:00       │
└─────────────────────────────────────────────────────────────────┘
```

### MiniTimeline with Multiple Small Durations (Flapping)
```
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  [OK][WARN][▓CRIT▓][OK][WARN][CRIT]                             │
│  10:00   10:04↑        10:07    10:12                           │
│            selected                                              │
└─────────────────────────────────────────────────────────────────┘
```

## 6. Duration Logic & Scenarios

### 6.1 What is a Duration?

A **duration** represents a continuous time period where a channel maintains the same severity level. Durations are derived from events, not stored directly.

```
Events:      [OK@10:00]  [WARN@10:15]  [CRIT@10:30]  [OK@10:45]
                 ↓            ↓             ↓            ↓
Durations:   [--OK--][--WARNING--][--CRITICAL--][----OK----]
             10:00   10:15        10:30         10:45      now
```

**Key Rules**:
- A duration **starts** when a new severity event occurs
- A duration **ends** when the next different-severity event occurs
- The **first duration** starts at channel creation with severity `NO_DATA`
- The **last duration** has `endTime: null` (ongoing/live)

### 6.2 Window Calculation Algorithm

```
INPUT: timestamp, direction, channelId
OUTPUT: { durations[], selectedIndex, hasPrevious, hasNext }

CONSTANTS:
  MINIMUM_WINDOW = 15 minutes
  MAXIMUM_DURATIONS = 10

ALGORITHM:

1. FIND SELECTED DURATION
   - Query event at or before timestamp → this gives the severity
   - Query next event after timestamp → this gives the end boundary
   - Selected duration = { severity, startTime, endTime }

2. EXPAND WINDOW (based on direction)
   
   If direction = "around":
     - Start with selected duration
     - Alternately add previous/next durations until:
       a) Window spans >= MINIMUM_WINDOW, OR
       b) We have MAXIMUM_DURATIONS, OR
       c) No more durations exist in either direction
   
   If direction = "before":
     - Start from timestamp, look backward
     - Add durations until window filled or channel start reached
     - selectedIndex = last index (rightmost)
   
   If direction = "after":
     - Start from timestamp, look forward
     - Add durations until window filled or current time reached
     - selectedIndex = 0 (leftmost)

3. DETERMINE NAVIGATION FLAGS
   - hasPrevious = EXISTS(event before first duration's startTime)
   - hasNext = EXISTS(event after last duration's endTime) OR last duration is ongoing

4. RETURN response
```

### 6.3 Scenario Catalog

#### Scenario A: Selected Duration Fully Within Visible Range
```
Channel Events: [OK@9:50] [CRIT@10:20] [OK@10:45] [WARN@11:15]
User clicks on: CRITICAL duration (10:20-10:45)

Window calculation:
  - Selected: CRITICAL 10:20→10:45 (25 min)
  - Window already >= 15 min with just selected
  - Add adjacent for context: prev OK, next OK

API Response:
  durations: [
    { severity: OK, startTime: 9:50, endTime: 10:20 },
    { severity: CRITICAL, startTime: 10:20, endTime: 10:45 },  ← selected
    { severity: OK, startTime: 10:45, endTime: 11:15 }
  ]
  selectedIndex: 1
  hasPrevious: true (events exist before 9:50)
  hasNext: true (events exist after 11:15)

UI Display:
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  [----OK----][▓▓▓▓CRITICAL▓▓▓▓][----OK----]                    │
│  9:50       10:20            10:45       11:15                  │
└─────────────────────────────────────────────────────────────────┘
```

#### Scenario B: Selected Duration Extends Before Window (Cutoff)
```
Channel Events: [OK@8:00] [CRIT@10:30]
User clicks on: OK duration while viewing 10:00-11:00 monitor window
Actual OK duration: 8:00→10:30 (2.5 hours!)

Window calculation:
  - Selected: OK 8:00→10:30
  - Duration > 24h? No, show proportionally BUT with cutoff
  - Add next: CRITICAL 10:30→now

API Response:
  durations: [
    { severity: OK, startTime: 8:00, endTime: 10:30 },  ← selected
    { severity: CRITICAL, startTime: 10:30, endTime: null }
  ]
  selectedIndex: 0
  hasPrevious: true
  hasNext: false (ongoing)

UI Display (with cutoff at display window start):
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  ┊  [======== OK (selected) ========][---- CRITICAL ----→      │
│  ┊← Started 8:00              10:30                   now       │
│  ┊                                                   (live)     │
│  cutoff                                                         │
└─────────────────────────────────────────────────────────────────┘

Frontend logic:
  - Display window: 10:00 → now (from monitor context)
  - Duration starts at 8:00, before display window
  - Show cutoff line at left edge
  - Annotate "← Started 8:00"
```

#### Scenario C: Very Long Previous Duration (Collapsed)
```
Channel Events: [OK@Jan-01] [CRIT@Feb-11 10:15] [WARN@Feb-11 10:45]
User clicks on: CRITICAL duration

Window calculation:
  - Selected: CRITICAL 10:15→10:45 (30 min)
  - Previous: OK Jan-01→Feb-11 10:15 (41 days!)
  - Next: WARNING 10:45→now

API Response:
  durations: [
    { severity: OK, startTime: "2026-01-01T00:00:00Z", endTime: "2026-02-11T10:15:00Z" },
    { severity: CRITICAL, startTime: "2026-02-11T10:15:00Z", endTime: "2026-02-11T10:45:00Z" },
    { severity: WARNING, startTime: "2026-02-11T10:45:00Z", endTime: null }
  ]
  selectedIndex: 1
  hasPrevious: true
  hasNext: false

UI Display (long duration collapsed):
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  [OK ⋯][========= CRITICAL (selected) =========][  WARNING →   │
│  "41d"  10:15                             10:45     now         │
└─────────────────────────────────────────────────────────────────┘

Frontend logic:
  - OK duration = 41 days > 24 hours threshold
  - Collapse to narrow segment with "41d" label
  - Click on collapsed segment still selects it
```

#### Scenario D: Multiple Small Durations (Flapping)
```
Channel Events: [OK@10:00] [WARN@10:02] [CRIT@10:04] [OK@10:05] [WARN@10:07] [CRIT@10:10] [OK@10:12]
User clicks on: CRITICAL @10:04-10:05 (1 minute!)

Window calculation:
  - Selected: CRITICAL 10:04→10:05 (1 min)
  - Window = 1 min < 15 min minimum
  - Expand: add prev WARN (2 min), next OK (2 min) → total 5 min
  - Still < 15 min, keep expanding...
  - Final: 6 durations spanning 10:00→10:12 (12 min, close enough)

API Response:
  durations: [
    { severity: OK, startTime: 10:00, endTime: 10:02 },
    { severity: WARNING, startTime: 10:02, endTime: 10:04 },
    { severity: CRITICAL, startTime: 10:04, endTime: 10:05 },  ← selected
    { severity: OK, startTime: 10:05, endTime: 10:07 },
    { severity: WARNING, startTime: 10:07, endTime: 10:10 },
    { severity: CRITICAL, startTime: 10:10, endTime: 10:12 }
  ]
  selectedIndex: 2
  hasPrevious: true
  hasNext: true

UI Display:
┌─────────────────────────────────────────────────────────────────┐
│  ← Previous                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  [OK][WARN][▓CRIT▓][OK][WARN][CRIT]                             │
│  10:00   10:04↑        10:07    10:12                           │
│            selected                                              │
└─────────────────────────────────────────────────────────────────┘
```

#### Scenario E: First Duration Ever (Channel Start)
```
Channel created: 2026-02-10T00:00:00Z
Channel Events: [CRIT@Feb-11 10:00]
User clicks on: The NO_DATA duration (before any events)

Window calculation:
  - Selected: NO_DATA from channel creation → 10:00 (34 hours)
  - Previous: none (this is the first)
  - Next: CRITICAL 10:00→now

API Response:
  durations: [
    { severity: NO_DATA, startTime: "2026-02-10T00:00:00Z", endTime: "2026-02-11T10:00:00Z" },
    { severity: CRITICAL, startTime: "2026-02-11T10:00:00Z", endTime: null }
  ]
  selectedIndex: 0
  hasPrevious: false  ← No previous exists!
  hasNext: false

UI Display:
┌─────────────────────────────────────────────────────────────────┐
│  (disabled)                                          Next →     │
├─────────────────────────────────────────────────────────────────┤
│  [▓▓NO_DATA (selected)▓▓][-------- CRITICAL --------→          │
│  "Since Feb 10"          10:00                      now         │
└─────────────────────────────────────────────────────────────────┘
```

#### Scenario F: Only One Duration Exists (New Channel, No Events)
```
Channel created: 2026-02-11T09:00:00Z
Channel Events: (none)

Window calculation:
  - Only duration: NO_DATA from 09:00 → now (ongoing)

API Response:
  durations: [
    { severity: NO_DATA, startTime: "2026-02-11T09:00:00Z", endTime: null }
  ]
  selectedIndex: 0
  hasPrevious: false
  hasNext: false

UI Display:
┌─────────────────────────────────────────────────────────────────┐
│  (disabled)                                        (disabled)   │
├─────────────────────────────────────────────────────────────────┤
│  [▓▓▓▓▓▓▓▓▓▓▓▓▓ NO_DATA (selected) ▓▓▓▓▓▓▓▓▓▓▓▓▓→              │
│  09:00                                            now           │
└─────────────────────────────────────────────────────────────────┘
```

#### Scenario G: Navigating at Window Edge
```
Current window shows durations 1-6 of many more
User is at selectedIndex: 5 (rightmost) and clicks "Next →"

Frontend behavior:
  1. Check: selectedIndex (5) === durations.length - 1 (5)? YES
  2. Check: hasNext? YES
  3. Make API call: GET /durations?timestamp={lastDuration.endTime}&direction=after
  
API returns new window:
  durations: [6, 7, 8, 9, 10, 11]  (next batch)
  selectedIndex: 0  (first in new window = what was "next")
  hasPrevious: true
  hasNext: true

Frontend updates display with new window.
```

#### Scenario H: Navigating Within Window (No API Call)
```
Current window shows durations [A, B, C, D, E]
User is at selectedIndex: 2 (C) and clicks "Next →"

Frontend behavior:
  1. Check: selectedIndex (2) === durations.length - 1 (4)? NO
  2. Simply update: selectedIndex = 3
  3. No API call needed!

UI updates to show D as selected.
```

### 6.4 Frontend Display Rules

| Condition | Display Behavior |
|-----------|------------------|
| Duration < 24 hours | Proportional width based on actual time |
| Duration ≥ 24 hours | Collapsed to ~10% width, labeled "Xd" or "Since [date]" |
| Duration starts before display window | Cutoff line (`┊`) with "← Started [time]" annotation |
| Duration ends after display window | Cutoff line at right edge |
| Duration ongoing (endTime: null) | Pulsing right edge, "→" indicator, "now" label |
| NO_DATA duration | Gray color, "No data" or "Since [channel creation]" label |
| `hasPrevious: false` | "← Previous" button disabled |
| `hasNext: false` | "Next →" button disabled |

### 6.5 Edge Cases & Error Handling

| Edge Case | Handling |
|-----------|----------|
| Timestamp in future | Return latest/ongoing duration as selected |
| Timestamp before channel creation | Return first (NO_DATA) duration as selected |
| Channel has no events | Return single NO_DATA duration from creation to now |
| Invalid channelId | 404 Not Found |
| Unauthorized access | 403 Forbidden |
| Malformed timestamp | 400 Bad Request with validation message |

## 7. Implementation Notes / Research

### Existing Code References
- `TimelineDuration` model: `server/src/main/java/io/github/eventify/api/monitor/model/TimelineDuration.java`
- `TimelineBuilder` utility: `server/src/main/java/io/github/eventify/api/monitor/util/TimelineBuilder.java`
- `ChannelSecurityService`: `server/src/main/java/io/github/eventify/api/channel/service/ChannelSecurityService.java`
- `DurationDetailsModal.svelte`: `client/src/lib/components/monitor/DurationDetailsModal.svelte`
- `MiniTimeline.svelte`: `client/src/lib/components/monitor/MiniTimeline.svelte`

### Query Approach
The duration calculation is based on **severity changes** in events. Consider reusing logic from `TimelineBuilder.fromEvents()` but adapted for:
- Unbounded lookback (not clamped to monitor window)
- Cursor-based window expansion
- Efficient LIMIT-based queries

### Potential Pitfalls
- **Performance**: Avoid `COUNT(*)` or full table scans. Use `EXISTS` for `hasPrevious`/`hasNext`.
- **Timezone handling**: All times in UTC (ISO-8601 with Z suffix)
- **Live durations**: `endTime: null` when the last event is still the current state
- **Channel creation time**: Needed to determine the start of the first NO_DATA duration
