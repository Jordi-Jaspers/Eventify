# Remove TRIAL Organization Status

**Completed:** 2026-05-15
**Epic:** ORGMGMT
**Source:** .opencode/refined/ORGMGMT-03-remove-trial-status.md

## Summary

Removed TRIAL from OrganizationStatus enum. New organizations now default to ACTIVE. Existing TRIAL orgs backfilled via migration.

## Plan Approved by the user:

Remove TRIAL enum value, change default to ACTIVE, add Liquibase migration, update all tests and frontend references.

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Update existing tests (TRIAL→ACTIVE/SUSPENDED) |
| 2.2 | spring-backend-agent | Remove TRIAL from enum/entity + add migration |
| 3.2 | svelte-frontend-agent | Remove TRIAL from UI |

## Implementation

### Backend
- Removed `TRIAL` from `OrganizationStatus` enum
- Changed `Organization` constructor default to `ACTIVE`
- Migration backfills existing TRIAL→ACTIVE

### Frontend
- Removed TRIAL filter option from admin organizations page
- Removed TRIAL case from badge utility
- Updated CreateOrganizationSheet copy
- Removed TRIAL from api.d.ts type

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Update tests TRIAL→ACTIVE | Complete |
| spring-backend-agent | Remove enum + migration | Complete |
| svelte-frontend-agent | Remove TRIAL from UI | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/organization/model/OrganizationStatus.java` - removed TRIAL
- `server/src/main/java/io/github/eventify/api/organization/model/Organization.java` - default→ACTIVE
- `server/src/main/resources/db/changelog/changesets/202605121000-PRD-backfill-organization-status-active.xml` - new migration
- `server/src/test/java/io/github/eventify/api/admin/controller/AdminOrganizationControllerTest.java` - TRIAL→ACTIVE
- `server/src/test/java/io/github/eventify/api/organization/service/OrganizationServiceTest.java` - TRIAL→ACTIVE
- `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` - removed filter
- `client/src/lib/utils/organization.ts` - removed case
- `client/src/lib/components/admin/CreateOrganizationSheet.svelte` - updated copy
- `client/src/lib/types/api.d.ts` - removed from union type

## Tests

- 11 backend tests updated, all passing
