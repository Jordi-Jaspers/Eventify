# Admin: Global Channel Overview (Backend + Frontend)

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-11

## 1. User Story
**As a** platform administrator
**I want** to view and manage all channels across the platform
**So that** I can provide support, monitor usage, and take action on problematic channels

## 2. Business Context & Value
Administrators need visibility into all channels for support tickets, abuse detection, and platform health monitoring. They should be able to see who owns what, filter by various criteria, and take administrative actions when necessary.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Admin views all channels
    *   Given I am a global admin
    *   When I navigate to the admin channels page
    *   Then I see a paginated list of all channels across all users and organizations

*   [ ] **Scenario 2**: Admin filters channels by scope
    *   Given I am viewing the admin channels list
    *   When I filter by scope "USER" or "ORGANIZATION"
    *   Then I see only channels matching that scope

*   [ ] **Scenario 3**: Admin filters channels by status
    *   Given I am viewing the admin channels list
    *   When I filter by status (ACTIVE, PAUSED, PENDING_DELETION)
    *   Then I see only channels matching that status

*   [ ] **Scenario 4**: Admin searches channels by owner
    *   Given I am viewing the admin channels list
    *   When I search by user email or organization name
    *   Then I see channels owned by matching users/organizations

*   [ ] **Scenario 5**: Admin deletes a channel
    *   Given I am viewing a channel in the admin list
    *   When I delete the channel
    *   Then it is marked as PENDING_DELETION
    *   And it will be cleaned up by the background job

*   [ ] **Scenario 6**: Admin views channel statistics
    *   Given I am on the admin dashboard or channels page
    *   When I view the statistics section
    *   Then I see: total channels, channels by scope, channels by status

## 4. Technical Requirements
*   **API Endpoints**:
    | Method | Path | Description |
    |--------|------|-------------|
    | GET | `/v1/admin/channels/search` | Search/list all channels with filters |
    | GET | `/v1/admin/channels/stats` | Get channel statistics |
    | DELETE | `/v1/admin/channels/{id}` | Admin delete channel |
*   **Search Parameters**: `scope`, `status`, `ownerEmail`, `organizationName`, `page`, `size`, `sort`
*   **Response DTO**: Include owner info (user email or org name) in list response
*   **Authorization**: Require global admin role (`Role.ADMIN`)

## 5. Design & UI/UX
*   **Admin Navigation**: Add "Channels" item to admin sidebar
*   **Page Layout**: Similar to existing Admin API Keys page
*   **DataTable Columns**: Name, Owner (User/Org), Scope, Status, Created, Actions
*   **Filters**: Dropdown for Scope, Status; Search input for owner
*   **Stats Cards**: Display key metrics at top of page

## 6. Implementation Notes / Research
*   **Controller**: `AdminChannelController`
*   **Follow patterns from**: `AdminApiKeyController`, `AdminUserController`
*   **Frontend Route**: `/admin/channels`
*   **JFrame Search**: Use existing search/pagination infrastructure
*   **Permission**: Check for `Permission.ADMIN_ACCESS` or equivalent
