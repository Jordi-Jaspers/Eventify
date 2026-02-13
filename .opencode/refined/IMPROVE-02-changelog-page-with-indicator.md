# Changelog Page with Version Indicator

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-02-13
**Depends On**: None

## 1. User Story
**As a** user of Eventify
**I want** to see what's new in recent releases via a changelog page and be notified when updates are available
**So that** I can stay informed about new features, improvements, and bug fixes

## 2. Business Context & Value
Users often miss new features and improvements when they're released. A changelog page provides transparency about product development and helps users discover functionality they may not know exists. The "new updates" indicator creates gentle awareness without being intrusive, improving feature adoption and user satisfaction.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Changelog accessible from sidebar
    * Given the user is logged in
    * When viewing the sidebar
    * Then a "What's New" link is visible in the USER WORKSPACE section
    * And clicking it navigates to `/changelog`

* [ ] **Scenario 2**: Changelog page displays releases
    * Given the user navigates to the changelog page
    * When the page loads
    * Then releases are displayed in reverse chronological order (newest first)
    * And each release shows: version number, release date, categorized changes

* [ ] **Scenario 3**: Changes are categorized
    * Given the changelog page is displayed
    * When viewing a release entry
    * Then changes are grouped by category: "New Features", "Improvements", "Bug Fixes"
    * And each category has a distinct visual style (badges/colors)

* [ ] **Scenario 4**: New release indicator appears
    * Given a new version has been released since the user last viewed the changelog
    * When the user views the sidebar
    * Then a small green dot indicator appears next to the "What's New" link
    * And the indicator is visible but not intrusive

* [ ] **Scenario 5**: Indicator disappears after viewing
    * Given the new release indicator is showing
    * When the user opens the changelog page
    * Then the indicator disappears
    * And it doesn't reappear until another new version is released

* [ ] **Scenario 6**: Version number displayed in sidebar
    * Given the user is logged in
    * When viewing the sidebar footer area (user section)
    * Then the current version number is displayed (e.g., "v2.5.0")
    * And the version is styled as small, muted text

## 4. Technical Requirements
* **Changelog Data**:
  - Static TypeScript file: `client/src/lib/data/changelog.ts`
  - Typed array of release entries with version, date, and categorized items
  - Version follows SemVer (e.g., `2.5.0`)
* **Route**: 
  - New page at `client/src/routes/(authenticated)/changelog/+page.svelte`
* **Version Tracking**:
  - Store "last seen version" in localStorage (key: `eventify_last_seen_version`)
  - Compare against latest version in changelog data
  - Use existing browser guard pattern from `$app/environment`
* **Sidebar Integration**:
  - Add "What's New" link to `AppSidebarNav.svelte` (USER WORKSPACE section)
  - Add version number to `AppSidebarUser.svelte` footer area
  - Indicator component shows when `lastSeenVersion < currentVersion`
* **Performance**: Changelog data is static, loaded at build time

## 5. Design & UI/UX
* **Changelog Page**:
  - Clean, readable layout with version headers
  - Category badges: "New" (green), "Improved" (blue), "Fixed" (amber)
  - Each item is a bullet point with brief description
  - Glass card styling consistent with app design
* **Sidebar Link**:
  - Icon: `Sparkles` from lucide-svelte
  - Text: "What's New"
  - Indicator: Small green pulsing dot (reuse live indicator pattern) when unread
* **Version in Footer**:
  - Small, muted text in user footer area: `v2.5.0`
  - Non-intrusive, informational only

## 6. Implementation Notes / Research
* **Files to create**:
  - `client/src/lib/data/changelog.ts` - Changelog entries array
  - `client/src/lib/types/changelog.ts` - TypeScript types for changelog
  - `client/src/routes/(authenticated)/changelog/+page.svelte` - Page component
  - `client/src/lib/stores/version.svelte.ts` - Version tracking with localStorage
* **Files to modify**:
  - `client/src/lib/components/layout/AppSidebarNav.svelte` - Add "What's New" link + indicator
  - `client/src/lib/components/layout/AppSidebarUser.svelte` - Add version display in footer
  - `client/src/lib/config/routes.ts` - Add CHANGELOG_PAGE route constant
* **Existing patterns to follow**:
  - Live indicator animation from dev-playbook (pulsing green dot):
    ```svelte
    <span class="relative flex h-2 w-2">
      <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
      <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
    </span>
    ```
  - LocalStorage with browser guard (see `client/src/lib/utils/sessionstorage.svelte.ts`)
  - Sidebar menu items structure (see `AppSidebarNav.svelte`)
  - Route constants pattern (see `client/src/lib/config/routes.ts`)
* **Changelog data structure**:
  ```typescript
  export interface ChangelogEntry {
    version: string;         // "2.5.0"
    date: string;            // "2026-02-13"
    features?: string[];     // New features
    improvements?: string[]; // Enhancements  
    fixes?: string[];        // Bug fixes
  }
  
  export const changelog: ChangelogEntry[] = [
    {
      version: "1.0.0",
      date: "2026-02-13",
      features: [
        "Initial release of Eventify",
        "Real-time event monitoring dashboard",
        "Channel management with API key authentication"
      ],
      improvements: [
        "Enhanced glassmorphism UI design"
      ]
    }
  ];
  ```
* **Version comparison logic**:
  - Use simple string comparison or semver library if needed
  - Latest version is always `changelog[0].version`
