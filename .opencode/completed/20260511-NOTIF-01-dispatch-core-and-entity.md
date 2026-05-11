# Notification Dispatch Core, Entity & Welcome Notification

**Completed:** 2026-05-11
**Epic:** NOTIF
**Source:** `.opencode/refined/NOTIF-01-dispatch-core-and-entity.md`

## Summary

Built the notification subsystem foundation: entity, repository, dispatch architecture (adapter pattern), CRUD API with JFrame pagination, and welcome notification on both registration paths.

## Plan Approved by the user:

### Requirements Summary

- Notification + NotificationBroadcast entities (DB tables)
- Dispatch architecture: `NotificationDispatchService` → `AudienceResolver` → `InAppNotificationAdapter`
- CRUD endpoints: search (paginated via JFrame), unread-count, mark-read, mark-all-read
- Welcome notification on email/password + OAuth2 registration
- Authorization: ownership via `WHERE user_id = principal.id`, 404 for cross-user
- Zero-recipient audience → WARN log, no exception

### Technical Approach

- Package: `api/notification/` with model/, repository/, service/, controller/, mapper/, adapter/
- Entities: `Notification` (Long IDENTITY), `NotificationBroadcast` (Long IDENTITY)
- Enums: `NotificationCategory`, `NotificationStyle`
- Core: `NotificationDispatchService`, `AudienceResolver`, `InAppNotificationAdapter`, `NotificationAdapter` interface
- Domain types: `NotificationAudience` (sealed interface), `NotificationPayload` (record)
- API: `NotificationController` — POST /search, GET /unread-count, POST /{id}/read, POST /read-all
- Mapper: `NotificationMapper` extends `PageMapper`
- MetaData: `NotificationMetaData` extends `AbstractSortSearchMetaData`
- Welcome hook: constructor-injected `NotificationDispatchService` in `UserService` + `CustomOAuth2UserService`

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Created 20 tests (8 controller, 3 dispatch, 2 audience, 7 service) |
| 2.2 | spring-backend-agent | Implemented all components, all tests passing |
| 2.3 | orchestrator + agents | Review fixes: Long IDs, JFrame pagination, constructor injection, migration authors |
| 2.4 | backend-optimizer-agent | Lombok on NotificationPayload, TimeProvider.now(), removed wrapper methods |

## Implementation

### Backend

- **Endpoints:** POST /v1/notifications/search, GET /v1/notifications/unread-count, POST /v1/notifications/{id}/read, POST /v1/notifications/read-all
- **Services:** NotificationService, NotificationDispatchService, AudienceResolver
- **Adapters:** NotificationAdapter (interface), InAppNotificationAdapter
- **DB:** Migration `202605111000-PRD-notification-tables.xml` — notification + notification_broadcast tables

### Deviations from Plan

- IDs changed from UUID to Long IDENTITY (consistency with all other entities)
- Pagination changed from raw limit/offset to JFrame SortablePageInput + Specification pattern
- Welcome hook uses constructor injection (not setter injection as initially implemented)
- Migration authors across all 20 migration files standardized to `jordi.jaspers`

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Create test suites (20 tests) | Complete |
| spring-backend-agent | Implement notification feature | Complete |
| deep-research-agent | Research patterns (4 parallel) | Complete |
| deep-research-agent | Migration author audit | Complete |
| backend-optimizer-agent | Code quality refactoring | Complete |

## Files Modified

### New files
- `server/src/main/java/io/github/eventify/api/notification/**` — 17 Java files (entity, repo, service, controller, mapper, adapter, model)
- `server/src/main/resources/db/changelog/changesets/202605111000-PRD-notification-tables.xml`
- `server/src/test/java/io/github/eventify/api/notification/**` — 4 test files

### Modified files
- `server/src/main/java/io/github/eventify/api/Paths.java` — notification path constants
- `server/src/main/java/io/github/eventify/api/user/service/UserService.java` — welcome notification hook
- `server/src/main/java/io/github/eventify/common/security/oauth2/CustomOAuth2UserService.java` — welcome notification hook
- `server/build.gradle.kts` — `@SuppressWarnings("unchecked")` for test compilation
- `server/src/test/java/.../support/IntegrationTest.java` — `aNotificationForUser()` factory
- `server/src/test/java/.../support/util/TestContextInitializer.java` — `NotificationRepository` autowired
- `server/src/test/java/.../UserServiceRegisterAuthProviderTest.java` — constructor arg
- `server/src/test/java/.../CustomOAuth2UserService*Test.java` (6 files) — constructor arg
- 20 migration XML files — author standardized to `jordi.jaspers`

## Tests

- 23 tests written, 23 passing (20 original + 3 added during review fixes)
