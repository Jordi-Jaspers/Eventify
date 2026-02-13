# Fix Monitor Page Not Loading Watchlist Default Filters

**Completed:** 2026-02-13
**Story:** `.opencode/refined/BUG-01-monitor-filter-loading.md`

## Summary

Fixed bug where Monitor page ignored watchlist's saved default filters. Users can now configure filters on the watchlist edit page and have them automatically applied when opening the Monitor view.

**Root Cause:** `loadWatchlist()` fetched watchlist data but never read `watchlist.filters` into the session state.

## Changes

### Filter Priority (implemented)
1. URL params (highest) - unchanged
2. Session state within tab - unchanged  
3. Watchlist saved defaults (new) - applied on first load or watchlist switch

### Features Added
- Watchlist default filters load on first visit (no session)
- Switching watchlists loads new watchlist's saved defaults
- "Reset to defaults" button in ConfigurePopover when filters differ
- Amber indicator dot on Configure button when filters modified

### Code Optimization
Extracted shared monitor page logic into a reusable service:

| File | Before | After | Change |
|------|--------|-------|--------|
| User Monitor Page | 474 | 221 | **-53%** |
| Org Monitor Page | 485 | 230 | **-53%** |
| New MonitorPageService | 0 | 356 | +356 |
| **Net Total** | 1288 | 1136 | **-12%** |

## Agents Used

| Agent | Task |
|-------|------|
| deep-research-agent | Researched existing monitor page implementation |
| svelte-frontend-agent | Implemented filter loading fix |
| frontend-optimizer-agent | Extracted shared service, reduced code duplication |

## Files Modified

- `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte`
  - Refactored to use MonitorPageService (474 → 221 lines)
  - Now pure presentation component

- `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/+page.svelte`
  - Refactored to use MonitorPageService (485 → 230 lines)
  - Now pure presentation component

- `client/src/lib/components/monitor/ConfigurePopover.svelte`
  - Added `showResetButton` and `showModifiedIndicator` props
  - Added "Reset to defaults" button with RotateCcw icon
  - Amber indicator dot on button when filters modified

- `client/src/lib/api/monitor/service/MonitorPageService.svelte.ts` (NEW)
  - Factory service encapsulating all monitor page logic
  - Configuration-driven for user vs org contexts
  - State management, filter logic, API orchestration, event handlers

## Tests

- Manual verification of all 8 acceptance criteria
- `bun run check` - 0 errors
- `bun run build` - successful

## Notes

- No backend changes required - watchlist already returns `filters` in `WatchlistDetailsResponse`
- No database changes required
- E2E tests for this feature are optional enhancement (documented in story)
- MonitorPageService pattern is a playbook candidate for similar page refactoring
