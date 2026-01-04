# Members List with DataTable Component

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-04

## 1. User Story
**As a** organization owner or admin
**I want** to view, filter, sort, and paginate the members list using the standard DataTable component
**So that** I can efficiently manage large teams with consistent UX across the application

## 2. Business Context & Value
The current members list uses a custom card-based layout that doesn't support filtering, sorting, or pagination. As organizations grow, this becomes unwieldy. By adopting the existing `DataTable` component (already used in Admin views), we provide:
- Consistent UX patterns across the application
- Scalable member management for large organizations
- Role-based default sorting (OWNER → ADMIN → MEMBER) for clearer hierarchy visibility

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: View paginated members list
    *   Given I am a member of an organization
    *   When I navigate to the organization members page
    *   Then I see members displayed in the DataTable component with pagination controls
    *   And the default sort is by role hierarchy (OWNER first, then ADMIN, then MEMBER)

*   [ ] **Scenario 2**: Filter members by name/email (debounced search)
    *   Given I am viewing the members list
    *   When I enter text in the search field
    *   Then the list updates to show only members matching the search term (name or email)

*   [ ] **Scenario 3**: Filter members by role (multi-select)
    *   Given I am viewing the members list
    *   When I select one or more roles from the role filter (OWNER, ADMIN, MEMBER)
    *   Then the list updates to show only members with those roles

*   [ ] **Scenario 4**: Sort members by column
    *   Given I am viewing the members list
    *   When I click on a sortable column header (Email, Role, Joined)
    *   Then the list is sorted by that column
    *   And clicking again reverses the sort direction

*   [ ] **Scenario 5**: Paginate through members
    *   Given there are more members than fit on one page
    *   When I click "Next" or "Previous" pagination buttons
    *   Then I navigate through the member pages
    *   And I see the current page range indicator (e.g., "Showing 1-10 of 50")

*   [ ] **Scenario 6**: Member actions remain functional
    *   Given I am an owner or admin viewing the members list
    *   When I click on member action buttons (update role, remove, transfer ownership)
    *   Then the existing action sheets/modals continue to work correctly

*   [ ] **Scenario 7**: Loading and empty states
    *   Given I am viewing the members list
    *   When data is loading, I see a loading skeleton
    *   And when there are no members, I see the empty state

*   [ ] **Scenario 8**: All quality checks pass
    *   Given the implementation is complete
    *   When I run `bun run check` and backend tests
    *   Then all checks and tests pass

## 4. Technical Requirements

### Backend Changes

*   **Create `OrganizationMembershipMetaData.java`**:
    *   Location: `server/src/main/java/io/github/eventify/api/organization/model/`
    *   Fields to support:
        *   `userEmail` - TEXT or FUZZY_TEXT
        *   `userFirstName` - FUZZY_TEXT
        *   `userLastName` - FUZZY_TEXT
        *   `role` - MULTI_ENUM (OWNER, ADMIN, MEMBER)
        *   `joinedAt` - DATE
        *   `search` - MULTI_COLUMN_FUZZY (email, firstName, lastName)

*   **API Endpoint**:
    *   Create new endpoint: `POST /v1/organization/{orgId}/members/search`
    *   Request: `SortablePageInput`
    *   Response: `PageResource<OrganizationMembershipResponse>`
    *   Must filter by organization ID automatically
    *   Add path constant `ORGANIZATION_MEMBERS_LIST_PATH` in `Paths.java`
    *   The existing `GET /v1/organization/{orgId}/members` endpoint remains unchanged for backward compatibility

*   **Service Layer**:
    *   Add method `searchOrganizationMembers(SortablePageInput input, Long orgId)` in `OrganizationMembershipService`
    *   Implement JFrame search integration for `OrganizationMembership` entity
    *   Default sort order when no sort specified: `role` with custom ordering (OWNER=1, ADMIN=2, MEMBER=3)

*   **Repository Layer**:
    *   Extend `OrganizationMembershipRepository` with JFrame `SearchableRepository` capabilities

*   **Security**: 
    *   Accessible by: org members, org admins, global admins
    *   Use existing `@PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")` pattern

*   **Tests**:
    *   Unit tests for metadata
    *   Integration tests for search endpoint
    *   Test search, sort, pagination, role filter

### Frontend Changes

*   **Add `searchMembers` to `OrganizationMembershipController.ts`**:
    ```typescript
    export async function searchMembers(
      orgId: number, 
      input: SortablePageInput
    ): Promise<PageResource<OrganizationMembershipResponse>>
    ```

*   **Create DataTable Service**:
    *   Organization ID comes from route params (`page.params.orgId`)
    *   Service needs to be recreated when orgId changes (use `$effect`)
    ```typescript
    const service = createDataTableService<OrganizationMembershipResponse>({
      fetchFn: (input) => searchMembers(orgId, input),
      pageSize: 10
    });
    ```

*   **Refactor Members Page** (`/organizations/[orgId]/members/+page.svelte`):
    *   Replace `MemberList.svelte` usage with `DataTable` component
    *   Keep add/remove/transfer sheet functionality
    *   Keep `OrganizationMembershipService.svelte.ts` for add/remove/transfer operations (DataTable service handles list/search only)

*   **Column Configuration**:
    ```typescript
    const columns: DataTableColumn<OrganizationMembershipResponse>[] = [
      {
        key: 'search',  // MULTI_COLUMN_FUZZY on backend
        label: 'Member',
        filterable: true,
        filterType: 'FUZZY_TEXT',
        filterPlaceholder: 'Search members...',
        colSpan: 3
      },
      {
        key: 'userEmail',
        label: 'Email',
        sortable: true,
        colSpan: 3
      },
      {
        key: 'role',
        label: 'Role',
        sortable: true,
        filterable: true,
        filterType: 'MULTI_ENUM',
        filterOptions: [
          { value: 'OWNER', label: 'Owner' },
          { value: 'ADMIN', label: 'Admin' },
          { value: 'MEMBER', label: 'Member' }
        ],
        colSpan: 2
      },
      {
        key: 'joinedAt',
        label: 'Joined',
        sortable: true,
        colSpan: 2
      },
      {
        key: 'actions',
        label: 'Actions',
        colSpan: 2
      }
    ];
    ```

*   **Performance**: Page size default 10, max 50. Response time < 300ms for 1000 members

## 5. Design & UI/UX
*   Maintain current glassmorphism card styling by wrapping DataTable
*   Use existing filter card pattern (separate card above main table card)
*   Role column should display colored badges (consistent with current MemberRow styling)
*   Actions column shows dropdown menu for owner/admin users:
    *   Update role dropdown
    *   Remove button
    *   Transfer ownership button (for owners only)
*   Empty state should use the existing "No members yet" design via DataTable's `empty` snippet
*   Loading skeleton displays during fetch

## 6. Implementation Notes / Research

### Reference Files
*   **DataTable Component**: `client/src/lib/components/data-table/DataTable.svelte`
*   **Column Types Definition**: `client/src/lib/components/data-table/types.ts`
*   **Reference Implementation**: `client/src/routes/(authenticated)/admin/organizations/+page.svelte`
*   **Existing Member Row Styling**: `client/src/lib/components/members/MemberRow.svelte` - extract into row snippet

### Backend Patterns
*   **JFrame Search**: The backend uses JFrame's `SortablePageInput` with `SearchInput` for filtering - see `UserService.searchUsers()` for pattern
*   **Role Sort Order**: Implement custom `SortOrder` or use database CASE expression: `CASE role WHEN 'OWNER' THEN 1 WHEN 'ADMIN' THEN 2 WHEN 'MEMBER' THEN 3 END`

### Files to Modify
*   `client/src/routes/(authenticated)/organizations/[orgId]/members/+page.svelte`
*   `client/src/lib/components/members/MemberList.svelte` (may be deprecated or simplified)
*   `client/src/lib/api/organization/OrganizationMembershipController.ts`
*   `client/src/lib/api/organization/OrganizationMembershipService.svelte.ts` (keep for mutations)
*   `server/src/main/java/io/github/eventify/api/organization/controller/OrganizationMembershipController.java`
*   `server/src/main/java/io/github/eventify/api/organization/service/OrganizationMembershipService.java`
*   `server/src/main/java/io/github/eventify/api/Paths.java`

### Potential Pitfalls
*   Ensure the new paginated endpoint doesn't break the existing `OrganizationMembershipService.svelte.ts` which uses the old array-based API - the old endpoint must remain
*   Service needs to be recreated when orgId changes in route params

### Dependencies
*   DataTable component (completed)
*   Organizations DataTable refactor (completed)
