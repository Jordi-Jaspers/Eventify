# User Dashboard Organization Quick Navigation

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-01-04

## 1. User Story
**As a** user who belongs to one or more organizations
**I want** to see my organizations on my dashboard and quickly navigate to an organization-specific dashboard
**So that** I can efficiently switch between my personal context and organizational workspaces

## 2. Business Context & Value
Eventify supports both personal and organizational contexts. Currently, the user dashboard shows account info but provides no quick access to organizations. Users must rely on the sidebar's "WORKSPACE" section, which only shows the *current* organization. This story:
- Surfaces all user organizations on the main dashboard
- Creates an organization-specific dashboard route for future org-specific content
- Adds the org dashboard to the WORKSPACE sidebar section
- Hides organization-related UI entirely for users without orgs (clean personal-only experience)

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: User with organizations sees organization grid
    *   Given I am logged in and belong to 1+ organizations
    *   When I navigate to `/dashboard`
    *   Then I see the existing welcome card
    *   And I see a grid of organization cards below it
    *   And each card shows the organization name

*   [ ] **Scenario 2**: Clicking organization card navigates to org dashboard
    *   Given I am viewing the organization grid on my dashboard
    *   When I click on an organization card
    *   Then the organization context switches to that organization
    *   And I am navigated to `/organizations/{orgId}/dashboard`

*   [ ] **Scenario 3**: Organization dashboard page exists
    *   Given I navigate to `/organizations/{orgId}/dashboard`
    *   When the page loads
    *   Then I see a placeholder/empty dashboard page for that organization
    *   And the sidebar WORKSPACE section shows I'm in that org context

*   [ ] **Scenario 4**: Sidebar shows Dashboard link in WORKSPACE
    *   Given I have selected an organization (context is set)
    *   When I view the sidebar
    *   Then the WORKSPACE section contains a "Dashboard" link
    *   And clicking it navigates to `/organizations/{orgId}/dashboard`
    *   And the existing "Members" link remains

*   [ ] **Scenario 5**: User without organizations sees no org UI
    *   Given I am logged in but do not belong to any organizations
    *   When I navigate to `/dashboard`
    *   Then I see the welcome card and personal account info
    *   And I do NOT see any organization grid or organization-related UI

*   [ ] **Scenario 6**: Organization grid handles loading state
    *   Given I navigate to `/dashboard` and organizations are loading
    *   When the organization store is in loading state
    *   Then the organization grid area shows loading skeletons

## 4. Technical Requirements
*   **Frontend - New Route**:
    *   Create `/organizations/[orgId]/dashboard/+page.svelte`
    *   Basic placeholder content: "Organization Dashboard for {orgName}" with org info card
    *   Page should use the existing authenticated layout with sidebar

*   **Frontend - Dashboard Enhancement** (`/dashboard/+page.svelte`):
    *   Add organization grid section below the "Getting Started" card
    *   Conditionally render only if `organizationStore.organizations.length > 0`
    *   Grid layout: responsive (1 col mobile, 2 cols tablet, 3 cols desktop)
    *   Each card: org name, role badge, "Go to Dashboard" action
    *   On card click: `organizationStore.switchOrganization(orgId)` then `goto(/organizations/${orgId}/dashboard)`

*   **Frontend - Organization Grid Component** (optional extraction):
    *   Consider creating `OrganizationGrid.svelte` in `$lib/components/` if reuse is anticipated
    *   Props: `organizations: UserOrganizationResponse[]`, `onSelect: (orgId: number) => void`

*   **Frontend - Sidebar Update** (`AppSidebarNav.svelte`):
    *   In WORKSPACE section, add "Dashboard" menu item above "Members"
    *   Route: `/organizations/${currentOrganization.organizationId}/dashboard`
    *   Icon: `LayoutDashboard` (already imported)

*   **Frontend - Routes Config** (`routes.ts`):
    *   Add `ORGANIZATION_DASHBOARD_PAGE: (orgId: number) => ({ path: `/organizations/${orgId}/dashboard`, type: RouteType.PRIVATE })`

*   **Security**: Route protected by existing authenticated layout; no new permissions needed

*   **Performance**: Organization list already fetched by `organizationStore.initialize()` on app load

## 5. Design & UI/UX
*   **Organization Cards**:
    *   Use existing Card component with glassmorphism styling (consistent with dashboard cards)
    *   Show organization name prominently
    *   Small role badge (OWNER/ADMIN/MEMBER) with appropriate color
    *   Subtle hover effect (scale + shadow, similar to navbar buttons)
    *   Grid gap: `gap-4` or `gap-6`

*   **Section Header** (above grid):
    *   "Your Organizations" with Building2 icon
    *   Subtext showing count: "You belong to {n} organization(s)"

*   **Empty State**: Not needed - entire section is hidden when no orgs

*   **Org Dashboard Page** (placeholder):
    *   Simple welcome message with org name
    *   Card showing org info (name, user's role, member count if available)
    *   "Coming soon" placeholder for future org-specific widgets

## 6. Implementation Notes / Research
*   **Organization Store**: `client/src/lib/stores/organization.svelte.ts` - already provides `organizations`, `currentOrganization`, `switchOrganization()`
*   **Existing Dashboard**: `client/src/routes/(authenticated)/dashboard/+page.svelte` - add new section below "Getting Started" card
*   **Sidebar Pattern**: `client/src/lib/components/layout/AppSidebarNav.svelte` - WORKSPACE section already conditionally rendered when `currentOrganization` exists
*   **Route Pattern**: Follow existing `/organizations/[orgId]/members/+page.svelte` structure
*   **UserOrganizationResponse fields**: `organizationId`, `organizationName`, `role`, `joinedAt` (from `models.ts`)
*   **Potential Pitfall**: Ensure `organizationStore.switchOrganization()` is called before navigation so the sidebar updates correctly
