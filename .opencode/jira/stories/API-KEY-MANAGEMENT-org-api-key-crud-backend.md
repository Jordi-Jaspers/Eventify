# Organization API Key CRUD (Backend)

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-06

## 1. User Story
**As an** organization owner or admin
**I want** to create, list, and revoke API keys for my organization
**So that** team members and systems can send events to organization channels

## 2. Business Context & Value
Organization API keys enable teams to integrate shared systems with Eventify without relying on individual user credentials. Unlike personal keys (limited to 5), organization keys are unlimited to support enterprise use cases with multiple environments, services, and integrations. The audit trail tracks who created/revoked each key for accountability.

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: Organization admin creates an API key
    *   Given I am an OWNER or ADMIN of organization "Acme Corp"
    *   When I POST to `/v1/organization/{orgId}/api-keys` with a valid name
    *   Then I receive a 201 response with the full key (shown only once)
    *   And the key is stored with `scope=ORGANIZATION` and the org ID
    *   And `user_id` records who created the key (me)

*   [ ] **Scenario 2**: Organization member cannot create keys
    *   Given I am a MEMBER (not OWNER/ADMIN) of organization "Acme Corp"
    *   When I POST to `/v1/organization/{orgId}/api-keys`
    *   Then I receive a 403 Forbidden response

*   [ ] **Scenario 3**: Organization admin lists API keys
    *   Given organization "Acme Corp" has 3 API keys
    *   And I am an OWNER or ADMIN of "Acme Corp"
    *   When I GET `/v1/organization/{orgId}/api-keys`
    *   Then I receive a list of all 3 keys with:
        *   id, name, prefix, suffix, createdAt, expiresAt
        *   createdBy (user info: id, name, email)
        *   lastUsedAt, totalRequests

*   [ ] **Scenario 4**: Organization member can view keys (read-only)
    *   Given I am a MEMBER of organization "Acme Corp"
    *   When I GET `/v1/organization/{orgId}/api-keys`
    *   Then I receive the list of keys (read access)
    *   But I cannot create or revoke keys

*   [ ] **Scenario 5**: Organization admin revokes an API key
    *   Given organization "Acme Corp" has an API key (id: 456)
    *   And I am an OWNER or ADMIN of "Acme Corp"
    *   When I DELETE `/v1/organization/{orgId}/api-keys/456`
    *   Then I receive a 204 response
    *   And the key is permanently deleted
    *   And an audit record is created with my user ID as `revoked_by`

*   [ ] **Scenario 6**: Cannot access other organization's keys
    *   Given I am an ADMIN of organization "Acme Corp"
    *   When I try to access `/v1/organization/{otherOrgId}/api-keys`
    *   Then I receive a 403 Forbidden response

*   [ ] **Scenario 7**: Global admin can manage any organization's keys
    *   Given I am a global ADMIN (role=ADMIN)
    *   When I access `/v1/organization/{anyOrgId}/api-keys`
    *   Then I can list, create, and revoke keys for that organization

*   [ ] **Scenario 8**: No limit on organization API keys
    *   Given organization "Acme Corp" has 100 API keys
    *   When I create another key
    *   Then it succeeds (no limit enforced)

## 4. Technical Requirements

### API Endpoints

#### Create Organization API Key
```
POST /v1/organization/{orgId}/api-keys
Authorization: Bearer {jwt}
Content-Type: application/json

Request:
{
  "name": "Production Backend",
  "expiresAt": "2027-01-01T00:00:00Z"  // optional
}

Response (201 Created):
{
  "id": 456,
  "name": "Production Backend",
  "prefix": "org_x9y8z7w6",
  "suffix": "a1b2",
  "key": "org_x9y8z7w6v5u4t3s2r1q0p9o8n7m6l5k4",  // ONLY returned on creation!
  "createdAt": "2026-01-06T10:00:00Z",
  "expiresAt": "2027-01-01T00:00:00Z",
  "createdBy": {
    "id": 42,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@acme.com"
  }
}
```

#### List Organization API Keys
```
GET /v1/organization/{orgId}/api-keys
Authorization: Bearer {jwt}

Response (200 OK):
{
  "keys": [
    {
      "id": 456,
      "name": "Production Backend",
      "prefix": "org_x9y8z7w6",
      "suffix": "a1b2",
      "createdAt": "2026-01-06T10:00:00Z",
      "expiresAt": null,
      "lastUsedAt": "2026-01-06T15:30:00Z",
      "totalRequests": 15420,
      "createdBy": {
        "id": 42,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john@acme.com"
      }
    }
  ],
  "count": 1
}
```

#### Revoke Organization API Key
```
DELETE /v1/organization/{orgId}/api-keys/{keyId}
Authorization: Bearer {jwt}

Response: 204 No Content
```

### Security Configuration

Use existing `orgSecurity` service for SpEL authorization:

```java
@RestController
@RequestMapping(ORGANIZATION_API_KEYS_PATH)
@RequiredArgsConstructor
public class OrganizationApiKeyController {

    @PostMapping
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<ApiKeyCreationResponse> createKey(
        @PathVariable Long orgId,
        @RequestBody @Valid CreateApiKeyRequest request
    ) { ... }

    @GetMapping
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<OrgApiKeyListResponse> listKeys(@PathVariable Long orgId) { ... }

    @DeleteMapping("/{keyId}")
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    public ResponseEntity<Void> revokeKey(
        @PathVariable Long orgId,
        @PathVariable Long keyId
    ) { ... }
}
```

### Path Constants
Add to `Paths.java`:
```java
public static final String ORGANIZATION_API_KEYS_PATH = ORGANIZATION_PATH + "/api-keys";
public static final String ORGANIZATION_API_KEY_PATH = ORGANIZATION_API_KEYS_PATH + "/{keyId}";
```

### Service Layer Extension

Add methods to `ApiKeyService.java`:
```java
/**
 * Creates a new API key for the organization.
 * @param creator The user creating the key (for audit)
 */
ApiKeyCreationResponse createOrganizationApiKey(
    Long orgId, 
    User creator, 
    CreateApiKeyRequest request
);

/**
 * Lists all API keys for an organization.
 * Includes creator information for each key.
 */
List<OrgApiKeyResponse> listOrganizationApiKeys(Long orgId);

/**
 * Revokes an organization API key.
 * @param revoker The user revoking the key (for audit)
 */
void revokeOrganizationApiKey(Long orgId, Long keyId, User revoker);
```

### Repository Extensions

Add to `ApiKeyRepository.java`:
```java
List<ApiKey> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

Optional<ApiKey> findByIdAndOrganizationId(Long id, Long organizationId);

int countByOrganizationId(Long organizationId);
```

### Response DTOs

#### `OrgApiKeyResponse.java`
```java
public record OrgApiKeyResponse(
    Long id,
    String name,
    String prefix,
    String suffix,
    OffsetDateTime createdAt,
    OffsetDateTime expiresAt,
    OffsetDateTime lastUsedAt,
    Long totalRequests,
    UserSummaryResponse createdBy  // Additional field for org keys
) {}
```

#### `UserSummaryResponse.java`
```java
public record UserSummaryResponse(
    Long id,
    String firstName,
    String lastName,
    String email
) {}
```

## 5. Design & UI/UX
N/A - This is a backend story. See Story 5 for frontend implementation.

## 6. Implementation Notes / Research

### File Locations
- Controller: `server/src/main/java/io/github/eventify/api/apikey/controller/OrganizationApiKeyController.java`
- Extend existing `ApiKeyService.java` with organization methods
- DTOs: Add to `server/src/main/java/io/github/eventify/api/apikey/model/response/`

### Key Differences from User API Keys
| Aspect | User Keys | Organization Keys |
|--------|-----------|-------------------|
| Prefix | `evt_` | `org_` |
| Limit | 5 per user | Unlimited |
| Scope | USER | ORGANIZATION |
| Created By | Always the owner | Tracked separately (user_id) |
| Access | Only owner | All org members (read), OWNER/ADMIN (write) |

### Audit Record for Org Keys
When revoking org keys, the audit record should include:
- `organization_id`: The org that owned the key
- `created_by`: Original creator's user ID
- `revoked_by`: Who revoked (could be different person)

### Existing Patterns to Follow
- See `OrganizationMembershipController.java` for org-scoped endpoints with role checks
- See `OrganizationSecurityService.java` for SpEL authorization patterns
- See `OrganizationMembershipService.java` for handling org-level operations

### Validation
- Verify organization exists and is not deleted (soft delete check)
- Validate key belongs to the specified organization before revocation
- Return 404 if key doesn't exist or belongs to different org (don't leak existence)
