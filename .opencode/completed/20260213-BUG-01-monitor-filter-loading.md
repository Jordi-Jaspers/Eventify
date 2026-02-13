# Fix Monitor Page Not Loading Watchlist Default Filters

**Completed:** 2026-02-13
**Story:** `.opencode/refined/BUG-01-monitor-filter-loading.md`

## Summary

Fixed bug where Monitor page ignored watchlist's saved default filters. Added visual indicators and reset functionality for filter state management.

## Approved Plan

### Requirements Summary
- Apply watchlist saved filters on first load (no existing session)
- Apply watchlist saved filters when switching watchlists
- URL params still take highest priority
- Add "Reset to defaults" button when filters differ from saved
- Add visual indicator when filters are modified

### Technical Approach
- **Backend:** None required - watchlist already returns `filters` in response
- **Frontend:** Modify monitor pages to read `watchlist.filters` on load, add UI indicators

### Execution Order
| Phase | Agent | Type | Task |
|-------|-------|------|------|
| 1 | deep-research-agent | global | Research existing monitor page implementation |
| 2 | svelte-frontend-agent | project | Implement filter loading fix + UI indicators |
| 3 | frontend-optimizer-agent | global | Extract shared service, reduce duplication |

## Implementation

### Root Cause
`loadWatchlist()` fetched watchlist data but never read `watchlist.filters` into the session state.

### Filter Priority (implemented)
1. URL params (highest) - unchanged
2. Session state within tab - unchanged  
3. Watchlist saved defaults (new) - applied on first load or watchlist switch

### Frontend
- Modified both monitor pages to apply `watchlist.filters` on initial load
- Added "Reset to defaults" button in ConfigurePopover (visible when filters differ)
- Added amber indicator dot on Configure button when filters modified
- Extracted shared logic into `MonitorPageService.svelte.ts`

### Code Optimization
| File | Before | After | Change |
|------|--------|-------|--------|
| User Monitor Page | 474 | 221 | **-53%** |
| Org Monitor Page | 485 | 230 | **-53%** |
| New MonitorPageService | 0 | 356 | +356 |
| **Net Total** | 1288 | 1136 | **-12%** |

### Deviations from Plan
- None - implementation followed the approved plan

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent | Researched existing monitor page implementation | ✅ Complete |
| svelte-frontend-agent | Implemented filter loading fix + UI indicators | ✅ Complete |
| frontend-optimizer-agent | Extracted shared service, reduced code duplication | ✅ Complete |

## Files Modified

- `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte` - Refactored to use MonitorPageService (474 → 221 lines)
- `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/+page.svelte` - Refactored to use MonitorPageService (485 → 230 lines)
- `client/src/lib/components/monitor/ConfigurePopover.svelte` - Added reset button and modified indicator props
- `client/src/lib/api/monitor/service/MonitorPageService.svelte.ts` - NEW: Factory service encapsulating all monitor page logic

## Tests

- Manual verification of all 8 acceptance criteria
- `bun run check` - 0 errors
- `bun run build` - successful

## Notes

- No backend changes required - watchlist already returns `filters` in `WatchlistDetailsResponse`
- No database changes required
- E2E tests for this feature are optional enhancement (documented in story)
- MonitorPageService pattern is a playbook candidate for similar page refactoring
