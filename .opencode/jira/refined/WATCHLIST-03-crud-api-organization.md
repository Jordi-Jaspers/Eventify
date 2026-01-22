# Watchlist CRUD API (Organization)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-01-entity-database-schema.md, WATCHLIST-02-crud-api-user.md

## 1. User Story
**As an** organization member
**I want** to create, read, update, and delete organization watchlists via API
**So that** my team can manage shared groups of channels for monitoring

## 2. Business Context & Value
Organizations need shared watchlists that all members can view and monitor. This enables teams to have a consistent view of their infrastructure status. CRUD operations are restricted to OWNER/ADMIN roles, while viewing is available to all members.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Create an organization watchlist
    *   Given an authenticated user with OWNER or ADMIN role
    *   When they POST to `/v1/organization/{orgId}/watchlists` with a valid payload
    *   Then a new watchlist is created for the organization
    *   And the response contains the created watchlist details

*   [ ] **Scenario 2**: Member cannot create watchlist
    *   Given an authenticated user with MEMBER role
    *   When they POST to create a watchlist
    *   Then a 403 Forbidden error is returned

*   [ ] **Scenario 3**: Search organization watchlists
    *   Given an authenticated organization member
    *   When they POST to `/v1/organization/{orgId}/watchlists/search`
    *   Then they receive a paginated list of the organization's watchlists

*   [ ] **Scenario 4**: Get single organization watchlist
    *   Given an authenticated organization member
    *   When they GET `/v1/organization/{orgId}/watchlists/{id}`
    *   Then they receive the full watchlist details including channels

*   [ ] **Scenario 5**: Update organization watchlist
    *   Given an authenticated user with OWNER or ADMIN role
    *   When they PUT to `/v1/organization/{orgId}/watchlists/{id}`
    *   Then the watchlist is updated

*   [ ] **Scenario 6**: Member cannot update watchlist
    *   Given an authenticated user with MEMBER role
    *   When they PUT to update a watchlist
    *   Then a 403 Forbidden error is returned

*   [ ] **Scenario 7**: Delete organization watchlist
    *   Given an authenticated user with OWNER or ADMIN role
    *   When they DELETE `/v1/organization/{orgId}/watchlists/{id}`
    *   Then the watchlist is permanently deleted

*   [ ] **Scenario 8**: Access watchlist from another organization fails
    *   Given organization A has a watchlist
    *   When a member of organization B tries to access it
    *   Then a 404 Not Found is returned

*   [ ] **Scenario 9**: Add personal channel to org watchlist fails
    *   Given a user creating an organization watchlist
    *   When they include a personal channel ID
    *   Then a 400 Bad Request is returned (strict separation)

*   [ ] **Scenario 10**: Add channel from different org fails
    *   Given a user creating a watchlist for organization A
    *   When they include a channel from organization B
    *   Then a 400 Bad Request is returned

*   [ ] **Scenario 11**: Global admin can manage any org watchlist
    *   Given a user with MANAGE_ORGANIZATIONS authority
    *   When they perform CRUD operations on any org watchlist
    *   Then the operations succeed

## 4. Technical Requirements

### API Endpoints

| Method | Path | Description | Authorization |
|--------|------|-------------|---------------|
| POST | `/v1/organization/{orgId}/watchlists` | Create watchlist | OWNER, ADMIN |
| POST | `/v1/organization/{orgId}/watchlists/search` | Search watchlists | Any member |
| GET | `/v1/organization/{orgId}/watchlists/{id}` | Get watchlist | Any member |
| PUT | `/v1/organization/{orgId}/watchlists/{id}` | Update watchlist | OWNER, ADMIN |
| DELETE | `/v1/organization/{orgId}/watchlists/{id}` | Delete watchlist | OWNER, ADMIN |

### Request/Response Models
- Reuses `CreateWatchlistRequest`, `UpdateWatchlistRequest` from WATCHLIST-02
- Reuses `WatchlistDetailsResponse`, `WatchlistSearchResponse` from WATCHLIST-02
- Response includes `organizationId` field

### Path Constants
Add to `Paths.java`:
```java
public static final String ORGANIZATION_WATCHLISTS_PATH = ORGANIZATION_PATH + WATCHLISTS_PART;
public static final String ORGANIZATION_WATCHLISTS_SEARCH_PATH = ORGANIZATION_WATCHLISTS_PATH + SEARCH_PART;
public static final String ORGANIZATION_WATCHLIST_PATH = ORGANIZATION_WATCHLISTS_PATH + ID_PART;
```

### Database
- Uses entities from WATCHLIST-01
- Organization watchlists have `organization_id` set

### Security
- CRUD operations: `@PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")`
- Read operations: `@PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")`
- Channel validation ensures only channels from the same organization can be added

### Performance
- Same as user watchlists

## 5. Design & UI/UX
- N/A (API only)

## 6. Implementation Notes / Research

### File Locations
- Controller: `server/src/main/java/io/github/eventify/api/watchlist/controller/OrganizationWatchlistController.java`
- Service: `server/src/main/java/io/github/eventify/api/watchlist/service/OrganizationWatchlistService.java`
  - Or extend `WatchlistService` with organization-specific methods

### Patterns to Follow
- Follow `OrganizationChannelController` for controller structure and security annotations
- Follow `OrganizationChannelService` for service patterns

### Reuse Strategy
The service layer can share logic with user watchlists:
- Create a base `WatchlistService` with common logic
- Or use a single service with methods that accept optional `organizationId`

### Validation Rules
Same as user watchlists, plus:
- All channels must belong to the specified organization
- User must be a member of the organization
- For CRUD: User must have OWNER or ADMIN role

### Test Cases
- Create org watchlist as OWNER
- Create org watchlist as ADMIN
- Create org watchlist as MEMBER (should fail)
- Search org watchlists as any member
- Add personal channel (should fail)
- Add channel from different org (should fail)
- Cross-organization access (should fail)
- Global admin access
