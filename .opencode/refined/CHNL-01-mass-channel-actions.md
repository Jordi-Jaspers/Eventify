---
epic: "CHNL"
title: "Mass Channel Actions"
estimate: M
status: ready
created: 2026-05-15
depends_on: [ ]
labels: [ backend, frontend ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** channel owner (personal) or org admin\
**I want** to pause, resume, or delete multiple channels at once\
**So that** I can efficiently manage large numbers of channels without repetitive one-by-one actions\

## 2. Business Context & Value
Users with many channels (especially organizations with dozens of monitoring channels) currently must act on each channel individually. Bulk actions reduce operational friction for common maintenance tasks like pausing channels during deployments or cleaning up unused channels.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Bulk pause channels
    * Given I have 5 ACTIVE channels selected in the channel list
    * When I click "Pause" in the bulk action toolbar
    * Then all 5 channels are paused in a single transaction and the list refreshes showing PAUSED status
* [ ] **Scenario 2**: Bulk resume channels
    * Given I have 3 PAUSED channels selected
    * When I click "Resume" in the bulk action toolbar
    * Then all 3 channels are resumed in a single transaction and the list refreshes showing ACTIVE status
* [ ] **Scenario 3**: Bulk delete channels with confirmation
    * Given I have 4 channels selected
    * When I click "Delete" in the bulk action toolbar
    * Then a confirmation dialog shows "Delete 4 channels?" with Cancel/Confirm
    * And on confirm, all 4 channels are set to PENDING_DELETION in a single transaction
* [ ] **Scenario 4**: Idempotent operations (mixed states)
    * Given I select 3 ACTIVE + 2 already-PAUSED channels and click "Pause"
    * When the batch request is processed
    * Then the operation succeeds (already-paused channels are no-ops) and all 5 show as PAUSED
* [ ] **Scenario 5**: Transaction rollback on real errors
    * Given I select channels including one that doesn't exist or I lack permission for
    * When the batch request is processed
    * Then the entire operation fails, no channels are modified, and an error toast explains the failure
* [ ] **Scenario 6**: Multi-select UX
    * Given I am on the channel list page
    * When I click a checkbox on a channel row
    * Then a bulk action toolbar appears at the top with Pause/Resume/Delete buttons and a count indicator
    * And I can select/deselect all visible channels with a header checkbox
* [ ] **Scenario 7**: Org-scoped bulk actions
    * Given I am an OWNER or ADMIN of an organization
    * When I select org channels and perform a bulk action
    * Then the operation uses the org-scoped endpoint and respects org role permissions
* [ ] **Scenario 8**: Single-channel actions use same endpoints
    * Given I click Pause/Resume/Delete on a single channel via the row action dropdown
    * When the action is performed
    * Then it calls the same batch endpoint with a single-element array

## 4. Technical Requirements
* **API Changes** (replaces existing `/{id}/pause`, `/{id}/resume`, `DELETE /{id}`):
    * `POST /v1/user/channel/pause` — body: `{ channelIds: [Long] }` → 204 No Content
    * `POST /v1/user/channel/resume` — body: `{ channelIds: [Long] }` → 204 No Content
    * `DELETE /v1/user/channel` — body: `{ channelIds: [Long] }` → 204 No Content
    * `POST /v1/organization/{orgId}/channels/pause` — same body → 204
    * `POST /v1/organization/{orgId}/channels/resume` — same body → 204
    * `DELETE /v1/organization/{orgId}/channels` — same body → 204
    * `GET /v1/user/channel/{id}` and `PUT /v1/user/channel/{id}` remain unchanged
    * All batch endpoints are `@Transactional` — all-or-nothing
    * Validation: all channel IDs must exist and belong to the user/org, else 404/403 rolls back entire batch
    * Request DTO: `ChannelBatchRequest { @NotEmpty List<Long> channelIds }`
* **Database**: N/A — no schema changes
* **Security**: User endpoints verify ownership of ALL channels in the list. Org endpoints verify OWNER/ADMIN role. Fail-fast on first unauthorized channel.
* **Performance**: No selection limit. Use single `UPDATE channel SET status = ? WHERE id IN (...)` query rather than N individual saves.

## 5. Design & UI/UX
- **Checkbox column** added as first column in channel DataTable (both personal and org pages)
- **Header checkbox** for select-all (visible page only)
- **Bulk action toolbar** appears above table when ≥1 channel selected: shows count ("3 selected") + action buttons (Pause, Resume, Delete)
- **Delete confirmation dialog**: "Delete N channels?" with Cancel/Confirm buttons
- **Pause/Resume**: no confirmation needed (non-destructive, idempotent)
- **After success**: deselect all, refresh table, show success toast ("N channels paused/resumed/deleted")
- **On error**: show error toast with backend message, no channels modified
- **Single-channel actions** (row dropdown): unchanged UX, but calls same batch endpoint with `[id]`

## 6. Implementation Notes
- **Backend**: Use `channelRepository.findAllById(ids)` → verify count matches input (else 404) → verify all belong to user/org (else 403) → single bulk UPDATE
- **Watchlist cleanup**: On batch delete, call `WatchlistRepository.removeChannelFromAllConfigurations(channelId)` per deleted channel (existing method)
- **Files to modify**:
    - `server/.../channel/controller/UserChannelController.java` — replace `/{id}/pause`, `/{id}/resume`, `DELETE /{id}` with batch endpoints
    - `server/.../channel/controller/OrganizationChannelController.java` — same
    - `server/.../channel/service/ChannelCreationService.java` — add batch methods, remove single-status-change methods
    - `client/src/routes/(authenticated)/channels/+page.svelte` — add multi-select + toolbar
    - `client/src/routes/(authenticated)/organizations/[orgId]/channels/+page.svelte` — same
    - `client/src/lib/components/channels/ChannelActions.svelte` — update to call batch endpoints with `[id]`
    - `client/src/lib/api/channel/UserChannelController.ts` — replace single endpoints with batch
    - `client/src/lib/api/channel/OrganizationChannelController.ts` — same
- **OpenAPI regeneration**: API types will change — regenerate `api.d.ts`

## 7. Test Impact Analysis
### Existing tests affected by this change:
| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `UserChannelControllerTest.java` | `shouldPauseChannel` | Single pause via `/{id}/pause` | YES | Rewrite to use new batch endpoint with `[id]` |
| `UserChannelControllerTest.java` | `shouldResumeChannel` | Single resume via `/{id}/resume` | YES | Rewrite to use new batch endpoint with `[id]` |
| `UserChannelControllerTest.java` | `shouldDeleteChannel` | Single delete via `DELETE /{id}` | YES | Rewrite to use new batch endpoint with `[id]` |
| `OrganizationChannelControllerTest.java` | Same pattern | Same | YES | Same |

### Test modification policy:
- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified: `UserChannelControllerTest.java`, `OrganizationChannelControllerTest.java`

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../channel/controller/UserChannelController.java` | Replace single action endpoints with batch |
| `server/.../channel/controller/OrganizationChannelController.java` | Same |
| `server/.../channel/service/ChannelCreationService.java` | Add batch pause/resume/delete methods |
| `client/src/routes/(authenticated)/channels/+page.svelte` | Add multi-select UI + toolbar |
| `client/src/routes/(authenticated)/organizations/[orgId]/channels/+page.svelte` | Same |
| `client/src/lib/components/channels/ChannelActions.svelte` | Call batch endpoints with single-element array |
| `client/src/lib/api/channel/UserChannelController.ts` | New batch API functions |
| `client/src/lib/api/channel/OrganizationChannelController.ts` | Same |
