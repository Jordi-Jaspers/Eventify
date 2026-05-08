# Duration Details Modal

**Completed:** 2026-02-11
**Epic:** WATCHLIST
**Story:** WATCHLIST-11 / USER_CHANGE_REQUEST
**Original Request:** Add ability to click on timeline duration segments in the Monitor page to see event details in a modal.

## Summary

Implemented a Duration Details Modal that appears when users click on timeline segments in the Monitor page. The modal shows:
- Channel information with current severity indicator
- A mini-timeline for navigating between durations (previous/selected/next)
- An events list with infinite scroll for the selected duration's time range

## Features Implemented

### Backend (Event Search API)
- `POST /v1/user/events/search` - Search events for user's personal channels
- `POST /v1/organization/{orgId}/events/search` - Search events for org channels
- EventService with JPA Specification pattern for filtering
- EventSecurityService with channel access validation
- EventSearchResponse DTO with timestamp, message fields

### Frontend (Modal Components)
- **DurationDetailsModal.svelte** - Main modal with enhanced header (channel icon, pulse indicator, date), gradient accent, time range display with duration length
- **MiniTimeline.svelte** - Navigation timeline showing selected duration + adjacent durations with Previous/Next buttons, position indicator (X of Y)
- **EventsList.svelte** - Infinite scroll events list with initial centered spinner, empty state, event count, timeline dots with hover effects
- **API Controllers** - UserEventController.ts and OrganizationEventController.ts for fetching events

### Integration
- MonitorRow, MonitorGroup, TimelineBar wired with onSegmentClick handlers
- Both user and organization monitor pages updated with modal state
- Events fetched using actual duration start/end times (not clamped to view window)

## Technical Details

### Key Design Decisions
1. **Separate API for events** - Events are fetched on-demand when modal opens, not preloaded with timeline data
2. **Remount on duration change** - EventsList uses Svelte `{#key}` block to remount and refetch when selected duration changes
3. **Actual duration times** - Modal uses `selectedDuration.startTime/endTime` directly, ensuring all events in the duration are fetched regardless of the monitor's visible window

### Known Limitation (Added to Backlog)
The MiniTimeline uses the clamped timeline data from the monitor page. If a duration extends beyond the visible window (e.g., a 2-hour duration in a 1-hour window), the timeline visualization shows clamped times even though events are correctly fetched for the full duration. A future story will add a dedicated API endpoint to fetch actual duration details.

## Agents Used

| Agent | Task |
|-------|------|
| spring-testing-agent | Backend test creation |
| spring-backend-agent | Event search API implementation |
| svelte-frontend-agent | Modal and component creation |
| Orchestrator | Bug fixes, API parameter fix, timeline redesign |

## Files Created

### Backend
- `server/src/main/java/io/github/eventify/api/event/controller/UserEventController.java`
- `server/src/main/java/io/github/eventify/api/event/controller/OrganizationEventController.java`
- `server/src/main/java/io/github/eventify/api/event/service/EventService.java`
- `server/src/main/java/io/github/eventify/api/event/service/EventSecurityService.java`
- `server/src/main/java/io/github/eventify/api/event/model/EventMetaData.java`
- `server/src/main/java/io/github/eventify/api/event/model/response/EventSearchResponse.java`
- `server/src/test/java/io/github/eventify/api/event/controller/UserEventControllerTest.java`
- `server/src/test/java/io/github/eventify/api/event/controller/OrganizationEventControllerTest.java`

### Frontend
- `client/src/lib/components/monitor/DurationDetailsModal.svelte`
- `client/src/lib/components/monitor/MiniTimeline.svelte`
- `client/src/lib/components/monitor/EventsList.svelte`
- `client/src/lib/api/event/UserEventController.ts`
- `client/src/lib/api/event/OrganizationEventController.ts`
- `client/src/lib/components/ui/dialog/` (shadcn dialog components)
- `client/test/components/monitor-modal.spec.ts`

## Files Modified

### Backend
- `server/src/main/java/io/github/eventify/api/Paths.java` - Added event search paths
- `server/src/main/java/io/github/eventify/api/event/model/Event.java` - Minor updates
- `server/src/main/java/io/github/eventify/api/event/model/mapper/EventMapper.java` - Added toPageResource
- `server/src/main/java/io/github/eventify/api/event/repository/EventRepository.java` - Added Specification support
- `server/src/main/java/io/github/eventify/api/channel/service/ChannelSecurityService.java` - Added access check methods

### Frontend
- `client/src/lib/components/monitor/MonitorRow.svelte` - Added onSegmentClick handler
- `client/src/lib/components/monitor/MonitorGroup.svelte` - Pass click handler to sub-channels
- `client/src/lib/components/monitor/TimelineBar.svelte` - Wire onclick
- `client/src/lib/components/monitor/index.ts` - Export new components
- `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte` - Added modal state
- `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/+page.svelte` - Added modal for org
- `client/src/lib/api/models.ts` - Added EventSearchResponse, PageResourceEventSearchResponse
- `client/src/lib/utils/date.ts` - Added formatTime function

## Tests

- Backend: 27+ tests for UserEventController and OrganizationEventController
- Frontend: Screenshot tests in monitor-modal.spec.ts

## Bug Fixes During Implementation

1. **API parameter mismatch** - Frontend was sending `numericValue` for channelId but backend expected `textValue`. Fixed to use `textValue: String(channelId)`.

## Notes

- Modal uses glassmorphism design consistent with the rest of the application
- Mini-timeline provides context by showing adjacent durations
- Infinite scroll handles large event sets efficiently
- Empty states provide clear feedback when no events exist
