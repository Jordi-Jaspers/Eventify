# Fix Profile Page Org Link Redirect

**Completed:** 2026-05-06
**Epic:** BUGFIXES
**Source:** `.opencode/refined/BUGFIXES-01-profile-org-link-redirect.md`

## Summary

Fixed broken organization links on profile page (used slug instead of ID) and marked `UserOrganizationResponse` fields as required in OpenAPI schema, eliminating `!` non-null assertions across the frontend.

## Plan Approved by the user:

### Requirements Summary

1. Backend: Change 5 `@Schema(requiredMode = NOT_REQUIRED)` → `REQUIRED` in `UserOrganizationResponse.java`
2. Frontend: Fix `OrganizationMembershipCard.svelte` link to use orgId + `/dashboard` + org switch on click
3. Frontend: Remove all `!` non-null assertions on `UserOrganizationResponse` fields
4. Regenerate API types via `bun run sync:api`

### Technical Approach

- Backend: Annotation-only change, no logic
- Frontend: Mirror existing `handleOrgSwitch` pattern from `AppSidebarUser`

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-backend-agent | Change 5 schema annotations |
| 2 | svelte-frontend-agent | Fix link, remove `!` assertions, regen types |

## Implementation

### Backend

- `UserOrganizationResponse.java`: 5× `NOT_REQUIRED` → `REQUIRED`

### Frontend

- `OrganizationMembershipCard.svelte`: proper org switch + navigate pattern
- `AppSidebarNav.svelte`: removed ~11 `!` assertions
- `AppSidebarUser.svelte`: removed 1 `!` assertion
- API types regenerated

### Deviations from Plan

- None

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-backend-agent | Schema annotations | Complete |
| svelte-frontend-agent | Link fix + assertion removal | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/organization/model/response/UserOrganizationResponse.java` - requiredMode REQUIRED
- `client/src/lib/components/profile/OrganizationMembershipCard.svelte` - fixed link with org switch
- `client/src/lib/components/layout/AppSidebarNav.svelte` - removed `!` assertions
- `client/src/lib/components/layout/AppSidebarUser.svelte` - removed `!` assertion
- `client/src/lib/types/api.d.ts` - regenerated (fields now required)

## Tests

- No new tests needed (annotation + link fix)
- Existing backend tests unaffected
- `bun run check` passes (type-safe without `!`)
