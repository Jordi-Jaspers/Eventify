# Duration Details API Endpoint

**Completed:** 2026-02-12
**Epic:** MONITOR
**Story:** MONITOR-01

## Summary

Added backend API endpoints and frontend integration to fetch accurate duration details when clicking on timeline segments in the monitor dashboard. Previously, durations were clamped to the visible window, causing confusion about when incidents actually started. Now the modal shows true duration boundaries with cursor-based navigation.

## Agents Used

| Agent | Task |
|-------|------|
| spring-testing-agent | Unit tests for DurationService, integration tests for controller endpoints |
| spring-backend-agent | API endpoints, service logic, repository queries |
| svelte-frontend-agent | DurationController, DurationService, MiniTimeline enhancements |

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/user/channel/{id}/durations` | Get duration window for user's channel |
| POST | `/v1/organization/{orgId}/channels/{id}/durations` | Get duration window for org channel |

### Request Body
```json
{
  "timestamp": "2026-02-11T10:30:00Z",
  "direction": "AROUND"  // AROUND | BEFORE | AFTER
}
```

### Response
```json
{
  "durations": [
    { "severity": "OK", "startTime": "...", "endTime": "..." },
    { "severity": "CRITICAL", "startTime": "...", "endTime": null }
  ],
  "selectedIndex": 1,
  "hasPrevious": true,
  "hasNext": false
}
```

## Files Created

### Backend
- `server/src/main/java/io/github/eventify/api/monitor/model/DurationDirection.java` - Enum for navigation direction
- `server/src/main/java/io/github/eventify/api/monitor/model/request/DurationDetailsRequest.java` - Request DTO
- `server/src/main/java/io/github/eventify/api/monitor/model/response/DurationDetailsResponse.java` - Response DTO
- `server/src/main/java/io/github/eventify/api/monitor/service/DurationService.java` - Window calculation logic
- `server/src/main/java/io/github/eventify/api/monitor/util/DurationBuilder.java` - Utility for building duration lists
- `server/src/test/java/io/github/eventify/api/monitor/service/DurationServiceTest.java` - 11 unit tests
- `server/src/test/java/io/github/eventify/api/channel/controller/ChannelDurationControllerTest.java` - 11 integration tests

### Frontend
- `client/src/lib/api/monitor/DurationController.ts` - API client for duration endpoints
- `client/src/lib/api/monitor/service/DurationService.svelte.ts` - State management + cursor navigation

## Files Modified

### Backend
- `server/src/main/java/io/github/eventify/api/Paths.java` - Added DURATIONS path constant
- `server/src/main/java/io/github/eventify/api/channel/controller/UserChannelController.java` - Added getChannelDurations endpoint
- `server/src/main/java/io/github/eventify/api/channel/controller/OrganizationChannelController.java` - Added getOrganizationChannelDurations endpoint
- `server/src/main/java/io/github/eventify/api/event/repository/EventRepository.java` - New queries for duration boundaries
- `server/src/main/java/io/github/eventify/api/monitor/model/TimelineDuration.java` - Added factory method
- `server/src/main/java/io/github/eventify/api/monitor/util/TimelineBuilder.java` - Uses factory method

### Frontend
- `client/src/lib/components/monitor/DurationDetailsModal.svelte` - Fetches from API on open
- `client/src/lib/components/monitor/MiniTimeline.svelte` - Non-linear time scale, collapsed segments (>24h), ongoing indicators, cursor navigation
- `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte` - Removed allDurations prop
- `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/+page.svelte` - Removed allDurations prop
- `client/src/lib/types/api.d.ts` - Regenerated from OpenAPI

## Key Features

### Backend
- **Window calculation algorithm**: Centers around clicked timestamp, expands to 15-min minimum or 10 durations max
- **Cursor navigation**: AROUND (initial), BEFORE (prev page), AFTER (next page)
- **hasPrevious/hasNext**: Determined by checking if events exist beyond window boundaries
- **Security**: @PreAuthorize with channelSecurity for user and org channels

### Frontend MiniTimeline
- **Non-linear time scale**: Proportional widths for durations, with collapsed segments for >24h
- **Collapsed segments**: Hatched pattern with "Xd" label for very long durations
- **Ongoing indicator**: Pulsing right edge + arrow for live durations
- **Cursor-based navigation**: Local state within window, API fetch only at edges
- **Prev/Next buttons**: Disabled state driven by hasPrevious/hasNext from API

## Tests

- 11 unit tests (DurationServiceTest)
- 11 integration tests (ChannelDurationControllerTest)
- 2 screenshot tests (monitor-modal dark/light)

All tests passing.

## Notes

- Direction changed from query parameter to request body (POST) for consistency
- MiniTimeline segments now use `TimelineDuration[]` from API response directly
- Frontend maintains local `currentIndex` separate from API's `selectedIndex` to allow smooth navigation within window
