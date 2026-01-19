# Production-Ready Authentication Flow

**Epic**: Authentication & Session Management
**Status**: Ready for Dev
**Estimate**: M (Medium - 3-5 days)
**Created Date**: 2026-01-19

## 1. User Story
**As a** logged-in user
**I want** my session to remain active as long as my refresh token is valid
**So that** I don't get unexpectedly logged out while using the application

## 2. Business Context & Value
Users are currently being unexpectedly logged out after ~15 minutes of inactivity (when access token expires), even though their refresh token remains valid for 7 days. This creates a frustrating user experience and undermines trust in the platform.

A production-ready auth flow should:
- Seamlessly refresh tokens without user intervention
- Gracefully handle authentication failures
- Provide clear feedback when a session truly expires
- Prevent stale UI state that doesn't match server-side session state

## 3. Acceptance Criteria

- [ ] **Scenario 1**: Session persists after access token expires
    - Given a user is logged in with valid access and refresh tokens
    - When the access token expires (after 15 minutes) and the user reloads the page
    - Then the backend automatically refreshes the tokens and the user remains logged in

- [ ] **Scenario 2**: API calls trigger token refresh when needed
    - Given a user's access token has expired but refresh token is valid
    - When any API call is made from the frontend
    - Then the backend refreshes the tokens via cookies and the API call succeeds

- [ ] **Scenario 3**: Global 401 handling with redirect
    - Given a user's session is completely expired (both tokens invalid)
    - When any API call returns a 401 Unauthorized error
    - Then the user is redirected to the login page with a clear message

- [ ] **Scenario 4**: Auth state synchronization on app mount
    - Given a user opens the app with cookies present
    - When the app initializes
    - Then the auth store fetches fresh user details from the backend before rendering protected content

- [ ] **Scenario 5**: Graceful degradation on session expiry
    - Given a user is actively using the app when their session expires
    - When an API call fails with 401
    - Then the user sees a toast notification explaining their session expired
    - And they are redirected to login without losing unsaved work (where applicable)

- [ ] **Scenario 6**: Proactive token refresh (optional enhancement)
    - Given a user is actively using the app
    - When their access token is about to expire (e.g., 2 minutes before)
    - Then the frontend proactively refreshes the token in the background

## 4. Technical Requirements

### Frontend Changes

**4.1 API Client Interceptor** (`client/src/lib/api/client.ts`)
- Add response interceptor to handle 401 errors globally
- On 401: Clear auth store, redirect to login with `?expired=true` query param
- Prevent multiple simultaneous redirects

**4.2 Auth Store Enhancement** (`client/src/lib/stores/auth.ts`)
- Add `validateSession()` method that calls `getUserDetails()` and updates store
- Handle errors gracefully (clear state on auth failure)
- Add `sessionExpired` flag for UI feedback

**4.3 Authenticated Layout Guard** (`client/src/routes/(authenticated)/+layout.svelte`)
- Call `authStore.validateSession()` on mount before rendering children
- Show loading state while validating
- This ensures fresh user data from backend on every authenticated page load

**4.4 Login Page Enhancement** (`client/src/routes/(public)/login/+page.svelte`)
- Check for `?expired=true` query param
- Show informative message: "Your session has expired. Please log in again."

**4.5 Optional: Proactive Token Refresh**
- Add `setInterval` in authenticated layout to call a lightweight "ping" endpoint every 10-12 minutes
- This triggers backend token refresh before access token expires
- Clear interval on component destroy

### Backend Changes
- **None required**: The backend already correctly handles token refresh in `JwtAuthenticationFilter.java`
- The filter:
  1. Tries access token first
  2. If access token invalid/expired, tries refresh token
  3. If refresh token valid, issues new access + refresh tokens as cookies
  4. If both invalid, returns 401

### Security Considerations
- Never expose token values to JavaScript (HTTP-only cookies maintained)
- Clear localStorage user data on logout/session expiry
- Ensure redirect loop prevention (don't redirect if already on login page)

## 5. Design & UI/UX

### Session Expired Toast
- Type: Warning/Info
- Message: "Your session has expired. Redirecting to login..."
- Duration: 3 seconds before redirect

### Loading State During Session Validation
- Show skeleton/spinner overlay on authenticated routes
- Prevent flash of content before auth is confirmed
- Duration: Typically < 500ms (one API round-trip)

### Login Page with Expired Message
- Show dismissible info alert at top of login form
- Message: "Your session has expired. Please log in again."
- Style: Matches existing alert patterns

## 6. Implementation Notes / Research

### File Locations
| File | Purpose |
|------|---------|
| `client/src/lib/api/client.ts` | API client - add 401 interceptor |
| `client/src/lib/stores/auth.ts` | Auth store - add `validateSession()` |
| `client/src/routes/(authenticated)/+layout.svelte` | Add session validation on mount |
| `client/src/routes/(public)/login/+page.svelte` | Handle `?expired=true` param |
| `client/src/lib/api/user/UserController.ts` | Already has `getUserDetails()` |

### Existing Patterns to Follow
- Error handling: Use `handleError()` from `$lib/utils/error-handler.ts`
- Toast notifications: Use `toast` from `svelte-sonner`
- Navigation: Use `goto()` from `$app/navigation`
- Route constants: Use `CLIENT_ROUTES` from `$lib/config/routes`

### Previous Related Work
- `20260107-AUTHENTICATION-token-refresh-bypass-fix.md` - Fixed `hooks.server.ts` to check both tokens

### Potential Pitfalls
1. **Redirect Loop**: Ensure 401 interceptor doesn't trigger on login/public routes
2. **Race Conditions**: Use a mutex/flag to prevent multiple 401 handlers firing
3. **SSR vs Client**: Session validation must happen client-side (cookies not accessible in SSR easily)
4. **Flash of Unauthenticated Content**: Loading state must cover the gap

### Testing Strategy
1. Manual test: Login, wait 15+ min, reload page - should stay logged in
2. Manual test: Delete access token cookie, make API call - should work (backend refreshes)
3. Manual test: Delete both cookies, make API call - should redirect to login
4. Unit tests: Mock 401 response and verify redirect behavior
