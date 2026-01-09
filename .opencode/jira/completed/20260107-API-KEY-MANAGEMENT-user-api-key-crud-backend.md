## [2026-01-07] - User API Key CRUD (Backend)

### Plan (approved)
**Requirements:** Enable users to create, list, and revoke personal API keys for programmatic event ingestion.

**Technical Approach:**
- 3 REST endpoints: POST/GET/DELETE `/v1/user/api-keys`
- Max 5 API keys per user limit
- BCrypt hashing for secure storage
- Full key returned ONLY on creation
- Masked display format: `evt_******<suffix>` (last 4 chars)
- Audit records on revocation

**Workflow:** TDD - Tests first, then implementation

### Actual Changes

**Backend - Controller Layer:**
- `UserApiKeyController` - REST endpoints (POST/GET/DELETE) with ResponseEntity<T>
- Uses custom `CreateApiKeyValidator` (no @Valid annotation)
- Uses `ApiKeyMapper` (MapStruct) to convert domain to DTOs

**Backend - Service Layer:**
- `ApiKeyService` - Business logic returning domain `ApiKey` objects (not DTOs)
  - `createUserApiKey()` - Creates key with limit validation (max 5)
  - `listUserApiKeys()` - Returns personal keys ordered by creation date
  - `revokeUserApiKey()` - Revokes and creates audit record
- `ApiKeyGenerator` - Generates secure API keys with format `evt_<random>` or `org_<random>`

**Backend - Domain Model:**
- `ApiKey` entity with `suffix` field, transient `key` field, and `getMaskedKey()` method
- `ApiKeyAudit` entity with `keySuffix` field for audit trail
- `ApiKeyScope` enum (USER, ORGANIZATION) with prefix getters
- `GeneratedApiKey` record for key generation results
- `CreateApiKeyRequest` - Generic request DTO for API key creation
- `ApiKeyCreationResponse` - Response with full key (only on creation)
- `ApiKeyResponse` - Response with masked key for listing
- `ApiKeyListResponse` - Wrapper for list of keys
- `CreateApiKeyValidator` - Custom validator for request validation
- `ApiKeyMapper` - MapStruct mapper for domain-to-DTO conversion

**Backend - Exceptions:**
- `ApiKeyLimitExceededException` - Thrown when user exceeds 5-key limit
- `ApiKeyInvalidExpirationException` - Thrown when expiration date is in past
- Added error codes to `ApiErrorCode`: `API_KEY_LIMIT_EXCEEDED`, `API_KEY_NOT_FOUND`, `API_KEY_INVALID_EXPIRATION`

**Backend - Repository:**
- `ApiKeyRepository` - JPA repository with query methods for personal keys
- `ApiKeyAuditRepository` - JPA repository for audit records

**Database:**
- Migration `202601071000-PRD-api-key-tables.xml`:
  - `api_key` table with suffix, hashed_key, scope, user_id, organization_id, etc.
  - `api_key_audit` table with key_suffix, revoked_by (nullable for SET NULL on delete)
  - Proper indexes and foreign key constraints

### Agents Used
1. **java-testing-agent** - Created test suites (51 test methods)
2. **java-backend-agent** - Implemented backend components
3. **Orchestrator fixes** - Repository field name fixes, entity/schema alignment, validator pattern

### Files Created

**Main Source:**
- `server/src/main/java/io/github/eventify/api/apikey/controller/UserApiKeyController.java`
- `server/src/main/java/io/github/eventify/api/apikey/service/ApiKeyService.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/mapper/ApiKeyMapper.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/request/CreateApiKeyRequest.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/response/ApiKeyCreationResponse.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/response/ApiKeyResponse.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/response/ApiKeyListResponse.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/validator/CreateApiKeyValidator.java`
- `server/src/main/java/io/github/eventify/common/exception/ApiKeyLimitExceededException.java`
- `server/src/main/java/io/github/eventify/common/exception/ApiKeyInvalidExpirationException.java`

**Test Source:**
- `server/src/test/java/io/github/eventify/api/apikey/controller/UserApiKeyControllerTest.java`
- `server/src/test/java/io/github/eventify/api/apikey/service/ApiKeyServiceTest.java`
- `server/src/test/java/io/github/eventify/api/apikey/service/ApiKeyGeneratorTest.java`
- `server/src/test/java/io/github/eventify/api/apikey/model/GeneratedApiKeyTest.java`
- `server/src/test/java/io/github/eventify/api/apikey/model/validator/CreateApiKeyValidatorTest.java`

### Files Modified
- `ApiKey.java` - Added suffix field, transient key field, getMaskedKey() method
- `ApiKeyAudit.java` - Changed keyPrefix to keySuffix, made revokedBy nullable
- `ApiKeyScope.java` - Added getPrefix() method for scope-based prefixes
- `GeneratedApiKey.java` - Added suffix extraction, updated key format
- `ApiKeyGenerator.java` - Updated to use scope prefix
- `ApiKeyRepository.java` - Removed unused prefix-based methods, added personal key query methods
- `ApiErrorCode.java` - Added 3 new error codes
- `Paths.java` - Added USER_API_KEYS_PATH, USER_API_KEY_PATH constants
- `202601071000-PRD-api-key-tables.xml` - Fixed revoked_by to be nullable

### Key Architecture Decisions
1. **Service returns domain objects** - ApiKeyService returns `ApiKey` entity, not DTOs
2. **Controller handles mapping** - Uses MapStruct `ApiKeyMapper` for domain-to-DTO conversion
3. **Custom validators** - `CreateApiKeyValidator` instead of @Valid annotation
4. **Transient key field** - Full key stored temporarily in entity during creation only
5. **Suffix-based display** - Only last 4 chars stored for masked display
6. **Nullable audit FK** - `revoked_by` nullable to support SET NULL on user deletion

### Quality Metrics
- ✅ Tests: 51 test methods (controller, service, generator, validator, model)
- ✅ All 493 project tests passing
- ✅ Build successful
- ✅ Quality checks passed (spotless, checkstyle, pmd, spotbugs)
