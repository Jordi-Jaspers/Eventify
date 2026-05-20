# Broadcast Recipient Detail View

**Completed:** 2026-05-13
**Epic:** NOTIF
**Source:** .opencode/refined/NOTIF-03-broadcast-recipient-detail.md

## Summary

Admin broadcast history expanded detail now shows a searchable, paginated recipient list. New backend endpoint uses jframe search/pagination pattern.

## Plan Approved by the user:

### Requirements Summary

- GET broadcast recipients with fuzzy search (email, first name, last name)
- Paginated response using jframe `PageResource<RecipientResponse>`
- Security: `hasAuthority('MANAGE_USERS')`
- Frontend: lazy-loaded recipient list in expanded broadcast row, 300ms debounce search, scrollable + paginated

### Technical Approach

- Backend: POST `/v1/admin/notifications/broadcasts/{id}/recipients/search` with `SortablePageInput`
- `NotificationRecipientMetaData` with MULTI_COLUMN_FUZZY on user.email, user.firstName, user.lastName
- `RecipientMapper` extends PageMapper
- `BroadcastRecipientService` with broadcast existence check
- Frontend: inline recipient list in expanded broadcast detail with search + pagination

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Backend tests (12 tests) |
| 2 | spring-backend-agent | Implementation + jframe refactor |
| 3 | backend-optimizer-agent | Review (no changes needed) |
| 4 | svelte-frontend-agent | Recipient list UI |
| 5 | frontend-optimizer-agent | Extract RecipientList component |

## Implementation

### Backend

- POST `/v1/admin/notifications/broadcasts/{id}/recipients/search`
- `NotificationRecipientMetaData` — MULTI_COLUMN_FUZZY search
- `RecipientMapper` — MapStruct PageMapper
- `BroadcastRecipientService` — existence check + paginated query
- `Notification` entity — added `@ManyToOne broadcast` FK mapping
- `NotificationPayload` — added broadcast field, set during dispatch
- `InAppNotificationAdapter` — sets broadcast on notification entity
- `BROADCAST_NOT_FOUND` error code

### Frontend

- `RecipientList.svelte` — extracted component with search, pagination, loading state
- Lazy-load on first expand, cached per broadcast ID
- Immutable state updates for Svelte 5 reactivity
- Generated OpenAPI types replace manual interfaces

### Deviations from Plan

- Endpoint changed from GET with query params to POST with SortablePageInput (jframe pattern)
- Bug fix added: broadcast_id wasn't being set on notifications during dispatch
- Type cleanup: replaced manual TS interfaces with generated OpenAPI types across notification + API key modules

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | 12 backend tests | Complete |
| spring-backend-agent | Implementation + jframe refactor + broadcast_id bug fix | Complete |
| backend-optimizer-agent | Review | No changes needed |
| svelte-frontend-agent | Recipient list UI | Complete |
| frontend-optimizer-agent | Extract component + type cleanup | Complete |

## Files Modified

- `server/.../notification/controller/AdminNotificationController.java` — new endpoint
- `server/.../notification/service/BroadcastRecipientService.java` — new service
- `server/.../notification/model/NotificationRecipientMetaData.java` — new MetaData
- `server/.../notification/model/mapper/RecipientMapper.java` — new mapper
- `server/.../notification/model/response/RecipientResponse.java` — new DTO
- `server/.../notification/model/Notification.java` — added broadcast FK
- `server/.../notification/model/NotificationPayload.java` — added broadcast field
- `server/.../notification/service/NotificationBroadcastService.java` — passes broadcast to payload
- `server/.../notification/adapter/InAppNotificationAdapter.java` — sets broadcast on notification
- `server/.../notification/repository/NotificationRepository.java` — removed native query
- `server/.../api/Paths.java` — added recipients search path
- `server/.../common/exception/ApiErrorCode.java` — added BROADCAST_NOT_FOUND
- `server/src/test/.../BroadcastRecipientsControllerTest.java` — 12 tests
- `server/src/test/.../support/IntegrationTest.java` — pagination helpers
- `server/src/test/.../support/util/TestDataCleanupService.java` — FK-safe cleanup
- `client/src/lib/components/notification/RecipientList.svelte` — new component
- `client/src/routes/(authenticated)/admin/tools/notifications/history/+page.svelte` — expanded detail
- `client/src/lib/api/admin/AdminNotificationController.ts` — new API function + type cleanup
- `client/src/lib/api/models.ts` — generated types
- `client/src/lib/api/admin/service/BroadcastSendService.svelte.ts` — type rename
- `client/src/lib/api/organization/service/ApiKeyManagementService.svelte.ts` — type cleanup
- `client/src/lib/api/apikey/service/UserApiKeyManagementService.svelte.ts` — type cleanup
- `client/src/lib/components/data-table/filters/DataTableFiltersSearchBar.svelte` — a11y fix
- `.opencode/skills/jframe-search-pagination/SKILL.md` — new skill reference

## Tests

- 12 tests written, 12 passing
