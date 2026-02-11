# Move StatCard to Shared Components

**Completed:** 2026-02-11
**Story:** .opencode/refined/DASHBOARD-01-statcard-shared-component.md

## Summary

Moved the StatCard component from admin-specific folder to shared UI components, following shadcn-svelte naming conventions. This enables reuse across all dashboards (admin, user, organization).

## Agents Used

| Agent | Task |
|-------|------|
| N/A | Pure refactor - orchestrator executed directly |

## Files Modified

- **Created:** `src/lib/components/ui/stat-card/stat-card.svelte`
- **Created:** `src/lib/components/ui/stat-card/index.ts`
- **Deleted:** `src/lib/components/admin/StatCard.svelte`
- **Updated:** `src/lib/components/admin/index.ts` (removed StatCard export)
- **Updated:** `src/routes/(authenticated)/admin/dashboard/+page.svelte`
- **Updated:** `src/routes/(authenticated)/admin/api-keys/+page.svelte`
- **Updated:** `src/routes/(authenticated)/organizations/[orgId]/settings/general/+page.svelte`
- **Updated:** `src/routes/(public)/dev-playbook/+page.svelte`

## Tests

- N/A (no business logic, pure file move)
- Build check: 0 errors

## Notes

- Component renamed from `StatCard.svelte` to `stat-card.svelte` (kebab-case per shadcn convention)
- All imports updated to use new path `$lib/components/ui/stat-card`
- No visual or functional changes
