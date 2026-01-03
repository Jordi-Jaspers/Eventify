# Story: Members Table with DataTable Component

## Epic: UI Components

## Summary
Integrate the members list with the new DataTable component to enable server-side search, sort, and pagination for organization members.

## Current State
- Members page uses `MemberList.svelte` with a simple list (no search/sort/pagination)
- Backend endpoint `GET /v1/organization/{orgId}/members` returns full list (not paginated)
- `OrganizationMembershipService.svelte.ts` manages list + add/remove/transfer operations
- DataTable component exists and works with `SortablePageInput`/`PageResource<T>` pattern

## Requirements

### Backend Changes

1. **Create `OrganizationMembershipMetaData.java`**
   - Location: `server/src/main/java/io/github/eventify/api/organization/model/`
   - Fields to support:
     - `userEmail` - TEXT or FUZZY_TEXT
     - `userFirstName` - FUZZY_TEXT  
     - `userLastName` - FUZZY_TEXT
     - `role` - MULTI_ENUM (OWNER, ADMIN, MEMBER)
     - `joinedAt` - DATE
     - `search` - MULTI_COLUMN_FUZZY (email, firstName, lastName)

2. **Create search endpoint**
   - `POST /v1/organization/{orgId}/members/search`
   - Request: `SortablePageInput`
   - Response: `PageResource<OrganizationMembershipResponse>`
   - Must filter by organization ID
   - Accessible by: org members, org admins, global admins

3. **Tests**
   - Unit tests for metadata
   - Integration tests for search endpoint
   - Test search, sort, pagination, role filter

### Frontend Changes

1. **Add `searchMembers` to `OrganizationMembershipController.ts`**
   ```typescript
   export async function searchMembers(
     orgId: number, 
     input: SortablePageInput
   ): Promise<PageResource<OrganizationMembershipResponse>>
   ```

2. **Refactor members page to use DataTable**
   - Replace `MemberList.svelte` usage with `DataTable`
   - Keep add/remove/transfer sheet functionality
   - Column configuration:
     - Member (name + avatar) - FUZZY_TEXT search
     - Email
     - Role - MULTI_ENUM filter
     - Joined - DATE, sortable
     - Actions

3. **Keep `OrganizationMembershipService.svelte.ts`**
   - Still needed for add/remove/transfer operations
   - DataTable service handles list/search only

4. **Row actions**
   - Update role dropdown
   - Remove button
   - Transfer ownership button (for owners)

## Column Definition (Reference)

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

## Technical Notes

- Organization ID comes from route params (`page.params.orgId`)
- Search function needs to be created dynamically with orgId:
  ```typescript
  const service = createDataTableService<OrganizationMembershipResponse>({
    fetchFn: (input) => searchMembers(orgId, input),
    pageSize: 10
  });
  ```
- Service needs to be recreated when orgId changes (use `$effect`)

## Acceptance Criteria

- [ ] Backend search endpoint created with tests
- [ ] Members table uses DataTable component
- [ ] Search by name/email works (debounced)
- [ ] Filter by role works (multi-select pills)
- [ ] Sort by email, role, joined date works
- [ ] Pagination works
- [ ] Add member sheet still works
- [ ] Remove member sheet still works
- [ ] Transfer ownership sheet still works
- [ ] Update role dropdown still works
- [ ] Loading skeleton displays during fetch
- [ ] Empty state displays when no members
- [ ] `bun run check` passes
- [ ] All tests pass

## Estimated Effort
- Backend: 1-2 hours
- Frontend: 1 hour
- Testing: 30 minutes

## Dependencies
- DataTable component (completed)
- Organizations DataTable refactor (completed)

## Related Files
- `client/src/lib/components/data-table/` (reference)
- `client/src/routes/(authenticated)/admin/organizations/+page.svelte` (reference)
- `client/src/routes/(authenticated)/organizations/[orgId]/members/+page.svelte`
- `client/src/lib/components/members/MemberList.svelte`
- `client/src/lib/api/organization/OrganizationMembershipController.ts`
- `client/src/lib/api/organization/OrganizationMembershipService.svelte.ts`
- `server/src/main/java/io/github/eventify/api/organization/controller/OrganizationMembershipController.java`
