# Monitor Page (Organization)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-09-monitor-page-user.md

## 1. User Story
**As an** organization member
**I want** to view organization watchlists in a timeline monitoring interface
**So that** I can see the health status of my team's channels over time

## 2. Business Context & Value
Organization members need access to shared watchlists for monitoring their team's infrastructure. All members can view the monitor page, regardless of their role. This provides visibility across the entire organization.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Organization monitor page loads
    *   Given an authenticated organization member
    *   When they navigate to `/organizations/{orgId}/monitor`
    *   Then they see the monitor interface with organization watchlists

*   [ ] **Scenario 2**: Only organization watchlists shown
    *   Given the watchlist dropdown
    *   When viewing available watchlists
    *   Then only watchlists belonging to this organization are shown

*   [ ] **Scenario 3**: All members can view
    *   Given a user with any role (OWNER, ADMIN, or MEMBER)
    *   When they navigate to the monitor page
    *   Then they have full access to view all watchlists

*   [ ] **Scenario 4**: Session storage per organization
    *   Given user switches between organizations
    *   When returning to an organization's monitor page
    *   Then the session state is specific to that organization

*   [ ] **Scenario 5**: All monitor features work
    *   Given the organization monitor page
    *   Then all features from WATCHLIST-09 work identically:
        - Time range controls
        - Filters
        - Timeline visualization
        - Event expansion
        - Auto-refresh

*   [ ] **Scenario 6**: Non-member cannot access
    *   Given a user who is not a member of the organization
    *   When they try to navigate to the monitor page
    *   Then they receive a 403/404 error

## 4. Technical Requirements

### Frontend Route
- Path: `/organizations/[orgId]/monitor`
- File: `client/src/routes/(authenticated)/organizations/[orgId]/monitor/+page.svelte`

### API Integration
- `POST /v1/organization/{orgId}/watchlists/search` - For watchlist dropdown
- `POST /v1/organization/{orgId}/monitor` - For timeline data

### Session Storage Key
Use organization-specific key:
```typescript
const SESSION_KEY = `monitor_state_org_${orgId}`;
```

### Security
- User must be a member of the organization
- Any role can view the monitor page

### Performance
- Same as user monitor page

## 5. Design & UI/UX

### Page Layout
Identical to user monitor page (WATCHLIST-09) with:
- Title includes organization context
- Only organization watchlists in dropdown

### Navigation
Add to organization sidebar:
- Icon: `Activity` or `BarChart3` from Lucide
- Label: "Monitor"
- Path: `/organizations/{orgId}/monitor`

## 6. Implementation Notes / Research

### File Locations
- Page: `client/src/routes/(authenticated)/organizations/[orgId]/monitor/+page.svelte`

### Component Reuse
The monitor components from WATCHLIST-09 should be fully reusable:

```svelte
<MonitorView
    watchlistSearchFn={() => searchOrganizationWatchlists(orgId)}
    monitorFn={(request) => fetchOrganizationMonitor(orgId, request)}
    sessionKey={`monitor_state_org_${orgId}`}
/>
```

Or, if using a more integrated approach, pass the organization context:

```svelte
<script lang="ts">
    import { page } from '$app/stores';
    import MonitorPage from '$lib/components/monitor/MonitorPage.svelte';
    
    const orgId = $derived(Number($page.params.orgId));
</script>

<MonitorPage {orgId} />
```

The `MonitorPage` component detects if `orgId` is provided and uses the appropriate API endpoints.

### Patterns to Follow
- Follow organization page patterns for layout
- Use `organizationStore` for context

### Authorization Check
```typescript
onMount(() => {
    // organizationStore already checks membership when loading
    // If user is not a member, they won't see this org in their list
    // Additional check for direct URL access:
    if (!organizationStore.currentOrganization) {
        goto('/dashboard');
    }
});
```

### Test Cases
- View as OWNER
- View as ADMIN
- View as MEMBER
- Access as non-member (should fail)
- Switch between organizations (separate session state)
- All timeline features work
- URL parameter handling with orgId
