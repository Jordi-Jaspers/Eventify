# Mass Channel Actions

**Completed:** 2026-05-15
**Epic:** CHNL
**Source:** .opencode/refined/CHNL-01-mass-channel-actions.md

## Summary

Replaced single-item pause/resume/delete channel endpoints with batch endpoints. Added multi-select UI with bulk action toolbar to both personal and organization channel pages.

## Plan Approved by the user:

### Requirements Summary

- Batch pause/resume/delete for user and org channels
- All-or-nothing transactional semantics
- Security via @PreAuthorize (user channels) and service-layer binding check (org channels)
- Multi-select checkboxes + select-all in DataTable
- Bulk action toolbar inside table card header
- Single-channel actions delegate to batch with [id]

### Technical Approach

- Backend: 6 batch endpoints (POST pause/resume, DELETE) returning 204
- Frontend: Checkbox column, ChannelBulkActionBar, ChannelSelectionService
- DTO: ChannelBatchRequest with custom validation

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Rewrite tests for batch endpoints |
| 2.2 | spring-backend-agent | Implement batch endpoints |
| 2.4 | backend-optimizer-agent | Extract batchUpdateStatus helpers |
| 3.2 | svelte-frontend-agent | Multi-select UI + batch API |
| 3.4 | frontend-optimizer-agent | Extract ChannelSelectionService |

## Implementation

### Backend

- `POST /v1/user/channel/pause` → 204
- `POST /v1/user/channel/resume` → 204
- `DELETE /v1/user/channel` → 204
- `POST /v1/organization/{orgId}/channels/pause` → 204
- `POST /v1/organization/{orgId}/channels/resume` → 204
- `DELETE /v1/organization/{orgId}/channels` → 204
- ChannelSecurityService: `canAccessChannelsAsUser` (batch), `canAccessChannelsInOrganization` (batch)
- OrganizationChannelService: `findActiveByIdInAndOrganizationId` (status-filtered)

### Frontend

- ChannelBulkActionBar component (pause/resume/delete with confirmation)
- ChannelSelectionService.svelte.ts (reusable reactive selection state)
- DataTable/DataTableHeader: selectable props for select-all checkbox
- Checkbox: indeterminate support

### Deviations from Plan

- Added @PreAuthorize with ChannelSecurityService for user batch endpoints (not originally planned, added during review)
- Org batch security stays in service layer (404 vs 403 requirement)
- Added 8 extra edge case tests for mixed valid/invalid ID scenarios

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Batch test suites | Complete |
| spring-backend-agent | Batch endpoints + security | Complete |
| backend-optimizer-agent | Extract helpers, normalize style | Complete |
| svelte-frontend-agent | Multi-select UI | Complete |
| frontend-optimizer-agent | Extract ChannelSelectionService | Complete |

## Files Modified

### New files
- `server/.../model/request/ChannelBatchRequest.java`
- `server/.../model/validator/ChannelBatchValidator.java`
- `server/.../controller/BatchDeleteOrgChannelControllerTest.java`
- `server/.../controller/BatchPauseResumeOrgChannelControllerTest.java`
- `client/src/lib/api/channel/service/ChannelSelectionService.svelte.ts`
- `client/src/lib/components/channels/ChannelBulkActionBar.svelte`

### Modified files
- `server/.../Paths.java` — batch path constants
- `server/.../UserChannelController.java` — batch endpoints
- `server/.../OrganizationChannelController.java` — batch endpoints
- `server/.../ChannelService.java` — batch methods
- `server/.../OrganizationChannelService.java` — batch methods
- `server/.../ChannelSecurityService.java` — batch access checks
- `server/.../ChannelValidator.java` — batch validation
- `server/.../ChannelRepository.java` — findActiveByIdInAndOrganizationId
- `server/.../UserChannelControllerTest.java` — rewritten for batch
- `server/.../ChannelServiceTest.java` — removed old unit tests
- `server/.../GetOrgChannelControllerTest.java` — use batch delete in setup
- `server/.../SearchOrgChannelControllerTest.java` — use batch delete in setup
- `client/src/lib/api/channel/UserChannelController.ts` — batch methods
- `client/src/lib/api/organization/OrganizationChannelController.ts` — batch methods
- `client/src/lib/api/channel/service/UserChannelService.ts` — delegates to batch
- `client/src/lib/api/channel/service/ChannelService.ts` — delegates to batch
- `client/src/lib/components/channels/ChannelRow.svelte` — checkbox column
- `client/src/lib/components/data-table/DataTable.svelte` — selectable props
- `client/src/lib/components/data-table/DataTableHeader.svelte` — select-all checkbox
- `client/src/lib/components/ui/checkbox/checkbox.svelte` — indeterminate support
- `client/src/lib/config/channel-table-columns.ts` — checkbox column
- `client/src/routes/(authenticated)/channels/+page.svelte` — multi-select
- `client/src/routes/(authenticated)/organizations/[orgId]/channels/+page.svelte` — multi-select
- `.opencode/skills/eventify-spring-standards/SKILL.md` — documented security pattern

### Deleted files
- `server/.../PauseResumeOrgChannelControllerTest.java` (replaced by batch)
- `server/.../DeleteOrgChannelControllerTest.java` (replaced by batch)

## Tests

- 43 batch integration tests written, all passing
- 7 old unit tests removed
- Full build + spotlessCheck + pmdMain passing
