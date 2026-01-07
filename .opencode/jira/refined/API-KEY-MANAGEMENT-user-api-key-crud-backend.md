# User API Key CRUD (Backend)

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-06

## 1. User Story
**As a** registered user
**I want** to create, list, and revoke my personal API keys via the API
**So that** I can programmatically send events to my personal channels

## 2. Business Context & Value
Personal API keys enable individual users to integrate external systems (CI/CD pipelines, monitoring tools, custom applications) with Eventify. Users are limited to 5 active keys to prevent abuse while still allowing multiple integration points (production, staging, local dev, etc.).

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: User creates a personal API key
    *   Given I am an authenticated user with fewer than 5 API keys
    *   When I POST to `/v1/user/api-keys` with a valid name
    *   Then I receive a 201 response with the full key (shown only once)
    *   And the key is stored as a BCrypt hash in the database
    *   And the response includes: id, name, prefix, fullKey, createdAt, expiresAt

*   [ ] **Scenario 2**: User cannot exceed 5 API keys
    *   Given I am an authenticated user with 5 active API keys
    *   When I POST to `/v1/user/api-keys`
    *   Then I receive a 400 response with error code `API_KEY_LIMIT_EXCEEDED`
    *   And no new key is created

*   [ ] **Scenario 3**: User lists their API keys
    *   Given I am an authenticated user with 3 API keys
    *   When I GET `/v1/user/api-keys`
    *   Then I receive a 200 response with a list of my keys
    *   And each key shows: id, name, prefix, suffix (last 4 chars), createdAt, expiresAt, lastUsedAt, totalRequests
    *   And the full key value is NOT included

*   [ ] **Scenario 4**: User revokes an API key
    *   Given I am an authenticated user with an API key (id: 123)
    *   When I DELETE `/v1/user/api-keys/123`
    *   Then I receive a 204 response
    *   And the key is permanently deleted from `api_key` table
    *   And an audit record is created in `api_key_audit`

*   [ ] **Scenario 5**: User cannot revoke another user's key
    *   Given I am an authenticated user
    *   When I DELETE `/v1/user/api-keys/{otherUsersKeyId}`
    *   Then I receive a 404 response (key not found for this user)

*   [ ] **Scenario 6**: API key with expiration date
    *   Given I am an authenticated user
    *   When I POST to `/v1/user/api-keys` with an `expiresAt` date
    *   Then the key is created with that expiration date
    *   And the expiration is validated to be in the future

*   [ ] **Scenario 7**: Key name validation
    *   Given I am an authenticated user
    *   When I POST to `/v1/user/api-keys` with an empty or too-long name (>100 chars)
    *   Then I receive a 400 response with validation error

## 4. Technical Requirements

### API Endpoints

#### Create API Key
```
POST /v1/user/api-keys
Authorization: Bearer {jwt}
Content-Type: application/json

Request:
{
  "name": "Production Server",
  "expiresAt": "2027-01-01T00:00:00Z"  // optional
}

Response (201 Created):
{
  "id": 123,
  "name": "Production Server",
  "prefix": "evt_a1b2c3d4",
  "suffix": "o5p6",
  "key": "evt_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",  // ONLY returned on creation!
  "createdAt": "2026-01-06T10:00:00Z",
  "expiresAt": "2027-01-01T00:00:00Z"
}
```

#### List API Keys
```
GET /v1/user/api-keys
Authorization: Bearer {jwt}

Response (200 OK):
{
  "keys": [
    {
      "id": 123,
      "name": "Production Server",
      "prefix": "evt_a1b2c3d4",
      "suffix": "o5p6",
      "createdAt": "2026-01-06T10:00:00Z",
      "expiresAt": "2027-01-01T00:00:00Z",
      "lastUsedAt": "2026-01-06T12:30:00Z",
      "totalRequests": 1542
    }
  ],
  "count": 1,
  "limit": 5
}
```

#### Revoke API Key
```
DELETE /v1/user/api-keys/{id}
Authorization: Bearer {jwt}

Response: 204 No Content
```

### New Error Codes
Add to `ApiErrorCode.java`:
```java
API_KEY_LIMIT_EXCEEDED("API_KEY_001", "Maximum number of API keys reached"),
API_KEY_NOT_FOUND("API_KEY_002", "API key not found"),
API_KEY_INVALID_EXPIRATION("API_KEY_003", "Expiration date must be in the future")
```

### Service Layer

#### `ApiKeyService.java`
Location: `server/src/main/java/io/github/eventify/api/apikey/service/ApiKeyService.java`

Key methods:
```java
public interface ApiKeyService {
    /**
     * Creates a new personal API key for the user.
     * @return ApiKeyCreationResponse containing the full key (only time it's available)
     * @throws ApiException if user has reached 5 key limit
     */
    ApiKeyCreationResponse createUserApiKey(User user, CreateApiKeyRequest request);
    
    /**
     * Lists all active API keys for the user (without exposing key values).
     */
    List<ApiKeyResponse> listUserApiKeys(User user);
    
    /**
     * Revokes (deletes) an API key and creates audit record.
     * @throws DataNotFoundException if key doesn't exist or doesn't belong to user
     */
    void revokeUserApiKey(User user, Long keyId);
    
    /**
     * Counts active API keys for a user.
     */
    int countUserApiKeys(Long userId);
}
```

### Repository Layer

#### `ApiKeyRepository.java`
```java
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    
    List<ApiKey> findByUserIdAndOrganizationIdIsNullOrderByCreatedAtDesc(Long userId);
    
    int countByUserIdAndOrganizationIdIsNull(Long userId);
    
    Optional<ApiKey> findByIdAndUserIdAndOrganizationIdIsNull(Long id, Long userId);
    
    Optional<ApiKey> findByHashedKey(String hashedKey);
}
```

#### `ApiKeyAuditRepository.java`
```java
public interface ApiKeyAuditRepository extends JpaRepository<ApiKeyAudit, Long> {
    // For admin queries later
}
```

### Security
- All endpoints require JWT authentication (existing `@PreAuthorize("isAuthenticated()")`)
- Users can only access their own keys (enforced by query filtering on `user_id`)
- Full key is only returned once on creation - stored response should never include it

### Path Constants
Add to `Paths.java`:
```java
public static final String USER_API_KEYS_PATH = USERS_PATH + "/api-keys";
public static final String USER_API_KEY_PATH = USER_API_KEYS_PATH + "/{keyId}";
```

## 5. Design & UI/UX
N/A - This is a backend story. See Story 3 for frontend implementation.

## 6. Implementation Notes / Research

### File Locations
- Controller: `server/src/main/java/io/github/eventify/api/apikey/controller/UserApiKeyController.java`
- Service: `server/src/main/java/io/github/eventify/api/apikey/service/ApiKeyService.java`
- DTOs: `server/src/main/java/io/github/eventify/api/apikey/model/request/` and `response/`

### Key Generation Implementation
```java
@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyAuditRepository auditRepository;
    
    private static final int USER_KEY_LIMIT = 5;
    
    public ApiKeyCreationResponse createUserApiKey(User user, CreateApiKeyRequest request) {
        // Check limit
        int currentCount = apiKeyRepository.countByUserIdAndOrganizationIdIsNull(user.getId());
        if (currentCount >= USER_KEY_LIMIT) {
            throw new ApiException(API_KEY_LIMIT_EXCEEDED);
        }
        
        // Generate key
        GeneratedApiKey generated = ApiKeyGenerator.generate(ApiKeyScope.USER);
        
        // Create entity
        ApiKey apiKey = new ApiKey();
        apiKey.setPrefix(generated.getPrefix());  // evt_a1b2c3d4
        apiKey.setHashedKey(passwordEncoder.encode(generated.getFullKey()));
        apiKey.setName(request.getName());
        apiKey.setScope(ApiKeyScope.USER);
        apiKey.setUser(user);
        apiKey.setExpiresAt(request.getExpiresAt());
        
        apiKeyRepository.save(apiKey);
        
        // Return with full key (only time!)
        return new ApiKeyCreationResponse(apiKey, generated.getFullKey());
    }
}
```

### Suffix Storage Consideration
The suffix (last 4 chars) should be stored for display purposes. Update schema to include:
```sql
suffix VARCHAR(4) NOT NULL  -- Last 4 characters for identification
```
This allows showing `evt_a1b2...o5p6` in the UI without any computation.

### Existing Patterns to Follow
- See `UserController.java` for REST controller patterns
- See `PasswordService.java` for `PasswordEncoder` usage
- See `OrganizationMembershipService.java` for audit-style record creation

### OpenAPI Annotations
Ensure proper `@Operation`, `@ApiResponse` annotations for generated client code.
