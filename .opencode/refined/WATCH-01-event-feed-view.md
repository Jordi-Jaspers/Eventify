---
epic: "WATCH"
title: "Event Feed View"
estimate: L
status: ready
created: 2026-05-15
depends_on: []
labels: [backend, frontend]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** watchlist user\
**I want** a chronological event feed tab on the monitor page\
**So that** I can see raw events across all my watchlist channels in real-time\

## 2. Business Context & Value
The Timeline tab shows severity durations visually but hides individual events. Users need to drill into the raw event stream for debugging, incident investigation, and understanding what triggered severity changes. A live-updating feed makes Eventify a real-time monitoring tool rather than a historical dashboard.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Tab navigation on monitor page
    * Given I am on the watchlist monitor page
    * When I view the page header
    * Then I see "Timeline" and "Events" tabs, with Timeline active by default
    * When I click "Events"
    * Then the feed view loads and Timeline content is hidden (state preserved when switching back)

* [ ] **Scenario 2**: Multi-channel aggregated feed
    * Given my watchlist has 5 channels with events
    * When I open the Events tab
    * Then I see all events from all 5 channels merged chronologically (newest first)
    * And each event shows: severity dot, title (bold), message (muted, truncated at 2 lines), channel pill, relative timestamp

* [ ] **Scenario 3**: Channel pill click-to-filter
    * Given I see an event with channel pill "api-server"
    * When I click the pill
    * Then the channel filter updates to show only "api-server" events
    * And the feed reloads with that filter applied

* [ ] **Scenario 4**: Sticky filters bar
    * Given I am on the Events tab
    * When I scroll down
    * Then the filters bar (channel multi-select, severity multi-select) remains pinned at top
    * And I can filter by multiple channels and/or multiple severities simultaneously
    * And a "Clear filters" link appears when any filter is active

* [ ] **Scenario 5**: Time range sync with Timeline tab
    * Given I set a time range in the ConfigurePopover (shared with Timeline tab)
    * When I switch to the Events tab
    * Then the feed shows only events within that time range

* [ ] **Scenario 6**: Live mode
    * Given I toggle the "Live" button on
    * Then the feed polls every 10 seconds for new events
    * And new events are prepended at the top with a fade-in animation
    * And if I've scrolled down, a "↑ N new events" banner appears (click scrolls to top)
    * When I toggle Live off
    * Then polling stops

* [ ] **Scenario 7**: Infinite scroll pagination
    * Given there are 200 events matching my filters
    * When I scroll to the bottom of the loaded events
    * Then the next page loads automatically and appends below
    * And "Showing X of Y events" counter updates

* [ ] **Scenario 8**: Message truncation — expand inline
    * Given an event has a message longer than 2 lines
    * When I view it in the feed
    * Then the message is truncated with a "Show more" link
    * When I click "Show more"
    * Then the full message expands inline

* [ ] **Scenario 9**: Channel grouping toggle
    * Given I am on the Events tab
    * When I toggle "Group by channel"
    * Then events are grouped into collapsible sections per channel (channel header + event count)
    * And each section is independently scrollable/collapsible

* [ ] **Scenario 10**: Error handling via toast
    * Given the event search API fails
    * When the error occurs
    * Then a toast notification appears with the error message and a "Retry" CTA button
    * When I click "Retry"
    * Then the failed request is retried

* [ ] **Scenario 11**: Empty states
    * Given no events match current filters → "No events matching filters" + Clear filters link
    * Given watchlist has no channels → "Add channels to see events"
    * Given channels exist but no events in time range → "No events in this time range"

* [ ] **Scenario 12**: Loading skeleton
    * Given the feed is loading for the first time
    * When the request is in flight
    * Then pulse animation placeholders are shown (not a spinner)

## 4. Technical Requirements
* **API Changes**:
    - Add `channelIds` field to `EventMetaData` with `SearchType.MULTI_NUMERIC` mapping to `channel.id` (requires JFrame 1.1.0)
    - Add `title` and `channelName` fields to `EventSearchResponse`
    - Existing endpoints `POST /v1/user/event/search` and `POST /v1/organization/{orgId}/event/search` remain unchanged — just accept the new filter field
* **Database**: N/A — no schema changes
* **Security**: Existing endpoint auth applies. Channel access already validated by user/org ownership specs in `EventMetaData`
* **Performance**: Page size 20. Live polling every 10s only fetches page 0 with timestamp filter (> last seen timestamp). No additional DB indexes needed — existing `event(channel_id, timestamp DESC)` index covers the query.

## 5. Design & UI/UX
- **Tab bar**: Below watchlist selector row, above content. Minimal underline style (not boxed tabs).
- **Feed layout**: Full remaining height (no fixed px). Each event is a card-like row: `[severity dot] [title bold] [channel pill right-aligned] / [message muted] [relative time right-aligned]`
- **Filters bar**: Horizontal row pinned below tabs. Channel multi-select (pills), severity multi-select (pills), "Clear filters" link, "Live" toggle button (green dot when active), "Group by channel" toggle.
- **Live banner**: Floating at top of scroll area, semi-transparent background, click dismisses and scrolls to top.
- **Channel pills**: Use existing badge component with channel-specific color (or neutral). Clickable with hover state.
- **Relative timestamps**: "2m ago", "1h ago", "3d ago" — full ISO datetime in tooltip on hover.
- **Grouped view**: Channel name as section header with event count badge, collapsible (chevron icon), events indented below.

## 6. Implementation Notes
**Backend:**
- `EventMetaData.java` — add `addField("channelIds", "channel.id", SearchType.MULTI_NUMERIC, true)` (JFrame 1.1.0)
- `EventSearchResponse.java` — add `String title` and `String channelName` fields
- `EventMapper` (or wherever response is built) — map `event.getTitle()` and `event.getChannel().getName()`

**Frontend:**
- New component: `client/src/lib/components/monitor/EventFeed.svelte` — the tab content
- New service: `client/src/lib/api/event/service/EventFeedService.svelte.ts` — multi-channel variant of `createEventService()`, adds live polling logic
- Modify: `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte` — add tab navigation, render EventFeed when "Events" tab active
- Modify: `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/+page.svelte` — same tab addition for org variant
- Reuse: `getSeverityColors()` from monitor types, existing badge component for channel pills
- Existing `EventsList.svelte` stays unchanged (modal use case)

**Patterns to follow:**
- `createEventService()` for pagination accumulation pattern
- Toast via existing toast utility (sonner)
