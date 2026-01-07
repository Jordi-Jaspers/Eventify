# API Key Database Schema & Entity

**Epic**: API Key Management
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-01-06

## 1. User Story
**As a** platform developer
**I want** a well-designed database schema and JPA entity for API keys
**So that** I can build the API key management features on a solid foundation

## 2. Business Context & Value
API keys enable programmatic access to the Eventify platform for sending events. This foundational story establishes the data model that supports:
- Personal (user-scoped) and organizational API keys
- Secure key storage (hashed, never plaintext)
- Usage tracking and statistics
- Audit trail for key lifecycle events

This is a prerequisite for all other API Key Management stories.

## 3. Acceptance Criteria

*   [ ] **Scenario 1**: API Key table is created via Liquibase migration
    *   Given the application starts
    *   When Liquibase runs the migration
    *   Then the `api_key` table exists with all required columns and indexes

*   [ ] **Scenario 2**: API Key Audit table is created via Liquibase migration
    *   Given the application starts
    *   When Liquibase runs the migration
    *   Then the `api_key_audit` table exists for tracking revocations

*   [ ] **Scenario 3**: JPA Entity maps correctly to the database
    *   Given the ApiKey entity is defined
    *   When the application context loads
    *   Then Hibernate validates the entity mapping without errors

*   [ ] **Scenario 4**: Enum types are properly defined
    *   Given the need for API key scopes
    *   When the ApiKeyScope enum is created
    *   Then it contains values: `USER`, `ORGANIZATION`

## 4. Technical Requirements

### Database Schema

#### Table: `api_key`
```sql
CREATE TABLE api_key (
    id              SERIAL PRIMARY KEY,
    prefix          VARCHAR(12) NOT NULL,          -- e.g., "evt_a1b2c3d4" (first 12 chars for display)
    hashed_key      TEXT NOT NULL,                 -- BCrypt hash of full key
    name            VARCHAR(100) NOT NULL,         -- User-provided label, e.g., "Production Server"
    scope           VARCHAR(20) NOT NULL,          -- ENUM: USER, ORGANIZATION
    user_id         INTEGER NOT NULL,              -- Owner (for USER scope) or creator (for ORG scope)
    organization_id INTEGER NULL,                  -- Only set for ORGANIZATION scope
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NULL,              -- Optional expiration
    last_used_at    TIMESTAMPTZ NULL,              -- Updated on each successful auth
    total_requests  BIGINT NOT NULL DEFAULT 0,     -- Lifetime request count
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organization(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_api_key_user ON api_key(user_id) WHERE organization_id IS NULL;
CREATE INDEX idx_api_key_org ON api_key(organization_id) WHERE organization_id IS NOT NULL;
CREATE INDEX idx_api_key_prefix ON api_key(prefix);
CREATE UNIQUE INDEX idx_api_key_hashed ON api_key(hashed_key);

-- Comments
COMMENT ON TABLE api_key IS 'Stores API keys for programmatic access to event ingestion';
COMMENT ON COLUMN api_key.prefix IS 'First 12 characters of the key for display/identification';
COMMENT ON COLUMN api_key.hashed_key IS 'BCrypt hash of the full API key - plaintext never stored';
COMMENT ON COLUMN api_key.name IS 'User-provided descriptive label for the key';
COMMENT ON COLUMN api_key.scope IS 'Whether key is USER-scoped or ORGANIZATION-scoped';
COMMENT ON COLUMN api_key.user_id IS 'For USER scope: the owner. For ORG scope: who created it';
COMMENT ON COLUMN api_key.organization_id IS 'The organization this key belongs to (ORG scope only)';
COMMENT ON COLUMN api_key.total_requests IS 'Lifetime count of successful authentications with this key';
```

#### Table: `api_key_audit`
```sql
CREATE TABLE api_key_audit (
    id              SERIAL PRIMARY KEY,
    key_prefix      VARCHAR(12) NOT NULL,          -- Prefix of the revoked key
    key_name        VARCHAR(100) NOT NULL,         -- Name at time of revocation
    scope           VARCHAR(20) NOT NULL,          -- USER or ORGANIZATION
    owner_user_id   INTEGER NULL,                  -- User who owned the key (for USER scope)
    organization_id INTEGER NULL,                  -- Org that owned the key (for ORG scope)
    created_by      INTEGER NOT NULL,              -- Who originally created the key
    created_at      TIMESTAMPTZ NOT NULL,          -- When key was originally created
    revoked_by      INTEGER NOT NULL,              -- Who revoked the key
    revoked_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    total_requests  BIGINT NOT NULL,               -- Lifetime requests at time of revocation
    
    FOREIGN KEY (revoked_by) REFERENCES "user"(id) ON DELETE SET NULL
);

CREATE INDEX idx_api_key_audit_org ON api_key_audit(organization_id);
CREATE INDEX idx_api_key_audit_user ON api_key_audit(owner_user_id);
CREATE INDEX idx_api_key_audit_revoked_at ON api_key_audit(revoked_at);

COMMENT ON TABLE api_key_audit IS 'Audit log for revoked API keys';
```

### JPA Entities

#### `ApiKey.java`
Location: `server/src/main/java/io/github/eventify/api/apikey/model/ApiKey.java`

```java
@Entity
@Table(name = "api_key")
public class ApiKey implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 12)
    private String prefix;
    
    @Column(name = "hashed_key", nullable = false)
    private String hashedKey;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApiKeyScope scope;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Owner (USER scope) or Creator (ORG scope)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
    
    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;
    
    @Column(name = "total_requests", nullable = false)
    private Long totalRequests = 0L;
}
```

#### `ApiKeyScope.java`
Location: `server/src/main/java/io/github/eventify/api/apikey/model/ApiKeyScope.java`

```java
public enum ApiKeyScope {
    USER,
    ORGANIZATION
}
```

#### `ApiKeyAudit.java`
Location: `server/src/main/java/io/github/eventify/api/apikey/model/ApiKeyAudit.java`

```java
@Entity
@Table(name = "api_key_audit")
public class ApiKeyAudit implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "key_prefix", nullable = false, length = 12)
    private String keyPrefix;
    
    @Column(name = "key_name", nullable = false, length = 100)
    private String keyName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApiKeyScope scope;
    
    @Column(name = "owner_user_id")
    private Long ownerUserId;
    
    @Column(name = "organization_id")
    private Long organizationId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by", nullable = false)
    private User revokedBy;
    
    @Column(name = "revoked_at", nullable = false)
    private OffsetDateTime revokedAt;
    
    @Column(name = "total_requests", nullable = false)
    private Long totalRequests;
}
```

### API Key Format Specification
- **Format**: `{prefix}_{random}`
- **Prefix**: `evt` for USER scope, `org` for ORGANIZATION scope
- **Random**: 32 cryptographically random alphanumeric characters
- **Display prefix**: First 12 characters (e.g., `evt_a1b2c3d4`)
- **Example full key**: `evt_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`
- **Storage**: Only BCrypt hash stored, plaintext shown once on creation

## 5. Design & UI/UX
N/A - This is a backend/database story.

## 6. Implementation Notes / Research

### File Locations (following existing patterns)
- Migration: `server/src/main/resources/db/changelog/changesets/202601061000-PRD-api-key-tables.xml`
- Entity package: `server/src/main/java/io/github/eventify/api/apikey/model/`
- Repository: `server/src/main/java/io/github/eventify/api/apikey/repository/ApiKeyRepository.java`

### Existing Patterns to Follow
- See `202512111000-PRD-organization-table.xml` for migration structure with comments
- See `Token.java` for entity patterns (though API keys use hashing, not plaintext storage)
- See `OrganizationalRole.java` for simple enum pattern

### Key Generation Utility
Create a utility class `ApiKeyGenerator.java` with:
```java
public class ApiKeyGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    
    public static GeneratedApiKey generate(ApiKeyScope scope) {
        String prefix = scope == ApiKeyScope.USER ? "evt" : "org";
        String random = generateRandomString(32);
        String fullKey = prefix + "_" + random;
        String displayPrefix = fullKey.substring(0, 12);
        return new GeneratedApiKey(fullKey, displayPrefix);
    }
}
```

### Security Considerations
- Use the existing `PasswordEncoder` bean (BCrypt) for hashing API keys
- Never log full API key values
- The `prefix` column allows key identification without exposing the full key
