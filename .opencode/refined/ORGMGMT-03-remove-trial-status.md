---
epic: "ORGMGMT"
title: "Remove TRIAL Organization Status"
estimate: S
status: ready
created: 2026-05-14
depends_on: []
labels: [backend, frontend, database, refactoring]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform administrator\
**I want** the organization lifecycle simplified to ACTIVE/SUSPENDED only\
**So that** the status model reflects reality (admin-provisioned orgs don't need a trial phase)\

## 2. Business Context & Value
Organizations are created by admins after sales conversations — there is no self-service signup that warrants a trial period. TRIAL status is never enforced and adds confusion. Removing it simplifies the data model and prepares for a future enterprise billing story.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Existing TRIAL orgs migrated to ACTIVE
    * Given organizations exist with status TRIAL in the database
    * When the migration runs
    * Then all TRIAL organizations have status ACTIVE
* [ ] **Scenario 2**: New orgs default to ACTIVE
    * Given an admin creates a new organization
    * When the org is persisted
    * Then its status is ACTIVE (not TRIAL)
* [ ] **Scenario 3**: TRIAL removed from enum and API
    * Given the OrganizationStatus enum
    * When a client sends or filters by "TRIAL"
    * Then it is rejected as an invalid value
* [ ] **Scenario 4**: Frontend removes TRIAL references
    * Given the admin organizations list page
    * When viewing status filters and badges
    * Then TRIAL is not an option — only ACTIVE and SUSPENDED exist
* [ ] **Scenario 5**: Create org sheet copy updated
    * Given the CreateOrganizationSheet
    * When displayed
    * Then it states the org will be created as ACTIVE (not TRIAL)

## 4. Technical Requirements
* **API Changes**: N/A — no new endpoints. Existing search/create endpoints now only accept ACTIVE/SUSPENDED.
* **Database**: Liquibase migration — `UPDATE organization SET status = 'ACTIVE' WHERE status = 'TRIAL'`; optionally add CHECK constraint limiting status to ACTIVE/SUSPENDED.
* **Security**: N/A
* **Performance**: N/A — single UPDATE on small table

## 5. Design & UI/UX
- Remove "Trial" option from status filter dropdown on admin org list
- Remove TRIAL case from badge utility (only ACTIVE/SUSPENDED remain)
- Update CreateOrganizationSheet copy: "...set the status to ACTIVE"

## 6. Implementation Notes
### Backend
- `server/src/main/java/io/github/eventify/api/organization/model/OrganizationStatus.java` — remove `TRIAL` enum value
- `server/src/main/java/io/github/eventify/api/organization/model/Organization.java` — change default from `TRIAL` to `ACTIVE`
- Liquibase changeset: migrate data + optional CHECK constraint

### Frontend
- `client/src/lib/types/api.d.ts` — regenerate (auto from OpenAPI)
- `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` — remove TRIAL filter option
- `client/src/lib/utils/organization.ts` — remove TRIAL case
- `client/src/lib/components/admin/CreateOrganizationSheet.svelte` — update copy

## 7. Test Impact Analysis
### Existing tests affected by this change:
| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `AdminOrganizationControllerTest.java` | Line 71 assertion | New org status is TRIAL | YES | Update to assert ACTIVE |
| `AdminOrganizationControllerTest.java` | Status filter tests (lines 623-710) | Filter by TRIAL returns results | YES | Replace TRIAL with ACTIVE in test data/assertions |
| `AdminOrganizationControllerTest.java` | `createOrganizationWithStatus("...", TRIAL)` | Test fixture setup | YES | Change to ACTIVE |
| `OrganizationServiceTest.java` | "Should default status to TRIAL" (line 239) | Default is TRIAL | YES | Update to assert ACTIVE |
| `OrganizationServiceTest.java` | All `setStatus(TRIAL)` fixtures | Test data setup | YES | Change to ACTIVE |

### Test modification policy:
- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified:
  - `server/src/test/java/.../AdminOrganizationControllerTest.java`
  - `server/src/test/java/.../OrganizationServiceTest.java`

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/src/main/java/.../model/OrganizationStatus.java` | Remove TRIAL value |
| `server/src/main/java/.../model/Organization.java` | Default to ACTIVE |
| `server/src/resources/db/changelog/...` | New migration changeset |
| `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` | Remove TRIAL filter |
| `client/src/lib/utils/organization.ts` | Remove TRIAL case |
| `client/src/lib/components/admin/CreateOrganizationSheet.svelte` | Update copy |
