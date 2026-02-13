# Fix Monitor Page Not Loading Watchlist Default Filters

**Epic**: Bugs & Technical Debt
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-02-13
**Depends On**: None

## 1. User Story
**As a** user editing a watchlist
**I want** the Monitor page to load my watchlist's saved default filters when I click "Monitor"
**So that** I see my configured view preferences without manually re-setting them each time

## 2. Business Context & Value
Currently, when a user:
1. Edits a watchlist and configures default filters (e.g., timeRange=7d, onlyCritical=true)
2. Clicks the "Monitor" button

The Monitor page ignores the saved filters and uses hardcoded defaults (24h, onlyCritical=false). This is confusing because users expect their saved preferences to apply.

**Impact**: Users on TST environment reported this as a bug - filters configured on the edit page don't carry over to monitoring.

## 3. Acceptance Criteria

### Filter Priority (lowest to highest)
* [ ] **Scenario 1**: First visit with no session state
    * Given a user navigates to `/watchlists/monitor?id=123` for the first time (no session)
    * When the page loads
    * Then the watchlist's saved default filters are loaded as the session state
    * And those filters are applied to the monitor view

* [ ] **Scenario 2**: Navigate via "Monitor" button from edit page
    * Given a user is on the edit watchlist page with saved filters `{timeRange: '7d', onlyCritical: true}`
    * When they click the "Monitor" button
    * Then the Monitor page loads with the watchlist's saved filters
    * And the session state is updated to match the watchlist defaults

* [ ] **Scenario 3**: Shared URL overrides everything
    * Given a user has a session state with `{timeRange: '24h'}`
    * When they open a shared URL `/watchlists/monitor?id=123&timeRange=7d&onlyCritical=true`
    * Then the URL params become the new session state
    * And the filters `{timeRange: '7d', onlyCritical: true}` are applied

* [ ] **Scenario 4**: Session state persists within tab
    * Given a user is on the Monitor page with session state `{timeRange: '12h'}`
    * When they change the filter to `timeRange: '4h'`
    * And they navigate away and back to the Monitor page (same tab)
    * Then the session state `{timeRange: '4h'}` is preserved

* [ ] **Scenario 5**: Switching watchlists loads new defaults
    * Given a user is monitoring watchlist A with filters `{timeRange: '24h'}`
    * When they select watchlist B (which has saved defaults `{timeRange: '7d'}`)
    * Then the watchlist B's saved defaults are loaded as the new session state

### Reset to Defaults Feature
* [ ] **Scenario 6**: Show "Reset to defaults" when filters differ
    * Given the current session filters differ from the watchlist's saved defaults
    * When the user views the filter controls
    * Then a "Reset to defaults" button is visible with an indicator

* [ ] **Scenario 7**: Reset to defaults restores watchlist settings
    * Given the user has modified filters away from watchlist defaults
    * When they click "Reset to defaults"
    * Then the watchlist's saved default filters are restored
    * And the session state is updated accordingly

* [ ] **Scenario 8**: Hide reset button when filters match defaults
    * Given the current session filters match the watchlist's saved defaults
    * When the user views the filter controls
    * Then the "Reset to defaults" button is not visible (or disabled)

## 4. Technical Requirements

### Frontend Changes

**Modified Files**:
| File | Change |
|------|--------|
| `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte` | Load watchlist filters on init, pass to ConfigurePopover |
| `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/+page.svelte` | Same changes for org context |
| `client/src/lib/api/monitor/monitor.service.ts` | Add helper to merge watchlist defaults with session |
| `client/src/lib/components/monitor/ConfigurePopover.svelte` | Add "Reset to defaults" button with indicator |

### Implementation Logic

**Filter Resolution Priority** (in `initializePage`):
```typescript
// 1. Parse URL params (highest priority)
const queryParams = parseMonitorQueryParams(url);

// 2. Load watchlist (to get saved defaults)
const watchlist = await getWatchlist(watchlistId);

// 3. Determine final filters
if (queryParams?.filters && Object.keys(queryParams.filters).length > 0) {
  // URL params override everything
  session.value = { watchlistId, ...createDefaultFilters(), ...queryParams.filters };
} else {
  // Use watchlist's saved defaults
  session.value = { 
    watchlistId, 
    ...createDefaultFilters(),
    ...watchlist.filters  // timeRange, onlyCritical, sortBySeverity, groupedView
  };
}
```

**Reset to Defaults Logic**:
```typescript
function resetToDefaults(): void {
  if (watchlist?.filters) {
    session.value = {
      watchlistId: session.value.watchlistId,
      ...createDefaultFilters(),
      ...watchlist.filters
    };
    loadMonitorData();
  }
}

const filtersMatchDefaults = $derived(
  watchlist?.filters && 
  filters.timeRange === (watchlist.filters.timeRange ?? '24h') &&
  filters.onlyCritical === (watchlist.filters.onlyCritical ?? false) &&
  filters.sortBySeverity === (watchlist.filters.sortBySeverity ?? false) &&
  filters.groupedView === (watchlist.filters.groupedView ?? false)
);
```

### API Changes
None required - watchlist already returns `filters` in `WatchlistDetailsResponse`.

### Database
No changes required.

## 5. Design & UI/UX

### Reset to Defaults Button
- **Location**: Inside `ConfigurePopover`, at the bottom of the filter options
- **Visibility**: Only shown when current filters differ from watchlist defaults
- **Style**: Ghost button with `RotateCcw` icon, text "Reset to defaults"
- **Indicator**: Small dot/badge on the Configure button when filters are modified (optional enhancement)

```svelte
{#if !filtersMatchDefaults}
  <Button variant="ghost" size="sm" onclick={resetToDefaults} class="w-full justify-start gap-2">
    <RotateCcw class="h-4 w-4" />
    Reset to defaults
  </Button>
{/if}
```

## 6. Implementation Notes / Research

### Existing Code References
- **Monitor page**: `client/src/routes/(authenticated)/watchlists/monitor/+page.svelte`
- **Session storage**: Uses `Sessionstorage` class which is tab-scoped (sessionStorage)
- **Watchlist filters type**: `WatchlistFiltersRequest` in `client/src/lib/types/api.d.ts`
- **ConfigurePopover**: `client/src/lib/components/monitor/ConfigurePopover.svelte`

### Key Points
1. `Sessionstorage` already uses `sessionStorage` which is per-tab ✓
2. Watchlist already returns filters via `getWatchlist()` → `watchlist.filters`
3. The `handleWatchlistChange` function should also load new watchlist's defaults

### Test Considerations
- E2E test: Edit watchlist filters → Monitor → verify filters applied
- E2E test: Share URL → open → verify URL params applied
- E2E test: Reset to defaults button appears/works
