# Admin Notification Broadcast Tool

**Completed:** 2026-05-12
**Epic:** NOTIF
**Source:** `.opencode/refined/NOTIF-02-admin-broadcast-tool.md`

## Summary

Admin broadcast tool allowing global admins to compose and send notifications to targeted audiences (ALL_USERS, ORGANIZATION, ALL_ORGANIZATION_OWNERS, USER, GLOBAL_ROLE). Includes composer UI with live preview, type-to-confirm gate for >100 recipients, and broadcast history table.

## Plan Approved by the user:

### Requirements Summary

- Admin composes notification with title, message, category, optional action URL/label
- Target audiences: ALL_USERS, ORGANIZATION, ALL_ORGANIZATION_OWNERS, USER, GLOBAL_ROLE
- Live recipient count preview (300ms debounce)
- Type-to-confirm when recipientCount > 100
- Broadcast history with paginated DataTable
- All endpoints require MANAGE_USERS authority

### Technical Approach

- Backend: 3 endpoints (send, preview, search), BroadcastValidator, AudienceResolver extended, PageMapper pattern
- Frontend: Send page (composer), History page (DataTable), tab navigation, BroadcastSendService

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Backend test suite (44 tests) |
| 2.2 | spring-backend-agent | Implementation to pass tests |
| 2.3 | — | Backend review (3 rounds) |
| 2.4 | backend-optimizer-agent | Refactor for maintainability |
| 3.2 | svelte-frontend-agent | Build UI (send + history pages) |
| 3.3 | — | Frontend review (4 rounds) |
| 3.4 | frontend-optimizer-agent | Extract BroadcastSendService |

## Implementation

### Backend

- `POST /v1/admin/notifications/broadcasts` → 201 BroadcastResponse
- `POST /v1/admin/notifications/broadcasts/search` → PageResource<BroadcastResponse>
- `POST /v1/admin/notifications/broadcasts/preview` → PreviewResponse
- AudienceResolver extended for 5 audience types + count()
- NotificationBroadcast entity completed (sentBy, audienceTargetId, audienceRole)
- BroadcastMapper (PageMapper pattern), BroadcastValidator, NotificationBroadcastMetaData

### Frontend

- `/admin/tools/notifications/send` — broadcast composer with org search, user search, role select
- `/admin/tools/notifications/history` — DataTable with expand detail
- Reusable `TabNav` component for page-level navigation
- `BroadcastSendService.svelte.ts` — extracted form state + logic

### Deviations from Plan

- Frontend tests skipped per user request
- listBroadcasts changed from GET to POST (JFrame search pattern)
- TabNav only used for page-level nav; content tabs remain inline buttons
- Created NOTIF-03 refined story for broadcast recipient detail view

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Backend tests (44) | Complete |
| spring-backend-agent | Implementation + 3 fix rounds | Complete |
| backend-optimizer-agent | Refactor service/validator | Complete |
| svelte-frontend-agent | UI implementation + 4 fix rounds | Complete |
| frontend-optimizer-agent | Extract service, consolidate helpers | Complete |
| deep-research-agent | Backend/frontend/test pattern research | Complete |

## Files Modified

### Backend (new)
- `api/notification/controller/AdminNotificationController.java`
- `api/notification/service/NotificationBroadcastService.java`
- `api/notification/repository/NotificationBroadcastRepository.java`
- `api/notification/model/validator/BroadcastValidator.java`
- `api/notification/model/request/AudienceRequest.java`
- `api/notification/model/request/CreateBroadcastRequest.java`
- `api/notification/model/response/BroadcastResponse.java`
- `api/notification/model/response/PreviewResponse.java`
- `api/notification/model/mapper/BroadcastMapper.java`
- `api/notification/model/NotificationAudienceType.java`
- `api/notification/model/NotificationBroadcastMetaData.java`

### Backend (modified)
- `api/Paths.java` — added broadcast path constants
- `api/notification/model/NotificationAudience.java` — Lombok, Type enum extracted, factories
- `api/notification/model/NotificationBroadcast.java` — added sentBy, audienceTargetId, audienceRole, PageableItem
- `api/notification/service/AudienceResolver.java` — 5 audience types + count()
- `api/user/repository/UserRepository.java` — findAllByRole()
- `api/organization/repository/OrganizationMembershipRepository.java` — findAllOwnersDistinct(), counts
- `support/util/TestDataCleanupService.java` — broadcast cleanup

### Frontend (new)
- `routes/(authenticated)/admin/tools/notifications/send/+page.svelte`
- `routes/(authenticated)/admin/tools/notifications/history/+page.svelte`
- `routes/(authenticated)/admin/tools/notifications/+layout.svelte`
- `routes/(authenticated)/admin/tools/notifications/+page.svelte`
- `lib/api/admin/AdminNotificationController.ts`
- `lib/api/admin/service/BroadcastSendService.svelte.ts`
- `lib/components/ui/tab-nav/TabNav.svelte`
- `lib/components/ui/tab-nav/index.ts`

### Frontend (modified)
- `routes/(authenticated)/admin/tools/+layout.svelte` — TabNav
- `routes/(authenticated)/admin/tools/+page.svelte` — redirect
- `lib/config/routes.ts` — 3 route constants
- `lib/components/settings/OrgSettingsNav.svelte` — delegates to TabNav
- `lib/utils/date.ts` — formatDateTime 24h format

### Tests
- `AdminNotificationControllerTest.java` (22 integration tests)
- `NotificationBroadcastServiceTest.java` (11 unit tests)
- `AudienceResolverTest.java` (16 unit tests)

## Tests

- 49 tests written, all passing
