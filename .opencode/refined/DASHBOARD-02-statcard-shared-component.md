# Move StatCard to Shared Components

**Epic**: Dashboard Enhancements
**Status**: Ready for Dev
**Estimate**: XS
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** developer
**I want** the StatCard component to be in the shared UI components folder
**So that** it can be used across admin, user, and organization dashboards consistently

## 2. Business Context & Value
The StatCard component currently lives in `/components/admin/` but is general-purpose. Moving it to the shared location enables reuse across all dashboards and follows the project's component organization pattern.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Component relocated
    * Given the StatCard is in `$lib/components/admin/`
    * When I move it to `$lib/components/ui/stat-card/`
    * Then all existing imports continue to work
    * And the component renders identically

* [ ] **Scenario 2**: Existing usages updated
    * Given StatCard is used in admin dashboard, admin api-keys, org settings, dev-playbook
    * When the component is moved
    * Then all import paths are updated to `$lib/components/ui/stat-card`

* [ ] **Scenario 3**: Admin barrel export updated
    * Given `$lib/components/admin/index.ts` exports StatCard
    * When StatCard is moved
    * Then the admin index re-exports from the new location (for backwards compatibility)
    * Or the export is removed if all usages are updated

## 4. Technical Requirements
* **File Move**:
  - From: `src/lib/components/admin/StatCard.svelte`
  - To: `src/lib/components/ui/stat-card/stat-card.svelte` (following shadcn naming)
  
* **New barrel file**: `src/lib/components/ui/stat-card/index.ts`
  ```typescript
  export { default as StatCard } from './stat-card.svelte';
  ```

* **Update imports in**:
  - `src/routes/(authenticated)/admin/dashboard/+page.svelte`
  - `src/routes/(authenticated)/admin/api-keys/+page.svelte`
  - `src/routes/(authenticated)/organizations/[orgId]/settings/general/+page.svelte`
  - `src/routes/(public)/dev-playbook/+page.svelte`

* **Update or remove**: `src/lib/components/admin/index.ts`

## 5. Design & UI/UX (If applicable)
No visual changes - purely a refactor.

## 6. Implementation Notes / Research
* **shadcn-svelte pattern**: Components in `/ui/` follow kebab-case folder naming with `index.ts` barrel
* **Existing examples**: `$lib/components/ui/card/`, `$lib/components/ui/badge/`
* **File naming**: Use `stat-card.svelte` (kebab-case) to match shadcn conventions
