# Refactor Navigation to Dashboard with Sidebar

**Epic**: Multi-Tenant User & Team Management (Admin-Provisioned)
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2024-12-18

## 1. User Story
**As a** authenticated user
**I want** a sidebar-based navigation layout
**So that** I can easily access different sections of the application with a clean, organized interface

## 2. Business Context & Value
The current top navigation bar becomes limiting as the application grows with more features. A sidebar provides:
- Better scalability for adding new navigation items
- Clearer visual hierarchy between navigation sections
- Modern dashboard UX that users expect from enterprise applications
- Space efficiency with collapsible icon mode

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Sidebar displays in collapsed (icon) mode by default
    *   Given I am an authenticated user on any authenticated route
    *   When I view the page
    *   Then I see a sidebar in collapsed/icon mode on the left side
    *   And the main content area adjusts accordingly

*   [ ] **Scenario 2**: Sidebar can be expanded via trigger button
    *   Given I am viewing the sidebar in collapsed mode
    *   When I click the sidebar trigger/toggle button
    *   Then the sidebar expands to show full labels
    *   And clicking again collapses it back to icon mode

*   [ ] **Scenario 3**: Sidebar state persists across page navigation
    *   Given I have expanded the sidebar
    *   When I navigate to another authenticated page
    *   Then the sidebar remains expanded
    *   And the state is preserved via cookie

*   [ ] **Scenario 4**: Sidebar displays correct navigation sections for regular users
    *   Given I am a regular (non-admin) authenticated user
    *   When I view the sidebar
    *   Then I see "Main" section with: Dashboard
    *   And I see "Coming Soon..." item (disabled/placeholder)
    *   And I see user account button at the bottom with my avatar (initials)

*   [ ] **Scenario 5**: Sidebar displays admin section for admin users
    *   Given I am an authenticated user with ADMIN role
    *   When I view the sidebar
    *   Then I see all regular user navigation items
    *   And I see "Administration" section with: Admin Dashboard, Organizations (sub-item: Create)
    *   And the admin section is visually distinct

*   [ ] **Scenario 6**: User account button shows avatar with initials
    *   Given I am authenticated as "John Doe"
    *   When I view the sidebar footer
    *   Then I see an avatar displaying "JD" (first letters of first and last name)
    *   And clicking it shows a dropdown with Profile and Logout options

*   [ ] **Scenario 7**: Active route is highlighted
    *   Given I am on the `/dashboard` route
    *   When I view the sidebar
    *   Then the "Dashboard" menu item is visually highlighted as active

*   [ ] **Scenario 8**: Sidebar rail allows hover-expand (optional enhancement)
    *   Given the sidebar is in collapsed mode
    *   When I hover over the sidebar rail
    *   Then the sidebar temporarily expands to show labels
    *   And collapses when I move my mouse away

## 4. Technical Requirements
*   **Component Changes**:
    *   Install shadcn-svelte `sidebar` component: `bunx shadcn-svelte@latest add sidebar-07`
    *   Create new `AppSidebar.svelte` component in `client/src/lib/components/layout/`
    *   Refactor `(authenticated)/+layout.svelte` to use `Sidebar.Provider` + `AppSidebar`
    *   Deprecate or repurpose `AppNavbar.svelte` (may keep for public pages if needed)

*   **CSS Changes**:
    *   Add sidebar CSS variables to `client/src/app.css` for theming:
      ```css
      :root {
        --sidebar: oklch(0.985 0 0);
        --sidebar-foreground: oklch(0.145 0 0);
        /* ... etc */
      }
      .dark { /* dark mode variants */ }
      ```

*   **State Management**:
    *   Use `Sidebar.Provider` with cookie persistence (`sidebar:state` cookie)
    *   Default state: `collapsed` (icon mode)
    *   Collapsible mode: `icon` (not `offcanvas`)

*   **Security**:
    *   Admin section visibility controlled by `$currentUser?.role === 'ADMIN'` (already exists)
    *   No new API endpoints required

*   **Performance**:
    *   Sidebar state read from cookie on server-side to prevent flash of incorrect state
    *   No additional API calls for navigation rendering

## 5. Design & UI/UX

should look like the sidebar-07 example in shadcn-svelte docs, adapted to our branding and requirements.

### Sidebar Structure
```
┌─────────────────────────────────┐
│ [Logo] Eventify        [Toggle] │  ← Header
├─────────────────────────────────┤
│ MAIN                            │  ← Group Label
│   🏠 Dashboard                  │  ← Active state
│   ⏳ Coming Soon...    (dimmed) │  ← Placeholder
├─────────────────────────────────┤
│ ADMINISTRATION (admin only)     │  ← Conditional Group
│   📊 Admin Dashboard            │
│   🏢 Organizations              │
│       └─ ➕ Create New          │  ← Sub-menu item
├─────────────────────────────────┤
│                                 │
│         (spacer)                │
│                                 │
├─────────────────────────────────┤
│ [JD] John Doe           [▼]     │  ← Footer (dropdown)
│      john@example.com           │
└─────────────────────────────────┘
```

### Avatar Component
- Circular background with gradient (primary → accent)
- White text showing initials (first letter of firstName + first letter of lastName)
- Fallback: "?" if no name available

### Collapsed (Icon) Mode
- Only icons visible in navigation
- Avatar still visible in footer
- Hover on rail expands temporarily

### Styling Guidelines
- Follow existing glassmorphism theme (`bg-card/50 backdrop-blur-xl`)
- Use `Sidebar.Rail` for hover-expand behavior
- Match existing color scheme from `AppNavbar.svelte`

## 6. Implementation Notes / Research

### Existing Code References
*   **Current navbar**: `client/src/lib/components/layout/AppNavbar.svelte` - Reference for logout logic, admin role check
*   **Auth store**: `client/src/lib/stores/auth.ts` - Use `currentUser` for role checks and user info
*   **Routes config**: `client/src/lib/config/routes.ts` - Use `CLIENT_ROUTES` for navigation paths
*   **Layout**: `client/src/routes/(authenticated)/+layout.svelte` - Primary file to refactor

### shadcn-svelte Sidebar Installation
```bash
cd client && pnpm dlx shadcn-svelte@latest add sidebar
```
This will add multiple files to `client/src/lib/components/ui/sidebar/`

### Recommended Component Structure
```
client/src/lib/components/
├── layout/
│   ├── AppSidebar.svelte          # Main sidebar component
│   ├── AppSidebarHeader.svelte    # Logo + brand
│   ├── AppSidebarNav.svelte       # Navigation groups
│   ├── AppSidebarUser.svelte      # User footer with avatar dropdown
│   └── AppBackground.svelte       # (keep existing)
```

### Cookie Handling for SSR
Use SvelteKit's `cookies` in `hooks.server.ts` or layout `load` function to read sidebar state and pass to client to prevent hydration mismatch.

### Potential Pitfalls
1. **Hydration mismatch**: If sidebar state differs between SSR and client, ensure cookie is read server-side
2. **Mobile breakpoints**: Since we're desktop-only, ensure sidebar doesn't break on smaller viewports (consider minimum supported width)
3. **Z-index conflicts**: Ensure sidebar doesn't conflict with modals/toasts (Sonner)
