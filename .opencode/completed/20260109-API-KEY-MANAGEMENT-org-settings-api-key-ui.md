## [2026-01-09] - Organization Settings & API Key Management UI

### Plan (approved)
Implement organization settings pages with API key management for organization owners/admins. Settings accessible via sidebar link for OWNER/ADMIN users. General tab shows org info (read-only). API Keys tab uses DataTable with server-side pagination/search, create/revoke functionality with permission checks.

### Actual Changes

**Frontend Routes Created:**
- `/organizations/{orgId}/settings` - Redirects to /general
- `/organizations/{orgId}/settings/general` - Organization details (read-only)
- `/organizations/{orgId}/settings/api-keys` - API key management with DataTable

**Components Created:**
- `OrgSettingsNav.svelte` - Tab navigation (General | API Keys)

**API Controllers Created:**
- `OrganizationApiKeyController.ts` - Search, create, revoke functions

**Files Modified:**
- `AppSidebarNav.svelte` - Added Settings link in WORKSPACE section
- `routes.ts` - Added ORGANIZATION_SETTINGS_* routes
- `models.ts` - Added PageResourceApiKeyResponse type
- `settings/index.ts` - Exported OrgSettingsNav

**Features:**
- Settings link in sidebar (visible for OWNER/ADMIN/global ADMIN)
- General tab: org name, slug, role badge, joined date
- API Keys tab with DataTable:
  - Server-side pagination
  - Search by name (FUZZY_TEXT)
  - Columns: Name, Created By, Created At, Expires At, Last Used, Requests, Actions
  - Create API Key sheet (reused from user keys)
  - Revoke with confirmation dialog
- Permission-based UI: all members can view, only OWNER/ADMIN can manage

### Agents Used
| Agent | Task |
|-------|------|
| sveltekit-frontend-agent | Implemented all frontend routes, components, and API integration |

### Files Created
```
client/src/routes/(authenticated)/organizations/[orgId]/settings/
├── +layout.server.ts
├── +layout.svelte
├── +page.svelte
├── general/+page.svelte
└── api-keys/+page.svelte

client/src/lib/components/settings/OrgSettingsNav.svelte
client/src/lib/api/organization/OrganizationApiKeyController.ts
client/test/components/org-settings.spec.ts
client/test/resources/screenshots/org-settings/*.png
```

### Files Modified
```
client/src/lib/components/layout/AppSidebarNav.svelte
client/src/lib/config/routes.ts
client/src/lib/api/models.ts
client/src/lib/components/settings/index.ts
```

### Quality Metrics
- Build: Successful (`bun run build`)
- Type Check: 0 errors, 1 pre-existing warning (`bun run check`)
- Screenshots: 2 captured (General tab, API Keys empty state)
- Accessibility: Keyboard navigation, ARIA labels, semantic HTML
