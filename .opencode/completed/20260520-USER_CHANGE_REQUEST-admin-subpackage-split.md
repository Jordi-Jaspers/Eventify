# Admin Sub-Package Split

**Completed:** 2026-05-20
**Epic:** USER_CHANGE_REQUEST
**Source:** ad-hoc request

## Summary

Split monolithic `api.admin` package into 5 focused sub-packages for better maintainability and navigation.

## Plan Approved by the user:

Split `io.github.eventify.api.admin` into sub-packages by domain:
- `admin.audit` — audit log controller, service, models, mappers
- `admin.apikeys` — API key admin controller, service, models, mapper
- `admin.stats` — dashboard controller, stats services, repositories, models, projections, validator
- `admin.users` — user controller, request models
- `admin.organizations` — organization controller

### Requirements Summary

- No behavior changes — pure structural refactoring
- All tests must continue passing
- External imports updated where needed

### Technical Approach

- Backend only (no frontend changes)
- Move files to new sub-packages, update package declarations and imports
- `SearchInputHelper` moved to `admin.apikeys` (only consumer)
- `DailyGrowthData` projection import updated in 3 external repositories

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | deep-research-agent | Research audit file dependencies |
| 2 | spring-backend-agent | Move audit sub-package |
| 3 | deep-research-agent | Research apikeys file dependencies |
| 4 | spring-backend-agent | Move apikeys sub-package |
| 5 | spring-backend-agent | Move stats sub-package |
| 6 | spring-backend-agent | Move users + organizations sub-packages |

## Implementation

### Backend

- 5 sub-packages created under `api.admin`
- ~55 source files + 13 test files moved
- External imports updated in: `AuditLogRepository`, `OrganizationRepository`, `UserRepository`, `EventRepository`, `OrganizationMembershipValidator`

### Deviations from Plan

- `SearchInputHelper` logic was inlined into `AdminAuditLogMetaData` (audit package) since it was package-private; the helper file itself moved to `apikeys` package for `AdminApiKeyMetaData`

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent | Audit file dependencies | Complete |
| spring-backend-agent | Move audit sub-package | Complete |
| deep-research-agent | ApiKeys file dependencies | Complete |
| spring-backend-agent | Move apikeys sub-package | Complete |
| spring-backend-agent | Move stats sub-package | Complete |
| spring-backend-agent | Move users + organizations | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/admin/` — all files reorganized into sub-packages
- `server/src/test/java/io/github/eventify/api/admin/` — all test files reorganized
- `common/audit/repository/AuditLogRepository.java` — import update
- `api/organization/repository/OrganizationRepository.java` — import update
- `api/user/repository/UserRepository.java` — import update
- `api/event/repository/EventRepository.java` — import update
- `api/organization/model/validator/OrganizationMembershipValidator.java` — import update

## Tests

- 13 test files moved, all passing
- No new tests needed (pure refactoring)
