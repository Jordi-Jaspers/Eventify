# User Settings & API Key Management UI

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-06

## 1. User Story
**As a** registered user
**I want** a dedicated settings area with API key management
**So that** I can create, view, and revoke my personal API keys through a user-friendly interface

## 2. Business Context & Value
Users need an intuitive interface to manage their API keys for integrating external systems with Eventify. This story introduces a new "Settings" navigation structure that separates profile information from developer-focused features, providing a scalable foundation for future settings pages.

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: User navigates to settings via top navigation
    *   Given I am on my profile page
    *   When I look at the top of the page
    *   Then I see a tab/pill navigation with "Profile" and "Developer" options
    *   And "Profile" shows the existing profile content
    *   And "Developer" navigates to `/settings/developer`

*   [ ] **Scenario 2**: User views empty API key state
    *   Given I am on the Developer settings page with no API keys
    *   When the page loads
    *   Then I see an empty state with illustration/icon
    *   And a message explaining what API keys are for
    *   And a prominent "Create API Key" button

*   [ ] **Scenario 3**: User creates an API key
    *   Given I am on the Developer settings page
    *   When I click "Create API Key"
    *   Then a modal/sheet opens with a form
    *   And I can enter a name (required) and optional expiration date
    *   When I submit the form
    *   Then the key is created and displayed in a success modal
    *   And I see the full key with a "Copy" button and warning it won't be shown again
    *   When I close the modal
    *   Then the new key appears in my list

*   [ ] **Scenario 4**: User views their API keys list
    *   Given I have 3 API keys
    *   When I view the Developer settings page
    *   Then I see a list/table of my keys showing:
        *   Name
        *   Key preview (prefix...suffix, e.g., `evt_a1b2...o5p6`)
        *   Created date
        *   Expiration date (or "Never")
        *   Last used (or "Never used")
        *   Total requests
    *   And each key has a "Revoke" action

*   [ ] **Scenario 5**: User sees key limit indicator
    *   Given I am on the Developer settings page
    *   When I view my API keys
    *   Then I see an indicator showing "X of 5 keys used"
    *   And when I have 5 keys, the "Create" button is disabled with tooltip

*   [ ] **Scenario 6**: User revokes an API key
    *   Given I have an API key named "Old Integration"
    *   When I click the "Revoke" action
    *   Then a confirmation dialog appears warning this is irreversible
    *   When I confirm
    *   Then the key is removed from the list
    *   And a success toast is shown

*   [ ] **Scenario 7**: User sees monthly quota indicator
    *   Given I am on the Developer settings page
    *   When I view the page header/summary area
    *   Then I see my monthly event quota usage
    *   And a progress bar showing usage (e.g., "342 / 1,000 events this month")
    *   And the bar color shifts toward red as I approach the limit

*   [ ] **Scenario 8**: Copy key to clipboard
    *   Given the key creation success modal is open
    *   When I click the "Copy" button
    *   Then the full key is copied to clipboard
    *   And the button shows a checkmark/success state briefly

## 4. Technical Requirements

### New Routes

```
/settings                    -> Redirect to /profile (or /settings/profile)
/profile                     -> Keep existing, add top navigation
/settings/developer          -> New API key management page
```

Alternative structure (recommended for cleaner URLs):
```
/profile                     -> Profile with top nav showing [Profile | Developer]
/developer                   -> API Keys management page
```

### Page Structure

#### Top Navigation Component
Create a reusable `SettingsNav.svelte` component:
```svelte
<script>
  import { page } from '$app/stores';
</script>

<nav class="flex gap-1 p-1 bg-muted/50 rounded-lg w-fit mb-6">
  <a 
    href="/profile" 
    class="px-4 py-2 rounded-md text-sm font-medium transition-colors
           {$page.url.pathname === '/profile' ? 'bg-background shadow-sm' : 'hover:bg-background/50'}"
  >
    Profile
  </a>
  <a 
    href="/developer" 
    class="px-4 py-2 rounded-md text-sm font-medium transition-colors
           {$page.url.pathname === '/developer' ? 'bg-background shadow-sm' : 'hover:bg-background/50'}"
  >
    Developer
  </a>
</nav>
```

### API Integration

Use generated API client from OpenAPI:
```typescript
// client/src/lib/api/apikey/UserApiKeyController.ts (generated)
export async function createUserApiKey(request: CreateApiKeyRequest): Promise<ApiKeyCreationResponse>
export async function listUserApiKeys(): Promise<ApiKeyListResponse>
export async function revokeUserApiKey(keyId: number): Promise<void>
```

### Components to Create

#### 1. `ApiKeyList.svelte`
Shared component for displaying API keys (reused in org context):
```typescript
interface Props {
  keys: ApiKeyResponse[];
  onRevoke: (keyId: number) => void;
  loading?: boolean;
  scope: 'USER' | 'ORGANIZATION';
}
```

#### 2. `CreateApiKeyModal.svelte`
Modal/sheet for creating new keys:
```typescript
interface Props {
  open: boolean;
  onClose: () => void;
  onCreate: (request: CreateApiKeyRequest) => Promise<ApiKeyCreationResponse>;
  scope: 'USER' | 'ORGANIZATION';
}
```

#### 3. `ApiKeyCreatedModal.svelte`
Success modal showing the full key (only time visible):
```typescript
interface Props {
  apiKey: ApiKeyCreationResponse;
  open: boolean;
  onClose: () => void;
}
```

#### 4. `QuotaProgressBar.svelte`
Visual indicator for monthly quota:
```typescript
interface Props {
  used: number;
  limit: number;
  label?: string;
}
```
- Green: 0-60%
- Yellow: 60-80%
- Orange: 80-95%
- Red: 95-100%

### State Management

Create `ApiKeyService.svelte.ts`:
```typescript
export function createApiKeyService() {
  let keys = $state<ApiKeyResponse[]>([]);
  let loading = $state(false);
  let quotaUsed = $state(0);
  let quotaLimit = $state(1000);
  
  async function load() { ... }
  async function create(request: CreateApiKeyRequest) { ... }
  async function revoke(keyId: number) { ... }
  
  return {
    get keys() { return keys; },
    get loading() { return loading; },
    get quotaUsed() { return quotaUsed; },
    get quotaLimit() { return quotaLimit; },
    get keyCount() { return keys.length; },
    get keyLimit() { return 5; },
    load,
    create,
    revoke
  };
}
```

## 5. Design & UI/UX

### Layout Structure
```
┌─────────────────────────────────────────────────────────┐
│  [Profile]  [Developer]                    <- Top nav   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  API Keys                              [+ Create Key]   │
│  Manage your personal API keys for event ingestion     │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Monthly Usage                                   │   │
│  │  ████████████░░░░░░░░  342 / 1,000 events       │   │
│  │  Resets on Feb 1, 2026                          │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  Production Server                              │   │
│  │  evt_a1b2...o5p6  •  Created Jan 1             │   │
│  │  Last used: 2 hours ago  •  1,542 requests     │   │
│  │                                    [Revoke]     │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │  CI Pipeline                                    │   │
│  │  evt_x9y8...z1a2  •  Expires Dec 31, 2026      │   │
│  │  Last used: Never  •  0 requests               │   │
│  │                                    [Revoke]     │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  2 of 5 keys used                                       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Key Creation Modal
```
┌─────────────────────────────────────────────────────────┐
│  Create API Key                                    [X]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Name *                                                 │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Production Server                                │   │
│  └─────────────────────────────────────────────────┘   │
│  A descriptive name to identify this key               │
│                                                         │
│  Expiration (optional)                                  │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Never                                        ▼  │   │
│  └─────────────────────────────────────────────────┘   │
│  Options: Never, 30 days, 90 days, 1 year, Custom     │
│                                                         │
│                          [Cancel]  [Create Key]         │
└─────────────────────────────────────────────────────────┘
```

### Key Created Success Modal
```
┌─────────────────────────────────────────────────────────┐
│  ✓ API Key Created                                 [X]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ⚠️  Copy your API key now. You won't be able to       │
│     see it again!                                       │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ evt_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6    [Copy] │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  Use this key in the X-Api-Key header:                 │
│  ┌─────────────────────────────────────────────────┐   │
│  │ curl -X POST https://api.eventify.io/v1/events  │   │
│  │   -H "X-Api-Key: evt_a1b2c3d4..."               │   │
│  │   -d '{"channel": "my-channel", ...}'           │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│                                      [Done]             │
└─────────────────────────────────────────────────────────┘
```

### Visual Design Notes
- Follow existing glassmorphism style from profile page
- Use `Card` components with `bg-card/50 backdrop-blur-xl`
- Key preview uses monospace font (`font-mono`)
- Revoke button: ghost/subtle by default, destructive on hover
- Progress bar: gradient from green to red based on usage

## 6. Implementation Notes / Research

### File Locations
```
client/src/routes/(authenticated)/
├── profile/
│   └── +page.svelte                    # Update: add SettingsNav
├── developer/
│   ├── +page.svelte                    # New: API key management
│   └── +page.server.ts                 # Optional: SSR data loading

client/src/lib/components/
├── settings/
│   ├── SettingsNav.svelte              # New: top navigation tabs
│   └── index.ts
├── api-keys/
│   ├── ApiKeyList.svelte               # New: shared key list
│   ├── ApiKeyCard.svelte               # New: individual key display
│   ├── CreateApiKeyModal.svelte        # New: creation form
│   ├── ApiKeyCreatedModal.svelte       # New: success with full key
│   ├── QuotaProgressBar.svelte         # New: usage indicator
│   └── index.ts

client/src/lib/api/apikey/
├── service/
│   └── ApiKeyService.svelte.ts         # New: state management
└── UserApiKeyController.ts             # Generated from OpenAPI
```

### Existing Patterns to Reference
- `client/src/routes/(authenticated)/profile/+page.svelte` - Page structure, Card styling
- `client/src/lib/components/members/AddMemberSheet.svelte` - Sheet/modal patterns
- `client/src/lib/api/user/service/ProfileService.svelte.ts` - Service pattern with $state

### Accessibility Requirements
- All interactive elements keyboard accessible
- Proper ARIA labels on buttons and modals
- Focus trap in modals
- Copy button announces success to screen readers

### Error Handling
- Show toast on API errors
- Disable create button while request pending
- Show inline validation for name field
