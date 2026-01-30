# Duration Details Modal

**Epic:** WATCHLIST
**Story:** 11 - Duration Details Modal
**Priority:** High
**Created:** 2026-01-29

## User Story

As a user monitoring my watchlist, I want to click on a duration segment in the timeline to see the events that occurred during that time period, so I can investigate issues and understand what happened.

## Description

When viewing the Monitor page, clicking on any duration segment (colored block in the timeline) opens a centered modal showing:
1. Channel name with current severity badge
2. A 1-hour mini timeline centered around the selected duration
3. The selected duration highlighted, with other durations clickable for navigation
4. An infinite-scrolling list of events within the selected duration

## Acceptance Criteria

### Modal Trigger
- [ ] Clicking a duration segment on the Monitor page opens the modal
- [ ] Modal appears centered with a backdrop overlay
- [ ] Modal closes when clicking the X button
- [ ] Modal closes when clicking outside the modal (backdrop)

### Modal Header
- [ ] Shows channel name with a status dot indicator
- [ ] Shows current severity as a colored badge (OK/WARNING/CRITICAL/NO_DATA)
- [ ] Close button (X) in top-right corner

### Mini Timeline
- [ ] Shows 1-hour window centered around the selected duration
- [ ] Displays time labels (start and end of the 1-hour window)
- [ ] Duration segments shown as pill-shaped blocks with rounded corners
- [ ] Selected duration is visually highlighted (double border, arrow indicator)
- [ ] Severity labels shown below each segment
- [ ] Clicking another duration navigates to it (updates events list)
- [ ] Color-coded: OK=green, WARNING=amber, CRITICAL=red, NO_DATA=gray

### Duration Info Bar
- [ ] Shows time range of selected duration (HH:mm:ss format)
- [ ] Shows total event count for the duration

### Events List
- [ ] Shows events sorted by timestamp (newest first)
- [ ] Each event displays: timestamp (HH:mm:ss) and message
- [ ] No severity shown (already known from duration)
- [ ] No metadata shown (keep it simple)
- [ ] Infinite scroll - loads more events when scrolling to bottom
- [ ] Loading indicator at bottom while fetching more
- [ ] Vertical line connecting events (timeline feel)

### Empty State
- [ ] When duration has 0 events, show "No events in this duration" message
- [ ] Same visual treatment as other empty states in the app

## UI Design

```
╭────────────────────────────────────────────────────────╮
│                                                        │
│  ╭────────────────────────────────────────────────╮    │
│  │  ● Payment Service                  CRITICAL   │  ✕ │
│  ╰────────────────────────────────────────────────╯    │
│                                                        │
│   14:00            14:30             15:00             │
│   ╭────╮╭────╮╭══════════════╮╭────╮╭────╮╭────╮╭────╮ │
│   │    ││    ││   ●  ●  ●    ││    ││    ││    ││    │ │
│   ╰────╯╰────╯╰══════════════╯╰────╯╰────╯╰────╯╰────╯ │
│    OK    OK       CRITICAL      WARN  OK    OK    OK   │
│                       ▲                                │
│                                                        │
│   ╭─────────────────────────────────────────────╮      │
│   │  14:25 → 14:35                   12 events  │      │
│   ╰─────────────────────────────────────────────╯      │
│                                                        │
│   ┃ 14:35:22                                           │
│   ┃ Connection restored to database                    │
│   ┃                                                    │
│   ┃ 14:33:15                                           │
│   ┃ Retry attempt 3/3 failed                           │
│   ┃                                                    │
│   ┃ 14:31:08                                           │
│   ┃ Retry attempt 2/3 failed                           │
│   ┃                                                    │
│   ○ loading...                                         │
│                                                        │
╰────────────────────────────────────────────────────────╯
```

### Empty State

```
╭────────────────────────────────────────────────────────╮
│                                                        │
│  ╭────────────────────────────────────────────────╮    │
│  │  ● Payment Service                   NO_DATA   │  ✕ │
│  ╰────────────────────────────────────────────────╯    │
│                                                        │
│   14:00            14:30             15:00             │
│   ╭────╮╭────╮╭══════════════╮╭────╮╭────╮╭────╮╭────╮ │
│   │    ││    ││   ●  ●  ●    ││    ││    ││    ││    │ │
│   ╰────╯╰────╯╰══════════════╯╰────╯╰────╯╰────╯╰────╯ │
│    OK    OK       NO_DATA       OK    OK    OK    OK   │
│                       ▲                                │
│                                                        │
│   ╭─────────────────────────────────────────────╮      │
│   │  14:25 → 14:35                    0 events  │      │
│   ╰─────────────────────────────────────────────╯      │
│                                                        │
│                                                        │
│                    ╭───────────╮                       │
│                    │  (empty)  │                       │
│                    ╰───────────╯                       │
│           No events in this duration                   │
│                                                        │
│                                                        │
╰────────────────────────────────────────────────────────╯
```

## Technical Design

### Backend

#### New API Endpoint

```
POST /v1/user/events/search
POST /v1/organization/{orgId}/events/search
```

**Request:**
```json
{
  "channelId": "uuid",
  "startTime": "2026-01-29T14:25:00Z",
  "endTime": "2026-01-29T14:35:00Z",
  "page": {
    "page": 0,
    "size": 20,
    "sort": [{"field": "timestamp", "direction": "DESC"}]
  }
}
```

**Response:**
```json
{
  "content": [
    { "message": "Connection restored to database", "timestamp": "2026-01-29T14:35:22Z" },
    { "message": "Retry attempt 3/3 failed", "timestamp": "2026-01-29T14:33:15Z" }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 12,
    "totalPages": 1
  }
}
```

#### Files to Create

| File | Purpose |
|------|---------|
| `UserEventController.java` | User-scoped event search endpoint |
| `OrganizationEventController.java` | Org-scoped event search endpoint |
| `EventService.java` | Business logic for event search |
| `EventSearchRequest.java` | Request DTO with channelId, time range, pagination |
| `EventSearchResponse.java` | Response DTO with message and timestamp only |
| `UserEventControllerTest.java` | Controller integration tests |
| `EventServiceTest.java` | Service unit tests |

#### Files to Modify

| File | Change |
|------|--------|
| `EventRepository.java` | Add paginated time-range query method |
| `ChannelSecurity.java` | Add authorization check for channel access |

### Frontend

#### Files to Create

| File | Purpose |
|------|---------|
| `DurationDetailsModal.svelte` | Main modal component |
| `MiniTimeline.svelte` | 1-hour timeline with clickable segments |
| `EventsList.svelte` | Infinite scroll events list |

#### Files to Modify

| File | Change |
|------|--------|
| `MonitorRow.svelte` | Wire `TimelineSegment.onclick` to open modal |
| `+page.svelte` (monitor) | Add modal state and component |

### Existing Patterns to Use

| Pattern | Location | Usage |
|---------|----------|-------|
| Dialog/Sheet | `lib/components/ui/dialog/` | Modal structure |
| SortablePageInput | jframe library | Pagination request |
| TimelineSegment | `lib/components/monitor/` | Reuse styling patterns |
| MonitorEmptyState | `lib/components/monitor/` | Empty state pattern |
| IntersectionObserver | Native API | Infinite scroll trigger |

## Implementation Order (TDD)

```
1. Testing Agent  → Write tests for EventService, EventController
2. Backend Agent  → Implement EventService, EventController, DTOs
3. ─── BACKEND REVIEW GATE ───
4. Frontend Agent → Build modal components, wire to Monitor page
5. ─── FRONTEND REVIEW GATE ───
6. Frontend Optimizer → Extract reusable patterns if needed
7. ─── UI POLISH GATE ───
8. UI Validation Loop
9. Complete & Document
```

## Out of Scope

- Metadata display in events (keep it simple)
- Severity display per event (redundant with duration)
- Channel detail page (separate feature)
- Event filtering/search within the modal
- Export events functionality

## Dependencies

- Monitor page must be functional (WATCHLIST-09) ✅
- Timeline components exist ✅
- Event entity and ingestion exist ✅

## Notes

- Reuse existing severity color palette from TimelineSegment
- Follow glassmorphism design patterns from STYLING-GUIDE.md
- After backend changes, run `bun run sync:api` to regenerate TypeScript types
- Consider extracting MiniTimeline as reusable component for future use
