## [2026-01-11] - Admin Force Password Reset

### Plan (approved)
Enable admins to force password reset for users due to security concerns. Immediately invalidates user's password and sends reset email.

**User Story:** As an administrator, I want to trigger a password reset for any user so that I can help users who are locked out or enforce password rotation for security purposes.

### Actual Changes

**Backend:**
- Added `POST /v1/admin/user/{id}/force-reset` endpoint
- Added `PasswordService.forcePasswordReset(Long userId)` method
- Security: `@PreAuthorize("hasAuthority('MANAGE_USERS')")`
- Behavior:
  1. Immediately invalidates password (sets to random UUID hash)
  2. Invalidates existing password reset tokens
  3. Sends password reset email
- User is locked out immediately and must use reset link

**Frontend:**
- Added `forcePasswordReset()` to `AdminUserController.ts`
- Added service method with loading state to `AdminUserService.svelte.ts`
- Enabled "Force Password Reset" in users table dropdown (was "Coming Soon")
- Added "Reset Password" button in user details sheet footer
- Toast: "Password invalidated. Reset email sent to {email}"

**UI Polish:**
- Fixed DataTable header grid alignment
- Enhanced filter card glassmorphism

### Agents Used
- java-testing-agent: Created 6 unit tests + 6 integration tests
- java-backend-agent: Implemented endpoint and service method
- sveltekit-frontend-agent: Added API, service, enabled UI
- ui-validator: 1 iteration (minor DataTable polish)

### Files Modified
**Backend:**
- `server/src/main/java/io/github/eventify/api/Paths.java`
- `server/src/main/java/io/github/eventify/api/user/service/PasswordService.java`
- `server/src/main/java/io/github/eventify/api/admin/controller/AdminUserController.java`

**Backend Tests:**
- `server/src/test/java/io/github/eventify/api/user/service/PasswordServiceTest.java` (created)
- `server/src/test/java/io/github/eventify/api/admin/controller/AdminUserControllerTest.java` (extended)

**Frontend:**
- `client/src/lib/api/admin/AdminUserController.ts`
- `client/src/lib/api/admin/service/AdminUserService.svelte.ts`
- `client/src/routes/(authenticated)/admin/users/+page.svelte`
- `client/src/lib/components/data-table/DataTableHeader.svelte`
- `client/src/lib/components/data-table/DataTable.svelte`

### Quality Metrics
- ✅ Tests: 13 written (6 unit + 7 integration), all passing
- ✅ Build: Backend + Frontend successful
- ✅ Type checks: 0 errors
- ✅ UI Polish: Complete (1 iteration)
