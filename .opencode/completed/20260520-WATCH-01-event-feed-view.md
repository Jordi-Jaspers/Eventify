# Event Feed View

**Completed:** 2026-05-20
**Epic:** WATCH
**Source:** `.opencode/refined/WATCH-01-event-feed-view.md`

## Summary

Added chronological event feed tab to the watchlist monitor page with multi-channel aggregation, severity/channel filters, live polling, infinite scroll, and share URL support.

## Plan Approved by the user:

### Requirements Summary

- Add "Events" tab alongside existing "Timeline" tab on monitor page
- Chronological feed of events across all channels in the selected watchlist
- Filter by severity and channel
- Live polling mode with green pulse indicator
- Infinite scroll pagination
- Tab persists across refresh (sessionStorage) and is included in share URL

### Technical Approach

- Backend: Add `channelIds` MULTI_NUMERIC filter to EventMetaData, add `title`/`channelName` to EventSearchResponse, add `@Mapping` for channel.name in EventMapper
- Frontend: New EventFeed component with EventFeedService, tab state in MonitorPageService, shared event search utils

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Create EventMapper + EventMetaData tests |
| 2.2 | spring-backend-agent | Implement backend changes |
| 3.2 | svelte-frontend-agent | Build EventFeed UI (multiple iterations) |

## Implementation

### Backend

- `EventMetaData.java` — added `channelIds` MULTI_NUMERIC search field
- `EventSearchResponse.java` — added `title`, `channelName` fields
- `EventMapper.java` — added `@Mapping(source = "channel.name", target = "channelName")`

### Frontend

- `EventFeed.svelte` — chronological timeline-dot feed with filters, shimmer skeleton, empty states, live mode
- `EventFeedService.svelte.ts` — multi-channel feed service with polling, pagination, filter state
- `eventSearchUtils.ts` — shared search body/input builders
- `UserEventController.ts` / `OrganizationEventController.ts` — unified search methods
- `MonitorCanvas.svelte` — tab bar (Timeline/Events), passes props to EventFeed
- `MonitorPageService.svelte.ts` — activeTab state with sessionStorage persistence, share URL includes tab
- `monitor.service.ts` — MonitorTab type, tab in parse/build URL functions
- `types.ts` — EventFeedSeverity, EventFeedChannel types

### Deviations from Plan

- Removed grouped mode (was fundamentally broken with infinite scroll)
- Added tab persistence via sessionStorage (user request)
- Added share URL tab support (user request)
- Liquibase checksum fixes for unrelated changesets

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | EventMapper + EventMetaData tests | Complete |
| spring-backend-agent | Backend implementation | Complete |
| svelte-frontend-agent | EventFeed UI (7 iterations) | Complete |
| deep-research-agent | Codebase research (multiple) | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/event/model/EventMetaData.java` — channelIds field
- `server/src/main/java/io/github/eventify/api/event/model/response/EventSearchResponse.java` — title, channelName
- `server/src/main/java/io/github/eventify/api/event/model/mapper/EventMapper.java` — channel.name mapping
- `server/src/test/java/io/github/eventify/api/event/model/mapper/EventMapperTest.java` — new
- `server/src/test/java/io/github/eventify/api/event/model/EventMetaDataTest.java` — new
- `server/src/main/resources/db/changelog/changesets/202602171400-PRD-channel-staleness-tracking.xml` — runOnChange fix
- `server/src/main/resources/db/changelog/changesets/202605201000-PRD-subscription-table.xml` — removed duplicate changeset
- `client/src/lib/components/monitor/EventFeed.svelte` — new
- `client/src/lib/api/event/service/EventFeedService.svelte.ts` — new
- `client/src/lib/api/event/eventSearchUtils.ts` — new
- `client/src/lib/api/event/UserEventController.ts` — unified search
- `client/src/lib/api/event/OrganizationEventController.ts` — unified search
- `client/src/lib/api/event/service/EventService.svelte.ts` — updated imports
- `client/src/lib/components/monitor/MonitorCanvas.svelte` — tabs, EventFeed integration
- `client/src/lib/components/monitor/types.ts` — feed types
- `client/src/lib/components/monitor/index.ts` — export
- `client/src/lib/api/monitor/monitor.service.ts` — MonitorTab, tab in URL
- `client/src/lib/api/monitor/service/MonitorPageService.svelte.ts` — activeTab, sessionStorage

## Tests

- 10 backend tests written, all passing
- Frontend tests skipped per project config
