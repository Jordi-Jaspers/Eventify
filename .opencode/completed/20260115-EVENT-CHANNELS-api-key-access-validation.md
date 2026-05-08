## [2026-01-15] - Channel Access Validation via API Key

### Plan (approved)
Security service to validate API key access to channels based on ownership matching:
- Personal API key (org=null) → only personal channels by same user
- Organization API key (org!=null) → only org channels in same org
- Cross-type access always denied
- Proper HTTP status codes: 403 (denied), 404 (not found), 422 (paused)

### Actual Changes

**Backend:**
- `ChannelSecurityService` (bean name: `channelSecurity`) - For `@PreAuthorize` SpEL expressions
  - `canAccess(Long channelId, ApiKeyPrincipal principal)` - returns boolean for SpEL
- `ChannelAccessService` - For programmatic validation with ApiKey entities
  - `validateAccess(ApiKey apiKey, Long channelId)` - throws exceptions on failure
- `ChannelAccessDeniedException` - 403 exception for ownership mismatches
- `ChannelPausedException` - 422 exception for paused channels
- Updated `ApiErrorCode` with CHANNEL_ACCESS_DENIED (ERR-0048) and CHANNEL_PAUSED (ERR-0049)

**Usage with @PreAuthorize:**
```java
@PreAuthorize("@channelSecurity.canAccess(#channelId, principal)")
public ResponseEntity<...> sendEvent(@PathVariable Long channelId, ...) {
    // principal is ApiKeyPrincipal from API key authentication
}
```

**Validation Logic:**
1. Null principal → 403 ChannelAccessDeniedException
2. Null/missing channel → 404 DataNotFoundException
3. Channel PENDING_DELETION → 404 (treat as non-existent)
4. Channel PAUSED → 422 ChannelPausedException
5. Cross-type access (personal key → org channel, vice versa) → 403
6. Ownership mismatch within same type → 403

**Frontend:** N/A - Backend security infrastructure

**UI Polish:** N/A - No UI

**Testing:** 21 unit tests:
- 11 tests for `ChannelAccessService.validateAccess()` (ApiKey entity)
- 10 tests for `ChannelSecurityService.canAccess()` (ApiKeyPrincipal for @PreAuthorize)

### Agents Used
- java-testing-agent: Created initial ChannelAccessServiceTest
- java-backend-agent: Implemented service and exceptions
- Orchestrator: Split into ChannelSecurityService + ChannelAccessService

### Files Created
- `server/src/main/java/io/github/eventify/api/channel/service/ChannelSecurityService.java`
- `server/src/main/java/io/github/eventify/api/channel/service/ChannelAccessService.java`
- `server/src/main/java/io/github/eventify/common/exception/ChannelAccessDeniedException.java`
- `server/src/main/java/io/github/eventify/common/exception/ChannelPausedException.java`
- `server/src/test/java/io/github/eventify/api/channel/service/ChannelSecurityServiceTest.java`
- `server/src/test/java/io/github/eventify/api/channel/service/ChannelAccessServiceTest.java`

### Files Modified
- `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java`

### Quality Metrics
- ✅ Tests: 21 written, 21 passing
- ✅ Code quality: spotless, checkstyle, PMD, SpotBugs all passing
- ✅ Build: Successful (unit tests)

### Notes
- `ChannelSecurityService` follows pattern of `OrganizationSecurityService`
- Bean name `channelSecurity` allows SpEL expressions: `@channelSecurity.canAccess(...)`
- Services ready for integration with Event Ingestion endpoint (future story)
