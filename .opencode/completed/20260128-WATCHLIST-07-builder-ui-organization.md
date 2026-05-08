# WATCHLIST - 07 Builder UI Organization

**Completed:** 2026-01-28
**Vibe Kanban Task:** WATCHLIST - 07 Builder UI Organization
**Task ID:** 30c0875f-04bc-428a-953e-2175099095ee

## Summary
Wired up the existing WatchlistBuilder component for organization watchlists. Created route pages that pass org-specific data with role-based access control.

## Agents Used
| Agent                  | Task |
|------------------------|------|
| orchestrator           | Direct implementation (copy pattern from user pages) |
| svelete-frontend-agent | Screenshot tests |

## Files Created
- `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/new/+page.svelte` - Create page
- `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/[id]/+page.svelte` - Edit page
- `client/test/components/org-watchlist-builder.spec.ts` - 4 screenshot tests
- 6 screenshots in `client/test/resources/screenshots/org-watchlist-builder/`

## Features
- 100% WatchlistBuilder component reuse
- OWNER/ADMIN role check with redirect + toast for unauthorized access
- Loads organization channels (not user channels)
- Auto-save on edit page with minimum spinner duration
- Monitor button on edit page to jump to monitoring view
- Back navigation to list page

## Tests
- 4 screenshot tests (2 scenarios × 2 themes)
- All passing

## Notes
- XS estimate - pure routing/wiring work
- Copied pattern from user watchlist builder pages
- Uses searchOrganizationChannels for channel dropdown
- Uses org watchlist API (created in WATCHLIST-03)
