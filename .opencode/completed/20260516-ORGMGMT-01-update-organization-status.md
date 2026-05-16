# Update Organization Status

**Completed:** 2026-05-16
**Epic:** ORGMGMT
**Source:** .opencode/refined/ORGMGMT-01-update-organization-status.md

## Summary

Admin can change organization status (ACTIVE↔SUSPENDED) via PATCH endpoint. Suspended orgs block non-admin member access via security filter. Admin UI gets EditOrganizationSheet with status dropdown and consolidated kebab row menu.

## Plan Approved by the user:

### Requirements Summary

- PATCH /api/v1/admin/organizations/{orgId}/status — secured with MANAGE_ORGANIZATIONS
- All transitions allowed (ACTIVE↔SUSPENDED, idempotent)
- Suspended org access blocked: non-admin members get 403 on org-scoped calls
- EditOrganizationSheet: status dropdown from row menu Edit action
- Consolidated row actions: kebab menu (Edit, API Keys, Members)
- Org switcher suspended handling deferred (UserOrganizationResponse lacks status field)

### Technical Approach

- Backend: New PATCH endpoint, SuspendedOrganizationFilter (OncePerRequestFilter), ForbiddenHandler, AbstractSecurityResponseHandler
- Frontend: EditOrganizationSheet, kebab DropdownMenu, AdminOrganizationController consolidation

### Execution Order

| Phase | Agent | Task |
| ----- | ----- | ---- |
| 1 | spring-testing-agent | Backend test suite (8+8 tests) |
| 2 | spring-backend-agent | Implementation (endpoint + filter) |
| 3 | backend-optimizer-agent | Refactor (-13%) |
| 4 | svelte-frontend-agent | UI (EditOrganizationSheet, kebab menu, controller consolidation) |
| 5 | frontend-optimizer-agent | Refactor (-13%) |

## Implementation

### Backend

- `PATCH /v1/admin/organizations/{orgId}/status` — validates status, updates org
- `SuspendedOrganizationFilter` — blocks `/v1/organization/{orgId}/**` for suspended orgs, skips admin/public/auth, allows MANAGE_ORGANIZATIONS authority
- `AbstractSecurityResponseHandler` — shared JSON error response writing
- `ForbiddenHandler` — 403 responses (mirrors UnauthorizedHandler)
- `RequestMatcherConfig` — added ADMIN_MATCHER, ORG_MATCHER, ORG_PATH_PATTERN

### Frontend

- `AdminOrganizationController.ts` — consolidated all admin org API calls (moved from OrganizationController.ts)
- `EditOrganizationSheet.svelte` — status dropdown with Select, toast feedback
- Admin orgs page — kebab DropdownMenu (Edit/API Keys/Members)
- OpenAPI spec synced

### Deviations from Plan

- Org switcher suspended UI deferred: UserOrganizationResponse has no status field
- Auto-context switch deferred: same reason

## Agents Used

| Agent | Task | Result |
| ----- | ---- | ------ |
| spring-testing-agent | Backend tests (16 tests) | Complete |
| spring-backend-agent | Endpoint + filter implementation | Complete |
| backend-optimizer-agent | Refactor handlers, config | Complete |
| svelte-frontend-agent | EditOrganizationSheet, kebab menu, controller | Complete |
| frontend-optimizer-agent | Dead code, merge handlers | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/organization/model/request/UpdateOrganizationStatusRequest.java` (new)
- `server/src/main/java/io/github/eventify/api/organization/model/validator/OrganizationValidator.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java`
- `server/src/main/java/io/github/eventify/api/admin/controller/AdminOrganizationController.java`
- `server/src/main/java/io/github/eventify/common/security/filter/SuspendedOrganizationFilter.java` (new)
- `server/src/main/java/io/github/eventify/common/security/filter/AbstractSecurityResponseHandler.java` (new)
- `server/src/main/java/io/github/eventify/common/security/filter/ForbiddenHandler.java` (new)
- `server/src/main/java/io/github/eventify/common/security/filter/UnauthorizedHandler.java`
- `server/src/main/java/io/github/eventify/common/config/RequestMatcherConfig.java`
- `server/src/main/java/io/github/eventify/common/config/WebSecurityConfig.java`
- `server/src/main/java/io/github/eventify/api/Paths.java`
- `server/src/test/java/io/github/eventify/api/admin/controller/AdminOrganizationControllerTest.java`
- `server/src/test/java/io/github/eventify/common/security/filter/SuspendedOrganizationFilterTest.java` (new)
- `client/src/lib/api/admin/AdminOrganizationController.ts` (new)
- `client/src/lib/api/organization/OrganizationController.ts` (deleted)
- `client/src/lib/components/admin/EditOrganizationSheet.svelte` (new)
- `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte`
- `client/src/lib/components/admin/CreateOrganizationSheet.svelte` (import update)
- + 6 other files with import path updates

## Tests

- 16 backend tests written, all passing
- Frontend type check: 0 errors
