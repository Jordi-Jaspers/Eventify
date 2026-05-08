# Settings Reorg: Security Page Merge

**Completed:** 2026-05-07
**Epic:** UI
**Source:** .opencode/refined/UI-01-settings-reorg-security-merge.md

## Summary

Merged Sessions and Connected Accounts pages into a single Security page at `/profile/security`. Reordered settings tabs to Profile → Security → Data & Storage → Developer.

## Plan Approved by the user:

### Requirements Summary

- Merge "Sessions" + "Connected Accounts" into single `/profile/security` page
- Reorder tabs: Profile → Security → Data & Storage → Developer
- Remove old routes/pages, update route constants
- Both sections load data independently in parallel
- No backend changes

### Technical Approach

- Frontend only: new route, updated nav, updated route constants, deleted old pages

### Execution Order

| Phase | Agent                | Task                                                              |
|-------|----------------------|-------------------------------------------------------------------|
| 1     | svelte-frontend-agent | Implement new security page, update nav, routes, delete old pages |

## Implementation

### Frontend

- Created `/profile/security/+page.svelte` — combined Sign-in methods + Active sessions cards
- Updated `SettingsNav.svelte` — 4 tabs in new order, removed `Link2` import
- Updated `routes.ts` — added `PROFILE_SECURITY_PAGE`, removed `PROFILE_SESSIONS_PAGE` + `PROFILE_CONNECTED_ACCOUNTS_PAGE`
- Deleted old `sessions/+page.svelte` and `connected-accounts/+page.svelte`

### Deviations from Plan

- None

## Agents Used

| Agent                 | Task                                    | Result   |
|-----------------------|-----------------------------------------|----------|
| svelte-frontend-agent | Implement security page merge + cleanup | Complete |

## Files Modified

- `client/src/routes/(authenticated)/profile/security/+page.svelte` - NEW: combined security page
- `client/src/lib/components/settings/SettingsNav.svelte` - Updated tabs array and order
- `client/src/lib/config/routes.ts` - Added PROFILE_SECURITY_PAGE, removed old constants
- `client/src/routes/(authenticated)/profile/sessions/+page.svelte` - DELETED
- `client/src/routes/(authenticated)/profile/connected-accounts/+page.svelte` - DELETED

## Tests

- No frontend test setup; skipped per user request
