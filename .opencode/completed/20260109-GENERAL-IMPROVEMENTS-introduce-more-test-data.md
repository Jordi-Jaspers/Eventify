## [2026-01-09] - Introduce More Test Data

### Plan (approved)
Create comprehensive test data seeded into the development database to test and demonstrate all application features (memberships, API keys, quotas) without manual setup.

**Requirements:**
- Organization memberships with varied roles (OWNER, ADMIN, MEMBER)
- User-scoped API keys with varied expiration and usage
- Organization-scoped API keys for multiple orgs
- API key audit records for revoked keys
- User event quota records with varied usage levels

### Actual Changes

**Backend (Liquibase migrations only):**
- `202601091000-TST-membership-test-data.xml` - Organization memberships
  - Admin (id=1) as OWNER of orgs 1, 2, 3
  - Org 1 with 7 members (various roles)
  - Org 21 with only 1 member (owner)
  - Additional orgs with varied membership patterns

- `202601091001-TST-api-key-test-data.xml` - API keys
  - 4 USER-scoped keys for admin (expired, expiring, no expiry, never used)
  - 2 USER-scoped keys for other test users
  - 6 ORGANIZATION-scoped keys across orgs 1-4

- `202601091002-TST-api-key-audit-test-data.xml` - Revoked key audit
  - 8 audit records spread across last 30 days
  - Both USER and ORGANIZATION scoped keys
  - Varied revocation timestamps

- `202601091003-TST-user-quota-test-data.xml` - User event quotas
  - User 1: 85% usage (near limit)
  - User 2: 1% usage (minimal)
  - User 5: 100% usage (at limit)
  - User 6: 0% usage
  - Various others between 12-79%

**Frontend:** N/A
**UI Polish:** N/A (backend-only)
**Testing:** Existing integration tests pass, migrations apply successfully

### Agents Used
- None (orchestrator executed directly - simple Liquibase migrations)

### Files Modified
```
server/src/main/resources/db/changelog/changesets/
├── 202601091000-TST-membership-test-data.xml (new)
├── 202601091001-TST-api-key-test-data.xml (new)
├── 202601091002-TST-api-key-audit-test-data.xml (new)
└── 202601091003-TST-user-quota-test-data.xml (new)
```

### Quality Metrics
- ✅ Build: Successful
- ✅ Tests: All passing
- ✅ Migrations: Apply cleanly with context="tst"
- ✅ Acceptance criteria: All 5 scenarios covered
