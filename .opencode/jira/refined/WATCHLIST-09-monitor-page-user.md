# Monitor Page (User)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: XL
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-08-timeline-aggregation-api.md

## 1. User Story
**As a** user
**I want** to view my watchlists in a timeline monitoring interface
**So that** I can see the health status of my channels over time at a glance

## 2. Business Context & Value
This is the core monitoring experience - the "most important part of the application." Users can visualize the severity status of their channels over time, quickly identify problems, and drill down into specific events. The interface must be intuitive, responsive, and visually clear.

## 3. Acceptance Criteria

### Watchlist Selection
*   [ ] **Scenario 1**: Watchlist dropdown
    *   Given an authenticated user with watchlists
    *   When they navigate to `/monitor`
    *   Then they see a searchable dropdown to select a watchlist

*   [ ] **Scenario 2**: No watchlists
    *   Given a user with no watchlists
    *   When they navigate to `/monitor`
    *   Then they see a message to create a watchlist first

*   [ ] **Scenario 3**: Session storage persistence
    *   Given a user selects a watchlist and filters
    *   When they refresh the page
    *   Then the same watchlist and filters are restored (from sessionStorage)

*   [ ] **Scenario 4**: URL parameter override
    *   Given URL params `?watchlist=123&range=7d`
    *   When the page loads
    *   Then those values are applied, saved to sessionStorage, and URL is cleaned

*   [ ] **Scenario 5**: Watchlist defaults fallback
    *   Given no sessionStorage and no URL params
    *   When selecting a watchlist
    *   Then the watchlist's default filters are applied

### Time Controls
*   [ ] **Scenario 6**: Time range presets
    *   Given the monitor page
    *   When they select "24h", "7d", or "30d"
    *   Then the timeline updates to show that range
    *   And the view is in "live" mode (auto-refresh)

*   [ ] **Scenario 7**: Custom date range
    *   Given the monitor page
    *   When they select a custom date range via date picker
    *   Then the timeline updates to show that range
    *   And the view is in "historical" mode (no auto-refresh)

*   [ ] **Scenario 8**: Live mode auto-refresh
    *   Given a preset time range (live mode)
    *   When 60 seconds elapse
    *   Then the timeline data refreshes automatically

### Timeline Visualization
*   [ ] **Scenario 9**: Sticky time axis
    *   Given a watchlist with multiple channels
    *   When scrolling down the channel list
    *   Then the time axis header stays visible

*   [ ] **Scenario 10**: Dashboard consolidated timeline
    *   Given a watchlist with multiple channels
    *   When viewing the monitor page
    *   Then the top timeline shows consolidated severity (worst wins)

*   [ ] **Scenario 11**: Channel timelines
    *   Given channels in the watchlist
    *   When viewing the monitor page
    *   Then each channel shows its timeline as a horizontal line with colored segments

*   [ ] **Scenario 12**: Segment colors
    *   Given timeline segments
    *   Then OK = green, WARNING = yellow/amber, CRITICAL = red, NO_DATA = grey

*   [ ] **Scenario 13**: Segment width proportional to time
    *   Given segments of different durations
    *   Then wider segments represent longer durations

*   [ ] **Scenario 14**: Channel info
    *   Given a channel timeline
    *   Then left side shows: channel name, status icon (active/paused), last event severity badge

*   [ ] **Scenario 15**: Paused channels
    *   Given a paused channel
    *   Then it appears greyed out with no timeline data

### Filtering & Sorting
*   [ ] **Scenario 16**: Only critical filter
    *   Given the "Only Critical" toggle is on
    *   When applied
    *   Then only channels with current CRITICAL status are shown

*   [ ] **Scenario 17**: Sort by severity
    *   Given "Sort by Severity" is on
    *   Then channels are grouped by severity (CRITICAL first, then WARNING, OK)
    *   And within each group, sorted by most recent event

*   [ ] **Scenario 18**: Original order
    *   Given "Sort by Severity" is off
    *   Then channels appear in their configured watchlist order

*   [ ] **Scenario 19**: Reset to defaults button
    *   Given modified filters
    *   When clicking "Reset to Defaults"
    *   Then the watchlist's configured defaults are applied

### Event Expansion
*   [ ] **Scenario 20**: Click on segment
    *   Given a channel timeline
    *   When clicking on a specific segment
    *   Then the channel card expands to show events in that duration

*   [ ] **Scenario 21**: Event list
    *   Given an expanded segment
    *   Then events are listed with: timestamp, severity badge, title, message

*   [ ] **Scenario 22**: Collapse events
    *   Given an expanded channel
    *   When clicking again or clicking a collapse button
    *   Then the event list collapses

### Channel Modal
*   [ ] **Scenario 23**: Click on channel name
    *   Given a channel in the list
    *   When clicking on the channel name
    *   Then a modal opens with channel details

## 4. Technical Requirements

### Frontend Route
- Path: `/monitor`
- File: `client/src/routes/(authenticated)/monitor/+page.svelte`

### API Integration
- `POST /v1/user/watchlists/search` - For watchlist dropdown (lightweight)
- `POST /v1/user/monitor` - For timeline data

### State Management

**Session Storage Structure**
```typescript
interface MonitorSessionState {
    watchlistId: number | null;
    timeRange: '24h' | '7d' | '30d' | 'custom';
    startTime?: string;  // ISO string for custom
    endTime?: string;    // ISO string for custom
    onlyCritical: boolean;
    sortBySeverity: boolean;
}
```

**Priority Chain**
```
1. URL params (one-time) → apply, save to session, clean URL
2. sessionStorage → restore state
3. Watchlist defaults → fallback
```

### Auto-Refresh
```typescript
let refreshInterval: number;

$effect(() => {
    if (isLiveMode) {
        refreshInterval = setInterval(fetchTimelineData, 60000);
    }
    return () => clearInterval(refreshInterval);
});
```

### Timeline Component Props
```typescript
interface TimelineProps {
    segments: TimelineSegment[];
    rangeStart: Date;
    rangeEnd: Date;
    onSegmentClick?: (segment: TimelineSegment) => void;
}
```

### Security
- User can only view their own watchlists
- API validates ownership

### Performance
- Debounce filter changes before API call
- Use CSS for segment widths (percentage-based)
- Lazy load event details on expansion
- Consider virtualization for many channels (future)

## 5. Design & UI/UX

### Page Layout
```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Monitor                                                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│  Watchlist: [Production Services ▼]                                         │
│                                                                              │
│  Time Range: (24h) (7d) (30d) [Custom 📅]              [Only Critical □]    │
│                                              [Sort by Severity ☑]           │
│                                              [Reset to Defaults]            │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─ TIME AXIS (sticky) ─────────────────────────────────────────────────┐   │
│  │  |  12:00  |  14:00  |  16:00  |  18:00  |  20:00  |  22:00  | now  │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─ DASHBOARD ──────────────────────────────────────────────────────────┐   │
│  │ 📊 Dashboard  │████████████████████░░░░░░░████████████████████████│   │
│  │    Timeline   │  (consolidated severity across all channels)       │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─ CHANNEL: Database ──────────────────────────────────────────────────┐   │
│  │ 💚 Database     │██████████████████████████░░░░██████████████████│   │
│  │   ● Active      │  (green = OK, grey = NO_DATA, green = OK)       │   │
│  │   Last: OK      │                                                   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│  ┌─ CHANNEL: API Gateway (expanded) ────────────────────────────────────┐   │
│  │ 🔴 API Gateway  │██████████████████████████████████████████████│   │
│  │   ● Active      │                    ▲ clicked segment              │   │
│  │   Last: CRITICAL│                                                   │   │
│  ├──────────────────────────────────────────────────────────────────────┤   │
│  │  Events (3)                                                          │   │
│  │  ┌───────────────────────────────────────────────────────────────┐   │   │
│  │  │ 18:45:23  [CRITICAL]  Connection timeout                      │   │   │
│  │  │           Database connection pool exhausted                  │   │   │
│  │  ├───────────────────────────────────────────────────────────────┤   │   │
│  │  │ 18:42:11  [CRITICAL]  High latency detected                   │   │   │
│  │  │           Response time > 5000ms                              │   │   │
│  │  ├───────────────────────────────────────────────────────────────┤   │   │
│  │  │ 18:40:05  [WARNING]   Elevated error rate                     │   │   │
│  │  │           Error rate at 2.5%                                  │   │   │
│  │  └───────────────────────────────────────────────────────────────┘   │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
│  ┌─ CHANNEL: Redis Cache ───────────────────────────────────────────────┐   │
│  │ 💛 Redis Cache  │████████████████████████████░░░░░░░░████████████│   │
│  │   ⏸ Paused      │  (greyed out - channel is paused)               │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Color Palette
| Severity | Background | Border | Text |
|----------|------------|--------|------|
| OK | `bg-green-500` | `border-green-600` | `text-green-400` |
| WARNING | `bg-amber-500` | `border-amber-600` | `text-amber-400` |
| CRITICAL | `bg-red-500` | `border-red-600` | `text-red-400` |
| NO_DATA | `bg-gray-400` | `border-gray-500` | `text-gray-400` |

### Status Icons
- Active: Solid green dot
- Paused: Grey pause icon

### Time Axis
- Show reasonable tick marks based on range
- 24h: Every 2-4 hours
- 7d: Every day
- 30d: Every 3-5 days
- Always show "now" marker for live mode

### Responsive Design
- On mobile: Stack controls vertically
- Timeline remains horizontal (scrollable if needed)
- Channel cards stack vertically

### Loading States
- Skeleton loaders for timeline while fetching
- Spinner overlay during refresh
- Show "Last updated: X" timestamp

### Empty States
- No watchlists: "Create a watchlist to start monitoring"
- Empty watchlist: "This watchlist has no channels. Add channels in the watchlist editor."
- No events in range: Entire timeline is NO_DATA (grey)

## 6. Implementation Notes / Research

### File Locations
- Page: `client/src/routes/(authenticated)/monitor/+page.svelte`
- Components:
  - `client/src/lib/components/monitor/WatchlistSelector.svelte`
  - `client/src/lib/components/monitor/TimeRangeControls.svelte`
  - `client/src/lib/components/monitor/TimeAxis.svelte`
  - `client/src/lib/components/monitor/ChannelTimeline.svelte`
  - `client/src/lib/components/monitor/TimelineSegment.svelte`
  - `client/src/lib/components/monitor/EventList.svelte`
  - `client/src/lib/components/monitor/ChannelDetailsModal.svelte`

### Timeline Rendering
Use CSS for segment widths:
```typescript
function calculateSegmentStyle(segment: TimelineSegment, rangeStart: Date, rangeEnd: Date): string {
    const totalMs = rangeEnd.getTime() - rangeStart.getTime();
    const startOffset = segment.startTime.getTime() - rangeStart.getTime();
    const duration = segment.endTime.getTime() - segment.startTime.getTime();
    
    const left = (startOffset / totalMs) * 100;
    const width = (duration / totalMs) * 100;
    
    return `left: ${left}%; width: ${width}%;`;
}
```

### Searchable Dropdown
Use a combobox pattern for watchlist selection:
- Search input
- Filtered dropdown list
- Keyboard navigation

Consider using `bits-ui` Combobox or building a custom one.

### Session Storage Helpers
```typescript
const SESSION_KEY = 'monitor_state';

function loadSessionState(): MonitorSessionState | null {
    const stored = sessionStorage.getItem(SESSION_KEY);
    return stored ? JSON.parse(stored) : null;
}

function saveSessionState(state: MonitorSessionState): void {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(state));
}
```

### URL Parameter Handling
```typescript
import { page } from '$app/stores';
import { goto } from '$app/navigation';

onMount(() => {
    const params = $page.url.searchParams;
    if (params.has('watchlist') || params.has('range')) {
        // Apply params
        const state = parseUrlParams(params);
        applyState(state);
        saveSessionState(state);
        // Clean URL
        goto('/monitor', { replaceState: true });
    }
});
```

### Patterns to Follow
- Follow existing page patterns for layout
- Use Card components for channel containers
- Use Badge for severity indicators
- Use existing modal patterns for channel details

### Accessibility
- Keyboard navigation for timeline segments
- ARIA labels for severity colors
- Screen reader announcements for auto-refresh
- Focus management when expanding events
