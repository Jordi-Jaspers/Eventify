# Admin API Key Dashboard

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: L (Large)
**Created Date**: 2026-01-06

## 1. User Story
**As a** platform administrator
**I want** a comprehensive dashboard to view and manage all API keys across the platform
**So that** I can monitor usage, support users/organizations, and revoke keys when necessary

## 2. Business Context & Value
Platform administrators need visibility into API key usage across all users and organizations for:
- **Support**: Helping users troubleshoot integration issues
- **Security**: Identifying suspicious activity or revoking compromised keys
- **Monitoring**: Understanding platform usage patterns
- **Compliance**: Audit trail of key lifecycle events

This dashboard provides the oversight tools necessary for effective platform management.

## 3. Acceptance Criteria

### Statistics Section
*   [ ] **Scenario 1**: Admin sees platform-wide API key statistics
    *   Given I am a global admin on the API Keys dashboard
    *   When the page loads
    *   Then I see summary cards showing:
        *   Total active keys (split: personal vs organization)
        *   Keys created this week/month
        *   Keys revoked this week/month
        *   Most active keys (by request count)

*   [ ] **Scenario 2**: Admin sees usage trends
    *   Given I am viewing the dashboard
    *   When I look at the statistics section
    *   Then I see a visual indicator of key creation trends
    *   And I see keys approaching expiration (next 30 days)

### All Keys Table
*   [ ] **Scenario 3**: Admin views all API keys in a table
    *   Given I am a global admin
    *   When I view the "All Keys" section
    *   Then I see a paginated DataTable with columns:
        *   Key (prefix...suffix)
        *   Name
        *   Scope (USER/ORGANIZATION badge)
        *   Owner (user name or org name, linked)
        *   Created by (for org keys)
        *   Created date
        *   Last used
        *   Total requests
        *   Status (Active/Expired)
        *   Actions (Revoke)

*   [ ] **Scenario 4**: Admin filters keys by scope
    *   Given I am viewing all API keys
    *   When I filter by scope = "ORGANIZATION"
    *   Then I only see organization API keys

*   [ ] **Scenario 5**: Admin filters keys by status
    *   Given I am viewing all API keys
    *   When I filter by status = "Expired"
    *   Then I only see keys that have passed their expiration date

*   [ ] **Scenario 6**: Admin searches for keys
    *   Given I am viewing all API keys
    *   When I search for "production"
    *   Then I see keys with "production" in their name
    *   When I search for "john@example.com"
    *   Then I see keys owned by that user

*   [ ] **Scenario 7**: Admin revokes any key
    *   Given I am viewing a key in the table
    *   When I click "Revoke"
    *   Then a confirmation dialog appears with key details
    *   When I confirm
    *   Then the key is revoked
    *   And an audit record is created with my user ID
    *   And the key disappears from the active list

### Navigation to Owner Details
*   [ ] **Scenario 8**: Admin navigates to user's API keys
    *   Given I am viewing a user-scoped key
    *   When I click the owner name
    *   Then I navigate to that user's API key management page (or user detail)

*   [ ] **Scenario 9**: Admin navigates to organization's API keys
    *   Given I am viewing an organization-scoped key
    *   When I click the organization name
    *   Then I navigate to that organization's settings/API keys page
    *   And I can manage keys like an organization owner

### Audit Trail Section
*   [ ] **Scenario 10**: Admin views revocation audit log
    *   Given I am on the API Keys dashboard
    *   When I view the "Recent Revocations" section
    *   Then I see recent audit entries showing:
        *   Key name and prefix
        *   Owner (user/org)
        *   Revoked by (admin name)
        *   Revoked at (timestamp)
        *   Lifetime requests at revocation

## 4. Technical Requirements

### Backend API Endpoints

#### Get API Key Statistics
```
GET /v1/admin/api-keys/stats
Authorization: Bearer {jwt}  // Requires ADMIN role

Response (200 OK):
{
  "totalActiveKeys": 245,
  "userKeys": 180,
  "organizationKeys": 65,
  "createdThisWeek": 12,
  "createdThisMonth": 47,
  "revokedThisMonth": 5,
  "expiringNext30Days": 8,
  "topKeysByUsage": [
    {
      "id": 123,
      "prefix": "org_x9y8z7w6",
      "name": "Production API",
      "ownerName": "Acme Corp",
      "totalRequests": 1542000
    }
  ],
  "neverUsedKeys": 23
}
```

#### Search All API Keys (Admin)
```
GET /v1/admin/api-keys/search
Authorization: Bearer {jwt}  // Requires ADMIN role

Query Parameters:
- search: string (name, owner email, org name)
- scope: USER | ORGANIZATION
- status: ACTIVE | EXPIRED
- page: number
- size: number
- sort: string (e.g., "createdAt,desc")

Response (200 OK):
{
  "content": [
    {
      "id": 123,
      "prefix": "evt_a1b2c3d4",
      "suffix": "o5p6",
      "name": "Production Server",
      "scope": "USER",
      "owner": {
        "id": 42,
        "type": "USER",
        "name": "John Doe",
        "email": "john@example.com"
      },
      "createdBy": null,  // Same as owner for USER scope
      "createdAt": "2026-01-01T10:00:00Z",
      "expiresAt": null,
      "lastUsedAt": "2026-01-06T15:30:00Z",
      "totalRequests": 1542,
      "isExpired": false
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 245, "totalPages": 13 }
}
```

#### Admin Revoke API Key
```
DELETE /v1/admin/api-keys/{keyId}
Authorization: Bearer {jwt}  // Requires ADMIN role

Response: 204 No Content
```

#### Get Revocation Audit Log
```
GET /v1/admin/api-keys/audit
Authorization: Bearer {jwt}  // Requires ADMIN role

Query Parameters:
- page: number
- size: number

Response (200 OK):
{
  "content": [
    {
      "id": 1,
      "keyPrefix": "evt_a1b2c3d4",
      "keyName": "Old Integration",
      "scope": "USER",
      "ownerName": "John Doe",
      "ownerEmail": "john@example.com",
      "createdAt": "2025-12-01T10:00:00Z",
      "revokedBy": {
        "id": 1,
        "name": "Admin User",
        "email": "admin@eventify.io"
      },
      "revokedAt": "2026-01-06T14:00:00Z",
      "totalRequestsAtRevocation": 5420
    }
  ],
  "page": { ... }
}
```

### Path Constants
Add to `Paths.java`:
```java
public static final String ADMIN_API_KEYS_PATH = ADMIN_PATH + "/api-keys";
public static final String ADMIN_API_KEYS_STATS_PATH = ADMIN_API_KEYS_PATH + "/stats";
public static final String ADMIN_API_KEYS_SEARCH_PATH = ADMIN_API_KEYS_PATH + SEARCH_PART;
public static final String ADMIN_API_KEYS_AUDIT_PATH = ADMIN_API_KEYS_PATH + "/audit";
public static final String ADMIN_API_KEY_PATH = ADMIN_API_KEYS_PATH + "/{keyId}";
```

### Controller
Location: `server/src/main/java/io/github/eventify/api/admin/controller/AdminApiKeyController.java`

```java
@RestController
@RequestMapping(ADMIN_API_KEYS_PATH)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")  // Admin permission
public class AdminApiKeyController {

    private final AdminApiKeyService adminApiKeyService;

    @GetMapping("/stats")
    public ResponseEntity<ApiKeyStatsResponse> getStats() { ... }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<AdminApiKeyResponse>> searchKeys(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) ApiKeyScope scope,
        @RequestParam(required = false) ApiKeyStatus status,
        Pageable pageable
    ) { ... }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revokeKey(@PathVariable Long keyId) { ... }

    @GetMapping("/audit")
    public ResponseEntity<PagedResponse<ApiKeyAuditResponse>> getAuditLog(Pageable pageable) { ... }
}
```

### Service Layer
Location: `server/src/main/java/io/github/eventify/api/admin/service/AdminApiKeyService.java`

```java
@Service
@RequiredArgsConstructor
public class AdminApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyAuditRepository auditRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public ApiKeyStatsResponse getStats() {
        // Aggregate queries for statistics
    }

    public Page<AdminApiKeyResponse> searchKeys(String search, ApiKeyScope scope, 
                                                 ApiKeyStatus status, Pageable pageable) {
        // Use Specification or custom query for flexible search
    }

    public void revokeKey(Long keyId, User admin) {
        // Find key, create audit record, delete key
    }

    public Page<ApiKeyAuditResponse> getAuditLog(Pageable pageable) {
        // Return paginated audit entries
    }
}
```

## 5. Design & UI/UX

### Dashboard Layout
```
┌─────────────────────────────────────────────────────────────────────┐
│  API Keys                                                           │
│  Monitor and manage API keys across the platform                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
│  │ Total Keys  │ │ User Keys   │ │ Org Keys    │ │ Created     │   │
│  │    245      │ │    180      │ │     65      │ │ This Month  │   │
│  │ ▲ 12%       │ │             │ │             │ │     47      │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
│                                                                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
│  │ Never Used  │ │ Expiring    │ │ Revoked     │ │ Top Key     │   │
│  │     23      │ │ (30 days)   │ │ This Month  │ │ 1.5M reqs   │   │
│  │             │ │      8      │ │      5      │ │ Acme Prod   │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│  All API Keys                                        [Search...]   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Filters: [Scope ▼] [Status ▼]                               │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Key          │ Name        │ Scope │ Owner    │ Requests │ ⋮ │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ evt_a1...p6  │ Prod Server │ USER  │ John Doe │   1,542  │ ⋮ │   │
│  │ org_x9...b2  │ Backend API │ ORG   │ Acme Corp│  15,420  │ ⋮ │   │
│  │ evt_c3...d4  │ CI Pipeline │ USER  │ Jane Doe │     234  │ ⋮ │   │
│  └─────────────────────────────────────────────────────────────┘   │
│  Showing 1-20 of 245                    [< 1 2 3 4 5 ... 13 >]     │
│                                                                     │
├─────────────────────────────────────────────────────────────────────┤
│  Recent Revocations                                                │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ "Old API Key" (evt_x1y2...) owned by john@example.com       │   │
│  │ Revoked by Admin User • 2 hours ago • 5,420 lifetime reqs  │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ "Staging Key" (org_a1b2...) owned by Acme Corp              │   │
│  │ Revoked by Admin User • 1 day ago • 120 lifetime reqs      │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Stat Cards Design
- Use the existing glassmorphism card style
- Include trend indicators where applicable (▲/▼ with percentage)
- Color coding: green for positive metrics, yellow for warnings (expiring soon), red for issues

### DataTable Row Design
Each row should be clickable to expand or navigate:
- Click owner name → Navigate to user detail or org settings
- Click row → Expand for more details (created by, expiration, etc.)
- Actions menu → Revoke (with confirmation)

### Revoke Confirmation Modal
```
┌─────────────────────────────────────────────────────────┐
│  Revoke API Key                                    [X]  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ⚠️  Are you sure you want to revoke this API key?     │
│                                                         │
│  Key: evt_a1b2c3d4...o5p6                              │
│  Name: Production Server                                │
│  Owner: John Doe (john@example.com)                    │
│  Total Requests: 1,542                                  │
│                                                         │
│  This action cannot be undone. Any systems using this  │
│  key will immediately lose access.                     │
│                                                         │
│                      [Cancel]  [Revoke Key]             │
└─────────────────────────────────────────────────────────┘
```

## 6. Implementation Notes / Research

### File Locations

#### Backend
```
server/src/main/java/io/github/eventify/api/admin/
├── controller/
│   └── AdminApiKeyController.java    # New
├── service/
│   └── AdminApiKeyService.java       # New
└── model/
    ├── response/
    │   ├── ApiKeyStatsResponse.java      # New
    │   ├── AdminApiKeyResponse.java      # New
    │   └── ApiKeyAuditResponse.java      # New
```

#### Frontend
```
client/src/routes/(authenticated)/admin/
├── api-keys/
│   └── +page.svelte                  # New: Admin API key dashboard

client/src/lib/components/admin/
├── api-keys/
│   ├── ApiKeyStatsCards.svelte       # New: Statistics cards
│   ├── AdminApiKeyTable.svelte       # New: All keys DataTable
│   ├── RecentRevocations.svelte      # New: Audit log preview
│   └── RevokeKeyModal.svelte         # New: Confirmation modal

client/src/lib/api/admin/
├── AdminApiKeyController.ts          # Generated from OpenAPI
└── service/
    └── AdminApiKeyService.svelte.ts  # New: State management
```

### Sidebar Update
Add to admin sidebar navigation:
```svelte
<a href="/admin/api-keys" class="sidebar-link">
  <Key class="w-4 h-4" />
  API Keys
</a>
```

### Existing Patterns to Follow
- `client/src/routes/(authenticated)/admin/users/+page.svelte` - DataTable with filters
- `client/src/routes/(authenticated)/admin/dashboard/+page.svelte` - Stat cards layout
- `server/src/main/java/io/github/eventify/api/admin/service/AdminStatsService.java` - Stats aggregation

### Performance Considerations
- Statistics queries should be optimized with proper indexes
- Consider caching stats with short TTL (5 minutes)
- Use pagination for all list endpoints
- Search should use database indexes (name, owner email)

### Repository Queries
Add to `ApiKeyRepository.java`:
```java
@Query("SELECT COUNT(k) FROM ApiKey k WHERE k.organization IS NULL")
long countUserKeys();

@Query("SELECT COUNT(k) FROM ApiKey k WHERE k.organization IS NOT NULL")
long countOrganizationKeys();

@Query("SELECT COUNT(k) FROM ApiKey k WHERE k.createdAt >= :since")
long countCreatedSince(@Param("since") OffsetDateTime since);

@Query("SELECT COUNT(k) FROM ApiKey k WHERE k.lastUsedAt IS NULL")
long countNeverUsed();

@Query("SELECT COUNT(k) FROM ApiKey k WHERE k.expiresAt BETWEEN :now AND :future")
long countExpiringBetween(@Param("now") OffsetDateTime now, @Param("future") OffsetDateTime future);
```
