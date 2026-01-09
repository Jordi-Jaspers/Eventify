## [2026-01-09] - Admin API Key Dashboard

### Plan (approved)
Implement a comprehensive admin dashboard for platform administrators to view and manage all API keys across the platform. Includes platform-wide statistics, searchable DataTable with filters, revoke functionality with audit trail, and recent revocations section.

### Actual Changes

**Backend Endpoints Created:**
- `GET /api/v1/admin/api-keys/stats` - Platform-wide API key statistics
- `POST /api/v1/admin/api-keys/search` - Search all API keys with JFrame filters
- `DELETE /api/v1/admin/api-keys/{keyId}` - Revoke any API key (creates audit record)
- `POST /api/v1/admin/api-keys/audit/search` - Search revocation audit log

**Backend Components:**
- `AdminApiKeyController.java` - 4 REST endpoints with ADMIN role requirement
- `AdminApiKeyService.java` - Business logic for stats aggregation, search, revoke
- `AdminApiKeyMetaData.java` - JFrame search field definitions (FUZZY_TEXT, ENUM, MULTI_ENUM filters)
- `AdminApiKeyStatsResponse.java` - 8 statistics + top key info
- `AdminApiKeyResponse.java` - Key details with owner info
- `AdminApiKeyAuditResponse.java` - Revocation audit entry
- `AdminApiKeyMapper.java` - MapStruct mapper for DTOs

**Repository Enhancements:**
- Added `countUserKeys()`, `countOrganizationKeys()`, `countCreatedSince()`, `countNeverUsed()`, `countExpiringBetween()` to `ApiKeyRepository.java`

**Frontend Created:**
- `/admin/api-keys` - Main dashboard page with:
  - 8 statistics cards (Total Keys, User Keys, Org Keys, Created This Month, Never Used, Expiring Soon, Revoked This Month, Top Key)
  - DataTable with Scope (USER/ORGANIZATION) and Status (Active/Expired) filters
  - Search by key name, owner name, or email
  - Revoke action with AlertDialog confirmation
  - Recent Revocations section showing audit trail
- `AdminApiKeyController.ts` - Frontend API functions

**Navigation:**
- Added "API Keys" link to admin sidebar (visible for ADMIN role)

### Agents Used
| Agent                   | Task |
|-------------------------|------|
| java-testing-agent      | Created 40 backend tests (25 integration + 15 unit) |
| java-backend-agent      | Implemented all backend endpoints and services |
| sveltekit-frontend-agent| Built dashboard page with DataTable and stats |
| ui-validator            | Polished UI with glassmorphism, typography, spacing |

### Files Created
```
server/src/main/java/io/github/eventify/api/admin/
├── controller/AdminApiKeyController.java
├── service/AdminApiKeyService.java
├── model/AdminApiKeyMetaData.java
└── model/
    ├── response/AdminApiKeyStatsResponse.java
    ├── response/AdminApiKeyResponse.java
    ├── response/AdminApiKeyAuditResponse.java
    └── mapper/AdminApiKeyMapper.java

server/src/test/java/io/github/eventify/api/admin/
├── controller/AdminApiKeyControllerTest.java (25 tests)
└── service/AdminApiKeyServiceTest.java (15 tests)

client/src/routes/(authenticated)/admin/api-keys/+page.svelte
client/src/lib/api/admin/AdminApiKeyController.ts
client/test/components/admin-api-keys.spec.ts
client/test/resources/screenshots/admin-api-keys/*.png
```

### Files Modified
```
server/src/main/java/io/github/eventify/api/Paths.java
server/src/main/java/io/github/eventify/api/apikey/repository/ApiKeyRepository.java
server/src/test/java/io/github/eventify/support/IntegrationTest.java
server/src/test/java/io/github/eventify/support/util/TestContextInitializer.java
client/src/lib/components/layout/AppSidebarNav.svelte
```

### Quality Metrics
- Backend Tests: 40/40 passing
- Build: Successful (`./gradlew build`)
- Frontend Check: 0 errors (`bun run check`)
- Screenshots: Captured via Playwright tests

### Acceptance Criteria Completed
- [x] Admin sees platform-wide API key statistics (8 stat cards)
- [x] Admin views all API keys in paginated DataTable
- [x] Admin filters keys by scope (USER/ORGANIZATION)
- [x] Admin filters keys by status (Active/Expired)
- [x] Admin searches for keys by name, owner, email
- [x] Admin revokes any key with confirmation dialog
- [x] Audit record created with admin user ID on revoke
- [x] Admin views revocation audit log (Recent Revocations section)
