# Playwright Screenshot Tests for Missing Pages

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: L (Large)
**Created Date**: 2026-01-09

## 1. User Story
**As a** developer
**I want** Playwright screenshot tests for all major application pages
**So that** I can visually verify UI consistency without manually spinning up the application

## 2. Business Context & Value
Currently, only 4 pages have screenshot tests (login, developer, org-settings, admin-api-keys). Many critical pages lack visual regression coverage, making it difficult to catch unintended UI changes. Comprehensive screenshot tests enable quick visual verification of the entire application and help maintain design consistency across releases.

## 3. Acceptance Criteria

### Phase 1: Public Pages (Unauthenticated)
*   [ ] **Scenario 1**: Registration page screenshots
    *   Given I am on the `/register` page
    *   Then capture: empty form, form with data filled, password strength indicator states

*   [ ] **Scenario 2**: Forgot password page screenshots
    *   Given I am on the `/forgot-password` page
    *   Then capture: empty form, form with email filled

*   [ ] **Scenario 3**: Email verification page screenshots
    *   Given I am on the `/verify` page
    *   Then capture: default state (awaiting verification or token input)

*   [ ] **Scenario 4**: Public landing page screenshots
    *   Given I am on the `/` root page
    *   Then capture: default landing page state

**⏸️ APPROVAL GATE**: Review Phase 1 screenshots before proceeding.

### Phase 2: User Dashboard & Profile
*   [ ] **Scenario 5**: User dashboard screenshots
    *   Given I am logged in and on `/dashboard`
    *   Then capture: default dashboard with test data visible

*   [ ] **Scenario 6**: User profile page screenshots
    *   Given I am logged in and on `/profile`
    *   Then capture: profile view, editable field focused

**⏸️ APPROVAL GATE**: Review Phase 2 screenshots before proceeding.

### Phase 3: Organization Pages
*   [ ] **Scenario 7**: Organization dashboard screenshots
    *   Given I am logged in and on `/organizations/[orgId]/dashboard`
    *   Then capture: org dashboard with test data

*   [ ] **Scenario 8**: Organization members page screenshots
    *   Given I am logged in and on `/organizations/[orgId]/members`
    *   Then capture: members list, add member sheet/modal

**⏸️ APPROVAL GATE**: Review Phase 3 screenshots before proceeding.

### Phase 4: Admin Pages
*   [ ] **Scenario 9**: Admin dashboard screenshots
    *   Given I am logged in as admin and on `/admin/dashboard`
    *   Then capture: admin dashboard with stats/charts

*   [ ] **Scenario 10**: Admin users page screenshots
    *   Given I am logged in as admin and on `/admin/users`
    *   Then capture: users table with test data, filters applied

*   [ ] **Scenario 11**: Admin organizations page screenshots
    *   Given I am logged in as admin and on `/admin/organizations`
    *   Then capture: organizations table with test data

*   [ ] **Scenario 12**: Admin create organization page screenshots
    *   Given I am logged in as admin and on `/admin/organizations/new`
    *   Then capture: empty form, form with data filled

**⏸️ FINAL APPROVAL**: Review all Phase 4 screenshots and confirm completion.

## 4. Technical Requirements
*   **Test Files**: Create new spec files following existing pattern:
    *   `client/test/components/register.spec.ts`
    *   `client/test/components/forgot-password.spec.ts`
    *   `client/test/components/verify.spec.ts`
    *   `client/test/components/landing.spec.ts`
    *   `client/test/components/dashboard.spec.ts`
    *   `client/test/components/profile.spec.ts`
    *   `client/test/components/org-dashboard.spec.ts`
    *   `client/test/components/org-members.spec.ts`
    *   `client/test/components/admin-dashboard.spec.ts`
    *   `client/test/components/admin-users.spec.ts`
    *   `client/test/components/admin-organizations.spec.ts`
*   **Screenshot Output**: `client/test/resources/screenshots/<page-name>/`
*   **Naming Convention**: `XX-<state>-<project>.png` (e.g., `01-default-desktop-chrome.png`)
*   **Dependencies**: 
    *   Phase 2-4 require the test data from "Introduce More Test Data" story to be implemented first
    *   Tests requiring authentication should use the "Fill Credentials" button pattern
*   **Performance**: Set appropriate timeouts (15-30s) for pages requiring authentication

## 5. Design & UI/UX
*   Screenshots should capture `fullPage: true` for complete page representation
*   Wait for animations/transitions to settle before capture (`waitForTimeout(500)`)
*   Key modals to capture:
    *   Add Member sheet (org members page)
    *   Create Organization form (admin page)
*   Avoid capturing transient loading states unless specifically testing them

## 6. Implementation Notes / Research
*   **Existing test patterns**: Reference `client/test/components/login.spec.ts` for public pages and `client/test/components/developer.spec.ts` for authenticated pages
*   **Authentication flow**:
    ```typescript
    await page.goto('/login');
    await page.getByRole('button', { name: 'Fill Credentials' }).click();
    await page.getByRole('button', { name: 'Sign In' }).click();
    await page.waitForURL('/dashboard', { timeout: 15000 });
    ```
*   **Pages to SKIP** (not meaningful for screenshot tests):
    *   `/oauth2/redirect` - Transient redirect handler
*   **Test organization**: Use org ID 1 (Acme Corporation from test data)
*   **Potential issues**:
    *   `/verify` page may require a valid token - capture the default/error state
    *   Admin pages require admin role - ensure test credentials have admin access
*   **File structure example**:
    ```
    client/test/
    ├── components/
    │   ├── register.spec.ts
    │   ├── dashboard.spec.ts
    │   └── ...
    └── resources/screenshots/
        ├── register/
        │   ├── 01-default-desktop-chrome.png
        │   └── 02-filled-desktop-chrome.png
        └── dashboard/
            └── 01-default-desktop-chrome.png
    ```
