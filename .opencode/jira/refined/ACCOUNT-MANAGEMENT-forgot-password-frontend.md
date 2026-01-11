# Implement Forgot Password Flow (Frontend)

**Epic**: Account Management
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-01-11

## 1. User Story
**As a** registered user who has forgotten my password
**I want** to request a password reset link and set a new password
**So that** I can regain access to my account without contacting support

## 2. Business Context & Value
Users occasionally forget their passwords. Without a self-service reset flow, users would need to contact support, creating friction and support overhead. The backend already supports password reset tokens; this story completes the frontend experience.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: User requests password reset
    *   Given I am on the `/forgot-password` page
    *   When I enter a valid email address and submit the form
    *   Then I see a success message: "If an account exists with this email, you will receive a password reset link"
    *   And the message is shown regardless of whether the email exists (security)

*   [ ] **Scenario 2**: User resets password with valid token
    *   Given I received a password reset email and clicked the link
    *   When I land on `/reset-password?token=<token>`
    *   Then I see a form with "New Password" and "Confirm Password" fields
    *   And I can submit to set my new password

*   [ ] **Scenario 3**: Successful password reset redirects to login
    *   Given I submitted valid matching passwords on the reset form
    *   When the password is successfully changed
    *   Then I am redirected to `/login` with a success toast: "Password reset successfully. Please log in."

*   [ ] **Scenario 4**: Invalid or expired token shows error
    *   Given I navigate to `/reset-password` with an invalid or expired token
    *   When the page loads or I submit the form
    *   Then I see an error message on the reset page
    *   And I see a link to request a new password reset email

*   [ ] **Scenario 5**: Password validation
    *   Given I am on the reset password form
    *   When I enter passwords that don't match or don't meet requirements
    *   Then I see inline validation errors before submission

## 4. Technical Requirements
*   **API Endpoints Used**:
    *   `POST /v1/public/reset-password/request?email=<email>` - Request reset (returns 204)
    *   `POST /v1/public/reset-password` - Execute reset with body `{ token, newPassword, confirmPassword }`
*   **Frontend Routes**:
    *   `/forgot-password` - Already exists as stub, needs implementation
    *   `/reset-password` - New page, reads `token` from query param
*   **Security**: 
    *   Token passed via query param (consistent with email link format)
    *   Never reveal if email exists in system
*   **Validation**: 
    *   Password min 8 characters (match existing registration validation)
    *   Passwords must match

## 5. Design & UI/UX
*   Follow existing auth page patterns (see `/login`, `/register`)
*   Use glassmorphism Card component with centered layout
*   Include password visibility toggle (eye icon) on password fields
*   Show loading state on submit button
*   Success/error feedback via toast notifications (svelte-sonner)

## 6. Implementation Notes / Research
*   **Existing stub**: `client/src/routes/(public)/forgot-password/+page.svelte` - replace placeholder content
*   **New page needed**: `client/src/routes/(public)/reset-password/+page.svelte`
*   **Route already defined**: `CLIENT_ROUTES.RESET_PASSWORD_PAGE` exists in `client/src/lib/config/routes.ts`
*   **Request DTOs exist**: `ForgotPasswordRequest` with `token`, `newPassword`, `confirmPassword` fields
*   **Email template note**: Current email links to `/password/reset/{token}` - update template OR use query param `/reset-password?token={token}` (recommend query param for SvelteKit compatibility)
*   **Reference components**: `ResendVerificationButton.svelte`, login page password field with toggle
