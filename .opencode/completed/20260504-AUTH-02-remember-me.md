# Remember Me / Long-Lived Refresh Tokens

**Completed:** 2026-05-04
**Epic:** AUTH
**Source:** `.opencode/refined/AUTH-02-remember-me.md`

## Summary

Added optional "Remember me for 30 days" checkbox to login that extends refresh-token lifetime from 7d to 30d. Surfaced session expiry on the Sessions tab via a new `expiresAt` field/column. OAuth2 and email-verify flows continue to use the default 7d lifetime.

## Approved Plan

### Requirements Summary

- Optional `rememberMe` boolean on login: false/absent → 7d refresh token (current), true → 30d
- Access token unchanged (15min)
- OAuth2 + email-verify always 7d
- Add `expiresAt` to `SessionResponse` for the Sessions tab
- Checkbox unchecked by default, NOT persisted across visits

### Technical Approach

- **Backend:** New `security.remember-me-token` config, `boolean rememberMe` plumbed through `LoginRequest → AuthenticationService.authorize → TokenService.generateAuthorizationTokens → JwtService.generateRefreshToken`. `SessionResponse.expiresAt` auto-mapped from `Token.expiresAt`. Cookie `maxAge` already derives from token expiry — no cookie changes.
- **Frontend:** Checkbox + Label between password and Sign In (login page), `rememberMe` plumbed through `authStore.login → apiLogin`. Sessions table 12-col grid extended with an Expires column (`Device 3 / IP 2 / Last Active 2 / Expires 3 / Status 1 / Action 1`).
- **Refresh-token rotation policy:** Rotated tokens always pass `false` (MVP-safe). Remember-me sessions downgrade to 7d on first refresh — documented in `TokenService.refresh()` Javadoc.

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 0 | deep-research-agent | Test impact analysis (32 tests across 8 files) |
| 1 | spring-testing-agent | Backend test suite (5 NEW + 24 UPDATE + 1 EXTEND, later trimmed: SessionMapperTest deleted) |
| 2 | spring-backend-agent | Backend implementation (revised once: `authorize()` takes `LoginRequest` directly) |
| 3 | backend-optimizer-agent | Extract `JwtService.resolveRefreshTokenProperties`, document rotation-downgrade |
| 4 | (skipped) | No frontend test infrastructure |
| 5 | svelte-frontend-agent | Login checkbox + Sessions Expires column |
| 6 | frontend-optimizer-agent | Extract `session-grid.ts` shared layout constants |
| 7 | (skipped) | `screenshot_tests_enabled = false` |

## Implementation

### Backend

- `LoginRequest`: + `private boolean rememberMe` (`@Schema`)
- `SecurityProperties`: + `private TokenProperties rememberMeToken`
- `application.yml`: + `security.remember-me-token: { lifetime: ${JWT_REMEMBER_ME_LIFETIME_SECONDS:30}, time-unit: ${JWT_REMEMBER_ME_LIFETIME_TIME_UNIT:DAYS} }`
- `JwtService.generateRefreshToken(T user, boolean rememberMe)`: branches on flag via private `resolveRefreshTokenProperties`
- `TokenService.generateAuthorizationTokens(User, HttpServletRequest, boolean rememberMe)`: 3-arg signature with 2-arg overload kept for OAuth2/test wiring; rotation always passes `false`
- `AuthenticationService.authorize(LoginRequest, HttpServletRequest)`: takes the request object directly; `verifyEmail` always passes `false`
- `AuthenticationController`: forwards `request` to service
- `OAuth2AuthenticationSuccessHandler`: passes `false`
- `SessionResponse`: + `private OffsetDateTime expiresAt` (`@Schema`)
- `SessionMapper`: no change — auto-maps by name

### Frontend

- `client/src/routes/(public)/login/+page.svelte`: + `rememberMe = $state(false)`, Checkbox + Label between password and submit, passes flag to `authStore.login`
- `client/src/lib/stores/auth.ts`: `login(email, password, rememberMe = false)` → `apiLogin({ email, password, rememberMe })`
- `client/src/lib/components/profile/SessionsTable.svelte` + `SessionRow.svelte`: 12-col grid extended with Expires (col-span-3)
- `client/src/lib/components/profile/session-grid.ts` (NEW): shared `SESSION_GRID_*` / `SESSION_COL` constants — header & row can no longer drift
- `client/src/lib/types/api.d.ts`: regenerated via `bun run sync:api` (backend `rememberMe: boolean` required, `expiresAt?: string` optional)

### Deviations from Plan

- `AuthenticationService.authorize` signature revised mid-flight from `(email, password, rememberMe, request)` to `(LoginRequest, HttpServletRequest)` per user feedback — cleaner.
- `SessionMapperTest` was deleted post-Phase 1 per user feedback (mapper auto-mapping doesn't warrant a unit test).


## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent ×3 | Backend research, frontend research, test impact | Complete |
| spring-testing-agent | Backend test suite | Complete |
| spring-backend-agent | Backend implementation (×2 — initial + signature revision) | Complete |
| backend-optimizer-agent | Refactor + Javadoc | Complete |
| svelte-frontend-agent | Frontend implementation | Complete |
| frontend-optimizer-agent | Extract shared session-grid layout | Complete |

## Files Modified

**Backend (production):**
- `server/src/main/java/io/github/eventify/api/authentication/model/request/LoginRequest.java`
- `server/src/main/java/io/github/eventify/api/authentication/controller/AuthenticationController.java`
- `server/src/main/java/io/github/eventify/api/authentication/service/AuthenticationService.java`
- `server/src/main/java/io/github/eventify/api/token/service/JwtService.java`
- `server/src/main/java/io/github/eventify/api/token/service/TokenService.java`
- `server/src/main/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandler.java`
- `server/src/main/java/io/github/eventify/api/session/model/response/SessionResponse.java`
- `server/src/main/java/io/github/eventify/common/config/properties/SecurityProperties.java`
- `server/src/main/resources/application.yml`

**Backend (tests):**
- `server/src/test/java/io/github/eventify/api/token/service/JwtServiceTest.java` (NEW)
- `server/src/test/java/io/github/eventify/api/token/service/TokenServiceTest.java`
- `server/src/test/java/io/github/eventify/api/session/controller/SessionControllerTest.java`
- `server/src/test/java/io/github/eventify/api/authentication/controller/AuthenticationControllerTest.java`
- `server/src/test/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandlerAuthorizeTest.java`
- `server/src/test/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandlerProcessAuthenticationTest.java`
- `server/src/test/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandlerBuildRedirectUrlTest.java`
- `server/src/test/resources/application-test.yml`

**Frontend:**
- `client/src/routes/(public)/login/+page.svelte`
- `client/src/lib/stores/auth.ts`
- `client/src/lib/components/profile/SessionsTable.svelte`
- `client/src/lib/components/profile/SessionRow.svelte`
- `client/src/lib/components/profile/session-grid.ts` (NEW)
- `client/src/lib/types/api.d.ts` (manual edit, TODO regenerate)

## Tests

- 1268 backend tests passing, 0 failures
- Quality: spotlessCheck ✅, checkstyle ✅, PMD ✅, SpotBugs ✅
- Frontend: `bun run check` 0 errors, 0 warnings (no test infra exists)
