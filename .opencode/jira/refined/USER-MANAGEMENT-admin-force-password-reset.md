# Admin Force Password Reset

**Epic**: User Management
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-01-04

## 1. User Story
**As an** administrator
**I want** to trigger a password reset for any user
**So that** I can help users who are locked out of their accounts or enforce password rotation for security purposes

## 2. Business Context & Value
Users occasionally lose access to their accounts or forget passwords. While they can self-service via "Forgot Password", there are scenarios where admin intervention is needed:
- User no longer has access to their registered email (needs admin to update email first, then reset)
- Security incident requiring immediate credential rotation
- Onboarding assistance for less technical users
- Account recovery for VIP/priority users

This story enables the "Force Password Reset" action in the Admin User Management page (currently disabled as "Coming Soon").

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: Force password reset from user details modal
    *   Given I am viewing a user's details in the modal
    *   When I click the "Force Password Reset" button
    *   Then a password reset email is sent to the user's registered email
    *   And a success toast is displayed: "Password reset email sent to {email}"
    *   And the button shows a loading state while processing

*   [ ] **Scenario 2**: Force password reset from actions dropdown
    *   Given I am viewing the users table
    *   When I click "Force Password Reset" in a user's actions dropdown
    *   Then a password reset email is sent to the user
    *   And a success toast is displayed

*   [ ] **Scenario 3**: Handle user with unvalidated email
    *   Given I am forcing password reset for a user who has not validated their email
    *   When I trigger the password reset
    *   Then the reset email is still sent (user can validate via reset flow)
    *   And the action succeeds normally

*   [ ] **Scenario 4**: Handle disabled/locked user
    *   Given I am forcing password reset for a locked user
    *   When I trigger the password reset
    *   Then the reset email is sent
    *   And the action succeeds (user can reset password, but still needs to be unlocked to login)

*   [ ] **Scenario 5**: Error handling
    *   Given the password reset request fails (e.g., email service down)
    *   When I trigger the force password reset
    *   Then an error toast is displayed with the failure reason
    *   And the UI remains functional for retry

*   [ ] **Scenario 6**: All quality checks pass
    *   Given the implementation is complete
    *   When I run `bun run check` and backend tests
    *   Then all checks and tests pass

## 4. Technical Requirements

### Backend - New Endpoint
Create a new admin endpoint in `AdminUserController.java`:

```java
@ResponseStatus(OK)
@Operation(summary = "Force password reset for a user (sends reset email)")
@PostMapping(
    path = ADMIN_USER_FORCE_RESET_PATH,  // "/v1/admin/user/{id}/force-reset"
    produces = APPLICATION_JSON_VALUE
)
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public ResponseEntity<Void> forcePasswordReset(@PathVariable final Long id) {
    userService.forcePasswordReset(id);
    return ResponseEntity.status(OK).build();
}
```

### Backend - Service Layer
Add method to `UserService.java` or `PasswordService.java`:

```java
/**
 * Forces a password reset for the specified user.
 * Sends a password reset email to the user's registered email address.
 *
 * @param userId the ID of the user to reset
 * @throws DataNotFoundException if user not found
 */
public void forcePasswordReset(final Long userId) {
    final User user = userRepository.findById(userId)
        .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));
    
    // Reuse existing password reset flow
    passwordService.requestPasswordReset(user.getEmail());
}
```

Alternatively, call `PasswordService.requestPasswordReset(email)` directly from controller after fetching user.

### Backend - Path Constant
Add to `Paths.java`:
```java
public static final String ADMIN_USER_FORCE_RESET_PATH = ADMIN_PATH + USER_PART + "/{id}/force-reset";
```

### Backend - Tests
*   Unit test for `forcePasswordReset` service method
*   Integration test for the endpoint:
    *   Success case: valid user ID → 200 OK
    *   Not found case: invalid user ID → 404
    *   Unauthorized case: non-admin user → 403

### Frontend - API Controller
Add to `AdminUserController.ts`:
```typescript
/**
 * Force a password reset for a user (sends reset email)
 * @param userId The user ID to reset
 */
export async function forcePasswordReset(userId: number): Promise<void> {
  const { error } = await client.POST('/v1/admin/user/{id}/force-reset', {
    params: { path: { id: userId } }
  });

  if (error) {
    throw error;
  }
}
```

### Frontend - UI Updates

**In User Details Modal (`UserDetailsSheet.svelte`):**
*   Add "Force Password Reset" button in the actions section
*   Button styling: outline/secondary variant with key icon
*   Loading state while request is in progress
*   On success: show toast, keep modal open
*   On error: show error toast

**In Users Table Actions Dropdown:**
*   Enable the "Force Password Reset" menu item (remove "Coming Soon")
*   On click: call API, show toast
*   Loading state on the menu item while processing

### Security Considerations
*   Only admins with `MANAGE_USERS` permission can trigger
*   Rate limiting recommended (prevent abuse) - could be a future enhancement
*   Audit logging recommended (track who triggered reset for whom) - could be a future enhancement

## 5. Design & UI/UX

### Button in User Details Modal
```
┌─────────────────────────────────────┐
│  [Lock User]  [Force Password Reset]│
│                         ↑           │
│                    Key icon         │
│              outline variant        │
└─────────────────────────────────────┘
```

### Actions Dropdown (table row)
```
┌─────────────────────────┐
│ View Details            │
│ Lock User               │
│ Change Role         →   │
│ ─────────────────────── │
│ 🔑 Force Password Reset │  ← Now enabled
└─────────────────────────┘
```

### Toast Messages
*   **Success**: "Password reset email sent to john.doe@example.com"
*   **Error**: "Failed to send password reset email. Please try again."

### Loading States
*   Button shows spinner + "Sending..." text
*   Dropdown item shows spinner icon replacing the key icon

## 6. Implementation Notes / Research

### Existing Password Reset Flow
The application already has a complete password reset flow:
*   `PasswordService.requestPasswordReset(email)` - Creates reset token and sends email
*   `PasswordController` handles `/v1/public/reset-password/request` for user-initiated resets
*   Email templates exist: `password-reset.html`, `password-reset.mjml`

The admin force reset simply reuses this existing flow, triggered by admin instead of user.

### Reference Files
*   **Password Service**: `server/src/main/java/io/github/eventify/api/user/service/PasswordService.java`
*   **Password Controller**: `server/src/main/java/io/github/eventify/api/user/controller/PasswordController.java`
*   **Email Service**: `server/src/main/java/io/github/eventify/common/email/service/sender/EmailService.java`
*   **Admin User Controller**: `server/src/main/java/io/github/eventify/api/admin/controller/AdminUserController.java`

### Potential Pitfalls
*   The existing `requestPasswordReset` silently succeeds even if email doesn't exist (security by design). For admin endpoint, we should verify user exists first and return 404 if not found.
*   Consider what happens if admin forces reset on themselves - should work fine, no special handling needed.

### Files to Create/Modify
| Action | File |
|--------|------|
| Modify | `server/src/main/java/io/github/eventify/api/admin/controller/AdminUserController.java` |
| Modify | `server/src/main/java/io/github/eventify/api/user/service/PasswordService.java` (or UserService) |
| Modify | `server/src/main/java/io/github/eventify/api/Paths.java` |
| Create | `server/src/test/java/.../AdminUserControllerForceResetTest.java` (or add to existing test) |
| Modify | `client/src/lib/api/admin/AdminUserController.ts` |
| Modify | `client/src/routes/(authenticated)/admin/users/+page.svelte` (enable dropdown action) |
| Modify | `client/src/lib/components/admin/UserDetailsSheet.svelte` (add button) |

### Dependencies
*   Requires `USER-MANAGEMENT-admin-users-page.md` to be implemented first (provides the UI where this action is triggered)
