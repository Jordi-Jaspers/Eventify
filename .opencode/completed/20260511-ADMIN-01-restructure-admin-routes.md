# Restructure Admin Routes

**Completed:** 2026-05-11
**Epic:** ADMIN
**Source:** `.opencode/refined/ADMIN-01-restructure-admin-routes.md`

## Summary

Reorganized flat admin routes into 3 grouped sections (Statistics, Resources, Tools) with tab navigation under Resources. Pure frontend refactor — no backend changes.

## Plan Approved by the user:

### Requirements Summary
- Move `/admin/dashboard` → `/admin/statistics`
- Move `/admin/users`, `/admin/organizations`, `/admin/api-keys` → `/admin/resources/{users|organizations|api-keys}` with tab layout
- Add `/admin/tools` with empty state
- Sidebar: 3 entries (Statistics, Resources, Tools) with icons
- Resources tab bar using nested `+layout.svelte`
- Deep-link support for detail pages (no tab bar on drill-in)
- Auth guard preserved

### Technical Approach
- Backend: N/A
- Frontend: Route constants update, page moves, new layouts with tab bar, sidebar restructure

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 3.2 | svelte-frontend-agent | Move pages, create layouts, update routes, sidebar, tab component |
| 3.4 | frontend-optimizer-agent | Cleanup stale titles, indentation |

## Implementation

### Frontend
- Route constants updated in `routes.ts` — renamed + added `ADMIN_RESOURCES_PAGE`, `ADMIN_TOOLS_PAGE`
- Resources layout with horizontal tab bar (Users | Organizations | API Keys), hidden on detail pages
- Tools page with empty state (Wrench icon + message)
- Statistics page = former dashboard (content unchanged)
- Sidebar: 3 entries replacing 4 flat entries
- All `goto()` calls auto-updated via CLIENT_ROUTES constants
- Fixed `window.location.href` → `goto()` in statistics page
- Fixed stale "Admin Dashboard" title references

### Deviations from Plan
- None

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| svelte-frontend-agent | Route restructure, layouts, sidebar | Complete |
| frontend-optimizer-agent | Cleanup stale text, indentation | Complete |

## Files Modified
- `client/src/lib/config/routes.ts` — updated route constants
- `client/src/lib/components/layout/AppSidebarNav.svelte` — 3 grouped entries
- `client/src/lib/components/layout/AppNavbar.svelte` — updated admin goto + title
- `client/src/routes/(authenticated)/admin/statistics/+page.svelte` — moved from dashboard
- `client/src/routes/(authenticated)/admin/resources/+layout.svelte` — new tab bar
- `client/src/routes/(authenticated)/admin/resources/+page.svelte` — redirect to users
- `client/src/routes/(authenticated)/admin/resources/users/+page.svelte` — moved
- `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` — moved
- `client/src/routes/(authenticated)/admin/resources/api-keys/+page.svelte` — moved
- `client/src/routes/(authenticated)/admin/tools/+layout.svelte` — new passthrough
- `client/src/routes/(authenticated)/admin/tools/+page.svelte` — empty state

## Files Deleted
- `client/src/routes/(authenticated)/admin/dashboard/`
- `client/src/routes/(authenticated)/admin/users/`
- `client/src/routes/(authenticated)/admin/organizations/`
- `client/src/routes/(authenticated)/admin/api-keys/`

## Tests
- No tests exist for admin routes (none before, none added — route restructuring only)
- `bun run check` passes with 0 errors
