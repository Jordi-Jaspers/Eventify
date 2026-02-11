# Update Favicon with App Logo

**Epic**: Future Considerations
**Status**: Ready for Dev
**Estimate**: XS
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** user
**I want** to see the Eventify logo as the browser favicon
**So that** I can easily identify the Eventify tab among my open browser tabs

## 2. Business Context & Value
Currently, the browser tab shows the default Svelte logo (orange "S"), which is confusing and doesn't represent the Eventify brand. The app already uses the Lucide `Radar` icon as its logo throughout the application. Using the same icon as the favicon creates brand consistency.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Favicon displays Radar icon
    * Given I open any Eventify page in a browser
    * When the page loads
    * Then the browser tab shows the Radar icon (matching the app logo)

* [ ] **Scenario 2**: Favicon works across browsers
    * Given the favicon is an SVG
    * When viewed in Chrome, Firefox, Safari, or Edge
    * Then the icon displays correctly

## 4. Technical Requirements
* **API Changes**: None
* **Database**: None
* **Security**: N/A
* **Performance**: N/A

### The Change
Replace the content of `client/src/lib/assets/favicon.svg` with the Lucide Radar icon SVG.

**Current file**: Svelte logo (orange "S" shape)
**New content**: Radar icon from Lucide, using the primary brand color

## 5. Design & UI/UX
The favicon should match the existing logo:
- Icon: Radar (from Lucide icons)
- Color: Primary brand color (matches `text-primary` used in AppLogo.svelte)

## 6. Implementation Notes / Research

### Files to Modify
| File | Change |
|------|--------|
| `client/src/lib/assets/favicon.svg` | Replace Svelte logo with Radar icon SVG |

### Getting the Radar SVG
The Radar icon can be obtained from Lucide:
- Source: https://lucide.dev/icons/radar
- SVG code needed (set appropriate viewBox and primary color)

### Current Setup (no changes needed)
The favicon is already properly wired up in `client/src/routes/+layout.svelte`:
```svelte
import favicon from '$lib/assets/favicon.svg';
...
<link rel="icon" href={favicon} />
```

This setup will automatically pick up the new SVG content - no other files need modification.

### Primary Color Reference
From the app's theme, `text-primary` maps to the CSS variable `--primary`. Check `app.css` or theme config for the exact hex value to use in the SVG.
