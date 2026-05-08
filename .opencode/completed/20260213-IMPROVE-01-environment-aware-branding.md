# Environment-Aware Branding

**Completed:** 2026-02-13
**Epic:** General Improvements
**Story:** IMPROVE-01

## Summary

Environment-aware visual branding so developers can instantly identify which environment (local/test/production) they're working in via favicon badges and logo indicators.

## Approved Plan

### Requirements
- Local development shows red DEV badge in favicon and logo
- Test environment shows green TST badge in favicon and logo
- Production shows clean branding (no badges)
- Dev Playbook displays all three variants for reference
- Centralized environment detection module
- Simplified configuration using single `PUBLIC_ENVIRONMENT` env var

### Technical Approach
- Create `env.ts` module with `getEnvironment()`, `isProduction()`, `showDevCredentials()`
- Create favicon variants with colored dot indicators
- Update AppLogo component with badge support
- Dynamic favicon selection in root layout
- Consolidate `PUBLIC_SHOW_DEV_CREDENTIALS` into `PUBLIC_ENVIRONMENT`

### Execution Order
| Phase | Agent | Task |
|-------|-------|------|
| 1 | Manual/Orchestrator | Frontend-only implementation |

## Implementation

### Environment Detection Module
**File:** `client/src/lib/config/env.ts`
- `getEnvironment()` - Returns 'local' | 'test' | 'production' (defaults to 'production' if not set)
- `isProduction()` - Returns true only for production environment
- `showDevCredentials()` - Returns true for non-production environments

### Favicon Variants
**Files created:**
- `client/src/lib/assets/favicon-dev.svg` - Radar icon with red "DEV" text pill in top-right corner
- `client/src/lib/assets/favicon-tst.svg` - Radar icon with green "TST" text pill in top-right corner
- Production uses original favicon (no badge)

### AppLogo Component
**File:** `client/src/lib/components/layout/AppLogo.svelte`
- Added `showEnvBadge` prop (default: true)
- Added `forceEnvironment` prop for playbook demos
- Shows small text badge (DEV=red, TST=green) positioned to top-right of icon on non-production
- Badge sizes scale with logo size (6px/7px/8px font for small/medium/large)

### Dynamic Favicon
**File:** `client/src/routes/+layout.svelte`
- Imports all favicon variants
- Selects correct favicon based on `getEnvironment()`
- Updates document head with appropriate favicon

### Dev Playbook
**File:** `client/src/routes/(public)/dev-playbook/+page.svelte`
- Added "Environment Variants" section showing all 3 logo variants side-by-side
- Access controlled by `showDevCredentials()`

### Configuration Simplification
**Removed:** `PUBLIC_SHOW_DEV_CREDENTIALS` env var
**Now using:** `PUBLIC_ENVIRONMENT` with values:
- `local` → Red DEV indicators + dev features enabled
- `test` → Green TST indicators + dev features enabled
- `production` → No indicators + no dev features (fail-safe default)

### Files Updated to Use `showDevCredentials()`
- `client/src/routes/(public)/login/+page.svelte`
- `client/src/routes/(public)/dev-playbook/+page.svelte`
- `client/src/lib/components/layout/AppSidebarUser.svelte`

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| N/A | Frontend-only implementation by orchestrator | ✅ Complete |

## Files Modified

- `client/src/lib/config/env.ts` (new) - Environment detection module
- `client/src/lib/assets/favicon-dev.svg` (new) - Red dot favicon
- `client/src/lib/assets/favicon-tst.svg` (new) - Green dot favicon
- `client/src/lib/components/layout/AppLogo.svelte` - Added badge support
- `client/src/routes/+layout.svelte` - Dynamic favicon selection
- `client/src/routes/(public)/dev-playbook/+page.svelte` - Environment variants showcase
- `client/src/routes/(public)/login/+page.svelte` - Use `showDevCredentials()`
- `client/src/lib/components/layout/AppSidebarUser.svelte` - Use `showDevCredentials()`
- `client/.env` - Updated to use `PUBLIC_ENVIRONMENT=local`
- `client/.env.example` - Documentation for `PUBLIC_ENVIRONMENT`

## Tests

- Build verification: ✅ `bun run build` passes
- Type checking: ✅ `bun run check` passes (0 errors)

## Notes

- Favicon uses text pill badges (DEV/TST) with colored backgrounds for clear environment identification
- AppLogo uses matching text badges that scale with logo size - kept small to avoid covering the Radar icon
- Single `PUBLIC_ENVIRONMENT` env var controls both visual branding AND dev feature visibility (simpler than two separate vars)
- Fail-safe default: If `PUBLIC_ENVIRONMENT` is not set or has invalid value, defaults to 'production' (no dev features shown)
