# Multi-Device Session Management

**Completed:** 2026-05-04
**Epic:** OAUTH2 Authentication
**Source:** `.opencode/refined/AUTH-01-multi-device-session-management.md`

## Summary

Removed single-session enforcement so users can stay logged in on multiple devices simultaneously. Added a Sessions tab in Profile listing all active sessions with revoke controls, captured device/IP/UA metadata on every login (standard + OAuth2), and switched session identity to a principal-bound `refreshTokenId` so logout reliably revokes only the current session. Three follow-up bug-fix passes hardened cookie behavior on Safari, normalized IPv6 loopback addresses, and added a defensive cookie fallback for logout.

## Approved Plan

Hybrid refactoring + greenfield, 7 phases. Phase 0 test-impact analysis identified 1 conflicting test (`logoutSuccess`); 28 other auth/token tests unaffected. Plan approved by user along with the recommended scoped follow-up pass replacing the cookie-based session identity with `principal.refreshTokenId` for cleaner architecture and fixing a latent silent-null bug in token refresh.

### Requirements Summary

- Login on Device B does NOT invalidate Device A
- Unlimited concurrent sessions per user
- Sessions tab in Profile lists all active sessions (device, IP, last active, current marker)
- Revoke individual session + "Revoke all other sessions"
- Capture device info from User-Agent + IP on every login (standard and OAuth2)
- New Token columns: `device_info`, `ip_address`, `last_active_at`, `user_agent`
- Endpoints: `GET /v1/user/sessions`, `DELETE /v1/user/sessions/{id}`, `DELETE /v1/user/sessions`

### Technical Approach

- **Backend**: Token entity expanded; `TokenService.generateAuthorizationTokens(User, HttpServletRequest)` (no-arg overload removed); `SessionService` + `SessionController` + `SessionMapper`; `DeviceInfoExtractor` utility with X-Forwarded-For support and IPv6 loopback normalization; `UserTokenPrincipal.refreshTokenId` populated by `JwtAuthenticationFilter` on access-token path and on refresh; single-query revoke-all-others via `TokenRepository.deleteByUserAndTypeAndIdNot`; configurable `Secure` cookie flag via `SecurityProperties.secureCookies`.
- **Frontend**: New route `/profile/sessions`, `SessionsTable` + `SessionRow` + `SessionRowSkeleton` components, `SessionService.svelte.ts` factory, `UserSessionController.ts`, generic `ConfirmDialog` primitive extracted at `$lib/components/ui/confirm-dialog/`. Sessions tab added to `SettingsNav` with Shield icon.
- **Database**: Liquibase migration adds 4 columns + composite index on `(user_id, type, last_active_at)`.
- **Security**: Token values removed from log statements; cookie security made environment-aware (production HTTPS unchanged, dev allows non-Secure cookies for Safari compatibility).

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 0 | deep-research-agent | Test impact analysis (4 parallel research tasks) |
| 1 | spring-testing-agent | Write tests + production stubs |
| 2 | spring-backend-agent | Implement to pass tests |
| 3 | backend-optimizer-agent (×3) | Maintainability passes + scoped follow-up + final tightening |
| 4 | svelte-frontend-agent | Sessions UI |
| 5 | frontend-optimizer-agent | Extract ConfirmDialog, SessionRow, a11y fixes |
| 6 | spring-testing-agent + spring-backend-agent | Three follow-up bug fixes (cookie config, IPv6, logout fallback) |

## Implementation

### Backend

- **Token entity** (`api/token/model/Token.java`): added `device_info` (length=255), `ip_address` (length=45), `user_agent` (length=512), `last_active_at` (OffsetDateTime). Added domain methods `captureDeviceMetadata(HttpServletRequest)` and `inheritDeviceMetadataFrom(Token)`.
- **TokenService**: removed bulk invalidation from `generateAuthorizationTokens` and `refresh`. Removed no-arg overloads. Added `deleteById(Long)` with idempotent `findById.ifPresent.delete`. Refresh token rotation now preserves device metadata and updates `lastActiveAt`. Stopped logging token values.
- **TokenRepository**: added `findByUserAndType(User, TokenType)` and `deleteByUserAndTypeAndIdNot(User, TokenType, Long keepId)` for single-query revoke-all-others. Removed unused `deleteByValue`.
- **SessionService** (`api/session/service/SessionService.java`): `listSessions(User)`, `listSessionsForUser(User, Long currentTokenId)`, `revokeSession(User, Long)` with ownership check (throws `DataNotFoundException` for both missing and not-owned to prevent existence leaks), `revokeAllOtherSessions(User, Long currentTokenId)` using single-query delete. `SESSION_ORDER` static comparator sorts current first, then by lastActiveAt DESC.
- **SessionController** (`api/session/controller/SessionController.java`): thin pass-through using `principal.getRefreshTokenId()`. No cookie or HttpServletRequest dependency.
- **DeviceInfoExtractor** (`common/util/DeviceInfoExtractor.java`): static utility extracting User-Agent label (e.g. "Chrome on Mac"), IP address (X-Forwarded-For first IP → remoteAddr fallback), with IPv6 loopback normalization (`::1` and `0:0:0:0:0:0:0:1` → `127.0.0.1`).
- **UserTokenPrincipal** (`common/security/principal/UserTokenPrincipal.java`): added `Long refreshTokenId`. 3-arg `@AllArgsConstructor` plus 2-arg convenience constructor delegating with `null` id.
- **JwtAuthenticationFilter** (`common/security/filter/JwtAuthenticationFilter.java`): `tryRefreshTokens` flattened from 4-deep nesting to guard-clause pattern, `applyRefreshedAuth` helper extracted. Reads `refreshedUser.getRefreshToken().getId()` directly after refresh (saves DB roundtrip and fixes a latent null bug where principal would have null id after refresh).
- **AuthenticationController.logout**: now `(@AuthenticationPrincipal principal, HttpServletRequest request, HttpServletResponse response)` — uses principal id when available, falls back to refresh-token cookie lookup as defensive measure.
- **AuthenticationService.logout(UserTokenPrincipal, HttpServletRequest)**: cookie fallback via `cookieService.readRefreshTokenValue(request)` → `tokenService.findAuthorizationTokenByValue` → `deleteById`. Logs WARN when fallback path used (diagnostic).
- **OAuth2AuthenticationSuccessHandler**: passes `HttpServletRequest` through to `generateAuthorizationTokens` so OAuth2 logins also capture device info.
- **EmailVerificationController + AuthenticationService.verifyEmail**: now thread `HttpServletRequest` so verify-email-issued sessions also have device metadata.
- **SecurityProperties**: added `secureCookies` boolean (default `true`).
- **CookieService**: `Secure` flag now reads from `securityProperties.isSecureCookies()`. Added `readRefreshTokenValue(HttpServletRequest) → Optional<String>`. Extracted `COOKIE_PATH`, `SAME_SITE_ATTRIBUTE`, `SAME_SITE_VALUE` constants. Removed token values from debug logs.
- **application-dev.yml**: `security.secure-cookies: false` to fix Safari cookie persistence on http://localhost.
- **Migration**: Liquibase changeset adds 4 columns to `token` table + composite index on `(user_id, type, last_active_at)` using raw `<sql>` per project convention.

### Frontend

- **Route** (`client/src/routes/(authenticated)/profile/sessions/+page.svelte`): page using `SettingsNav` + glassmorphism Card matching sibling profile pages. Header action button for "Revoke all other sessions".
- **SessionsTable** (`client/src/lib/components/profile/SessionsTable.svelte`): shadcn-svelte Table primitives (not DataTable since endpoint returns plain List). Renders `SessionRow` per session, `SessionRowSkeleton` during load, empty state, current-session badge.
- **SessionRow** (`client/src/lib/components/profile/SessionRow.svelte`): single row with device label, IP, relative-time last-active, current badge, revoke button (disabled with descriptive aria-label for current session).
- **ConfirmDialog** (`client/src/lib/components/ui/confirm-dialog/`): generic confirm-dialog primitive (props: open, title, confirmLabel, cancelLabel, destructive, onOpenChange, onConfirm, description snippet). Reusable across the app.
- **SessionService.svelte.ts** (`client/src/lib/api/user/service/`): factory function returning state getters + actions; `$state` reactivity. Toast via `svelte-sonner` for success and error.
- **UserSessionController.ts** (`client/src/lib/api/user/`): API client wrapping `client.GET/DELETE` for the three session endpoints.
- **SettingsNav.svelte**: Sessions tab with Shield icon already wired from prior session.
- **routes.ts**: `PROFILE_SESSIONS_PAGE` route already wired from prior session.

### Deviations from Plan

- **Story scope expanded mid-flight** with a recommended scoped follow-up pass (principal-based session identity, single-query revoke-all-others, no token values in logs) after orchestrator security/efficiency assessment. Approved by user. Cleaner architecture; eliminated cookie reads from controllers.
- **Three follow-up bug fixes** added after manual testing surfaced (1) logout not revoking current session in some flows, (2) IPv6 localhost displaying as `0:0:0:0:0:0:0:1`, (3) Safari unable to redirect after login due to always-Secure cookie flag on http://localhost. All three fixed in a single TDD bundle.
- **YAML key fix** for the Safari cookie issue: `application-dev.yml` initially set `security.cookies.secure: false` but `SecurityProperties` binds via relaxed binding to `security.secure-cookies`. Fixed inline by orchestrator.
- **Hashing + theft detection** (originally listed as "future work" in scope notes) deferred to AUTH-04, which was also expanded to cover same-session-replace via shared token-family-id infrastructure.

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent (×4 parallel) | Test impact + backend/frontend pattern research | Complete |
| spring-testing-agent | Initial test suite (29 tests + stubs) | Complete |
| spring-backend-agent | Initial implementation | Escalated stub conflict |
| spring-testing-agent | Reconcile OAuth2 test stubs (5 tests updated) | Complete |
| backend-optimizer-agent | Round 1: 4 user-flagged cleanups | Complete |
| backend-optimizer-agent | Round 2: full 20-category maintainability pass | Complete |
| spring-testing-agent | Update SessionServiceTest constructor for SessionMapper injection | Complete |
| spring-testing-agent | Phase A of scoped follow-up: principal-based identity tests | Complete |
| spring-backend-agent | Phase B implementation | Complete (with one defensive override needed) |
| spring-backend-agent | Phase B fix: thread HttpServletRequest through verifyEmail, remove cleanup | Complete |
| backend-optimizer-agent | Round 3: final tightening (5 cleanups + dead code) | Complete |
| svelte-frontend-agent | Sessions UI implementation | Complete |
| frontend-optimizer-agent | Extract ConfirmDialog, SessionRow, a11y fixes, dead state cleanup | Complete |
| deep-research-agent | Diagnose Safari login failure | Root cause confirmed |
| spring-testing-agent | Tests for cookie config, IPv6 normalization, logout cookie fallback | Complete |
| spring-backend-agent | Implement three follow-up fixes | Complete |

## Files Modified

### Backend (production)
- `server/src/main/java/io/github/eventify/api/token/model/Token.java` — 4 new columns + 2 domain methods
- `server/src/main/java/io/github/eventify/api/token/service/TokenService.java` — removed bulk invalidation; added `deleteById`; finder methods stop logging token values
- `server/src/main/java/io/github/eventify/api/token/repository/TokenRepository.java` — added `findByUserAndType`, `deleteByUserAndTypeAndIdNot`; removed `deleteByValue`
- `server/src/main/java/io/github/eventify/api/session/service/SessionService.java` — new
- `server/src/main/java/io/github/eventify/api/session/controller/SessionController.java` — new
- `server/src/main/java/io/github/eventify/api/session/model/response/SessionResponse.java` — new
- `server/src/main/java/io/github/eventify/api/session/model/mapper/SessionMapper.java` — new
- `server/src/main/java/io/github/eventify/common/util/DeviceInfoExtractor.java` — new (with IPv6 loopback normalization)
- `server/src/main/java/io/github/eventify/common/security/principal/UserTokenPrincipal.java` — added refreshTokenId
- `server/src/main/java/io/github/eventify/common/security/filter/JwtAuthenticationFilter.java` — populates refreshTokenId; flattened tryRefreshTokens
- `server/src/main/java/io/github/eventify/api/authentication/controller/AuthenticationController.java` — logout signature update + refresh threads request
- `server/src/main/java/io/github/eventify/api/authentication/service/AuthenticationService.java` — per-session logout with cookie fallback; refresh + verifyEmail accept request
- `server/src/main/java/io/github/eventify/api/authentication/controller/EmailVerificationController.java` — accepts HttpServletRequest
- `server/src/main/java/io/github/eventify/api/authentication/service/CookieService.java` — configurable Secure flag, readRefreshTokenValue, no token values in logs
- `server/src/main/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandler.java` — passes HttpServletRequest
- `server/src/main/java/io/github/eventify/common/config/properties/SecurityProperties.java` — `secureCookies` field
- `server/src/main/java/io/github/eventify/api/Paths.java` — `USER_SESSIONS_PATH`, `USER_SESSION_PATH`
- `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java` — `SESSION_NOT_FOUND_ERROR` (ERR-0052)
- `server/src/main/resources/db/changelog/...` — Liquibase migration (4 columns + index)
- `server/src/main/resources/application-dev.yml` — `security.secure-cookies: false`

### Backend (tests)
- `AuthenticationControllerTest`, `TokenServiceTest` (new), `SessionServiceTest` (new), `SessionControllerTest` (new), `DeviceInfoExtractorTest` (new), `OAuth2AuthenticationSuccessHandler*Test` (5 files updated), `CookieServiceTest` (new), `AuthenticationControllerLogoutTest` (new), `JwtAuthenticationFilterTest` (new), `support/IntegrationTest`

### Frontend
- `client/src/routes/(authenticated)/profile/sessions/+page.svelte` — new
- `client/src/lib/components/profile/SessionsTable.svelte` — new
- `client/src/lib/components/profile/SessionRow.svelte` — new
- `client/src/lib/components/profile/SessionRowSkeleton.svelte` — new
- `client/src/lib/components/profile/index.ts` — barrel export
- `client/src/lib/components/ui/confirm-dialog/confirm-dialog.svelte` — new generic primitive
- `client/src/lib/components/ui/confirm-dialog/index.ts` — barrel export
- `client/src/lib/api/user/service/SessionService.svelte.ts` — new
- `client/src/lib/api/user/UserSessionController.ts` — new
- `client/src/lib/components/settings/SettingsNav.svelte` — Sessions tab (pre-wired)
- `client/src/lib/config/routes.ts` — PROFILE_SESSIONS_PAGE (pre-wired)
- `client/src/lib/types/api.d.ts` — regenerated via `bun run sync:api`
- `client/src/lib/data/changelog.ts` — version 0.1.0 entry

## Tests

- Backend: 1264 tests passing (up from 1242 pre-AUTH-01). 0 failures. spotlessCheck ✅, pmdMain ✅.
- Frontend: `bun run check` 0 errors, `bun run build` ✅.
- Screenshot tests skipped per `screenshot_tests_enabled = false`.
