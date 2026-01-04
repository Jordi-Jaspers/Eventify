## [2026-01-04] - Backlog Bug Fixes Batch

### Feature plan approved by user
**Bugs Fixed**

| # | Bug | Group | Scope |
|---|-----|-------|-------|
| 1 | Avatar doesn't fit in minimized sidebar | C | Frontend |
| 2 | User details not retrieved during login - using auth response | A | Backend |
| 3 | LEFT JOIN orgs during details retrieval | A | Backend |
| 4 | Global admin can't manage org users | B | Backend |
| 5 | User details page missing orgs & roles | A | Frontend |

**Grouping Strategy**

- **Group A (2+3+5):** User Details & Organizations - Backend + Frontend
- **Group B (4):** Global Admin Permissions - Backend only (refined first, tackled last)
- **Group C (1):** Avatar Sidebar Fix - Frontend only

**Implementation Order:** A → C → B

---

### Actual changelog after completion

#### Group A: User Details & Organizations

**Summary:** Include user's organizations in UserDetailsResponse and display on profile page.

**Backend Changes:**
- Added `organizations` field to `UserDetailsResponse` (List<UserOrganizationResponse>)
- Added `findByIdWithOrganizations()` query to `UserRepository` with LEFT JOIN FETCH
- Added `getUserWithOrganizations()` method to `UserService`
- Updated `UserDetailsMapper` to map OrganizationMembership → UserOrganizationResponse
- Updated `UserController.getUserDetails()` to fetch user with organizations

**Frontend Changes:**
- Added "Organizations" section to profile page after "Account Status"
- Each org shows name (clickable), role badge, and join date
- Empty state: "You are not a member of any organization"
- Role badge colors: OWNER=default, ADMIN=secondary, MEMBER=outline

**Testing:**
- 2 new tests in UserControllerTest
  - `shouldReturnEmptyOrganizationsListWhenUserHasNoOrganizations`
  - `shouldReturnUserDetailsWithOrganizationsWhenUserHasMemberships`
- All tests passing

#### Group C: Avatar Sidebar Fix

**Summary:** Fix avatar overflow in collapsed sidebar.

**Root Cause:** Avatar was `size-10` (40px) but collapsed sidebar button forced `size-8` (32px).

**Fix Applied:**
- Added responsive classes to avatar: `group-data-[collapsible=icon]:size-8`
- Added text scaling: `group-data-[collapsible=icon]:text-xs`
- Added smooth transition: `transition-all`

**File Modified:** `client/src/lib/components/layout/AppSidebarUser.svelte`

#### Group B: Global Admin Permissions

**Summary:** Allow global admins to manage org members without being org members.

**Problem:** Service layer required org membership even when controller auth allowed via `MANAGE_ORGANIZATIONS` permission.

**Fix Applied in `OrganizationMembershipService.java`:**
- `updateMemberRole()`: Check `caller.hasPermission(MANAGE_ORGANIZATIONS)` before requiring org membership
- `removeMember()`: Check `callerUser.hasPermission(MANAGE_ORGANIZATIONS)` before requiring org membership
- Follows existing pattern from `transferOwnership()` method

**Testing:**
- 2 new tests in OrganizationMembershipServiceTest
  - `shouldUpdateMemberRoleWhenGlobalAdminNotMemberOfOrganization`
  - `shouldRemoveMemberWhenGlobalAdminNotMemberOfOrganization`
- All 33 service tests passing

---

#### Agents Used
- java-testing-agent (test suite creation for Group A and B)
- java-backend-agent (implementation for Group A and B)
- sveltekit-frontend-agent (profile page update for Group A)
- Orchestrator (avatar fix for Group C - simple CSS change)

#### Files Modified

**Group A - Backend:**
- `server/src/main/java/.../user/model/response/UserDetailsResponse.java`
- `server/src/main/java/.../user/model/mapper/UserDetailsMapper.java`
- `server/src/main/java/.../user/repository/UserRepository.java`
- `server/src/main/java/.../user/service/UserService.java`
- `server/src/main/java/.../user/controller/UserController.java`
- `server/src/test/java/.../user/controller/UserControllerTest.java`
- `server/openapi.json`
- `client/src/lib/types/api.d.ts`

**Group A - Frontend:**
- `client/src/routes/(authenticated)/profile/+page.svelte`

**Group C - Frontend:**
- `client/src/lib/components/layout/AppSidebarUser.svelte`

**Group B - Backend:**
- `server/src/main/java/.../organization/service/OrganizationMembershipService.java`
- `server/src/test/java/.../organization/service/OrganizationMembershipServiceTest.java`

#### Quality Metrics
- Tests: 4 new tests written, all passing
- Coverage: Maintained >90% line coverage
- Build: Successful
- Type checks: 0 errors (bun run check)
- Quality checks: All passed (Spotless, Checkstyle, PMD, SpotBugs)

#### Commits Created
1. `feat: include user organizations in UserDetailsResponse` (Group A)
2. `fix: avatar overflow in collapsed sidebar` (Group C)
3. `fix: allow global admin to manage org members without membership` (Group B)
