# Global Admin Organization Listing

**Epic**: Multi-Tenant User & Organization Management
**Status**: Completed
**Completed Date**: 2025-12-29
**Last Updated**: 2025-12-30 (Refactored to Jframe PageResource pattern)
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
- Jframe `PageResource<OrganizationResponse>` response (NOT custom PageResponse)
- `OrganisationMetaData` component for search/sort field mapping
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
- Extended `OrganizationResponse` with `memberCount` field (implements `PageableItemResource`)
- `OrganizationMapper` extends `PageMapper<OrganizationResponse, Organization>`
- Created `OrganisationMetaData` component for search field configuration
- Extended `OrganizationRepository` with `JpaSpecificationExecutor`
- Added `searchOrganizations()` to `OrganizationService` using `JpaSearchSpecification`
- Added `POST /admin/organizations/search` endpoint returning `PageResource<OrganizationResponse>`

Phase 3: Frontend Implementation
- Created listing page with search, filters, table, pagination
- Added generic `PageResource<T>` interface to models.ts
- Added `searchOrganizations()` to OrganizationController
- Updated routes.ts with ADMIN_ORGANIZATIONS_PAGE
- Added "View All" link to sidebar navigation

---

## Actual changelog after completion

### Summary
Built searchable, paginated organization listing page for global admins using Jframe's `PageResource` pattern with unified `OrganizationResponse` DTO.

### Changes

**Backend:**
- Added `MANAGE_ORGANIZATIONS` permission to `Permission.java` and `Role.ADMIN`
- Extended `OrganizationResponse.java` with `memberCount` field, implements `PageableItemResource`
- Created `OrganisationMetaData.java` component for search/sort field mapping (name, status, memberCount)
- `OrganizationMapper` now extends `PageMapper<OrganizationResponse, Organization>` (provides `toPageResource()`)
- Extended `OrganizationRepository` with `JpaSpecificationExecutor<Organization>`
- Added `countMembersByOrganizationId()` to repository
- Added `searchOrganizations(SortablePageInput)` to `OrganizationService` using `JpaSearchSpecification`
- Added `POST /admin/organizations/search` endpoint returning `PageResource<OrganizationResponse>`
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
  - Generic `PageResource<T>` interface
  - `PageResourceOrganizationResponse` type alias
  - `SortablePageInput`, `SearchInput`, `SortableColumn` (Jframe types)
- Added `searchOrganizations()` to `OrganizationController.ts` using POST with SortablePageInput
- Added `ADMIN_ORGANIZATIONS_PAGE` route to `routes.ts`
- Added "View All" link to sidebar navigation in `AppSidebarNav.svelte`

**Testing:**
- 9 integration tests in `AdminOrganizationControllerTest`
- Tests cover: listing, pagination, search, filter, combined, member count, empty results, 401, 403
- Tests use Jframe's `PageResource` (not custom `PageResponse`)
- Empty result assertion handles `null` content (Jframe returns null, not empty list)

### Agents Used
- java-testing-agent (test suite creation)
- java-backend-agent (backend implementation)
- sveltekit-frontend-agent (frontend implementation)

### Files Created
- `server/src/main/java/io/github/eventify/api/organization/model/OrganisationMetaData.java`
- `client/src/routes/(authenticated)/admin/organizations/+page.svelte`

### Files Modified
- `server/src/main/java/io/github/eventify/api/Paths.java` (added ADMIN_ORGANIZATIONS_SEARCH_PATH)
- `server/src/main/java/io/github/eventify/api/auth/model/Permission.java`
- `server/src/main/java/io/github/eventify/api/auth/model/Role.java`
- `server/src/main/java/io/github/eventify/api/organization/model/response/OrganizationResponse.java` (added memberCount, implements PageableItemResource)
- `server/src/main/java/io/github/eventify/api/organization/model/mapper/OrganizationMapper.java` (extends PageMapper)
- `server/src/main/java/io/github/eventify/api/organization/repository/OrganizationRepository.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java`
- `server/src/main/java/io/github/eventify/api/organization/controller/AdminOrganizationController.java`
- `server/src/test/java/io/github/eventify/api/organization/controller/AdminOrganizationControllerTest.java`
- `client/src/lib/api/models.ts`
- `client/src/lib/api/organization/OrganizationController.ts`
- `client/src/lib/config/routes.ts`
- `client/src/lib/components/layout/AppSidebarNav.svelte`

### Files Deleted (Refactored Away)
- `server/src/main/java/io/github/eventify/api/organization/model/response/OrganizationListResponse.java` (merged into OrganizationResponse)
- `server/src/main/java/io/github/eventify/api/organization/model/mapper/OrganizationListMapper.java` (merged into OrganizationMapper)
- `server/src/main/java/io/github/eventify/common/model/PageResponse.java` (replaced by Jframe's PageResource)

### Quality Metrics
- Tests: 9 list/search tests, all 260+ tests passing
- Build: Successful
- `bun run check`: 0 errors, 0 warnings
- Responsive: Mobile and desktop layouts

### Notes
- Uses Jframe `PageResource` pattern (NOT custom `PageResponse`)
- `OrganizationResponse` is unified DTO for both single org and list responses
- `memberCount` field populated via repository query for list, 0 for single org operations
- `OrganisationMetaData` defines searchable/sortable fields with `SearchType` configuration
- Frontend handles `null` content from `PageResource` (Jframe returns null, not empty list for 0 results)
- Uses direct fetch in frontend (OpenAPI types regenerated when backend runs)
- Sortable columns supported by `SortablePageInput` but UI deferred as future enhancement
- Foundation ready for org detail page and actions (suspend, activate, etc.)

### API Request/Response

**Request:**
```
POST /admin/organizations/search
Content-Type: application/json
Authorization: Bearer <token>

{
  "pageNumber": 0,
  "pageSize": 10,
  "searchInputs": [
    {"fieldName": "name", "textValue": "tech", "searchType": "TEXT"},
    {"fieldName": "status", "textValue": "ACTIVE", "searchType": "ENUM"}
  ],
  "sortableColumns": [
    {"fieldName": "name", "direction": "ASC"}
  ]
}
```

**Response:**
```json
{
  "totalElements": 25,
  "totalPages": 3,
  "pageSize": 10,
  "pageNumber": 0,
  "content": [
    {
      "id": 1,
      "name": "Tech Corp",
      "slug": "tech-corp",
      "status": "ACTIVE",
      "memberCount": 15,
      "createdAt": "2025-01-15T10:30:00Z",
      "owner": {
        "id": 42,
        "firstName": "John",
        "lastName": "Doe"
      }
    }
  ]
}
```

**Empty Results Response:**
```json
{
  "totalElements": 0,
  "totalPages": 0,
  "pageSize": 10,
  "pageNumber": 0,
  "content": null
}
```

### Jframe Pattern Reference

This feature follows the Jframe Search & Pagination pattern documented in `.opencode/agent/subagents/java-backend-agent.md`:

1. **MetaData Component** (`OrganisationMetaData`) - Maps field names to entity properties and search types
2. **Mapper** (`OrganizationMapper`) - Extends `PageMapper<Response, Entity>` for automatic page conversion
3. **Service** - Uses `JpaSearchSpecification` with metadata for dynamic filtering
4. **Controller** - Returns `PageResource<Response>` (from Jframe, not custom class)
5. **Response DTO** - Implements `PageableItemResource` interface
