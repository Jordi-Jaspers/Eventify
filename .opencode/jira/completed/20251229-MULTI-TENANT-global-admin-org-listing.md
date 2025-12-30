# Global Admin Organization Listing

**Epic**: Multi-Tenant User & Organization Management
**Status**: Completed
**Completed Date**: 2025-12-29
**Estimate**: M

## Feature plan approved by user

### Requirements Summary

- Global admins can view searchable, paginated list of all organizations
- Table columns: Name, Slug, Status, Member Count, Created Date
- Search by name (case-insensitive) with 300ms debounce
- Filter by status: TRIAL, ACTIVE, SUSPENDED, or All
- Pagination with Previous/Next controls
- Protected by `MANAGE_ORGANIZATIONS` permission

### Technical Approach

**Backend:**
- `POST /admin/organizations/search` with Jframe `SortablePageInput` body
- JPA Specifications for dynamic filtering (name, status fields)
- `PageResponse<OrganizationListResponse>` wrapper
- `MANAGE_ORGANIZATIONS` permission added to Role.ADMIN

**Frontend:**
- Organizations listing page at `/admin/organizations`
- Glassmorphism card design with responsive table
- Status filter button group
- Search input with debounce
- Pagination controls
- Loading skeleton, empty, and error states

### Implementation Workflow

Phase 1: Backend Tests
- 9 integration tests in `AdminOrganizationControllerTest`
- Covers pagination, search, filter, combined filters, member count, edge cases, auth

Phase 2: Backend Implementation
- Added `MANAGE_ORGANIZATIONS` permission
- Created `OrganizationListResponse` DTO
- Added `OrganizationListMapper` for entity-to-DTO
- Extended `OrganizationRepository` with `JpaSpecificationExecutor`
- Added `searchOrganizations()` to `OrganizationService` using `SortablePageInput`
- Added `POST /admin/organizations/search` endpoint to `AdminOrganizationController`
- Created `PageResponse<T>` generic wrapper
- Added `ADMIN_ORGANIZATIONS_SEARCH_PATH` to `Paths.java`

Phase 3: Frontend Implementation
- Created listing page with search, filters, table, pagination
- Added types to models.ts
- Added `searchOrganizations()` to OrganizationController
- Updated routes.ts with ADMIN_ORGANIZATIONS_PAGE
- Added "View All" link to sidebar navigation

---

## Actual changelog after completion

### Summary
Built searchable, paginated organization listing page for global admins with full CRUD foundation.

### Changes

**Backend:**
- Added `MANAGE_ORGANIZATIONS` permission to `Permission.java` and `Role.ADMIN`
- Created `OrganizationListResponse.java` DTO (id, name, slug, status, memberCount, createdAt)
- Created `PageResponse.java` generic pagination wrapper
- Created `OrganizationListMapper.java` for entity-to-DTO mapping
- Extended `OrganizationRepository` with `JpaSpecificationExecutor<Organization>`
- Added `countMembersByOrganizationId()` to repository
- Added `searchOrganizations(SortablePageInput)` to `OrganizationService` with JPA Specifications
- Added `POST /admin/organizations/search` endpoint to `AdminOrganizationController`
- Added `ADMIN_ORGANIZATIONS_SEARCH_PATH` constant to `Paths.java`

**Frontend:**
- Created `client/src/routes/(authenticated)/admin/organizations/+page.svelte`
  - Responsive table layout (mobile stacks, desktop grid)
  - Search input with 300ms debounce
  - Status filter button group (All, Trial, Active, Suspended)
  - Pagination controls (Previous/Next)
  - Loading skeleton, empty state, error state with retry
  - Glassmorphism card design
- Added types to `client/src/lib/api/models.ts`:
  - `OrganizationStatus`, `OrganizationListResponse`, `PageResponseOrganizationListResponse`
  - `SortablePageInput`, `SearchInput`, `SortableColumn` (Jframe types)
- Added `searchOrganizations()` to `OrganizationController.ts` using POST with SortablePageInput
- Added `ADMIN_ORGANIZATIONS_PAGE` route to `routes.ts`
- Added "View All" link to sidebar navigation in `AppSidebarNav.svelte`

**Testing:**
- 9 integration tests in `AdminOrganizationControllerTest`
- Tests cover: listing, pagination, search, filter, combined, member count, empty results, 401, 403
- All 26 organization-related tests pass

### Agents Used
- java-testing-agent (test suite creation)
- java-backend-agent (backend implementation)
- sveltekit-frontend-agent (frontend implementation)

### Files Created
- `server/src/main/java/io/github/eventify/api/organization/model/response/OrganizationListResponse.java`
- `server/src/main/java/io/github/eventify/api/organization/model/mapper/OrganizationListMapper.java`
- `server/src/main/java/io/github/eventify/common/model/PageResponse.java`
- `client/src/routes/(authenticated)/admin/organizations/+page.svelte`

### Files Modified
- `server/src/main/java/io/github/eventify/api/Paths.java` (added ADMIN_ORGANIZATIONS_SEARCH_PATH)
- `server/src/main/java/io/github/eventify/api/auth/model/Permission.java`
- `server/src/main/java/io/github/eventify/api/auth/model/Role.java`
- `server/src/main/java/io/github/eventify/api/organization/repository/OrganizationRepository.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java`
- `server/src/main/java/io/github/eventify/api/organization/controller/AdminOrganizationController.java`
- `server/src/test/java/io/github/eventify/api/organization/controller/AdminOrganizationControllerTest.java`
- `client/src/lib/api/models.ts`
- `client/src/lib/api/organization/OrganizationController.ts`
- `client/src/lib/config/routes.ts`
- `client/src/lib/components/layout/AppSidebarNav.svelte`

### Quality Metrics
- Tests: 9 list/search tests, all 260+ tests passing
- Build: Successful
- `bun run check`: 0 errors, 0 warnings
- Responsive: Mobile and desktop layouts

### Notes
- Uses Jframe `SortablePageInput` pattern for consistency with other search endpoints
- Uses direct fetch in frontend (OpenAPI types regenerated when backend runs)
- Member count currently returns actual count from organization_membership table
- Sortable columns supported by `SortablePageInput` but UI deferred as future enhancement
- Foundation ready for org detail page and actions (suspend, activate, etc.)

### API Migration
Changed from:
```
GET /admin/organizations?page=0&size=10&search=tech&status=ACTIVE
```

To Jframe pattern:
```
POST /admin/organizations/search
Content-Type: application/json

{
  "pageNumber": 0,
  "pageSize": 10,
  "searchInputs": [
    {"fieldName": "name", "textValue": "tech"},
    {"fieldName": "status", "textValue": "ACTIVE"}
  ]
}
```
