# Release Notification Startup Job

**Completed:** 2026-05-13
**Epic:** NOTIF
**Source:** `.opencode/refined/NOTIF-05-release-notification-startup-job.md`

## Summary

On app startup, dispatches a release notification to all users if the current version has a changelog entry and no broadcast was previously sent for that version. Idempotent, skips SNAPSHOTs.

## Plan Approved by the user:

### Requirements Summary

- Dispatch release notification on ApplicationReadyEvent
- Skip SNAPSHOT versions
- Skip if no changelog entry (WARN log)
- Skip if broadcast already exists (idempotent)
- Create marker broadcast with recipient_count=0 for empty DB
- Category: UPDATE, title: "Release {version}"

### Technical Approach

- Backend: New `ReleaseNotificationStartupJob` @Component with @EventListener
- Added `UPDATE` to `NotificationCategory` enum
- Added `existsByCategoryAndTitle()` to `NotificationBroadcastRepository`
- Refactored DTOs to use enum types (NotificationCategory, NotificationAudienceType)
- Refactored entity `NotificationBroadcast.audienceType` from String to enum
- Frontend: Updated OpenAPI types, added UPDATE category to UI

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Created ReleaseNotificationStartupJobTest (5 tests) |
| 2 | spring-backend-agent | Implemented job + enum additions |
| 3 | spring-backend-agent | DTO→enum refactoring (category, audienceType) |
| 4 | spring-backend-agent | Entity enum fix + Lombok constructor |
| 5 | svelte-frontend-agent | OpenAPI sync, type consolidation, service migration |

## Implementation

### Backend

- `ReleaseNotificationStartupJob` — @EventListener on ApplicationReadyEvent
- `NotificationCategory.UPDATE` — new enum value
- `NotificationBroadcastRepository.existsByCategoryAndTitle()` — idempotency check
- `CreateBroadcastRequest.category` — String → NotificationCategory
- `AudienceRequest.type` — String → NotificationAudienceType
- `NotificationBroadcast.audienceType` — String → NotificationAudienceType with @Enumerated
- `BroadcastResponse` — enum fields
- `BroadcastValidator` — null checks for enums
- `NotificationBroadcastService` — simplified (no valueOf calls)

### Frontend

- Deleted `$lib/services/` — migrated to `$lib/api/dashboard/DashboardController.ts`
- Deleted `$lib/types/changelog.ts` — types from OpenAPI spec
- `models.ts` — enum derivations via NonNullable<>, consolidated
- `AdminNotificationController.ts` — rewritten with openapi-fetch client
- History/Send pages — UPDATE category support

### Deviations from Plan

- Added DTO→enum refactoring (user-requested improvement)
- Added entity audienceType enum (consistency)
- Migrated frontend services to api/ structure (cleanup)
- Updated skills with new conventions

## Files Modified

- `server/src/main/java/.../notification/job/ReleaseNotificationStartupJob.java` (new)
- `server/src/main/java/.../notification/model/NotificationCategory.java`
- `server/src/main/java/.../notification/model/NotificationBroadcast.java`
- `server/src/main/java/.../notification/model/request/CreateBroadcastRequest.java`
- `server/src/main/java/.../notification/model/request/AudienceRequest.java`
- `server/src/main/java/.../notification/model/response/BroadcastResponse.java`
- `server/src/main/java/.../notification/model/mapper/BroadcastMapper.java`
- `server/src/main/java/.../notification/model/validator/BroadcastValidator.java`
- `server/src/main/java/.../notification/service/NotificationBroadcastService.java`
- `server/src/main/java/.../notification/repository/NotificationBroadcastRepository.java`
- `server/openapi.json`
- `client/src/lib/types/api.d.ts`
- `client/src/lib/api/models.ts`
- `client/src/lib/api/dashboard/DashboardController.ts` (new)
- `client/src/lib/api/notification/AdminNotificationController.ts`
- `client/src/routes/(authenticated)/admin/tools/notifications/history/+page.svelte`
- `client/src/routes/(authenticated)/admin/tools/notifications/send/+page.svelte`
- `.opencode/skills/eventify-spring-standards/SKILL.md`
- `.opencode/skills/eventify-svelte-standards/SKILL.md`
- `.opencode/METADATA.md`
- Test files (4 updated)

## Tests

- 5 new tests (ReleaseNotificationStartupJobTest), 90 total backend tests passing
- Frontend: `bun run check` 0 errors
