# Global Admin Organization Listing

**Epic**: Multi-Tenant User & Organization Management
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2024-12-23

## 1. User Story
**As a** global admin
**I want** to view a searchable, paginated list of all organizations
**So that** I can efficiently administer the platform and monitor tenant activity

## 2. Business Context & Value
Global administrators need visibility into all organizations on the platform to perform administrative tasks, troubleshoot issues, and understand platform growth. This is the foundation for all org management capabilities.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: View organization listing
    *   Given I am logged in as a global admin with `MANAGE_ORGANIZATIONS` permission
    *   When I navigate to the admin organizations page
    *   Then I see a table displaying organizations with columns: Name, Slug, Status, Member Count, Created Date

*   [ ] **Scenario 2**: Search organizations by name
    *   Given I am on the organizations listing page
    *   When I enter a search term in the search field
    *   Then the table filters to show only organizations whose name contains the search term (case-insensitive)

*   [ ] **Scenario 3**: Filter organizations by status
    *   Given I am on the organizations listing page
    *   When I select a status filter (TRIAL, ACTIVE, SUSPENDED, or All)
    *   Then the table shows only organizations matching the selected status

*   [ ] **Scenario 4**: Paginate through results
    *   Given there are more organizations than the page size (default: 10)
    *   When I navigate to the next page
    *   Then I see the next set of organizations
    *   And pagination controls show current page and total pages

*   [ ] **Scenario 5**: Unauthorized access
    *   Given I am logged in as a user without `MANAGE_ORGANIZATIONS` permission
    *   When I attempt to access the admin organizations endpoint
    *   Then I receive a 403 Forbidden response

## 4. Technical Requirements
*   **API Changes**:
    *   `GET /admin/organizations` — List organizations with pagination, search, and filter
        *   Query params: `page` (default 0), `size` (default 10), `search` (optional), `status` (optional)
        *   Response: `Page<OrganizationListResponse>` containing `id`, `name`, `slug`, `status`, `memberCount`, `createdAt`
*   **Database**:
    *   Add query methods to `OrganizationRepository`:
        *   `findAllWithMemberCount(Pageable, String search, OrganizationStatus status)`
    *   Consider adding `organization_membership` table first (Story 3 dependency) OR return `memberCount: 0` until memberships exist
*   **Security**:
    *   New permission: `MANAGE_ORGANIZATIONS` added to `Permission` enum
    *   Add `MANAGE_ORGANIZATIONS` to `Role.ADMIN` permission set
    *   Endpoint protected with `@PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")`
*   **Performance**:
    *   Paginated query, no full table scans
    *   Response time < 200ms for up to 10,000 organizations

## 5. Design & UI/UX
*   Admin sidebar navigation item: "Organizations" under Admin section
*   Table with sortable columns (future enhancement, not MVP)
*   Search input with debounce (300ms)
*   Status filter dropdown
*   Pagination controls at bottom of table
*   Empty state: "No organizations found" with prompt to create one
*   Loading skeleton while fetching data

## 6. Implementation Notes / Research
*   **Existing Code**:
    *   `AdminOrganizationController` at `server/src/main/java/io/github/eventify/api/organization/controller/AdminOrganizationController.java` — extend this
    *   `OrganizationRepository` at `server/src/main/java/io/github/eventify/api/organization/repository/OrganizationRepository.java` — add pagination methods
    *   `Paths.java` already has `ADMIN_ORGANIZATIONS_PATH = "/admin/organizations"`
*   **New Files Needed**:
    *   `OrganizationListResponse.java` — DTO with member count
    *   `client/src/routes/(authenticated)/admin/organizations/+page.svelte` — listing page
*   **Dependencies**:
    *   Story 3 (Organization Membership) creates the `organization_membership` table needed for `memberCount`
    *   Can be developed in parallel if `memberCount` returns 0 initially
*   **Patterns to Follow**:
    *   Follow `UserManagementController` pattern for admin endpoints
    *   Use Spring Data `Pageable` for pagination
