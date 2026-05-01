---
epic: "GEN"
title: "Notification Bell with What's New Panel"
estimate: M
status: ready
created: 2026-04-04
depends_on: [ ]
labels: [ frontend ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user\
**I want** a notification bell in the sidebar that shows me when new features are available\
**So that** I don't miss important product updates and can easily access the changelog\

## 2. Business Context & Value
The current "What's New" indicator is a 2x2px green pulsing dot next to text in the sidebar footer — easily overlooked. Users miss feature announcements, reducing adoption of new capabilities. A universally recognized notification bell with badge is more visible and follows SaaS conventions (GitHub, Figma, Linear). Additionally, the bell establishes infrastructure for future notification types (org invites, quota warnings, system alerts).

## 3. Acceptance Criteria
* [ ] **Bell replaces What's New in sidebar footer**: The "What's New" sparkles button in the sidebar footer quick actions area is replaced with a notification bell icon button
    * Given a logged-in user on any authenticated page
    * When the sidebar footer renders
    * Then a bell icon (from lucide) is shown where "What's New" used to be
    * And the bell respects sidebar collapsed state (icon-only when collapsed)

* [ ] **Badge indicator on bell when unread**: A visible badge/dot appears on the bell when there are unread notifications
    * Given the user has not seen the latest changelog version (localStorage state differs from latest changelog version)
    * When the sidebar renders
    * Then a badge indicator (dot or count) appears on/near the bell icon
    * And the indicator is more visible than the current 2x2px dot (at least a colored badge or larger dot)

* [ ] **Bell opens notification sheet panel**: Clicking the bell opens a slide-out Sheet from the right side
    * Given a user clicks the bell icon
    * When the Sheet opens
    * Then it shows a "Notifications" header
    * And lists notification items grouped by type
    * And the first/only type is "What's New" showing the latest version title, date, and a "View full changelog" CTA button

* [ ] **Sheet CTA navigates to changelog**: The "View full changelog" button navigates to the changelog page
    * Given the notification sheet is open
    * When the user clicks "View full changelog"
    * Then the sheet closes
    * And the user is navigated to `/changelog`

* [ ] **What's New moved to user dropdown menu**: The "What's New" link is accessible from the user avatar dropdown
    * Given a user opens the avatar dropdown in the sidebar footer
    * When the Account Actions section renders
    * Then a "What's New" item with Sparkles icon appears (alongside Profile and Theme toggle)
    * And clicking it navigates to `/changelog`

* [ ] **Bell state syncs with seen state**: Dismissing notifications marks them as seen
    * Given the user has an unread changelog notification
    * When the user opens the sheet panel (or navigates to `/changelog` via any path)
    * Then the changelog is marked as seen
    * And the bell badge indicator disappears

* [ ] **No badge when no new content**: Bell shows without badge when everything is read
    * Given the user has already seen the latest changelog version
    * When the sidebar renders
    * Then the bell icon shows without any badge/indicator

* [ ] **Expandable notification architecture**: The notification system is designed for future notification types
    * Given the notification panel implementation
    * When reviewing the code structure
    * Then notifications are modeled as a list/array of typed items (e.g., `{ type: 'changelog' | 'system' | ... }`)
    * And the Sheet panel renders items based on type
    * And adding a new notification type requires only adding a new type + renderer, not restructuring

## 4. Technical Requirements
* **API Changes**: N/A — changelog data is static client-side, no backend changes needed
* **Database**: N/A — no schema changes
* **Security**: N/A — all client-side state, authenticated routes already protected
* **Performance**: N/A — lightweight UI change, no data fetching

## 5. Design & UI/UX
- **Bell location**: Sidebar footer, replacing the current "What's New" button position (quick actions area above user menu)
- **Bell icon**: `Bell` from `@lucide/svelte`, same `size-4` as other sidebar icons
- **Badge**: Small colored dot (e.g., `bg-primary` or `bg-green-500`) positioned top-right of bell icon, or a `Badge` component with count
- **Sheet panel**: Opens from right side, glassmorphism styling consistent with existing sheets (`bg-card/50 backdrop-blur-xl`)
- **Notification item**: Card-like layout showing icon + title + date + CTA. For changelog type: Sparkles icon, "Version X.Y.Z released", date, "View changelog" button
- **User dropdown**: "What's New" item added in Account Actions group between Profile and Theme toggle, with Sparkles icon
- **Collapsed sidebar**: Bell shows as icon-only, badge still visible
- **Follow existing patterns**: Use `Sheet` component from `$lib/components/ui/sheet/`, `Badge` from `$lib/components/ui/badge`

## 6. Implementation Notes

### Key components:
- **AppSidebarUser.svelte**: Replace "What's New" quick action with bell; add "What's New" to user dropdown menu
- **New NotificationPanel.svelte**: Sheet component rendering typed notification items
- **New notification store**: Aggregates changelog state (from existing `versionStore`) + future notification sources
- **New notification types**: TypeScript types for `NotificationType`, `NotificationItem`

### Patterns to follow:
- Existing `Sheet` usage in the codebase for slide-out panel patterns
- Existing `versionStore` for changelog state — wrap/extend, don't replace
- Existing `Badge` component from `$lib/components/ui/badge`
- Sidebar collapsed state: use `group-data-[collapsible=icon]:` classes for responsive behavior

### Future considerations (NOT in scope):
- Profile page notification tab — add to backlog when backend notification storage is introduced
- Backend notification API — needed when real user-targeted notifications exist (org invites, alerts)
- Notification preferences/settings
