# Organization Channel CRUD (Backend + Frontend)

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: L (Large)
**Created Date**: 2026-01-11

## 1. User Story
**As an** organization owner or admin
**I want** to create, view, update, pause, and delete organization channels
**So that** my team can share event streams across the organization

## 2. Business Context & Value
Organizations need shared channels that all members can access. This enables teams to collectively monitor production systems, share event data, and collaborate on debugging. Only admins/owners should manage channels, but all members can view them.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Org owner/admin creates an organization channel
    *   Given I am an OWNER or ADMIN of an organization
    *   When I create a channel for that organization
    *   Then a new channel is created with scope ORGANIZATION
    *   And organization_id is set to my org

*   [ ] **Scenario 2**: Org member views organization channels
    *   Given I am a MEMBER of an organization
    *   When I view the organization's channel list
    *   Then I see all channels for that organization
    *   But I cannot create, edit, or delete channels

*   [ ] **Scenario 3**: Org admin updates a channel
    *   Given I am an ADMIN of an organization
    *   When I update an organization channel
    *   Then the channel is updated successfully

*   [ ] **Scenario 4**: Org owner deletes a channel
    *   Given I am an OWNER of an organization
    *   When I delete an organization channel
    *   Then the channel status becomes PENDING_DELETION

*   [ ] **Scenario 5**: Regular member cannot manage channels
    *   Given I am a MEMBER (not ADMIN or OWNER) of an organization
    *   When I try to create, update, or delete a channel
    *   Then I receive a 403 Forbidden error

*   [ ] **Scenario 6**: Channel names unique within organization
    *   Given an organization has a channel named "Production"
    *   When someone tries to create another "Production" channel in the same org
    *   Then a 409 Conflict error is returned

*   [ ] **Scenario 7**: Frontend shows org channels
    *   Given I am on an organization dashboard
    *   When the page loads
    *   Then I see the organization's channels
    *   And management actions are shown only if I am ADMIN or OWNER

## 4. Technical Requirements
*   **API Endpoints**:
    | Method | Path | Description |
    |--------|------|-------------|
    | POST | `/v1/organization/{orgId}/channels` | Create org channel |
    | GET | `/v1/organization/{orgId}/channels` | List org channels (paginated) |
    | GET | `/v1/organization/{orgId}/channels/{id}` | Get channel details |
    | PUT | `/v1/organization/{orgId}/channels/{id}` | Update channel |
    | POST | `/v1/organization/{orgId}/channels/{id}/pause` | Pause channel |
    | POST | `/v1/organization/{orgId}/channels/{id}/resume` | Resume channel |
    | DELETE | `/v1/organization/{orgId}/channels/{id}` | Delete channel |
*   **Authorization**:
    *   All endpoints: User must be member of organization
    *   Read: MEMBER, ADMIN, OWNER
    *   Write (create/update/pause/resume/delete): ADMIN, OWNER only
*   **Reuse**: Same DTOs and service methods as User Channel CRUD where possible

## 5. Design & UI/UX
*   **Organization Dashboard**: Add "Channels" section similar to user dashboard
*   **Role-based UI**: Hide create/edit/delete buttons for MEMBER role
*   **Navigation**: Consider adding "Channels" to org sidebar settings
*   **Same component patterns**: Reuse channel list/form components from user channels

## 6. Implementation Notes / Research
*   **Controller**: `OrganizationChannelController`
*   **Authorization**: Use existing `OrganizationRole` enum and membership checks
*   **Reference**: See `OrganizationApiKeyController` for similar pattern
*   **Frontend Route**: `/organizations/[orgId]/channels` or section on org dashboard
*   **Shared Service**: `ChannelService` handles both user and org channels with scope parameter
