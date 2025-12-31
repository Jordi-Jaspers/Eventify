# Organization Members UI

**Epic**: Multi-Tenant User & Organization Management
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2024-12-30
**Depends On**: Organization Membership Management (completed), Org Switcher Frontend

## 1. User Story
**As an** organization owner or admin
**I want** a UI to view and manage my organization's members
**So that** I can add team members, update roles, and remove members visually

## 2. Business Context & Value
The backend for organization membership management is complete. This story adds the frontend UI to make these capabilities accessible to users through the web application.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: View member list
    *   Given I am a member of an organization
    *   When I navigate to the organization members page
    *   Then I see a table with all members showing: avatar, name, email, role, joined date

*   [ ] **Scenario 2**: Add member (Owner/Admin only)
    *   Given I am an OWNER or ADMIN of the organization
    *   When I click "Add Member" and search for a user by email
    *   Then I can select a user from search results
    *   And assign them a role (ADMIN or MEMBER)
    *   And they appear in the member list after saving

*   [ ] **Scenario 3**: Update member role (Owner/Admin only)
    *   Given I am an OWNER or ADMIN
    *   When I click on a member's role dropdown
    *   Then I can change their role (with restrictions per backend rules)
    *   And the change is reflected immediately

*   [ ] **Scenario 4**: Remove member (Owner/Admin only)
    *   Given I am an OWNER or ADMIN
    *   When I click remove on a member (not the owner)
    *   Then a confirmation dialog appears
    *   And after confirming, the member is removed from the list

*   [ ] **Scenario 5**: Transfer ownership (Owner only)
    *   Given I am the OWNER of the organization
    *   When I access the "Transfer Ownership" action
    *   Then I can select another member to become the new owner
    *   And after confirmation, they become OWNER and I become ADMIN

*   [ ] **Scenario 6**: Permission-based UI
    *   Given I am a MEMBER (not admin/owner)
    *   When I view the members page
    *   Then I do NOT see add/remove/edit controls
    *   And I can only view the member list

## 4. Technical Requirements
*   **Frontend Routes**:
    *   `/organizations/[orgId]/members` - Member management page
    *   Or integrate as tab in organization settings
*   **API Integration** (endpoints already exist):
    *   `GET /v1/organizations/{orgId}/members` - List members
    *   `GET /v1/organizations/{orgId}/members/search?query=` - Search users to add
    *   `POST /v1/organizations/{orgId}/members` - Add member
    *   `PATCH /v1/organizations/{orgId}/members/{userId}` - Update role
    *   `DELETE /v1/organizations/{orgId}/members/{userId}` - Remove member
    *   `POST /v1/organizations/{orgId}/transfer-ownership` - Transfer ownership
*   **Components Needed**:
    *   Member list table with role badges
    *   Add member modal with user search (reuse `UserSearchCombobox`)
    *   Role dropdown (inline editing)
    *   Remove confirmation dialog
    *   Transfer ownership modal with extra confirmation
*   **State Management**:
    *   Current user's role in org (to show/hide admin controls)
    *   Member list with optimistic updates

## 5. Design & UI/UX
*   **Layout**: Card with glassmorphism styling matching existing admin pages
*   **Member Table Columns**:
    *   Avatar (initials or image)
    *   Name (first + last)
    *   Email
    *   Role (badge: Owner=purple, Admin=blue, Member=gray)
    *   Joined date (relative, e.g., "2 days ago")
    *   Actions (dropdown: Change Role, Remove, Transfer Ownership)
*   **Add Member Modal**:
    *   User search input (debounced, min 3 chars)
    *   Role selector (ADMIN/MEMBER radio or dropdown)
    *   Add button
*   **Transfer Ownership**:
    *   Separate section or modal
    *   Warning text about consequences
    *   Type confirmation (e.g., "transfer" to confirm)
*   **Empty State**: "No members yet. Add your first team member."
*   **Loading States**: Skeleton loaders for table rows

## 6. Implementation Notes / Research
*   **Reusable Components**:
    *   `UserSearchCombobox` - already exists from user search story
    *   `Badge` - for role display
    *   `DropdownMenu` - for actions
    *   `AlertDialog` - for confirmations
*   **Permission Logic**:
    *   Check current user's membership role from member list response
    *   Hide admin actions if role === 'MEMBER'
    *   Hide transfer ownership if role !== 'OWNER'
*   **Error Handling**:
    *   Toast notifications for success/error
    *   Handle 403 gracefully (permissions changed mid-session)
*   **Depends On**:
    *   Org switcher to know which org is currently selected
    *   Or pass orgId via URL params

## 7. API Response Reference

```typescript
// GET /v1/organizations/{orgId}/members
interface OrganizationMembershipResponse {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
  createdAt: string; // ISO date
}

// POST /v1/organizations/{orgId}/members
interface AddMemberRequest {
  email: string;
  role: 'ADMIN' | 'MEMBER';
}

// PATCH /v1/organizations/{orgId}/members/{userId}
interface UpdateMemberRoleRequest {
  role: 'ADMIN' | 'MEMBER';
}

// POST /v1/organizations/{orgId}/transfer-ownership
interface TransferOwnershipRequest {
  newOwnerUserId: number;
}
```
