# Organization Settings & API Key Management UI

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-06

## 1. User Story
**As an** organization owner or admin
**I want** a settings section for my organization with API key management
**So that** I can manage API keys for my team in a centralized location

## 2. Business Context & Value
Organizations need a dedicated settings area to manage API keys, similar to how individual users have their developer settings. This creates a consistent UX pattern across the application and provides organization admins with the tools to manage programmatic access for their team. The settings structure also establishes a foundation for future organization settings (billing, integrations, etc.).

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: Sidebar shows Settings link for org admins
    *   Given I am an OWNER or ADMIN of organization "Acme Corp"
    *   When I am viewing the organization context
    *   Then I see a "Settings" link in the sidebar (below Members or similar)
    *   And clicking it navigates to `/organizations/{orgId}/settings`

*   [ ] **Scenario 2**: Regular members don't see Settings link
    *   Given I am a MEMBER (not OWNER/ADMIN) of organization "Acme Corp"
    *   When I view the organization sidebar
    *   Then I do NOT see a "Settings" link

*   [ ] **Scenario 3**: Settings page has navigation tabs
    *   Given I am on the organization settings page
    *   When I view the page
    *   Then I see a top navigation with tabs: "General" and "API Keys"
    *   And the style matches the user settings navigation pattern

*   [ ] **Scenario 4**: General settings shows org details
    *   Given I am on the organization settings page
    *   When I view the "General" tab
    *   Then I see organization details: name, slug, status, created date, member count
    *   And this is read-only for now (future: editable)

*   [ ] **Scenario 5**: API Keys tab shows key management
    *   Given organization "Acme Corp" has 2 API keys
    *   When I view the "API Keys" tab
    *   Then I see the list of keys with:
        *   Name, key preview (prefix...suffix)
        *   Created by (user name)
        *   Created date, expiration, last used, total requests
    *   And I see a "Create API Key" button

*   [ ] **Scenario 6**: Creating org API key
    *   Given I am an OWNER or ADMIN viewing the API Keys tab
    *   When I click "Create API Key" and fill the form
    *   Then the key is created and I see the full key once
    *   And the key shows in the list with my name as "Created by"

*   [ ] **Scenario 7**: Member sees read-only view
    *   Given I am a MEMBER of "Acme Corp" (somehow navigated to settings URL)
    *   When I view the API Keys tab
    *   Then I see the list of keys (read access)
    *   But the "Create API Key" button is hidden or disabled
    *   And "Revoke" actions are not available

*   [ ] **Scenario 8**: Revoking org API key
    *   Given I am an OWNER or ADMIN
    *   When I revoke an API key
    *   Then a confirmation dialog appears
    *   When I confirm
    *   Then the key is removed and success toast shown

*   [ ] **Scenario 9**: Global admin access
    *   Given I am a global ADMIN
    *   When I navigate from admin org list to an org's settings
    *   Then I can view and manage API keys like an org owner

## 4. Technical Requirements

### New Routes

```
/organizations/{orgId}/settings              -> Redirects to /general
/organizations/{orgId}/settings/general      -> Organization details (read-only)
/organizations/{orgId}/settings/api-keys     -> API key management
```

### Sidebar Update

Modify organization sidebar to include Settings link:

```svelte
<!-- Only show for OWNER/ADMIN -->
{#if userRole === 'OWNER' || userRole === 'ADMIN' || isGlobalAdmin}
  <a href="/organizations/{orgId}/settings" class="sidebar-link">
    <Settings class="w-4 h-4" />
    Settings
  </a>
{/if}
```

### Page Structure

#### Organization Settings Layout
Create a layout similar to user settings with top navigation:

```svelte
<!-- /organizations/{orgId}/settings/+layout.svelte -->
<script>
  import { page } from '$app/stores';
  import OrgSettingsNav from '$lib/components/settings/OrgSettingsNav.svelte';
</script>

<div class="container mx-auto px-4 py-8">
  <div class="max-w-4xl mx-auto">
    <h1 class="text-2xl font-bold mb-2">Organization Settings</h1>
    <p class="text-muted-foreground mb-6">{orgName}</p>
    
    <OrgSettingsNav orgId={$page.params.orgId} />
    
    <slot />
  </div>
</div>
```

#### Settings Navigation Component
```svelte
<!-- OrgSettingsNav.svelte -->
<script lang="ts">
  import { page } from '$app/stores';
  
  interface Props {
    orgId: string;
  }
  
  let { orgId }: Props = $props();
  
  const basePath = `/organizations/${orgId}/settings`;
</script>

<nav class="flex gap-1 p-1 bg-muted/50 rounded-lg w-fit mb-6">
  <a 
    href="{basePath}/general" 
    class="px-4 py-2 rounded-md text-sm font-medium transition-colors
           {$page.url.pathname.includes('/general') ? 'bg-background shadow-sm' : 'hover:bg-background/50'}"
  >
    General
  </a>
  <a 
    href="{basePath}/api-keys" 
    class="px-4 py-2 rounded-md text-sm font-medium transition-colors
           {$page.url.pathname.includes('/api-keys') ? 'bg-background shadow-sm' : 'hover:bg-background/50'}"
  >
    API Keys
  </a>
</nav>
```

### Reusing Shared Components

The API key management should reuse components from Story 3:

```svelte
<!-- /organizations/{orgId}/settings/api-keys/+page.svelte -->
<script lang="ts">
  import { ApiKeyList, CreateApiKeyModal, ApiKeyCreatedModal } from '$lib/components/api-keys';
  import { createOrgApiKeyService } from '$lib/api/apikey/service/OrgApiKeyService.svelte';
  import { page } from '$app/stores';
  
  const orgId = $page.params.orgId;
  const apiKeyService = createOrgApiKeyService(orgId);
  
  // Check if user can manage keys (OWNER/ADMIN)
  let canManage = $derived(/* check role from org context */);
</script>

<ApiKeyList 
  keys={apiKeyService.keys}
  onRevoke={apiKeyService.revoke}
  loading={apiKeyService.loading}
  scope="ORGANIZATION"
  showCreatedBy={true}
  canManage={canManage}
/>
```

### API Integration

Create organization-specific API key service:

```typescript
// client/src/lib/api/apikey/service/OrgApiKeyService.svelte.ts
export function createOrgApiKeyService(orgId: string) {
  let keys = $state<OrgApiKeyResponse[]>([]);
  let loading = $state(false);
  
  async function load() {
    loading = true;
    try {
      const response = await listOrganizationApiKeys(orgId);
      keys = response.keys;
    } finally {
      loading = false;
    }
  }
  
  async function create(request: CreateApiKeyRequest) {
    const response = await createOrganizationApiKey(orgId, request);
    await load(); // Refresh list
    return response;
  }
  
  async function revoke(keyId: number) {
    await revokeOrganizationApiKey(orgId, keyId);
    await load(); // Refresh list
  }
  
  return {
    get keys() { return keys; },
    get loading() { return loading; },
    load,
    create,
    revoke
  };
}
```

### Component Props Extension

Update `ApiKeyList.svelte` to support organization context:

```typescript
interface Props {
  keys: ApiKeyResponse[] | OrgApiKeyResponse[];
  onRevoke: (keyId: number) => void;
  loading?: boolean;
  scope: 'USER' | 'ORGANIZATION';
  showCreatedBy?: boolean;       // New: show who created the key
  canManage?: boolean;           // New: show/hide management actions
}
```

## 5. Design & UI/UX

### Organization Settings Layout
```
┌─────────────────────────────────────────────────────────┐
│  ← Back to Acme Corp                                    │
│                                                         │
│  Organization Settings                                  │
│  Acme Corp                                              │
│                                                         │
│  [General]  [API Keys]                   <- Top nav     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  API Keys                              [+ Create Key]   │
│  Manage API keys for your organization                 │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Production Backend                              │   │
│  │  org_x9y8...a1b2  •  Created Jan 1              │   │
│  │  Created by: John Doe                           │   │
│  │  Last used: 2 hours ago  •  15,420 requests     │   │
│  │                                    [Revoke]      │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Staging Environment                             │   │
│  │  org_a1b2...x9y8  •  Expires Dec 31, 2026       │   │
│  │  Created by: Jane Smith                          │   │
│  │  Last used: Never  •  0 requests                │   │
│  │                                    [Revoke]      │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  2 API keys                                            │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### General Settings Tab (Read-Only for MVP)
```
┌─────────────────────────────────────────────────────────┐
│  [General]  [API Keys]                                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Organization Details                                   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Name                                            │   │
│  │  Acme Corp                                       │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  Slug                                            │   │
│  │  acme-corp                                       │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  Status                                          │   │
│  │  [ACTIVE]                                        │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  Members                                         │   │
│  │  12                                              │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  Created                                         │   │
│  │  January 1, 2026                                 │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Key Differences from User API Keys UI

| Aspect | User Keys | Organization Keys |
|--------|-----------|-------------------|
| Location | `/developer` | `/organizations/{id}/settings/api-keys` |
| Quota bar | Yes (1000/month) | No (unlimited) |
| Key limit | "2 of 5 keys" | Just "2 API keys" |
| Created by | Not shown (always you) | Shown (team member) |
| Navigation | Profile ↔ Developer | General ↔ API Keys |

## 6. Implementation Notes / Research

### File Locations
```
client/src/routes/(authenticated)/organizations/[orgId]/
├── settings/
│   ├── +layout.svelte              # New: settings layout with nav
│   ├── +layout.server.ts           # New: load org data, check permissions
│   ├── +page.svelte                # New: redirect to /general
│   ├── general/
│   │   └── +page.svelte            # New: org details (read-only)
│   └── api-keys/
│       └── +page.svelte            # New: API key management

client/src/lib/components/
├── settings/
│   ├── OrgSettingsNav.svelte       # New: org settings navigation
│   └── SettingsNav.svelte          # Existing: user settings navigation
├── api-keys/
│   ├── ApiKeyList.svelte           # Update: add showCreatedBy, canManage props
│   ├── ApiKeyCard.svelte           # Update: conditionally show created by
│   └── ...                         # Reuse other components

client/src/lib/api/apikey/
├── service/
│   ├── ApiKeyService.svelte.ts     # Existing: user keys
│   └── OrgApiKeyService.svelte.ts  # New: org keys
└── OrganizationApiKeyController.ts # Generated from OpenAPI
```

### Sidebar Update Location
Update: `client/src/lib/components/layout/AppSidebar.svelte` or equivalent organization navigation component.

### Permission Checking
Load user's organization role in layout server to determine `canManage`:

```typescript
// +layout.server.ts
export const load = async ({ params, locals }) => {
  const orgId = params.orgId;
  const membership = await getMembership(orgId, locals.user.id);
  
  return {
    canManage: membership.role === 'OWNER' || membership.role === 'ADMIN' || locals.user.role === 'ADMIN'
  };
};
```

### Existing Patterns to Reference
- `client/src/routes/(authenticated)/organizations/[orgId]/+page.svelte` - Org detail page
- `client/src/lib/components/members/MemberList.svelte` - Org-scoped list component
- Story 3's components for API key list/modals

### Accessibility
- Ensure role-based visibility is also enforced server-side
- Don't just hide buttons - also prevent API calls for unauthorized users
