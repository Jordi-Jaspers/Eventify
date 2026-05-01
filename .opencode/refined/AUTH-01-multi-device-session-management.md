---
epic: "AUTH"
title: "Multi-Device Session Management"
estimate: L
status: ready
created: 2026-04-04
depends_on: [ ]
labels: [ backend, frontend, security, database ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user with multiple devices\
**I want** to stay logged in on all my devices simultaneously and manage my active sessions\
**So that** I don't get logged out unexpectedly when I log in elsewhere\

## 2. Business Context & Value
Currently, logging in on any device invalidates ALL existing refresh tokens for the user (`generateAuthorizationTokens()` calls `invalidateTokensForUser()` first). This means a single user can only have one active session — logging in on a phone logs out the laptop. Multi-device session support is a basic expectation for modern web apps. This story also adds session management UI so users can view and revoke their active sessions.

## 3. Acceptance Criteria
* [ ] **Scenario 1: Multi-device login**
    * Given a user is logged in on Device A
    * When they log in on Device B
    * Then both sessions remain active (Device A is NOT invalidated)

* [ ] **Scenario 2: Unlimited concurrent sessions**
    * Given a user has sessions on multiple devices
    * When they log in on yet another device
    * Then a new session is created without invalidating any existing sessions

* [ ] **Scenario 3: Session listing in Profile**
    * Given a user navigates to Profile → Sessions tab
    * When the page loads
    * Then they see a table of all active sessions with: device/browser info, last active timestamp, IP address, and a "current session" indicator

* [ ] **Scenario 4: Revoke single session**
    * Given a user views their active sessions
    * When they click "Revoke" on a specific session
    * Then that session's refresh token is invalidated and the row is removed from the list

* [ ] **Scenario 5: Revoke all other sessions**
    * Given a user views their active sessions
    * When they click "Revoke all other sessions"
    * Then all sessions except the current one are invalidated

* [ ] **Scenario 6: Logout invalidates only current session**
    * Given a user is logged in on Device A and Device B
    * When they log out on Device A
    * Then only Device A's session is invalidated; Device B remains active

* [ ] **Edge case: Expired sessions cleaned up**
    * Given some sessions have expired refresh tokens
    * When the token cleanup job runs
    * Then expired sessions are removed from the database and no longer appear in session list

## 4. Technical Requirements
* **API Changes**:
  - `GET /api/v1/users/me/sessions` — list active sessions for current user
  - `DELETE /api/v1/users/me/sessions/{sessionId}` — revoke specific session
  - `DELETE /api/v1/users/me/sessions` — revoke all sessions except current
  - Modify login flow to NOT invalidate existing tokens
  - Modify logout to invalidate only current session's refresh token (not all)

* **Database**:
  - Add columns to `token` table (or create `session` table): `device_info` (varchar), `ip_address` (varchar), `last_active_at` (timestamp), `user_agent` (varchar)
  - Liquibase migration for schema change

* **Security**:
  - Session endpoints require authenticated user
  - Users can only see/revoke their own sessions
  - Device info extracted from `User-Agent` header and IP from request on login

* **Performance**: N/A — session count per user will be small (single digits)

## 5. Design & UI/UX
- New "Sessions" tab in Profile page (`/profile/sessions` or tab within `/profile`)
- Table columns: Device/Browser | IP Address | Last Active | Status (current badge) | Actions (Revoke button)
- "Revoke all other sessions" button above or below table
- Current session row highlighted with a badge, revoke button disabled for current session
- Confirmation dialog before revoking all sessions
- Toast notification on successful revocation

## 6. Implementation Notes

### Backend
- **TokenService** (`server/.../api/token/service/TokenService.java`): Remove `invalidateTokensForUser()` call from `generateAuthorizationTokens()`. Instead, just create new tokens.
- **Token entity** (`server/.../api/token/model/Token.java`): Add `deviceInfo`, `ipAddress`, `lastActiveAt`, `userAgent` fields
- **New SessionController** or extend **UserController** with session management endpoints
- **AuthenticationService** (`server/.../api/authentication/service/AuthenticationService.java`): Update login to capture device info from request
- **OAuth2AuthenticationSuccessHandler** (`server/.../common/security/OAuth2AuthenticationSuccessHandler.java`): Capture device info on OAuth2 login
- **LogoutService/Controller**: Update to invalidate only current token (identify by cookie value)
- Consider parsing `User-Agent` with a library like `ua-parser` or simple substring extraction

### Frontend
- **Profile page**: Add Sessions tab — `client/src/routes/(authenticated)/profile/`
- **Session service**: New `client/src/lib/services/sessionService.ts` for API calls
- **Session table component**: `client/src/lib/components/profile/SessionsTable.svelte`
- Reuse existing `DataTable` patterns from the codebase

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../api/token/service/TokenService.java` | Remove bulk invalidation from `generateAuthorizationTokens()` |
| `server/.../api/token/model/Token.java` | Add device info fields |
| `server/.../api/authentication/service/AuthenticationService.java` | Capture device info on login |
| `server/.../common/security/OAuth2AuthenticationSuccessHandler.java` | Capture device info on OAuth2 login |
| `server/src/main/resources/db/changelog/` | New migration for token table columns |
| New: Session management controller + service | Session list, revoke single, revoke all |
| `client/src/routes/(authenticated)/profile/` | Add Sessions tab/page |
| New: `client/src/lib/components/profile/SessionsTable.svelte` | Session table UI |
| New: `client/src/lib/services/sessionService.ts` | API integration |
