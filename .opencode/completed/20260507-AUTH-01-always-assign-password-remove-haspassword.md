# AUTH-01: Always Assign Password — Remove hasPassword

**Completed:** 2026-05-07
**Epic:** AUTH
**Source:** `.opencode/refined/AUTH-01-always-assign-password-remove-haspassword.md`

## Summary

Removed `hasPassword` boolean field from User entity and DB. Built ChangePasswordDialog modal on security page. LOCAL provider row now shows "Change" + "Reset via email" buttons. OAuth rows show brand SVG icons with emerald checkmark when connected.

## Plan Approved by the user:

### Requirements Summary
- Remove `hasPassword` field from User entity + `has_password` DB column
- Remove all `setHasPassword`/`isHasPassword` calls across 4 production files
- Build ChangePasswordDialog (current + new + confirm) calling `POST /v1/password/update-password`
- Add "Reset via email" button calling existing reset endpoint
- LOCAL row: no badge, just "Change" + "Reset via email"
- OAuth rows: brand icons, emerald checkmark when connected, no text badges

### Technical Approach
- Backend: remove field, migration to drop column, clean up services
- Frontend: new ChangePasswordDialog + ChangePasswordService, update ConnectedAccountRow

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Remove hasPassword from 5 test files |
| 2.2 | spring-backend-agent | Remove field, services, migration |
| 2.4 | backend-optimizer-agent | Refactor services (-31% lines) |
| 2.4+ | spring-backend-agent | Restore Javadoc, fix log levels, migrate error strings |
| 3.1 | typescript-testing-agent | Create 35 frontend tests |
| 3.2 | svelte-frontend-agent | Implement dialog + row redesign |
| 3.4 | frontend-optimizer-agent | Remove dead imports, fix paths |

## Implementation

### Backend
- Removed `hasPassword` field from `User.java`
- Removed `setHasPassword()` calls from UserService, PasswordService, CustomOAuth2UserService
- Migration: `202605071000-PRD-drop-has-password-column.xml`
- Added `OAUTH2_EMAIL_NOT_AVAILABLE` (ERR-0059) to ApiErrorCode
- Migrated inline error strings to ApiErrorCode constants
- Refactored 3 services: 584→403 lines (-31%)

### Frontend
- `ChangePasswordDialog.svelte` — modal with password fields + strength meter
- `ChangePasswordService.svelte.ts` — reactive service (canSubmit, submit, reset)
- `ConnectedAccountRow.svelte` — LOCAL: KeyRound + "Change"/"Reset via email"; OAuth: brand SVGs + CircleCheck
- `provider-meta.ts` — centralized brand SVG icons
- `OAuthButton.svelte` — deduplicated SVGs, uses provider-meta

### Deviations from Plan
- Added brand SVG icons (user feedback during review)
- Removed badges from OAuth rows, replaced with emerald checkmark (user feedback)
- Migrated 2 additional inline error strings to ApiErrorCode (user feedback)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Remove hasPassword from tests | Complete |
| spring-backend-agent | Remove field + migration | Complete |
| backend-optimizer-agent | Refactor services | Complete |
| spring-backend-agent | Javadoc + logs + error codes | Complete |
| typescript-testing-agent | Frontend tests (35) | Complete |
| svelte-frontend-agent | Dialog + row redesign | Complete |
| frontend-optimizer-agent | Cleanup | Complete |

## Files Modified

**Backend (modified):**
- `server/.../user/model/User.java` — removed hasPassword field
- `server/.../user/service/UserService.java` — removed setHasPassword, refactored
- `server/.../user/service/PasswordService.java` — removed setHasPassword, refactored
- `server/.../security/oauth2/CustomOAuth2UserService.java` — removed hasPassword logic, migrated error strings
- `server/.../common/exception/ApiErrorCode.java` — added OAUTH2_EMAIL_NOT_AVAILABLE

**Backend (new):**
- `server/.../db/changelog/changesets/202605071000-PRD-drop-has-password-column.xml`

**Backend tests (modified):**
- `PasswordServiceTest.java`, `UserAuthProviderServiceTest.java`, `UserServiceRegisterAuthProviderTest.java`
- `CustomOAuth2UserServiceAuthProviderTest.java`, `CustomOAuth2UserServiceLoginModeTest.java`
- `CustomOAuth2UserServiceCreateUserTest.java`, `CustomOAuth2UserServiceProcessUserTest.java`

**Frontend (modified):**
- `client/.../PasswordController.ts` — added updatePassword()
- `client/.../ConnectedAccountsService.svelte.ts` — added dialog/reset methods
- `client/.../ConnectedAccountRow.svelte` — redesigned LOCAL + OAuth rows
- `client/.../provider-meta.ts` — added brandIcon field + SVGs
- `client/.../OAuthButton.svelte` — deduplicated SVGs
- `client/.../profile/index.ts` — exported ChangePasswordDialog
- `client/.../security/+page.svelte` — wired dialog

**Frontend (new):**
- `client/.../profile/ChangePasswordDialog.svelte`
- `client/.../authentication/service/ChangePasswordService.svelte.ts`
- `client/.../authentication/__tests__/PasswordController.test.ts`
- `client/.../authentication/__tests__/ChangePasswordService.test.ts`
- `client/.../authentication/__tests__/fixtures/password.fixtures.ts`
- `client/.../user/__tests__/ConnectedAccountRowBehavior.test.ts`
- `client/.../user/__tests__/fixtures/provider.fixtures.ts` (modified)

## Tests

- Backend: 1338 tests passing
- Frontend: 35 new tests passing
