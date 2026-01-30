## [2026-01-07] - API Key Authentication Filter

### Plan (approved)
**User Story**: As an external system or integration, I want to authenticate using an API key via the `X-Api-Key` header, so that I can send events to Eventify programmatically without user credentials.

**Requirements**:
1. Authenticate requests with `X-Api-Key` header for event endpoints only (`/v1/events`, `/v1/channels`)
2. Support both user keys (`evt_*`) and organization keys (`org_*`)
3. Reject expired, invalid, and revoked keys with proper error codes
4. Verify user is enabled for USER scope keys
5. Update usage stats (lastUsedAt, totalRequests) on successful auth
6. JWT continues working for all endpoints (no regression)
7. API key takes precedence when both headers present for event endpoints

**Technical Approach**:
- New `ApiKeyAuthenticationFilter` extending `OncePerRequestFilter`
- New `ApiKeyAuthenticationService` for validation logic
- New `ApiKeyPrincipal` and `ApiKeyAuthenticationToken` for security context
- Register filter BEFORE JWT filter in security chain
- Lookup by suffix (last 4 chars) for O(1) candidate lookup, then BCrypt verify

### Actual Changes

**Backend**:
- Created `ApiKeyAuthenticationFilter` - processes X-Api-Key header for /v1/events and /v1/channels
- Created `ApiKeyAuthenticationService` - validates key format, hash, expiration, user status
- Created `ApiKeyPrincipal` - principal with limited SEND_EVENTS authority
- Created `ApiKeyAuthenticationToken` - Spring Security authentication token
- Created 3 exception classes: `InvalidApiKeyException`, `ApiKeyExpiredException`, `UserDisabledException`
- Extended `ApiKeyRepository` with `findBySuffix()` method
- Added 3 error codes to `ApiErrorCode`: INVALID_API_KEY, API_KEY_EXPIRED, API_KEY_USER_DISABLED
- Added path constants: EVENTS_PATH, CHANNELS_PATH
- Updated `WebSecurityConfig` to register filter before JWT filter

**Testing**: 28 tests covering all acceptance criteria
- `ApiKeyAuthenticationServiceTest` - 13 unit tests
- `ApiKeyAuthenticationFilterTest` - 15 unit tests

### Agents Used
1. **java-testing-agent**: Created comprehensive test suite (Phase 1 - TDD)
2. **java-backend-agent**: Implemented all components (Phase 2)
3. **orchestrator**: Fixed test issues (lenient mocking), quality check violations

### Files Created
```
server/src/main/java/io/github/eventify/
├── api/apikey/service/
│   └── ApiKeyAuthenticationService.java
├── common/exception/
│   ├── InvalidApiKeyException.java
│   ├── ApiKeyExpiredException.java
│   └── UserDisabledException.java
├── common/security/filter/
│   └── ApiKeyAuthenticationFilter.java
└── common/security/principal/
    ├── ApiKeyPrincipal.java
    └── ApiKeyAuthenticationToken.java

server/src/test/java/io/github/eventify/
├── api/apikey/service/
│   └── ApiKeyAuthenticationServiceTest.java
└── common/security/filter/
    └── ApiKeyAuthenticationFilterTest.java
```

### Files Modified
- `server/src/main/java/io/github/eventify/api/Paths.java` - Added EVENTS_PATH, CHANNELS_PATH
- `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java` - Added 3 error codes
- `server/src/main/java/io/github/eventify/api/apikey/repository/ApiKeyRepository.java` - Added findBySuffix()
- `server/src/main/java/io/github/eventify/common/config/WebSecurityConfig.java` - Registered filter

### Quality Metrics
- Tests: 28 written, 28 passing
- Coverage: All acceptance criteria covered
- Build: Successful (quality checks pass: checkstyle, pmd, spotbugs)
- Security: BCrypt timing-safe comparison, no key logging, context cleared on failure

### Acceptance Criteria Status
- [x] AC1: Valid user API key authenticates, updates stats
- [x] AC2: Valid org API key authenticates with org context
- [x] AC3: Expired key rejected (API_KEY_EXPIRED)
- [x] AC4: Invalid/revoked key rejected (INVALID_API_KEY)
- [x] AC5: Malformed key rejected (INVALID_API_KEY)
- [x] AC6: API key only for /v1/events and /v1/channels
- [x] AC7: JWT still works (no regression)
- [x] AC8: API key precedence (filter runs before JWT filter)
- [x] AC9: Disabled user's key rejected (API_KEY_USER_DISABLED)
