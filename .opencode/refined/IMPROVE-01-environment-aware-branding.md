# Environment-Aware Branding

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-02-13
**Depends On**: None

## 1. User Story
**As a** developer or support engineer
**I want** to visually distinguish which environment I'm working in via favicon and logo badges
**So that** I can avoid confusion when multiple tabs/environments are open simultaneously

## 2. Business Context & Value
When users (especially developers and support staff) have multiple browser tabs open across different environments (local dev, test, production), it's easy to accidentally perform actions in the wrong environment. Visual environment indicators in the favicon and application logo provide an immediate, always-visible cue that reduces errors and improves confidence during debugging and development.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Local development shows DEV badge (red)
    * Given the app is running in local/development environment
    * When the user views the browser tab or sidebar logo
    * Then the favicon shows a red "DEV" badge overlay
    * And the AppLogo component shows a red "DEV" badge

* [ ] **Scenario 2**: Test environment shows TST badge (green)
    * Given the app is running in the test environment
    * When the user views the browser tab or sidebar logo
    * Then the favicon shows a green "TST" badge overlay
    * And the AppLogo component shows a green "TST" badge

* [ ] **Scenario 3**: Production shows clean branding
    * Given the app is running in production environment
    * When the user views the browser tab or sidebar logo
    * Then the favicon shows the clean icon without any badge
    * And the AppLogo component shows no environment badge

* [ ] **Scenario 4**: Dev Playbook displays all variants
    * Given the user navigates to the dev-playbook page
    * When viewing the "Logo" section
    * Then all three environment variants are displayed (DEV/red, TST/green, PRD/none)
    * And the current environment is highlighted

## 4. Technical Requirements
* **Environment Detection**: 
  - Add new env var `PUBLIC_ENVIRONMENT` with values: `local`, `test`, `production`
  - Centralize detection in `client/src/lib/config/env.ts`
* **Favicon**:
  - Create 3 favicon variants: `favicon.svg` (clean), `favicon-dev.svg` (red DEV badge), `favicon-tst.svg` (green TST badge)
  - Dynamically set favicon in `+layout.svelte` based on environment
* **AppLogo Component**:
  - Add optional `showEnvBadge` prop (default: `true`)
  - Badge should appear as small colored pill/banner on the icon portion
  - Colors: red (`bg-red-500`) for DEV, green (`bg-green-500`) for TST
* **Security**: No sensitive information exposed; environment name is acceptable
* **Performance**: No impact - static assets, no runtime computation

## 5. Design & UI/UX
* **Favicon Badge**: Small colored banner in top-right corner of the icon with 3-letter text (DEV/TST)
* **Logo Badge**: Small colored pill positioned near the Radar icon, not obtrusive but clearly visible
* **Colors**:
  - DEV: Red (`#ef4444` / red-500) - implies "danger/caution"
  - TST: Green (`#22c55e` / green-500) - implies "safe testing ground"
  - PRD: No badge - clean professional appearance

## 6. Implementation Notes / Research
* **Files to modify**:
  - `client/src/lib/assets/favicon.svg` - Base favicon (currently only file)
  - `client/src/lib/components/layout/AppLogo.svelte` - Add badge support
  - `client/src/routes/+layout.svelte` - Dynamic favicon selection
  - `client/.env.example` - Add PUBLIC_ENVIRONMENT
  - `client/src/routes/(public)/dev-playbook/+page.svelte` - Add environment variants showcase
* **New files**:
  - `client/src/lib/assets/favicon-dev.svg` - Red badge variant
  - `client/src/lib/assets/favicon-tst.svg` - Green badge variant
  - `client/src/lib/config/env.ts` - Centralized environment detection
* **Existing patterns**:
  - Environment vars already used via `$env/dynamic/public` (see `AppSidebarUser.svelte` line 19)
  - Current favicon is SVG-based, easy to create variants
* **Current favicon structure** (for reference when creating variants):
  ```svg
  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" 
       fill="none" stroke="hsl(205 85% 50%)" stroke-width="2" 
       stroke-linecap="round" stroke-linejoin="round">
    <!-- Radar icon paths -->
  </svg>
  ```
* **AppLogo component** currently at `client/src/lib/components/layout/AppLogo.svelte`:
  - Uses Lucide `Radar` icon
  - Has size variants: small, medium, large
  - Has display variants: full, icon, text
