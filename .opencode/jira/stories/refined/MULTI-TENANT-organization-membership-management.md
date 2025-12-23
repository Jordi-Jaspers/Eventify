# Organization Membership Management

**Epic**: Multi-Tenant User & Organization Management
**Status**: Ready for Dev
**Estimate**: L
**Created Date**: 2024-12-23

## 1. User Story
**As an** organization owner or admin
**I want** to add existing platform users to my organization and manage their roles
**So that** I can build my team and delegate responsibilities

## 2. Business Context & Value
Organizations need to onboard team members to collaborate. By allowing owners and admins to add existing users, we enable organic team growth while maintaining control over who has access to organizational resources.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Add member to organization
    *   Given I am an OWNER or ADMIN of an organization
    *   When I add an existing user by email with role MEMBER
    *   Then an organization membership is created
    *   And the user appears in the organization's member list

*   [ ] **Scenario 2**: Search users to add
    *   Given I am adding a member to my organization
    *   When I type at least 3 characters of an email
    *   Then I see matching users who are not already members of this organization

*   [ ] **Scenario 3**: Update member role
    *   Given I am an OWNER of an organization
    *   When I change a member's role from MEMBER to ADMIN
    *   Then the membership role is updated
    *   And the user's permissions within the organization change accordingly

*   [ ] **Scenario 4**: Role update restrictions
    *   Given I am an ADMIN of an organization
    *   When I attempt to change another member's role to OWNER
    *   Then I receive a 403 Forbidden (only OWNER can assign OWNER)
    *   And when I attempt to modify the OWNER's role
    *   Then I receive a 403 Forbidden

*   [ ] **Scenario 5**: Remove member from organization
    *   Given I am an OWNER or ADMIN of an organization
    *   When I remove a member (not the OWNER)
    *   Then the membership is deleted
    *   And the user no longer appears in the member list

*   [ ] **Scenario 6**: Cannot remove owner
    *   Given I am an OWNER of an organization
    *   When I attempt to remove myself (the only OWNER)
    *   Then I receive a 400 Bad Request with message "Cannot remove the organization owner. Transfer ownership first."

*   [ ] **Scenario 7**: Transfer ownership
    *   Given I am the OWNER of an organization
    *   When I transfer ownership to another ADMIN or MEMBER
    *   Then that user becomes the new OWNER
    *   And my role changes to ADMIN

*   [ ] **Scenario 8**: List organization members
    *   Given I am a member of an organization
    *   When I request the member list
    *   Then I see all members with their roles, names, and join dates

*   [ ] **Scenario 9**: User belongs to multiple organizations
    *   Given a user is already a member of Organization A
    *   When an admin of Organization B adds them
    *   Then the user now belongs to both organizations
    *   And can switch between them using the org switcher

## 4. Technical Requirements
*   **API Changes**:
    *   `POST /organizations/{orgId}/members` — Add member
        *   Request: `{ "email": "user@example.com", "role": "MEMBER" }`
        *   Response: `OrganizationMembershipResponse`
    *   `GET /organizations/{orgId}/members` — List members
        *   Response: `List<OrganizationMembershipResponse>`
    *   `PATCH /organizations/{orgId}/members/{userId}` — Update role
        *   Request: `{ "role": "ADMIN" }`
    *   `DELETE /organizations/{orgId}/members/{userId}` — Remove member
    *   `POST /organizations/{orgId}/transfer-ownership` — Transfer ownership
        *   Request: `{ "newOwnerUserId": 123 }`
*   **Database**:
    *   Create `organization_membership` table:
        ```sql
        CREATE TABLE organization_membership (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
            organization_id INTEGER NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
            role TEXT NOT NULL,  -- OWNER, ADMIN, MEMBER
            invited_by INTEGER REFERENCES "user"(id),
            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
            UNIQUE(user_id, organization_id)
        );
        CREATE INDEX idx_org_membership_org ON organization_membership(organization_id);
        CREATE INDEX idx_org_membership_user ON organization_membership(user_id);
        ```
*   **Security**:
    *   All endpoints require user to be OWNER or ADMIN of the organization (use `@RequireOrgRole` from Story 4)
    *   OWNER-only actions: transfer ownership, remove other admins, promote to OWNER
*   **Performance**:
    *   Member list query < 100ms for organizations with up to 1,000 members

## 5. Design & UI/UX
*   Organization settings page with "Members" tab
*   Member list table: Avatar, Name, Email, Role, Joined Date, Actions
*   "Add Member" button opens modal with:
    *   Email search input (autocomplete)
    *   Role dropdown (ADMIN, MEMBER — OWNER only via transfer)
*   Role dropdown in table row for inline role changes (OWNER sees all options, ADMIN restricted)
*   Remove button with confirmation dialog
*   Transfer ownership in separate section/modal for safety

## 6. Implementation Notes / Research
*   **New Files Needed**:
    *   `OrganizationMembership.java` — entity
    *   `OrganizationMembershipRepository.java`
    *   `OrganizationalRole.java` — enum (OWNER, ADMIN, MEMBER)
    *   `OrganizationMembershipService.java`
    *   `OrganizationMembershipController.java`
    *   `OrganizationMembershipResponse.java`
    *   `AddMemberRequest.java`, `UpdateMemberRoleRequest.java`, `TransferOwnershipRequest.java`
*   **Frontend Pages**:
    *   `client/src/routes/(authenticated)/organizations/[orgId]/members/+page.svelte`
    *   Or integrate into org settings page
*   **Validation Rules**:
    *   Cannot add user who is already a member
    *   Cannot add non-existent or disabled users
    *   Must have exactly one OWNER at all times
*   **Patterns to Follow**:
    *   Follow existing user management patterns in `UserManagementController`
