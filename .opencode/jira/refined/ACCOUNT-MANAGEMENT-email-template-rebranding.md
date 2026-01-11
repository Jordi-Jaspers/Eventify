# Rebrand and Refactor Email Templates

**Epic**: Account Management
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-01-11

## 1. User Story
**As a** user receiving transactional emails from Eventify
**I want** the emails to have consistent Eventify branding
**So that** I trust the emails are legitimate and have a professional experience

## 2. Business Context & Value
Current email templates reference "Aniflix" (a previous project). This creates brand confusion and appears unprofessional. All transactional emails should consistently represent the Eventify brand.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Password reset email has correct branding
    *   Given I request a password reset
    *   When I receive the email
    *   Then it displays "Eventify" branding (logo/name)
    *   And the footer says "The Eventify Team"
    *   And copyright references Eventify with current year

*   [ ] **Scenario 2**: Account created/verification email has correct branding
    *   Given I register a new account
    *   When I receive the verification email
    *   Then it displays "Eventify" branding consistently

*   [ ] **Scenario 3**: Email links use correct application URL
    *   Given I receive any transactional email
    *   When I inspect the links
    *   Then they point to the configured `applicationUrl` for the environment

*   [ ] **Scenario 4**: Emails render correctly across clients
    *   Given the updated email templates
    *   When viewed in major email clients (Gmail, Outlook, Apple Mail)
    *   Then they render without broken layouts

## 4. Technical Requirements
*   **Templates to Update**:
    *   `server/src/main/resources/templates/password-reset.html`
    *   `server/src/main/resources/templates/account-created.html`
*   **Branding Changes**:
    *   Replace "Aniflix" → "Eventify" (all occurrences)
    *   Update logo/header color from `#E60000` to Eventify primary color
    *   Update copyright year to dynamic or 2026
    *   Update team signature to "The Eventify Team"
*   **URL Updates**:
    *   Password reset link: Change from `/password/reset/{token}` to `/reset-password?token={token}` (align with frontend route)

## 5. Design & UI/UX
*   Use Eventify primary brand color for header and CTA buttons
*   Maintain responsive email design (MJML-based structure is already good)
*   Consider adding Eventify logo image if available
*   Keep clean, minimal design that works in all email clients

## 6. Implementation Notes / Research
*   **Current templates use Thymeleaf**: Variables like `${applicationUrl}` and `${token}` are injected
*   **MJML-compiled HTML**: Templates appear to be compiled from MJML - if source MJML exists, update there
*   **Variables available**: `token`, `applicationUrl`, `emailAddress` (see `DefaultEmailService.java`)
*   **Testing**: Use Mailtrap or similar for email preview testing
*   **Future consideration**: Move to a templating system that separates content from layout (not in scope)
