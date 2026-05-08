# Hash Refresh Tokens + Same-Session-Replace

**Completed:** 2026-05-08
**Epic:** AUTH
**Source:** .opencode/refined/AUTH-04-hash-refresh-tokens-same-session-replace.md

## Summary

Store SHA-256 hashed refresh tokens instead of raw values (OWASP ASVS L2). Persistent `EVENTIFY_DEVICE_ID` cookie enables same-session-replace: re-login from the same device updates the existing token row instead of creating a new one.

## Plan Approved by the user:

### Requirements Summary

- Token issuance stores only SHA-256 hash; refresh by hash lookup
- Persistent device cookie (10yr, HttpOnly, Secure, SameSite=Lax) carries family_id
- On login, upsert by (user_id, family_id) — update existing or insert new
- Logout clears auth cookies but keeps device cookie
- Access token validation unchanged (RSA only, no DB hit)
- Null/malformed device cookie falls back to insert-new-row

### Technical Approach

- Backend: HashUtil (SHA-256), Token entity (valueHash, familyId, rawValue transient), TokenRepository (findByValueHash, findByUserIdAndFamilyId), TokenService (upsert logic), CookieService (device cookie), JwtAuthenticationFilter (ensureDeviceId)
- Database: Migration drops value column, adds value_hash + family_id + indexes, deletes existing refresh tokens
- Frontend: No changes (backend-only)

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Create/update backend test suite (29 conflicts + 5 new TokenHasher + 5 new device cookie) |
| 2 | spring-backend-agent | Implement to pass tests |
| 3 | spring-backend-agent | Refactoring fixes (HashUtil move, controller simplify, extract upsert, edge cases) |
| 4 | spring-testing-agent | Edge case tests (null familyId, malformed UUID) |
| 5 | backend-optimizer-agent | Code quality optimization |

## Implementation

### Backend

- **HashUtil** (`common.util`) — `@UtilityClass`, `sha256(String)` → 64-char hex
- **Token entity** — `valueHash` (VARCHAR 64, unique), `familyId` (UUID), `rawValue` (@Transient); dropped `value` column
- **TokenRepository** — `findByValueHash(String)`, `findByUserIdAndFamilyId(Long, UUID)`
- **TokenService** — `generateAuthorizationTokens(User, HttpServletRequest, boolean, UUID familyId)` with upsert; `refresh()` hashes before lookup; null familyId guard
- **CookieService** — `ensureDeviceId()`, `readDeviceId()`, `setDeviceCookie()`; extracted `readCookieValue()` helper; `clearAuthCookies` does NOT clear device cookie
- **AuthenticationController** — simplified logout (removed dead cookie reads)
- **JwtAuthenticationFilter** — calls `ensureDeviceId` on every request
- **Constants** — added `DEVICE_ID_COOKIE = "EVENTIFY_DEVICE_ID"`

### Database

- Migration: `202605081000-PRD-hash-refresh-tokens-add-family-id.xml`
  - Deletes existing REFRESH_TOKENs (breaking change — all sessions invalidated)
  - Drops `value` column + `idx_token_value` index
  - Adds `value_hash VARCHAR(64) NOT NULL` + `family_id UUID NOT NULL`
  - Creates `idx_token_value_hash` (unique) + `idx_token_user_family` indexes

### Deviations from Plan

- TokenHasher moved to `common.util.HashUtil` with `@UtilityClass` (review feedback)
- Controller logout simplified — dead cookie reads removed (review feedback)
- CookieService refactored with `readCookieValue` helper (review feedback)
- `generateAuthorizationTokens` upsert logic extracted to private method (review feedback)
- Added null familyId guard + edge case tests (review feedback)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent (×4) | Research token/auth/filter/migration code | Complete |
| spring-testing-agent | Create/update 29 conflicting + 10 new tests | Complete |
| spring-backend-agent | Implement hash + device cookie + upsert | Complete |
| spring-backend-agent | Refactoring fixes (HashUtil, controller, edge cases) | Complete |
| spring-testing-agent | Edge case tests (null familyId, malformed UUID) | Complete |
| backend-optimizer-agent | Code quality optimization | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/common/util/HashUtil.java` — NEW
- `server/src/main/resources/db/changelog/changesets/202605081000-PRD-hash-refresh-tokens-add-family-id.xml` — NEW
- `server/src/main/java/io/github/eventify/api/token/model/Token.java` — valueHash, familyId, rawValue
- `server/src/main/java/io/github/eventify/api/token/repository/TokenRepository.java` — findByValueHash, findByUserIdAndFamilyId
- `server/src/main/java/io/github/eventify/api/token/service/TokenService.java` — upsert logic, hash lookups
- `server/src/main/java/io/github/eventify/api/token/service/JwtService.java` — rawValue builder
- `server/src/main/java/io/github/eventify/api/authentication/service/AuthenticationService.java` — familyId from device cookie
- `server/src/main/java/io/github/eventify/api/authentication/service/CookieService.java` — device cookie methods
- `server/src/main/java/io/github/eventify/api/authentication/controller/AuthenticationController.java` — simplified logout
- `server/src/main/java/io/github/eventify/common/security/filter/JwtAuthenticationFilter.java` — ensureDeviceId
- `server/src/main/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandler.java` — familyId
- `server/src/main/java/io/github/eventify/common/constant/Constants.java` — DEVICE_ID_COOKIE
- `server/src/test/java/io/github/eventify/common/util/HashUtilTest.java` — NEW (5 tests)
- 13 test files updated (29 methods + 8 new tests)

## Tests

- 1351 tests passing, 0 failing
