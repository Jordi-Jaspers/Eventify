# Watchlist List Page (Organization)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-03-crud-api-organization.md, WATCHLIST-04-list-page-user.md

## 1. User Story
**As an** organization member
**I want** to view a list of my organization's watchlists in a searchable table
**So that** I can manage and navigate to shared watchlists

## 2. Business Context & Value
Organization members need to see all shared watchlists for their organization. OWNER/ADMIN users can create, edit, and delete, while regular members have read-only access.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: View organization watchlists list
    *   Given an authenticated organization member
    *   When they navigate to `/organizations/{orgId}/watchlists`
    *   Then they see a DataTable with the organization's watchlists

*   [ ] **Scenario 2**: Empty state
    *   Given an organization with no watchlists
    *   When a member navigates to the watchlists page
    *   Then they see an empty state message

*   [ ] **Scenario 3**: OWNER/ADMIN sees create button
    *   Given a user with OWNER or ADMIN role
    *   When they view the watchlists page
    *   Then they see the "New Watchlist" button

*   [ ] **Scenario 4**: MEMBER does not see create button
    *   Given a user with MEMBER role
    *   When they view the watchlists page
    *   Then the "New Watchlist" button is hidden

*   [ ] **Scenario 5**: OWNER/ADMIN sees edit/delete actions
    *   Given a user with OWNER or ADMIN role
    *   When they view a watchlist row
    *   Then they see edit and delete action buttons

*   [ ] **Scenario 6**: MEMBER does not see edit/delete actions
    *   Given a user with MEMBER role
    *   When they view a watchlist row
    *   Then the edit and delete buttons are hidden

*   [ ] **Scenario 7**: Navigate to create
    *   Given an OWNER/ADMIN on the watchlists page
    *   When they click "New Watchlist"
    *   Then they navigate to `/organizations/{orgId}/watchlists/new`

*   [ ] **Scenario 8**: Navigate to edit
    *   Given an OWNER/ADMIN viewing a watchlist
    *   When they click the edit action
    *   Then they navigate to `/organizations/{orgId}/watchlists/{id}`

## 4. Technical Requirements

### Frontend Route
- Path: `/organizations/[orgId]/watchlists`
- File: `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/+page.svelte`

### API Integration
- Uses `POST /v1/organization/{orgId}/watchlists/search` for list data
- Uses `DELETE /v1/organization/{orgId}/watchlists/{id}` for deletion

### DataTable Columns
Same as user watchlists (WATCHLIST-04)

### Authorization
- Use `organizationStore` to check user's role in the organization
- Show/hide buttons based on `isOwnerOrAdmin` check

### Security
- Page requires authentication
- User must be a member of the organization
- Create/Edit/Delete UI only visible to OWNER/ADMIN

### Performance
- Same as user watchlists

## 5. Design & UI/UX

### Page Layout
Same as user watchlists with these differences:
- Title: "Organization Watchlists" or "{Org Name} Watchlists"
- Subtitle: "Manage shared watchlists for your organization"
- Conditional rendering of action buttons based on role

### Empty State (OWNER/ADMIN)
- Title: "No watchlists yet"
- Description: "Create your first watchlist to start monitoring channels"
- CTA Button: "Create Watchlist"

### Empty State (MEMBER)
- Title: "No watchlists yet"
- Description: "Your organization doesn't have any watchlists. Contact an admin to create one."
- No CTA button

### Role-Based UI
```svelte
{#if isOwnerOrAdmin}
    <Button onclick={() => goto(`/organizations/${orgId}/watchlists/new`)}>
        New Watchlist
    </Button>
{/if}
```

## 6. Implementation Notes / Research

### File Locations
- Page: `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/+page.svelte`

### Patterns to Follow
- Follow `client/src/routes/(authenticated)/organizations/[orgId]/channels/+page.svelte`
- Use `organizationStore` for role checking
- Follow existing org page patterns for authorization

### Role Check Helper
```typescript
const isOwnerOrAdmin = $derived(
    organizationStore.currentRole === 'OWNER' || 
    organizationStore.currentRole === 'ADMIN'
);
```

### Navigation
Add to organization sidebar/settings navigation:
- Icon: `ClipboardList` or `ListChecks`
- Label: "Watchlists"
- Path: `/organizations/{orgId}/watchlists`

### Component Reuse
- Reuse components created in WATCHLIST-04 where possible
- Consider extracting a shared `WatchlistDataTable` component
