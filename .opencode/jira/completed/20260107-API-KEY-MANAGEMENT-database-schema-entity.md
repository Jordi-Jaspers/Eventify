## [2026-01-07] - API Key Database Schema & Entity

### Feature plan approved by user
**Requirements Summary**
- Create `api_key` table with Liquibase migration
- Create `api_key_audit` table for tracking revocations
- JPA entities: `ApiKey`, `ApiKeyAudit`, `ApiKeyScope` enum
- `ApiKeyRepository` with basic queries
- Utility class `ApiKeyGenerator` for secure key generation
- Key format: `{scope_prefix}_{32_random_chars}` (e.g., `evt_a1b2c3...`)
- BCrypt hashing - plaintext never stored

**Technical Approach**

**Database:**
- Migration file: `202601071000-PRD-api-key-tables.xml`
- Two tables: `api_key`, `api_key_audit`
- Indexes on user_id, organization_id, prefix, hashed_key
- Foreign keys to `user` and `organization`

**Backend:**
- Package: `io.github.eventify.api.apikey`
- Entities: `ApiKey`, `ApiKeyAudit`, `ApiKeyScope`
- Repository: `ApiKeyRepository` with `findByPrefix`, `findAllByUserId`, `findAllByOrganizationId`
- Utility: `ApiKeyGenerator` for secure key generation
- Value object: `GeneratedApiKey` to hold full key + display prefix

**Implementation Workflow**

Phase 1: Tests First
- Agent: java-testing-agent
- Task: Create comprehensive test suite

Phase 2: Backend Implementation
- Agent: java-backend-agent
- Task: Implement entities, migration, repository, generator

**Success Criteria**
- All tests passing (>90% coverage)
- Gradle build successful
- Liquibase migration applies cleanly
- Entity mapping validated by Hibernate

---

### Actual changelog after completion

#### Summary
Created foundational database schema and JPA entities for API Key management feature. This enables programmatic access to the Eventify platform via API keys for both personal (user-scoped) and organizational use cases.

#### Changes

**Database:**
- Created `api_key` table with:
  - BCrypt-hashed key storage (never plaintext)
  - Prefix-based lookup (12 chars for display)
  - User and organization foreign keys
  - Usage tracking (last_used_at, total_requests)
  - Optional expiration support
- Created `api_key_audit` table for revocation audit trail
- Added comprehensive indexes for performance
- Full documentation comments on all tables/columns/indexes

**Backend:**
- `ApiKeyScope` enum: USER, ORGANIZATION
- `ApiKey` entity with ManyToOne relationships to User and Organization
- `ApiKeyAudit` entity for tracking revoked keys
- `ApiKeyRepository` with custom queries:
  - `findByPrefix(String)`
  - `findAllByUserId(Long)`
  - `findAllByOrganizationId(Long)`
  - `existsByPrefixAndHashedKey(String, String)`
- `ApiKeyAuditRepository` for audit records
- `ApiKeyGenerator` utility:
  - SecureRandom for cryptographic key generation
  - USER scope: `evt_` prefix
  - ORGANIZATION scope: `org_` prefix
  - 36 total characters (prefix + underscore + 32 random alphanumeric)
- `GeneratedApiKey` value object for one-time key display

**Testing:**
- 16 tests created across 3 test classes
- `ApiKeyGeneratorTest` (6 tests): Key format, uniqueness, character set
- `GeneratedApiKeyTest` (3 tests): Prefix extraction, hashing
- `ApiKeyRepositoryTest` (7 tests): CRUD, queries, validation
- All tests passing

#### Agents Used
- java-testing-agent (test suite creation)
- java-backend-agent (implementation)

#### Files Created
- `server/src/main/java/io/github/eventify/api/apikey/model/ApiKeyScope.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/ApiKey.java`
- `server/src/main/java/io/github/eventify/api/apikey/model/ApiKeyAudit.java`
- `server/src/main/java/io/github/eventify/api/apikey/repository/ApiKeyRepository.java`
- `server/src/main/java/io/github/eventify/api/apikey/repository/ApiKeyAuditRepository.java`
- `server/src/main/java/io/github/eventify/api/apikey/util/GeneratedApiKey.java`
- `server/src/main/java/io/github/eventify/api/apikey/util/ApiKeyGenerator.java`
- `server/src/main/resources/db/changelog/changesets/202601071000-PRD-api-key-tables.xml`
- `server/src/test/java/io/github/eventify/api/apikey/util/ApiKeyGeneratorTest.java`
- `server/src/test/java/io/github/eventify/api/apikey/util/GeneratedApiKeyTest.java`
- `server/src/test/java/io/github/eventify/api/apikey/repository/ApiKeyRepositoryTest.java`

#### Quality Metrics
- Tests: 16 written, 16 passing
- Build: Successful (clean build)
- Quality checks: Passed (Checkstyle, PMD, SpotBugs, Spotless)
- Security: BCrypt hashing, SecureRandom generation

#### Notes
- This is the foundation story for API Key Management epic
- Next stories will add CRUD services, controllers, and UI
- Key format designed for easy identification: `evt_` for user, `org_` for organization
- Audit table enables compliance tracking of key lifecycle
