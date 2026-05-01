---
epic: "AUTH"
title: "Remember Me / Long-Lived Refresh Tokens"
estimate: S
status: ready
created: 2026-04-04
depends_on: ["AUTH-01-multi-device-session-management"]
labels: [ backend, frontend, security ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user who trusts my device\
**I want** a "Remember me" option when logging in\
**So that** I stay logged in for 30 days instead of being logged out after 7 days\

## 2. Business Context & Value
The current refresh token lifetime is fixed at 7 days. Users on personal devices expect to stay logged in longer without re-authenticating every week. A "Remember me" option is standard UX for web apps, reducing login friction while keeping the default session secure.

## 3. Acceptance Criteria
* [ ] **Scenario 1: Default login (no remember me)**
    * Given a user logs in without checking "Remember me"
    * When the refresh token is created
    * Then it has a 7-day lifetime (unchanged from current behavior)

* [ ] **Scenario 2: Remember me login**
    * Given a user checks "Remember me" on the login form
    * When the refresh token is created
    * Then it has a 30-day lifetime

* [ ] **Scenario 3: Remember me session visibility**
    * Given a user logged in with "Remember me"
    * When they check their session in the Sessions tab (AUTH-01)
    * Then the session shows the extended expiry date

* [ ] **Scenario 4: OAuth2 login — default lifetime**
    * Given a user logs in via Google/GitHub OAuth2
    * When the refresh token is created
    * Then it uses the default 7-day lifetime (no remember-me option for OAuth2 flows)

* [ ] **Edge case: Checkbox state not persisted**
    * Given a user visits the login page
    * When the page loads
    * Then "Remember me" is unchecked by default

## 4. Technical Requirements
* **API Changes**:
  - Modify login endpoint (`POST /api/v1/authentication/login`) to accept optional `rememberMe: boolean` field in request body
  - `TokenService.generateAuthorizationTokens()` accepts a `rememberMe` flag to determine refresh token lifetime

* **Database**: N/A — no schema changes (refresh token already has `expires_at` column)

* **Security**:
  - Remember-me only affects refresh token lifetime, NOT access token (stays 15 min)
  - Cookie `maxAge` must match the token's actual expiry
  - OAuth2 login always uses default lifetime (no way to pass checkbox in redirect flow)

* **Performance**: N/A

## 5. Design & UI/UX
- "Remember me" checkbox on login form, below password field, unchecked by default
- Standard checkbox + label: `☐ Remember me for 30 days`
- Not shown on OAuth2 login buttons (Google/GitHub) — they use default lifetime

## 6. Implementation Notes

### Backend
- **LoginRequest** (or similar DTO): Add `rememberMe` boolean field (default `false`)
- **AuthenticationService**: Pass `rememberMe` flag through to `TokenService`
- **TokenService**: Use `JWT_REFRESH_LIFETIME_SECONDS` (7 days) when `rememberMe=false`, use new `JWT_REMEMBER_ME_LIFETIME_SECONDS` (30 days, configurable) when `rememberMe=true`
- **CookieService**: Already calculates `maxAge` from token expiry — no changes needed

### Frontend
- **Login page** (`client/src/routes/(public)/login/`): Add checkbox to form, send `rememberMe` field in login request body
- No changes to token refresh logic — the frontend already uses whatever expiry the backend sets

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `server/.../api/authentication/model/request/LoginRequest.java` (or equivalent) | Add `rememberMe` boolean |
| `server/.../api/authentication/service/AuthenticationService.java` | Pass flag to token generation |
| `server/.../api/token/service/TokenService.java` | Conditional refresh token lifetime |
| `server/src/main/resources/application.yml` | Add `JWT_REMEMBER_ME_LIFETIME_SECONDS` config |
| `client/src/routes/(public)/login/+page.svelte` | Add "Remember me" checkbox |
