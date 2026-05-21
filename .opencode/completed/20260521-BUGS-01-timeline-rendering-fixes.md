# Fix Timeline Rendering Issues

**Completed:** 2026-05-21
**Epic:** BUGS
**Source:** .opencode/refined/BUGS-01-timeline-rendering-fixes.md

## Summary

Fixed three timeline rendering bugs: grey gaps now carry forward previous severity, hairline gaps between segments eliminated via cumulative positioning, and live tail no longer flickers at historical/recent boundary.

## Plan Approved by the user:

### Requirements Summary

1. Grey gaps carry forward severity (not NO_DATA)
2. No white hairline gaps between segments
3. Live tail bucketed consistently (no flickering)
4. Zoom reveals raw data only at ≤4h (handled by existing logic)

### Technical Approach

- Backend: Fix `AggregateTimelineBuilder` gap handling, fix `MonitorService.buildStitchedTimeline()` boundary stitching
- Frontend: Cumulative left positioning in `TimelineBar`

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Update gap assertion tests |
| 2 | spring-backend-agent | Fix gap severity + stitched timeline |
| 3 | backend-optimizer-agent | Refactor for compactness |
| 4 | svelte-frontend-agent | Fix hairline gaps |
| 5 | frontend-optimizer-agent | Remove dead code |

## Implementation

### Backend

- `AggregateTimelineBuilder.java`: Gap uses `prevSeverity` instead of `NO_DATA`; no reset
- `MonitorService.java`: Synthetic prior event bridges historical→recent severity
- Optimization: -97 lines across both files

### Frontend

- `types.ts`: New `calculateCumulativeSegmentStyles` (removed old `calculateSegmentStyle`)
- `TimelineBar.svelte`: Computes cumulative styles
- `TimelineSegment.svelte`: Accepts pre-computed `style` prop
- Optimization: -20 lines

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Update gap tests | Complete |
| spring-backend-agent | Fix gap + stitching | Complete |
| backend-optimizer-agent | Refactor | -18% lines |
| svelte-frontend-agent | Fix hairline gaps | Complete |
| frontend-optimizer-agent | Remove dead code | -7.5% lines |

## Files Modified

- `server/src/main/java/io/github/eventify/api/monitor/util/AggregateTimelineBuilder.java` — carry forward severity in gaps
- `server/src/main/java/io/github/eventify/api/monitor/service/MonitorService.java` — synthetic prior event for stitching
- `server/src/main/java/io/github/eventify/api/monitor/util/TimelineBuilder.java` — always extend last duration to rangeEnd
- `server/src/test/java/io/github/eventify/api/monitor/util/AggregateTimelineBuilderTest.java` — updated gap tests
- `server/src/test/java/io/github/eventify/api/monitor/util/TimelineBuilderTest.java` — updated non-live extension test
- `client/src/lib/components/monitor/types.ts` — cumulative segment styles
- `client/src/lib/components/monitor/TimelineBar.svelte` — use cumulative styles
- `client/src/lib/components/monitor/TimelineSegment.svelte` — accept style prop
- `client/src/lib/api/monitor/service/MonitorPageService.svelte.ts` — clamp zoom end to now
- `server/src/main/resources/changelog.json` — what's new entry

## Tests

- 2 tests updated/added for gap severity, all 75 monitor unit tests passing
