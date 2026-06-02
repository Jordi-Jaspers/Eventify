---
epic: "NOTIF"
title: "Notification Settings UI"
estimate: L
status: ready
created: 2026-06-02
depends_on: ["NOTIF-01-adapter-infrastructure", "NOTIF-02-adapter-implementations"]
labels: [frontend, backend, notification, settings]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user or org admin\
**I want** a settings interface to link, configure, and test my notification adapters\
**So that** I can manage where my notifications are delivered\

## 2. Business Context & Value

Without a UI, users cannot link adapters or test connections. This story provides the management interface in both user profile and org settings, enabling the full notification pipeline to be configured end-to-end.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: User profile — Notifications tab exists
    * Given an authenticated user navigating to their profile
    * When they click the "Notifications" tab
    * Then they see a list of their personal adapter configs grouped by type
* [ ] **Scenario 2**: Add Mattermost/Slack adapter config
    * Given a user on the Notifications settings tab
    * When they click "Add Adapter" and select Mattermost
    * Then a form appears with: label (text input), webhook URL (text input), and Save + Test buttons
* [ ] **Scenario 3**: Add Email adapter config
    * Given a user on the Notifications settings tab
    * When they click "Add Adapter" and select Email
    * Then a form shows their account email (read-only) with an Enable toggle and Save button (no URL input)
* [ ] **Scenario 4**: Test connection from UI
    * Given a saved adapter config
    * When user clicks "Test" button
    * Then a test notification is sent and success/failure toast is shown
* [ ] **Scenario 5**: Edit existing config
    * Given an existing Mattermost config with label "DevOps Channel"
    * When user edits the label to "Prod Alerts" and saves
    * Then the config is updated and the new label is displayed
* [ ] **Scenario 6**: Delete config with confirmation
    * Given an existing adapter config
    * When user clicks delete and confirms in the modal
    * Then the config is removed and no longer appears in the list
* [ ] **Scenario 7**: Org settings — Notifications tab
    * Given an org owner/admin navigating to org settings
    * When they click the "Notifications" tab
    * Then they see org-level adapter configs, manageable same as personal
* [ ] **Scenario 8**: Non-admin org member cannot see org notifications tab
    * Given an org member with MEMBER role
    * When they view org settings
    * Then the "Notifications" tab is not visible
* [ ] **Edge Case**: Webhook URL masking
    * Given a saved config with webhook URL `https://hooks.slack.com/services/T00/B00/xxxx`
    * When the config list is displayed
    * Then the URL shows as `••••••••/xxxx` (last 8 chars visible)

## 4. Technical Requirements

* **API Changes**: Uses endpoints from NOTIF-01 (`/v1/adapter-configs` CRUD + `/test` from NOTIF-02)
* **Database**: N/A — uses existing `adapter_config` table
* **Security**:
    - Frontend never stores full webhook URL in state after initial save
    - Org tab guarded by role check (OWNER/ADMIN)
* **Performance**: N/A — simple CRUD UI

## 5. Design & UI/UX

- **User Profile**: New "Notifications" tab alongside existing tabs
- **Org Settings**: New "Notifications" tab (visible only to OWNER/ADMIN)
- **Layout**: Card per adapter config showing: icon + type badge, label, masked URL, enabled toggle, Edit/Test/Delete actions
- **Add flow**: "Add Adapter" button → dropdown (Mattermost, Slack, Email) → inline form or modal
- **Empty state**: Illustration + "No adapters linked yet. Add one to start receiving notifications outside Eventify."
- **CTA**: If user has zero configs, show prominent setup prompt

## 6. Implementation Notes

- Frontend routes: `/profile/notifications`, `/org/[orgId]/settings/notifications`
- Reuse existing profile/org settings layout and tab patterns
- New components: `AdapterConfigList`, `AdapterConfigCard`, `AdapterConfigForm`, `AdapterTestButton`
- API client: add `adapterConfig` resource to openapi-fetch typed client
- Adapter type enum shared between frontend and backend (or derived from API response)
- Toast notifications for test success/failure using existing toast system
- Confirmation modal for delete using existing `ConfirmDialog` component

## 7. Test Impact Analysis

### Test modification policy:

- [x] No existing tests should be modified (greenfield UI)

### Potential files impacted by implementation

| File | Change |
|------|--------|
| New: `src/routes/(app)/profile/notifications/+page.svelte` | User notification settings page |
| New: `src/routes/(app)/org/[orgId]/settings/notifications/+page.svelte` | Org notification settings page |
| New: `src/lib/components/notification-settings/` | Shared components |
| Profile layout/nav | Add "Notifications" tab |
| Org settings layout/nav | Add "Notifications" tab (role-gated) |
| OpenAPI types | Regenerate for adapter-config endpoints |
