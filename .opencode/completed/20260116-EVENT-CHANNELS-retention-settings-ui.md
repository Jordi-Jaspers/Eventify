# User/Organization Retention Settings UI

**Epic**: Event Channels
**Date**: 2026-01-16
**Status**: Completed

## Plan (approved)

Build "Data & Storage" settings pages for configuring event data retention periods.

**Backend:**
- GET/PUT `/v1/user/settings/retention` for personal retention
- GET/PUT `/v1/organization/{orgId}/settings/retention` for org retention
- `RetentionValidator` using jframe Validator pattern
- Valid values: 90, 180, 365, 730, 1095, 1825 days
- OWNER/ADMIN authorization on org endpoints

**Frontend:**
- `/profile/data-storage` page for user settings
- `/organizations/[orgId]/settings/data-storage` for org settings
- "Data & Storage" tab in settings navigation (hidden for MEMBER role)
- Discrete slider with snap points and tick marks
- Warning alert when reducing retention, confirmation modal

## Actual Changes

### Backend

**Created:**
- `RetentionValidator.java` - jframe Validator for retention values
- `UpdateRetentionRequest.java` - request DTO
- `RetentionSettingsResponse.java` - response DTO
- `UserSettingsController.java` - user retention endpoints
- `OrganizationSettingsController.java` - org retention endpoints

**Modified:**
- `UserService.java` - added `updateRetentionDays()` method
- `OrganizationService.java` - added `updateRetentionDays()` method
- `Paths.java` - added retention endpoint constants

### Frontend

**Created:**
- `DataRetentionSettings.svelte` - reusable retention settings component
- `/profile/data-storage/+page.svelte` - user settings page
- `/organizations/[orgId]/settings/data-storage/+page.svelte` - org settings page
- Slider component (shadcn-svelte)
- 10 screenshot tests

**Modified:**
- `SettingsNav.svelte` - added "Data & Storage" tab with Database icon
- `OrgSettingsNav.svelte` - added "Data & Storage" tab (conditional on role)
- `routes.ts` - added DATA_STORAGE_PAGE and ORGANIZATION_SETTINGS_DATA_STORAGE_PAGE
- Org settings layout - added role-based canManage prop

### UI Polish (1 iteration)
- Improved warning alert styling (icon size, amber colors)
- Enhanced button gradient

## Agents Used
- java-testing-agent: Created 51 tests (user, org, validator)
- java-backend-agent: Implemented controllers, DTOs, validation (refactored to jframe pattern)
- sveltekit-frontend-agent: Built pages, component, navigation, screenshot tests
- ui-validator: 1 iteration for visual polish

## Files Modified

**Backend (11 files):**
- `server/src/main/java/io/github/eventify/api/Paths.java`
- `server/src/main/java/io/github/eventify/api/user/controller/UserSettingsController.java`
- `server/src/main/java/io/github/eventify/api/user/model/request/UpdateRetentionRequest.java`
- `server/src/main/java/io/github/eventify/api/user/model/response/RetentionSettingsResponse.java`
- `server/src/main/java/io/github/eventify/api/user/model/validator/RetentionValidator.java`
- `server/src/main/java/io/github/eventify/api/user/service/UserService.java`
- `server/src/main/java/io/github/eventify/api/organization/controller/OrganizationSettingsController.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java`
- `server/src/test/java/io/github/eventify/api/user/controller/UserSettingsControllerTest.java`
- `server/src/test/java/io/github/eventify/api/user/model/validator/RetentionValidatorTest.java`
- `server/src/test/java/io/github/eventify/api/organization/controller/OrganizationSettingsControllerTest.java`

**Frontend (24 files):**
- `client/src/lib/components/settings/DataRetentionSettings.svelte`
- `client/src/lib/components/settings/index.ts`
- `client/src/lib/components/settings/SettingsNav.svelte`
- `client/src/lib/components/settings/OrgSettingsNav.svelte`
- `client/src/lib/components/ui/slider/` (new component)
- `client/src/lib/config/routes.ts`
- `client/src/routes/(authenticated)/profile/data-storage/+page.svelte`
- `client/src/routes/(authenticated)/organizations/[orgId]/settings/data-storage/+page.svelte`
- `client/src/routes/(authenticated)/organizations/[orgId]/settings/+layout.svelte`
- `client/test/components/data-storage.spec.ts`
- 10 screenshot files

## Quality Metrics
- ✅ Tests: 51 backend (35 integration + 16 unit), 10 screenshot tests
- ✅ Build: Successful
- ✅ Type check: 0 errors
- ✅ UI Polish: Complete (1 iteration)
