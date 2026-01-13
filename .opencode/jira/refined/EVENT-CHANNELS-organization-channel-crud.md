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
    *   Then a new channel is created with organization_id set to my org
    *   And user_id is set to me (the creator, for audit purposes)

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
    *   Then a Bad request is returned

*   [ ] **Scenario 7**: Frontend shows org channels
    *   Given I am on an organization dashboard
    *   When the page loads
    *   Then I see the organization's channels
    *   And management actions are shown only if I am ADMIN or OWNER

*   [ ] **Scenario 8**: Empty state when no channels exist
    *   Given the organization has no channels
    *   When I view the organization dashboard
    *   Then I see a friendly message (e.g., "This organization doesn't have any channels yet")
    *   And a "Create Channel" button is visible only if I am OWNER or ADMIN


## 4. API Endpoints

| Method | Path | Auth |
|--------|------|------|
| POST | `/v1/organization/{orgId}/channels` | OWNER, ADMIN |
| POST | `/v1/organization/{orgId}/channels/search` | Any member |
| GET | `/v1/organization/{orgId}/channels/{id}` | Any member |
| PUT | `/v1/organization/{orgId}/channels/{id}` | OWNER, ADMIN |
| POST | `/v1/organization/{orgId}/channels/{id}/pause` | OWNER, ADMIN |
| POST | `/v1/organization/{orgId}/channels/{id}/resume` | OWNER, ADMIN |
| DELETE | `/v1/organization/{orgId}/channels/{id}` | OWNER, ADMIN |

## 5. Implementation Notes

### Backend
- **Controller**: `OrganizationChannelController` - follow `OrganizationApiKeyController` pattern
- **Service**: Add org methods to existing `ChannelService`
- **Authorization**: Use `@orgSecurity.isOwnerOrAdmin()` and `@orgSecurity.isMember()`
- **Reuse**: Same DTOs, mapper, validator as User Channel CRUD
- **Ownership**: `organization_id` = owning org, `user_id` = creator (audit)
- **Test data**: Add sample org channels via migration (use subselect for org ID)

### Frontend
- **Route**: `/organizations/[orgId]/channels`
- **API client**: `OrganizationChannelController.ts`
- **Page**: Copy from `/channels` page, add orgId param, add role-based visibility
- **Reuse**: Same `CreateChannelSheet`, `EditChannelSheet` components
- **Role check**: Hide action buttons when user is MEMBER (not OWNER/ADMIN)
- **Navigation**: Add "Channels" to org settings nav
- Should be available for a global admin to access via the admin organisations page

### Testing
- Controller integration tests: auth scenarios for each role (OWNER, ADMIN, MEMBER, non-member)
- Service unit tests: org channel CRUD operations
- Screenshot tests: list view, role-based button visibility

## 6. Reference Patterns
- **User Channel CRUD**: `UserChannelController`, `ChannelService`, `/channels` page
- **Org API Keys**: `OrganizationApiKeyController` (same auth pattern)
- **Org Members**: `OrganizationMembershipController` (same security checks)
