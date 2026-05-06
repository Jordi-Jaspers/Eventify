---
epic: "AUTH"
title: "Hash refresh tokens at rest + same-session-replace via persistent device cookie"
estimate: L
status: ready
created: 2026-05-06
depends_on: ["AUTH-01-always-assign-password-remove-haspassword"]
labels: [ backend, security, sessions ]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** security-conscious platform owner\
**I want** refresh tokens stored as hashes (never plaintext) and one row per browser device in the session table\
**So that** a database leak yields no usable tokens AND users see an accurate, manageable list of "Active Sessions" instead of orphan rows accumulating on every login\

## 2. Business Context & Value

Two related hardenings sharing one schema migration:

**Hashing (Goal 1).** Today `Token.value` stores raw refresh JWTs. Anyone with read access to the `token` table (DB leak, backup compromise, rogue ops) can paste any token into a `Cookie` header and impersonate that user — no password, no MFA, just session hijack at scale. Storing only `SHA-256(token)` makes a leaked DB useless: the user's browser still has the raw token, but the attacker has a hash they cannot reverse (256-bit input entropy makes brute force physically infeasible). OWASP ASVS L2 mandates this; Auth0, Okta, Cognito, Clerk all do it. Eventify currently fails this baseline.

**Same-session-replace (Goal 3).** Observed during AUTH-01 testing: opening Safari and logging in/out 5 times produces 5 rows in the `token` table for what is functionally one browser. The "Active Sessions" UI shows 5 sessions; revoking "all other sessions" just cleans up the user's own debris. Root cause: every login inserts a new row because the server has no way to recognise "same browser as last time". Fix: a long-lived `EVENTIFY_DEVICE_ID` cookie set on first visit, separate from auth cookies and surviving logout. On every login, look up `(user_id, device_id)` — if a row exists, replace it; otherwise insert. One device = one row, forever. Pattern used at Google, Microsoft, Apple ID.

**Theft detection** (originally bundled as Goal 2) is deferred to Future Considerations — defense-in-depth feature with no concrete pre-MVP threat and behavioural complexity (network-race false positives needing grace-period logic). The `family_id` infrastructure introduced here is the prerequisite, so the future story is unblocked.

## 3. Acceptance Criteria

### Hashing (Goal 1)

* [ ] **Scenario 1 — Token issuance stores only hash**
    * Given a user logs in successfully
    * When the server issues a refresh token
    * Then the raw JWT is sent to the browser as the `EVENTIFY_REFRESH_TOKEN` cookie
    * And the database row contains only `SHA-256(rawToken)` in `value_hash` (the `value` column no longer exists)

* [ ] **Scenario 2 — Refresh by hash lookup**
    * Given a browser holds a valid refresh token cookie
    * When the browser hits the refresh endpoint
    * Then the server hashes the incoming raw token
    * And looks up the row by `value_hash` (no raw-value lookup anywhere in the code)
    * And rotates the token (issues a new raw token, stores new hash, keeps the same `family_id`)

* [ ] **Scenario 3 — Logout clears DB row and auth cookies but keeps device cookie**
    * Given an authenticated user
    * When they log out
    * Then the `token` row for `(user_id, family_id)` is deleted
    * And `EVENTIFY_ACCESS_TOKEN` and `EVENTIFY_REFRESH_TOKEN` cookies are cleared (Max-Age=0)
    * And `EVENTIFY_DEVICE_ID` cookie remains intact in the browser

* [ ] **Scenario 4 — Access token validation unchanged**
    * Given an incoming request with a valid access token cookie
    * When `JwtAuthenticationFilter` processes it
    * Then validation uses RSA signature verification only (no DB hit)
    * And no behavioural change versus current implementation

### Same-session-replace (Goal 3)

* [ ] **Scenario 5 — First visit ever generates device cookie**
    * Given a browser with no `EVENTIFY_DEVICE_ID` cookie
    * When it makes any request to the application
    * Then the server generates a UUID and sets `EVENTIFY_DEVICE_ID` as `HttpOnly`, `Secure`, `SameSite=Lax`, `Max-Age=10y`, `Path=/`
    * And the cookie is sent on all subsequent requests

* [ ] **Scenario 6 — Re-login on same browser replaces the row**
    * Given a browser with an existing `EVENTIFY_DEVICE_ID` cookie that has logged in before
    * When the user logs out and logs in again as the same user
    * Then no new row is inserted; the existing row for `(user_id, device_id)` is updated with the new `value_hash` and refreshed `last_active_at`
    * And the "Active Sessions" page shows exactly 1 row for that device after 5 sequential login/logout cycles

* [ ] **Scenario 7 — Login as different user on same browser inserts new row**
    * Given a browser with `EVENTIFY_DEVICE_ID` cookie that has a session row for user A
    * When user A logs out and user B logs in
    * Then a new row is inserted for `(userB, device_id)`
    * And user A's row remains untouched (it is user A's session, not this device's session)
    * And `device_id` is per-device, not per-user; rows are keyed on `(user, device)`

* [ ] **Scenario 8 — Login from a second browser inserts new row**
    * Given user A logged in on browser 1 (device_id_1)
    * When user A logs in from browser 2 (device_id_2, separate UUID)
    * Then a new row is inserted; both sessions appear in "Active Sessions"

* [ ] **Scenario 9 — Manual revoke from "Active Sessions" deletes the row**
    * Given user A has a session row for `(userA, device_id_X)`
    * When user A revokes that session from the UI
    * Then the row is deleted
    * And the next login on that browser inserts a fresh row (no orphans)

* [ ] **Scenario 10 — Refresh preserves `family_id`**
    * Given a token row with `family_id = F1`
    * When the refresh endpoint rotates the token
    * Then the row is updated in place: `value_hash` changes, `family_id` remains `F1`

* [ ] **Scenario 11 — OAuth callback flow propagates device cookie**
    * Given a browser with `EVENTIFY_DEVICE_ID` cookie initiates OAuth login
    * When the user is redirected back from Google/GitHub and `OAuth2AuthenticationSuccessHandler` runs
    * Then the handler reads `EVENTIFY_DEVICE_ID` from the request
    * And applies same-session-replace logic against `(user_id, device_id)`
    * And the cookie survives the redirect chain (`SameSite=Lax` allows top-level redirects)

* [ ] **Scenario 12 — Email-verification auto-login uses device cookie**
    * Given a newly-registered user clicks the verification link in the same browser they registered from
    * When verification succeeds and auto-login runs
    * Then session creation uses the existing `EVENTIFY_DEVICE_ID` (replaces if a row exists, inserts otherwise)

### Edge cases

* [ ] **Edge case 13 — User clears all cookies**
    * Given a user with an active session clears their browser cookies
    * When they log in again
    * Then a new `EVENTIFY_DEVICE_ID` is generated and a new row is inserted
    * And the previous row remains in DB until the user revokes it manually from "Active Sessions" or it expires
    * (Acceptable: matches Google/Microsoft behaviour)

* [ ] **Edge case 14 — Incognito / private window**
    * Given a user logs in from a private/incognito window
    * When the window is closed and re-opened
    * Then a fresh `EVENTIFY_DEVICE_ID` is generated each session (cookie not persisted)
    * And each session creates a row that becomes orphaned on window close
    * (Acceptable: documented limitation)

* [ ] **Edge case 15 — Cookies disabled entirely**
    * Given a browser with all cookies disabled
    * When the user attempts to log in
    * Then login fails (no session can be established)
    * (Same as today; not a regression)

* [ ] **Edge case 16 — Two tabs racing on refresh**
    * Given two tabs of the same browser hold the same refresh token
    * When both tabs simultaneously hit the refresh endpoint
    * Then both compute the same hash, both find the same row
    * One transaction wins the rotation; the other gets `401`
    * The losing tab silently re-fetches the now-current cookie value and retries (existing client behaviour)
    * (No theft detection in this scope, so no family revocation triggered)

* [ ] **Edge case 17 — Migration wipes existing sessions**
    * Given a deployed system with existing rows in `token`
    * When the Liquibase migration runs
    * Then all existing `REFRESH_TOKEN` rows are deleted in a pre-migration step (because raw values cannot be retroactively hashed)
    * And every user must log in once after deploy
    * (Acceptable: pre-MVP, low cost, simpler than dual-mode lookup)

## 4. Technical Requirements

* **API Changes**: No public API contract change. `POST /v1/authentication/refresh` still accepts the refresh token from cookie and rotates it. Internal: refresh token comparison switches from `findByValue(rawToken)` to `findByValueHash(sha256Hex(rawToken))`.

* **Database**: Liquibase changeset under `server/src/main/resources/db/changelog/changesets/` (next sequential timestamp).
    * Pre-step: `DELETE FROM token WHERE type = 'REFRESH_TOKEN';` (wipes existing sessions)
    * `ALTER TABLE token DROP COLUMN value;`
    * `ALTER TABLE token ADD COLUMN value_hash VARCHAR(64) NOT NULL;` (SHA-256 hex = 64 chars)
    * `ALTER TABLE token ADD COLUMN family_id UUID NOT NULL;`
    * `CREATE UNIQUE INDEX idx_token_value_hash ON token(value_hash);`
    * `CREATE INDEX idx_token_user_family ON token(user_id, family_id);`
    * Use `<sql>` tags only per liquibase-migrations-standards skill — no `<addColumn>` / `<dropColumn>` Liquibase XML.

* **Security**:
    * `EVENTIFY_DEVICE_ID` cookie attributes: `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/`, `Max-Age=315360000` (10 years in seconds).
    * Hash algorithm: `MessageDigest.getInstance("SHA-256")` → hex-encoded with `org.apache.commons.codec.binary.Hex.encodeHexString` (already on classpath via Spring).
    * Hash computation must happen on every refresh-token persist and lookup site — no raw token values written to DB ever.
    * Logout endpoint MUST NOT clear `EVENTIFY_DEVICE_ID`.

* **Performance**: SHA-256 of a ~500-byte JWT is sub-microsecond on commodity hardware. No measurable latency impact on refresh path.

## 5. Design & UI/UX

N/A — backend-only change. The "Active Sessions" page consumes existing endpoints and will simply show fewer (and correct) rows after this change. No UI work required.

Optional note for follow-up: the "Active Sessions" list currently shows `device_info` and `user_agent` derived from request headers. With persistent `device_id`, a future enhancement could show "This device" badge by comparing the request's `EVENTIFY_DEVICE_ID` against each row's `family_id`. Out of scope for this story.

## 6. Implementation Notes

### Files to touch (8 backend files + 1 migration + tests)

1. **`server/src/main/java/io/github/eventify/api/token/model/Token.java`**
   - Remove `value` field (lines 51-56).
   - Add `valueHash String` field (`@Column(name = "value_hash", unique = true, nullable = false)`).
   - Add `familyId UUID` field (`@Column(name = "family_id", nullable = false)`).
   - Update `@EqualsAndHashCode.Include` to `valueHash`.

2. **`server/src/main/java/io/github/eventify/api/token/repository/TokenRepository.java`**
   - Replace `findByValue(String token)` (line 50) → `findByValueHash(String hash)`.
   - Add `findByUserIdAndFamilyId(Long userId, UUID familyId)` for same-session-replace lookup.

3. **`server/src/main/java/io/github/eventify/api/token/service/TokenService.java`**
   - Inject a small `TokenHasher` utility (or static helper in same package) wrapping `SHA-256(rawToken) → hex`.
   - `generateAuthorizationTokens(...)` (lines 47-75): replace blind `INSERT` with `findByUserIdAndFamilyId(...)` → if present update `valueHash` + `lastActiveAt`, else insert with new `familyId = UUID.randomUUID()` (or the device cookie UUID — see point 6 below).
   - `findAuthorizationTokenByValue(...)` (line 155): rename to `findAuthorizationTokenByRawValue(...)`, internally hash the raw token then call `findByValueHash`.
   - `refresh(...)` (lines 87-109): hash incoming raw token before lookup; on rotation, update `valueHash` in place and **keep `familyId` unchanged**.
   - Remove the existing comment "Each call creates a new session without invalidating existing sessions" (line 38).

4. **`server/src/main/java/io/github/eventify/api/token/service/JwtService.java`**
   - No structural change. Token generation still produces raw JWTs with random `jti`. The hashing happens at persistence time in `TokenService`.

5. **`server/src/main/java/io/github/eventify/api/authentication/service/AuthenticationService.java`**
   - `login(...)` (line 71) and `verifyEmail(...)` (line 93): pass the `device_id` UUID extracted from the request cookie (or generated fresh) into `tokenService.generateAuthorizationTokens(...)` so it can be used as `family_id`.

6. **`server/src/main/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandler.java`**
   - Line 110: same — read `EVENTIFY_DEVICE_ID` cookie from `HttpServletRequest`, pass into token-issuance call.

7. **`server/src/main/java/io/github/eventify/api/authentication/service/CookieService.java`**
   - Add `setDeviceCookie(HttpServletResponse, UUID deviceId)` — 10-year Max-Age, HttpOnly, Secure, SameSite=Lax, Path=/.
   - Add `readOrCreateDeviceId(HttpServletRequest, HttpServletResponse) → UUID` — read existing or generate new + write cookie.
   - **Important:** `clearAuthCookies(...)` (lines 75-77) must NOT clear the device cookie. Verify by inspection.

8. **`server/src/main/java/io/github/eventify/common/security/filter/JwtAuthenticationFilter.java`**
   - On every request: ensure device cookie exists, generate + set if missing. (This is the only filter run for every request, so it's the right place to ensure cookie presence — call `cookieService.readOrCreateDeviceId(request, response)` early.)
   - Lines 215-220: `extractJwtFromCookies(request, REFRESH_TOKEN_COOKIE)` then `findAuthorizationTokenByValue(...)` — update to use the renamed `findAuthorizationTokenByRawValue` which internally hashes.

### New files

- **`server/src/main/java/io/github/eventify/api/token/service/TokenHasher.java`** — small utility class (or `@Component`) with one static `hash(String) → String` method using SHA-256 → hex.
- **`server/src/main/resources/db/changelog/changesets/<timestamp>-PRD-hash-refresh-tokens-add-family-id.xml`** — Liquibase migration per skill standards (raw `<sql>` only).
- **`server/src/main/java/io/github/eventify/common/constant/Constants.java`** — extend the existing `Security` inner class (line 39-40) with `public static final String DEVICE_ID_COOKIE = "EVENTIFY_DEVICE_ID";`.

### Patterns to follow / pitfalls to avoid

- Use the project's existing `Constants.Security.*_COOKIE` pattern — do NOT introduce ad-hoc cookie name strings.
- All cookie writes go through `CookieService` — do NOT call `response.addCookie(...)` from controllers/services directly.
- Hex-encode the hash, do NOT base64. Hex matches the `VARCHAR(64)` column width and is the project's prevailing pattern (see other hashed identifiers if any exist).
- When same-session-replace updates an existing row, also reset `lastActiveAt`, `userAgent`, `ipAddress`, `deviceInfo` (the user might be logging in from the same browser in a different city / network).
- The `family_id` UUID **is** the device UUID. There is no separate "session id" concept — `(user_id, family_id)` uniquely identifies a session row, and `family_id` survives logout via the device cookie.

## 7. Test Impact Analysis

### Existing tests affected by this change

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `server/src/test/java/io/github/eventify/api/token/service/TokenServiceTest.java` | all token-creation tests | `Token.value` equals raw JWT; new row inserted on every call | YES | Update: assert `valueHash = SHA-256(rawToken)`; assert update-in-place when `(user, family_id)` exists |
| `TokenServiceTest.java` | `findByValue` lookup tests | repository lookup by raw value | YES | Update: assert lookup by hashed value; raw token is never queried |
| `server/src/test/java/io/github/eventify/api/token/service/JwtServiceTest.java` | JWT generation/parsing | raw JWT structure, signature, claims | NO | Keep as-is — JWT generation unchanged |
| `server/src/test/java/io/github/eventify/api/authentication/service/AuthenticationServiceTest.java` | `login`, `refresh`, `logout` flows | session row created/found/deleted by raw `value` | YES | Update: hash-based lookups; same-session-replace assertions; logout preserves device cookie |
| `server/src/test/java/io/github/eventify/api/authentication/service/AuthenticationServiceLogoutTest.java` | logout cookie clearing | both auth cookies cleared | YES | Add assertion: `EVENTIFY_DEVICE_ID` cookie is NOT cleared |
| `server/src/test/java/io/github/eventify/common/security/filter/JwtAuthenticationFilterTest.java` | refresh-token auto-refresh path | `extractJwtFromCookies` + `findAuthorizationTokenByValue` chain | YES | Update: hashed lookup; assert `EVENTIFY_DEVICE_ID` cookie generated on first request if missing |
| `server/src/test/java/io/github/eventify/api/authentication/service/CookieServiceTest.java` | cookie-setting attribute tests | `HttpOnly`, `Secure`, `SameSite`, `Max-Age` per cookie | NO (additive) | Add tests for `setDeviceCookie` (10y Max-Age) and `readOrCreateDeviceId` |
| `server/src/test/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandlerTest.java` | OAuth callback issues tokens | session row inserted per OAuth login | YES | Update: assert same-session-replace when `EVENTIFY_DEVICE_ID` cookie present in callback request |
| `server/src/test/java/io/github/eventify/api/token/repository/TokenRepositoryTest.java` (if exists) | `findByValue` tests | finds by raw value | YES | Replace with `findByValueHash` and `findByUserIdAndFamilyId` tests |

### Test modification policy

- [x] Existing tests MAY be updated where they assert behaviour being moved (raw value → hash lookup; new-row-on-login → replace-existing).
- Specific files explicitly in scope for modification: all rows in the table above marked Conflicts? = YES.

### New tests required (mandatory)

- **`TokenHasherTest`** — pure-function test: same input → same hash; different inputs → different hashes; hex output is 64 chars.
- **`TokenServiceSameSessionReplaceTest`** — new dedicated test class covering Scenarios 6, 7, 8, 9, 10 above.
- **`CookieServiceDeviceCookieTest`** — covers `readOrCreateDeviceId` paths (cookie present, cookie absent → generated + set) and asserts attribute values per Scenario 5.
- **`JwtAuthenticationFilterDeviceCookieTest`** — assert device cookie auto-creation on first request (Scenario 5).
- **Liquibase migration test** (integration) — assert pre-existing `token` rows are wiped and new schema applied cleanly.

### Files to modify (MANDATORY)

| File | Change | Lines (approx) |
|------|--------|----------------|
| `server/src/main/java/io/github/eventify/api/token/model/Token.java` | drop `value`, add `valueHash` + `familyId` | 51-56 |
| `server/src/main/java/io/github/eventify/api/token/repository/TokenRepository.java` | rename method; add `findByUserIdAndFamilyId` | 50 |
| `server/src/main/java/io/github/eventify/api/token/service/TokenService.java` | hash on persist + lookup; same-session-replace | 38-49, 87-109, 155 |
| `server/src/main/java/io/github/eventify/api/authentication/service/AuthenticationService.java` | thread device cookie into token issuance | 71, 93 |
| `server/src/main/java/io/github/eventify/common/security/oauth2/OAuth2AuthenticationSuccessHandler.java` | read device cookie, pass to token issuance | 110 |
| `server/src/main/java/io/github/eventify/api/authentication/service/CookieService.java` | add device cookie helpers; preserve on logout | 42-77 |
| `server/src/main/java/io/github/eventify/common/security/filter/JwtAuthenticationFilter.java` | ensure device cookie present every request; use hashed lookup | 119-121, 215-220 |
| `server/src/main/java/io/github/eventify/common/constant/Constants.java` | add `DEVICE_ID_COOKIE` constant | 39-40 |

### Files to create (MANDATORY)

| File | Purpose |
|------|---------|
| `server/src/main/java/io/github/eventify/api/token/service/TokenHasher.java` | SHA-256 hex hashing utility |
| `server/src/main/resources/db/changelog/changesets/<timestamp>-PRD-hash-refresh-tokens-add-family-id.xml` | Schema migration (wipe + drop value + add value_hash, family_id, indexes) |
| `server/src/main/resources/db/changelog/db.changelog-master.xml` | Append `<include>` for new changeset |
