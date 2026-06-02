# Sidebar Restructure & Route Renames

**Completed:** 2026-05-22
**Epic:** ORGMGMT
**Source:** `.opencode/refined/ORGMGMT-02-sidebar-restructure-route-renames.md`

## Summary

Reordered all 3 sidebar sections, renamed `/admin/resources` → `/admin/manage`, moved `/watchlists/monitor` → `/monitor`, removed org dashboard route, added org statistics placeholder.

## Plan Approved by the user:

### Requirements Summary

1. Reorder user workspace sidebar: Dashboard → Monitor → Watchlists → Channels
2. Reorder org workspace sidebar: Monitor → Watchlists → Channels → Members → Statistics → Settings
3. Reorder admin sidebar: Manage → Tools → Statistics; rename "Resources" → "Manage"
4. Move `/watchlists/monitor` → `/monitor`
5. Rename `/admin/resources` → `/admin/manage`
6. Remove org dashboard route constant
7. Create org statistics placeholder
8. Create org monitor route

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | svelte-frontend-agent | All file moves, route updates, sidebar reorder |

## Implementation

### Frontend

- Routes: `MONITOR_PAGE` (`/monitor`), `ADMIN_MANAGE_PAGE` (`/admin/manage`), removed `ORGANIZATION_DASHBOARD_PAGE`, added `ORGANIZATION_STATISTICS_PAGE`
- Sidebar: Reordered all 3 sections, renamed "Resources" → "Manage"
- File moves: `watchlists/monitor/` → `monitor/`, `admin/resources/` → `admin/manage/`
- New routes: org monitor, org statistics placeholder

### Deviations from Plan

- None

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| svelte-frontend-agent | Full implementation | Complete |

## Files Modified

- `client/src/lib/config/routes.ts` — route constant renames and path updates
- `client/src/lib/components/layout/AppSidebarNav.svelte` — reorder, rename, update paths
- `client/src/routes/(authenticated)/monitor/+page.svelte` — moved from watchlists/monitor
- `client/src/routes/(authenticated)/admin/manage/` — moved from admin/resources (all sub-routes)
- `client/src/routes/(authenticated)/organizations/[orgId]/monitor/+page.svelte` — new
- `client/src/routes/(authenticated)/organizations/[orgId]/statistics/+page.svelte` — new placeholder
- `client/src/routes/(authenticated)/watchlists/+page.svelte` — updated route reference
- `client/src/routes/(authenticated)/watchlists/[id]/+page.svelte` — updated route reference
- `client/src/routes/(authenticated)/dashboard/+page.svelte` — updated route reference
- `client/src/routes/(authenticated)/organizations/[orgId]/settings/data-storage/+page.svelte` — updated route reference
- `client/src/lib/components/profile/OrganizationMembershipCard.svelte` — updated route reference

## Tests

- Skipped (per METADATA `skip_frontend_tests: true`)
