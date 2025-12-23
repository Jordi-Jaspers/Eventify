# Organization Provisioning with Owner Assignment

**Epic**: Multi-Tenant User & Organization Management
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2024-12-23

## 1. User Story
**As a** global admin
**I want** to create a new organization and assign an owner in a single operation
**So that** the organization has immediate leadership and can begin onboarding members

## 2. Business Context & Value
Organizations must have an owner from the moment of creation to ensure accountability and enable immediate self-service management. This prevents orphaned organizations and establishes the tenant hierarchy from day one.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Create organization with owner
    *   Given I am logged in as a global admin with `PROVISION_ORGANIZATIONS` permission
    *   When I submit a request with organization name and owner email
    *   Then a new organization is created with status TRIAL
    *   And an organization membership is created with role OWNER for the specified user
    *   And the response includes the organization details and owner information

*   [ ] **Scenario 2**: Owner must be existing user
    *   Given I submit an organization creation request
    *   When the specified owner email does not match an existing, enabled user
    *   Then I receive a 400 Bad Request with message "Owner must be an existing, active user"

*   [ ] **Scenario 3**: Owner email is required
    *   Given I submit an organization creation request
    *   When the owner email is missing or empty
    *   Then I receive a 400 Bad Request with validation error

*   [ ] **Scenario 4**: Owner receives notification (future enhancement)
    *   Given an organization is successfully created
    *   When the owner is assigned
    *   Then the owner receives an email notification with organization details
    *   *(Note: Email notification is a future enhancement, not MVP)*

*   [ ] **Scenario 5**: Duplicate slug handling
    *   Given an organization with name "Acme Corp" already exists (slug: acme-corp)
    *   When I create another organization with name "Acme Corp"
    *   Then the new organization is created with slug "acme-corp-1"

## 4. Technical Requirements
*   **API Changes**:
    *   Modify `POST /admin/organizations` request body:
        ```json
        {
          "name": "Acme Corporation",
          "ownerEmail": "john@example.com"
        }
        ```
    *   Response includes owner details:
        ```json
        {
          "id": 1,
          "name": "Acme Corporation",
          "slug": "acme-corporation",
          "status": "TRIAL",
          "createdAt": "2024-12-23T10:00:00Z",
          "owner": {
            "id": 5,
            "email": "john@example.com",
            "firstName": "John",
            "lastName": "Doe"
          }
        }
        ```
*   **Database**:
    *   Create `organization_membership` table (see Story 3 for full schema, but OWNER record is created here)
    *   Columns needed: `id`, `user_id`, `organization_id`, `role`, `created_at`, `invited_by`
*   **Security**:
    *   Existing `PROVISION_ORGANIZATIONS` permission is sufficient
*   **Performance**:
    *   Single transaction: create org + create membership
    *   Response time < 300ms

## 5. Design & UI/UX
*   Modify existing create organization form at `/admin/organizations/new`
*   Add "Owner Email" field with:
    *   Email input with validation
    *   Autocomplete/search for existing users (type-ahead, min 3 chars)
    *   Show user name when valid email is found
    *   Error state if user not found or not active
*   Form fields: Organization Name, Owner Email
*   Success: Toast notification + redirect to organizations list

## 6. Implementation Notes / Research
*   **Existing Code to Modify**:
    *   `ProvisionOrganizationRequest.java` — add `ownerEmail` field
    *   `OrganizationService.create()` — add owner lookup and membership creation
    *   `AdminOrganizationController.provisionOrganization()` — no changes needed if service handles it
    *   `client/src/routes/(authenticated)/admin/organizations/new/+page.svelte` — add owner field
*   **New Files Needed**:
    *   `OrganizationMembership.java` — entity (can be created in Story 3, but OWNER insert happens here)
    *   `OrganizationMembershipRepository.java`
    *   `OrganizationalRole.java` — enum with OWNER, ADMIN, MEMBER
*   **Transaction Boundary**:
    *   Both org creation and membership creation must be in same transaction
    *   If membership creation fails, rollback org creation
*   **Validation**:
    *   Owner email must be non-empty, valid email format
    *   Owner must exist in `user` table with `enabled = true`
