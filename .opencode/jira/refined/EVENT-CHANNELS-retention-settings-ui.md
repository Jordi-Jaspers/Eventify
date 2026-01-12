# User/Organization Retention Settings UI

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-12

## 1. User Story
**As a** user or organization owner/admin
**I want** to configure how long my event data is retained
**So that** I can balance storage costs with data availability based on my needs

## 2. Business Context & Value
Event data accumulates over time and storage is not infinite. Users and organizations need control over their data retention policy. This self-service feature:
- Reduces support requests for data management
- Enables compliance with data governance requirements
- Helps users understand the lifecycle of their data
- Provides transparency about when data will be automatically deleted

The retention period applies to **all channels** owned by the user or organization. Per-channel retention override is a future enhancement.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: User views their retention setting
    *   Given I am on the "Data & Storage" tab in my settings
    *   When the page loads
    *   Then I see my current retention period displayed in human-readable format
    *   And I see a slider with discrete options

*   [ ] **Scenario 2**: User changes retention period
    *   Given I am on the "Data & Storage" tab
    *   When I move the slider to a new value (e.g., "1 year")
    *   Then the selected value is displayed
    *   And a "Save" button becomes enabled

*   [ ] **Scenario 3**: User reduces retention period - warning shown
    *   Given my current retention is 1 year (365 days)
    *   When I slide to 90 days
    *   Then a warning message is displayed: "Reducing retention will permanently delete events older than 90 days. This action cannot be undone."
    *   And the warning is visible until I save or revert

*   [ ] **Scenario 4**: User saves retention setting
    *   Given I have selected a new retention value
    *   When I click "Save"
    *   Then a confirmation modal appears if I reduced the retention period
    *   And after confirming, the setting is saved immediately
    *   And a success toast is shown

*   [ ] **Scenario 5**: Organization owner/admin views retention setting
    *   Given I am an OWNER or ADMIN of an organization
    *   When I navigate to the organization's "Data & Storage" tab
    *   Then I see the organization's current retention period
    *   And I can modify it

*   [ ] **Scenario 6**: Organization member cannot see Data & Storage tab
    *   Given I am a MEMBER (not OWNER/ADMIN) of an organization
    *   When I view the organization settings navigation
    *   Then I do not see the "Data & Storage" tab

*   [ ] **Scenario 7**: Slider has discrete snap points
    *   Given I am interacting with the retention slider
    *   Then it snaps to these values only:
    *   - 90 days (3 months) - minimum
    *   - 180 days (6 months)
    *   - 365 days (1 year)
    *   - 730 days (2 years)
    *   - 1095 days (3 years)
    *   - 1825 days (5 years) - maximum

*   [ ] **Scenario 8**: Human-readable display
    *   Given a retention value is set
    *   Then it displays as: "3 months", "6 months", "1 year", "2 years", "3 years", or "5 years"

## 4. Technical Requirements
*   **API Endpoints**:
    | Method | Path | Description |
    |--------|------|-------------|
    | PUT | `/v1/user/settings/retention` | Update user retention days |
    | GET | `/v1/user/settings/retention` | Get user retention days |
    | PUT | `/v1/organization/{orgId}/settings/retention` | Update org retention days |
    | GET | `/v1/organization/{orgId}/settings/retention` | Get org retention days |

*   **Request DTO**: `UpdateRetentionRequest`
    ```json
    { "retentionDays": 365 }
    ```

*   **Response DTO**: `RetentionSettingsResponse`
    ```json
    { "retentionDays": 365 }
    ```

*   **Validation**:
    *   `retentionDays` must be one of: 90, 180, 365, 730, 1095, 1825
    *   Backend validates against allowed values (not just range)

*   **Authorization**:
    *   User endpoint: Authenticated user, updates their own setting
    *   Org endpoint: OWNER or ADMIN role required

*   **Database**: No schema changes - columns already exist with CHECK constraints

## 5. Design & UI/UX
*   **New Tab**: Add "Data & Storage" tab to:
    *   User settings navigation (after "Developer")
    *   Organization settings navigation (after "API Keys") - hidden for MEMBER role
*   **Tab Icon**: Use `Database` or `HardDrive` icon from Lucide
*   **Page Layout**:
    *   Card with title "Data Retention"
    *   Description: "Configure how long events are stored before automatic deletion"
    *   Discrete slider with snap points
    *   Current value displayed prominently (e.g., "1 year")
    *   Warning alert (yellow/amber) when reducing retention
    *   Save button (disabled until value changes)
*   **Slider Design**:
    *   Show tick marks at each snap point
    *   Labels below: "3mo", "6mo", "1y", "2y", "3y", "5y"
*   **Confirmation Modal** (when reducing retention):
    *   Title: "Reduce Data Retention?"
    *   Body: "Events older than [X] will be permanently deleted. This cannot be undone."
    *   Actions: "Cancel" / "Confirm & Save"

## 6. Implementation Notes / Research
*   **Backend**:
    *   Add new controller methods to `UserController` or create `UserSettingsController`
    *   Add new controller `OrganizationSettingsController` or extend `OrganizationController`
    *   Reuse existing `UserService.updateUserDetails()` pattern or add dedicated method
    *   Validation: Create custom validator for allowed retention values

*   **Frontend**:
    *   **Slider component**: Install shadcn-svelte Slider component (`npx shadcn-svelte@next add slider`)
    *   **User settings route**: Create `/profile/data-storage/+page.svelte`
    *   **Org settings route**: Create `/organizations/[orgId]/settings/data-storage/+page.svelte`
    *   **Navigation updates**:
        *   `SettingsNav.svelte`: Add "Data & Storage" tab
        *   `OrgSettingsNav.svelte`: Add "Data & Storage" tab (conditionally hidden for MEMBER)
    *   **Routes config**: Add new routes to `CLIENT_ROUTES` in `routes.ts`

*   **Retention value mapping**:
    ```typescript
    const RETENTION_OPTIONS = [
      { days: 90, label: "3 months" },
      { days: 180, label: "6 months" },
      { days: 365, label: "1 year" },
      { days: 730, label: "2 years" },
      { days: 1095, label: "3 years" },
      { days: 1825, label: "5 years" }
    ];
    ```

*   **Existing code references**:
    *   `User.java` line 104-107: `retentionDays` field
    *   `Organization.java` line 81-84: `retentionDays` field
    *   `SettingsNav.svelte`: User settings navigation
    *   `OrgSettingsNav.svelte`: Organization settings navigation
    *   `UpdateUserDetailsRequest.java`: Pattern for update request DTO

*   **Testing**:
    *   Backend: Unit tests for validation, integration tests for endpoints
    *   Frontend: Component tests for slider behavior, warning display
