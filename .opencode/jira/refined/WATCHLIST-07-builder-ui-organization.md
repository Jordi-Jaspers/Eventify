# Watchlist Builder UI (Organization)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-03-crud-api-organization.md, WATCHLIST-06-builder-ui-user.md

## 1. User Story
**As an** organization OWNER or ADMIN
**I want** to create and edit organization watchlists using a drag-and-drop interface
**So that** I can configure shared watchlists for my team

## 2. Business Context & Value
Organization watchlists are shared across all members. Only OWNER/ADMIN users can create and edit them, ensuring proper governance over what the team monitors.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Create new organization watchlist
    *   Given an OWNER or ADMIN user
    *   When they navigate to `/organizations/{orgId}/watchlists/new`
    *   Then they see the builder with organization channels available

*   [ ] **Scenario 2**: Edit existing organization watchlist
    *   Given an OWNER or ADMIN user
    *   When they navigate to `/organizations/{orgId}/watchlists/{id}`
    *   Then they see the builder pre-populated with the watchlist data

*   [ ] **Scenario 3**: Only organization channels shown
    *   Given the builder page
    *   When viewing available channels
    *   Then only channels belonging to this organization are shown

*   [ ] **Scenario 4**: MEMBER cannot access builder
    *   Given a user with MEMBER role
    *   When they try to navigate to `/organizations/{orgId}/watchlists/new`
    *   Then they are redirected (or shown access denied)

*   [ ] **Scenario 5**: All builder features work
    *   Given an authorized user on the builder
    *   When they perform drag-drop, reorder, auto-save operations
    *   Then all features work as per WATCHLIST-06

*   [ ] **Scenario 6**: Back navigation returns to org watchlists
    *   Given the builder page
    *   When they click "Back"
    *   Then they return to `/organizations/{orgId}/watchlists`

## 4. Technical Requirements

### Frontend Routes
- Create: `/organizations/[orgId]/watchlists/new`
- Edit: `/organizations/[orgId]/watchlists/[id]`

### API Integration
- `GET /v1/organization/{orgId}/watchlists/{id}` - Load existing
- `POST /v1/organization/{orgId}/watchlists` - Create
- `PUT /v1/organization/{orgId}/watchlists/{id}` - Update
- `POST /v1/organization/{orgId}/channels/search` - Load available channels

### Authorization
- Check user's role before rendering page
- Redirect MEMBER users to watchlist list page

### Security
- Only OWNER/ADMIN can access builder
- Only organization channels can be added

### Performance
- Same as user builder

## 5. Design & UI/UX

### Page Layout
Same as user builder (WATCHLIST-06) with:
- Title includes organization name
- Only organization channels in available list

### Access Denied
If a MEMBER tries to access:
- Show toast: "You don't have permission to edit watchlists"
- Redirect to `/organizations/{orgId}/watchlists`

## 6. Implementation Notes / Research

### File Locations
- Create: `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/new/+page.svelte`
- Edit: `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/[id]/+page.svelte`

### Component Reuse
The `WatchlistBuilder.svelte` component from WATCHLIST-06 should be reusable:

```svelte
<WatchlistBuilder
    mode={isNew ? 'create' : 'edit'}
    watchlistId={id}
    organizationId={orgId}
    channelSearchFn={searchOrganizationChannels}
    saveFn={saveOrganizationWatchlist}
    backPath={`/organizations/${orgId}/watchlists`}
/>
```

### Patterns to Follow
- Follow organization channel page patterns for authorization
- Use `organizationStore` for role checks

### Authorization Check
```typescript
import { organizationStore } from '$lib/stores/organization.svelte';
import { goto } from '$app/navigation';
import { toast } from 'svelte-sonner';

onMount(() => {
    const role = organizationStore.currentRole;
    if (role !== 'OWNER' && role !== 'ADMIN') {
        toast.error("You don't have permission to edit watchlists");
        goto(`/organizations/${orgId}/watchlists`);
    }
});
```

### Test Cases
- Create as OWNER
- Create as ADMIN
- Access as MEMBER (redirect)
- Edit existing watchlist
- Only org channels visible
- Auto-save works
