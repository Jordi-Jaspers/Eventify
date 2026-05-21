---
epic: "BUGS"
title: "Add tooltips to sidebar icons when collapsed"
estimate: S
status: ready
created: 2026-05-21
depends_on: []
labels: [frontend, ux]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user with the sidebar collapsed\
**I want** tooltips on hover showing the route label\
**So that** I know where each icon navigates without expanding the sidebar\

## 2. Business Context & Value
When the sidebar is collapsed to icon-only mode, users lose all text labels. Without tooltips, navigation becomes guesswork — especially for new users or infrequently used routes.

## 3. Acceptance Criteria
* [ ] **Tooltips appear on hover when collapsed**
    * Given the sidebar is in collapsed (icon-only) state
    * When the user hovers over a navigation icon
    * Then a tooltip appears to the right showing the item label (e.g., "Dashboard", "Channels")
* [ ] **Tooltips hidden when expanded**
    * Given the sidebar is in expanded state
    * When the user hovers over a navigation item
    * Then no tooltip appears (label is already visible as text)
* [ ] **Tooltips hidden on mobile**
    * Given the user is on a mobile viewport
    * When interacting with sidebar items
    * Then no tooltips appear (mobile uses full overlay sidebar)
* [ ] **All nav items have tooltips**
    * Given any navigation icon in the sidebar (all ~13 items across all sections)
    * When collapsed and hovered
    * Then the correct label is shown

## 4. Technical Requirements
* **API Changes**: N/A
* **Database**: N/A
* **Security**: N/A
* **Performance**: N/A — tooltips are CSS/DOM only, no network calls

## 5. Design & UI/UX
- Tooltip appears to the right of the icon, vertically centered
- Uses existing shadcn Tooltip component (already configured with `delayDuration={0}`)
- Label only — no section prefix (e.g., "Dashboard" not "User Workspace → Dashboard")

## 6. Implementation Notes

The tooltip infrastructure is **already fully built** — just not wired up.

**File:** `client/src/lib/components/layout/AppSidebarNav.svelte`

For each `<Sidebar.MenuButton>`, add the `tooltipContent` prop with the item's label string:

```svelte
<Sidebar.MenuButton onclick={...} isActive={...} tooltipContent="Dashboard">
  <LayoutDashboard class="size-4" />
  <span>Dashboard</span>
</Sidebar.MenuButton>
```

The `sidebar-menu-button.svelte` component already handles:
- Only showing tooltip when `sidebar.state === "collapsed"`
- Hiding on mobile (`sidebar.isMobile`)
- Positioning to the right with center alignment

No changes needed to the tooltip component itself.

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `client/src/lib/components/layout/AppSidebarNav.svelte` | Add `tooltipContent` prop to all ~13 MenuButton instances |
