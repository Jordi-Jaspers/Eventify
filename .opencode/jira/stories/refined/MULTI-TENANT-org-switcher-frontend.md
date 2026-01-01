# Organization Switcher Frontend

**Epic**: Multi-Tenant User & Organization Management
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2024-12-23

## 1. User Story
**As a** user belonging to multiple organizations
**I want** to switch between organizations from the sidebar
**So that** I can access different workspaces without logging out

## 2. Business Context & Value
Users who belong to multiple organizations (e.g., consultants, contractors, or employees of partner companies) need a seamless way to switch contexts. This improves user experience and enables the multi-tenant architecture to function smoothly.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Display current organization
    *   Given I am logged in and have selected an organization
    *   When I view the sidebar
    *   Then I see the current organization name/logo displayed

*   [ ] **Scenario 2**: Switch organization
    *   Given I belong to multiple organizations
    *   When I click the org switcher and select a different organization
    *   Then the `currentOrganizationId` cookie is updated
    *   And the page reloads or navigates appropriately

*   [ ] **Scenario 3**: Redirect on org-specific pages
    *   Given I am on an org-specific page (e.g., org dashboard)
    *   When I switch to a different organization
    *   Then I am redirected to that organization's dashboard

*   [ ] **Scenario 4**: Stay on page for non-org pages
    *   Given I am on a user-private page (e.g., profile settings)
    *   When I switch organizations
    *   Then I remain on the current page
    *   And future org-scoped requests use the new context

*   [ ] **Scenario 5**: User with no organizations
    *   Given I have no organization memberships
    *   When I view the sidebar
    *   Then I see an empty state: "No organizations" with no switcher
    *   And I can still access user-private and admin pages (if applicable)

*   [ ] **Scenario 6**: User with one organization
    *   Given I belong to exactly one organization
    *   When I view the sidebar
    *   Then I see my organization displayed (no dropdown needed)
    *   And the context is automatically set to that organization

*   [ ] **Scenario 7**: Fetch user's organizations on load
    *   Given I log in to the application
    *   When the app initializes
    *   Then my organization memberships are fetched
    *   And the last selected org (from cookie) is validated and restored
    *   Or the first org is selected by default

*   [ ] **Scenario 8**: Invalid org context handling
    *   Given my cookie contains an org ID I'm no longer a member of
    *   When the app initializes
    *   Then the invalid context is cleared
    *   And I'm either prompted to select an org or default to the first one

*   [ ] **Scenario 9**: Navigate to organization members from sidebar
    *   Given I am viewing the sidebar with my organizations
    *   When I expand an organization
    *   Then I see sub-items: Dashboard, Members, Settings (future)
    *   And clicking "Members" navigates to `/organizations/[orgId]/members`

## 4. Technical Requirements
*   **API Changes**:
    *   `GET /v1/user/organizations` — Get current user's organization memberships
        *   Response: `List<UserOrganizationResponse>` with `orgId`, `orgName`, `orgSlug`, `role`, `joinedAt`
*   **Cookie**:
    *   Name: `currentOrganizationId`
    *   Value: Organization ID (Long)
    *   HttpOnly: false (needs JS access)
    *   Secure: true (production)
    *   SameSite: Lax
    *   Max-Age: 30 days (persistent across sessions)
*   **Header**:
    *   Frontend sends `X-Organization-Id` header on all org-scoped API requests
    *   API client interceptor reads from cookie and adds header
*   **Performance**:
    *   Org list cached in memory/store after initial fetch
    *   Refresh on login or manual trigger

## 5. Design & UI/UX
*   **Location**: Sidebar, below the logo/app name area (similar to Vercel/Linear)
*   **Component**: Collapsible section showing:
    *   Current org with checkmark
    *   List of other orgs (show role badge: Owner, Admin, Member)
    *   Organization avatar/icon (first letter or uploaded logo - future)
    *   Sub-navigation for each org: Dashboard, Members, Settings (future)
*   **Visual States**:
    *   Loading: Skeleton while fetching orgs
    *   Empty: "No organizations" text
    *   Single org: Static display with sub-nav (no dropdown)
    *   Multiple orgs: Clickable dropdown with sub-nav for selected org
*   **Transitions**: Smooth animation when switching
*   **Mobile**: Full-width dropdown in mobile sidebar

## 6. Implementation Notes / Research
*   **New Files Needed**:
    *   Backend:
        *   `UserOrganizationController.java` or add to `UserController`
        *   `UserOrganizationResponse.java`
    *   Frontend:
        *   `client/src/lib/components/layout/OrgSwitcher.svelte`
        *   `client/src/lib/stores/organization.ts` — Svelte store for org context
        *   `client/src/lib/api/user/UserOrganizationController.ts`
*   **Existing Files to Modify**:
    *   `AppSidebar.svelte` or `AppSidebarNav.svelte` — integrate OrgSwitcher
    *   `client.ts` — Add interceptor for `X-Organization-Id` header
    *   `hooks.server.ts` — Read cookie on SSR for initial org context
*   **Route Classification**:
    *   Org-specific routes: `/dashboard`, `/settings`, `/organizations/[orgId]/members`
    *   User-private routes: `/profile`, `/account`
    *   Admin routes: `/admin/*`
    *   Logic needed to determine current route type for redirect behavior
*   **Patterns to Follow**:
    *   Similar to Vercel's team switcher or Linear's workspace switcher
    *   Use shadcn-svelte dropdown-menu or collapsible component
*   **Existing Route**:
    *   `/organizations/[orgId]/members` page already exists (built 2026-01-01)
    *   Add navigation link in org switcher sub-menu
