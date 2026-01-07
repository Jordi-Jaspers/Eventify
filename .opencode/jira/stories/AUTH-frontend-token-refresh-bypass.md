# Frontend Token Refresh Bypass - Access Token Expiry Redirects to Login

**Epic**: Authentication & Session Management
**Status**: Ready for Dev
**Type**: Bug Fix
**Estimate**: S (Small)
**Created Date**: 2026-01-07

## 1. Problem Statement
**As a** logged-in user
**I expect** my session to remain active as long as my refresh token is valid
**But instead** I am redirected to the login page when my access token expires, even if my refresh token is still valid

## 2. Business Context & Value
- **User Frustration**: Users are unexpectedly logged out during active sessions, losing context and work
- **Expected Behavior**: The backend already correctly handles token refresh (access token expires → use refresh token → issue new tokens). The frontend is bypassing this mechanism entirely.
- **Root Cause**: `hooks.server.ts` only checks for `accessToken` presence and redirects to login if missing, without checking the refresh token or allowing the backend to attempt refresh.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Access token expired, refresh token valid
    *   Given a user has a valid refresh token but their access token has expired or been deleted
    *   When they navigate to a protected route
    *   Then the request reaches the backend, the backend refreshes the tokens, and the user remains authenticated

*   [ ] **Scenario 2**: Both tokens expired/invalid
    *   Given a user has no valid tokens (both expired or deleted)
    *   When they navigate to a protected route
    *   Then they are redirected to the login page

*   [ ] **Scenario 3**: Only access token present (edge case)
    *   Given a user has only an access token (no refresh token)
    *   When they navigate to a protected route
    *   Then normal authentication proceeds (access token validated by backend)

*   [ ] **Scenario 4**: Login page redirect for authenticated users still works
    *   Given a user is authenticated (has valid access token)
    *   When they try to access the login page
    *   Then they are redirected to the dashboard

## 4. Technical Requirements

### 4.1 Root Cause Analysis
**File**: `client/src/hooks.server.ts` (lines 6-20)

```typescript
// CURRENT (BUGGY) CODE:
let {accessToken} = CookieService.getAuthTokens(event.cookies);
const isAuthenticated: boolean = !!accessToken;  // ❌ Only checks access token

if (!isAuthenticated && !isPublicPath(pathname)) {
    throw redirect(302, CLIENT_ROUTES.LOGIN_PAGE.path);  // ❌ Redirects without checking refresh token
}
```

### 4.2 Recommended Fix
**Approach**: Check for EITHER token before redirecting. If refresh token exists, allow the request through so the backend can handle token refresh.

```typescript
// PROPOSED FIX:
let {accessToken, refreshToken} = CookieService.getAuthTokens(event.cookies);
const hasValidSession: boolean = !!accessToken || !!refreshToken;  // ✅ Check both tokens

if (!hasValidSession && !isPublicPath(pathname)) {
    throw redirect(302, CLIENT_ROUTES.LOGIN_PAGE.path);
}
```

### 4.3 Alternative Approaches Considered

| Approach | Pros | Cons | Recommendation |
|----------|------|------|----------------|
| **A. Check both tokens (recommended)** | Simple, minimal change, leverages existing backend logic | If refresh also expired, user gets 401 before redirect | ✅ Recommended - handle 401 in client |
| **B. Call refresh endpoint from hooks** | Explicit refresh before proceeding | Adds complexity, extra round-trip, hooks.server.ts becomes stateful | ❌ Over-engineered |
| **C. Add fetch interceptor for 401** | Centralized retry logic | Doesn't solve initial page load issue, more complex | ❌ Solves different problem |

### 4.4 Handling 401 After Letting Request Through
When the backend returns a 401 (both tokens expired), the frontend should gracefully redirect to login. This may already be handled by error boundaries or API error handling, but should be verified.

**Check these files for 401 handling**:
- `client/src/lib/utils/error-handler.ts`
- `client/src/routes/+error.svelte`
- Any fetch wrappers in `client/src/lib/api/`

### 4.5 Files to Modify

| File | Change |
|------|--------|
| `client/src/hooks.server.ts` | Update authentication check to include refresh token |

### 4.6 Files for Reference (No Changes)

| File | Purpose |
|------|---------|
| `server/.../JwtAuthenticationFilter.java` | Backend token refresh logic (working correctly) |
| `client/src/lib/api/authentication/service/cookie.service.ts` | Cookie utilities (already returns both tokens) |
| `client/src/lib/config/constants.ts` | Cookie names: `EVENTIFY_ACCESS_TOKEN`, `EVENTIFY_REFRESH_TOKEN` |

## 5. Testing Requirements

### 5.1 Manual Testing Steps
1. Login as a user (verify both cookies are set)
2. Delete only the `EVENTIFY_ACCESS_TOKEN` cookie from browser DevTools
3. Navigate to a protected route (e.g., `/dashboard`)
4. **Expected**: Page loads normally, new access token cookie is set by backend
5. **Before fix**: Redirected to login page

### 5.2 Edge Cases to Verify
- Delete both cookies → should redirect to login
- Delete only refresh token → should work until access expires
- Expired access token + valid refresh → should refresh (main scenario)

## 6. Implementation Notes

### 6.1 Why This Works
The backend's `JwtAuthenticationFilter` already implements the correct token refresh flow:
1. Extract access token from cookie (line 102)
2. If access token valid → authenticate (lines 106-112)
3. If access token invalid/missing → try refresh token (lines 114-118)
4. If refresh token valid → issue new tokens, set cookies, authenticate (lines 136-154)
5. If both invalid → clear security context, proceed (line 90-91) → downstream security returns 401

### 6.2 Low Risk
- Single line change in `hooks.server.ts`
- No backend changes required
- Backend logic is already tested and working
- Behavior for new/unauthenticated users unchanged
