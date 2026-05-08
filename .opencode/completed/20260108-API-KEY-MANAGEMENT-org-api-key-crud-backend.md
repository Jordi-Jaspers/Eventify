## [2026-01-08] - Organization API Key CRUD Backend

### Plan (approved)
Enable organization OWNER/ADMIN to create, list, and revoke API keys for their organization. Members have read-only access. Global admins can manage any org's keys. No limit on org keys (unlike user 5-key limit). Keys use `org_` prefix and track who created them.

### Actual Changes

**Backend:**
- New `OrganizationApiKeyController` with SpEL security annotations
  - POST `/v1/organization/{orgId}/api-keys` - Create (OWNER/ADMIN only)
  - GET `/v1/organization/{orgId}/api-keys` - List (any member)
  - DELETE `/v1/organization/{orgId}/api-keys/{keyId}` - Revoke (OWNER/ADMIN only)
- Extended `ApiKeyService` with 3 organization methods
- Extended `ApiKeyRepository` with 2 org-specific queries
- Extended `ApiKeyCreationResponse` and `ApiKeyResponse` with `createdBy` field
- Extended `ApiKeyMapper` with UserMapper integration for createdBy
- Added path constants to `Paths.java`

**Security:**
- SpEL: `@orgSecurity.isOwnerOrAdmin` for write operations
- SpEL: `@orgSecurity.isMember` for read operations
- `hasAuthority('MANAGE_ORGANIZATIONS')` for global admin bypass
- Cross-org access prevented (returns 404 to avoid enumeration)

### Agents Used
- **java-testing-agent**: Created 27 integration tests
- **java-backend-agent**: Implemented all components

### Files Modified
- `server/src/test/java/.../OrganizationApiKeyControllerTest.java` (NEW - 827 lines)
- `server/src/main/java/.../OrganizationApiKeyController.java` (NEW)
- `server/src/main/java/.../ApiKeyService.java` (extended)
- `server/src/main/java/.../ApiKeyRepository.java` (extended)
- `server/src/main/java/.../ApiKeyCreationResponse.java` (extended)
- `server/src/main/java/.../ApiKeyResponse.java` (extended)
- `server/src/main/java/.../ApiKeyMapper.java` (extended)
- `server/src/main/java/.../Paths.java` (added constants)

### Quality Metrics
- Tests: 27 written, 27 passing
- Coverage: All acceptance criteria covered
- Build: Successful (570 total tests passing)
- Quality checks: Spotless, Checkstyle, PMD passed
