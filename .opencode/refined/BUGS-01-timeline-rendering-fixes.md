---
epic: "BUGS"
title: "Fix timeline rendering issues (grey gaps, white hairlines, flickering tail)"
estimate: M
status: ready
created: 2026-05-21
depends_on: []
labels: [backend, frontend, bugfix]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user monitoring channels\
**I want** timelines to render without visual artifacts\
**So that** I can trust the severity display at every zoom level\

## 2. Business Context & Value
The timeline is the primary monitoring visualization. Grey gaps, white hairlines, and flickering raw data in aggregate views erode trust in the data. Users cannot distinguish "no data" from "system bug" when grey appears on channels with known history.

## 3. Acceptance Criteria
* [ ] **Grey gaps carry forward severity**: No NO_DATA segments between buckets when prior data exists
    * Given a channel with historical events and an aggregate timeline view
    * When there is a gap between materialized buckets
    * Then the gap inherits the last known severity (carry-forward) instead of showing grey/NO_DATA
* [ ] **No white hairline gaps between segments**: Segments tile seamlessly
    * Given a timeline with multiple adjacent durations
    * When rendered at any viewport width
    * Then no background color bleeds through between segments (no sub-pixel gaps)
* [ ] **Live tail is bucketed consistently**: No raw event flickering in aggregate views
    * Given a live aggregate timeline (e.g., 30min resolution)
    * When the current incomplete period has not yet been materialized as a bucket
    * Then it is displayed as a single bucket with worst-severity aggregation, matching the rest of the timeline
* [ ] **Zoom reveals raw data**: Raw events only shown at appropriate zoom level
    * Given a user viewing a bucketed timeline
    * When they zoom into a segment
    * Then finer resolution or raw events are shown only when the time window is ≤ 4h

## 4. Technical Requirements
* **API Changes**: N/A — no endpoint signature changes
* **Database**: N/A — no schema changes
* **Security**: N/A
* **Performance**: No additional queries; tail bucketing is computed in-memory from already-fetched raw events

## 5. Design & UI/UX
N/A — no new UI elements. Existing timeline bars render correctly without visual artifacts.

## 6. Implementation Notes

### Fix 1: Grey gaps → carry forward severity
**File:** `server/src/main/java/.../monitor/util/AggregateTimelineBuilder.java` (~line 136-139)

Current code inserts `NO_DATA` and nulls `prevSeverity` when a gap exists between buckets. Fix: instead of `Severity.NO_DATA`, use the previous bucket's last severity (or `initialSeverity` if before first bucket). Remove the `prevSeverity = null` reset.

### Fix 2: White hairline gaps → CSS fix
**File:** `client/src/lib/components/monitor/types.ts` (`calculateSegmentStyle`)

Compute each segment's `left` as the sum of all previous segments' widths (cumulative), not independently from time math. This guarantees `left[n] = left[n-1] + width[n-1]` with no float drift. Alternatively, round to 4 decimal places and add a tiny overlap (0.01%).

**File:** `client/src/lib/components/monitor/TimelineBar.svelte`

Pass cumulative left to each segment rather than computing independently.

### Fix 3: Flickering tail → bucket the stitched portion
**File:** `server/src/main/java/.../monitor/service/MonitorService.java` (~line 172-199)

In `buildStitchedTimeline()`: instead of passing raw events to `TimelineBuilder`, aggregate them in-memory into a single bucket (worst severity) covering the tail period. Use the same `bucketSize` as the historical portion. Alternatively, remove the stitch entirely and let `AggregateTimelineBuilder` handle the full range — the last incomplete bucket simply aggregates whatever events exist in that window.

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../monitor/util/AggregateTimelineBuilder.java` | Carry forward severity in gaps instead of NO_DATA |
| `server/.../monitor/service/MonitorService.java` | Bucket the stitched tail instead of raw events |
| `client/src/lib/components/monitor/types.ts` | Fix cumulative left calculation to prevent sub-pixel gaps |
| `client/src/lib/components/monitor/TimelineBar.svelte` | Pass cumulative positioning to segments |
