# Add Tooltips to Sidebar Icons When Collapsed

**Completed:** 2026-05-21
**Epic:** BUGS
**Source:** .opencode/refined/BUGS-03-sidebar-collapsed-tooltips.md

## Summary

Added `tooltipContent` prop to all 13 sidebar navigation MenuButton instances so tooltips appear on hover when the sidebar is collapsed.

## Plan Approved by the user:

Frontend-only change: add `tooltipContent="<label>"` to each `Sidebar.MenuButton` in `AppSidebarNav.svelte`. The tooltip infrastructure was already built into the MenuButton component.

## Implementation

### Frontend

- Added `tooltipContent` prop to all 13 `Sidebar.MenuButton` instances
- Labels match the visible `<span>` text: Dashboard, Channels, Watchlists, Monitor, Members, Settings, Statistics, Resources, Tools

### Deviations from Plan

- None

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| svelte-frontend-agent | Add tooltipContent props | Complete |

## Files Modified

- `client/src/lib/components/layout/AppSidebarNav.svelte` - Added tooltipContent prop to all 13 MenuButton instances

## Tests

- Type check passes (bun run check)
- No new tests (trivial prop addition, skip_frontend_tests=true)
