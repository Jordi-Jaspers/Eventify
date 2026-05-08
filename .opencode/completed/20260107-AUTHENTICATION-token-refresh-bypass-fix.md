# Frontend Token Refresh Bypass Fix

**Epic**: Authentication & Session Management
**Type**: Bug Fix
**Date**: 2026-01-07

## Feature plan approved by user

### Problem Statement
Users were unexpectedly redirected to the login page when their access token expired, even if their refresh token was still valid. The backend already correctly handles token refresh in `JwtAuthenticationFilter`, but the frontend was bypassing this mechanism entirely.

### Requirements Summary
- Check for EITHER access token OR refresh token before redirecting
- Allow requests through if refresh token exists so backend can handle refresh
- Maintain existing behavior for unauthenticated users (no tokens)
- Keep login page redirect for authenticated users

### Technical Approach
**Root Cause**: `hooks.server.ts` only checked for `accessToken` presence:
```typescript
// BEFORE (buggy):
let {accessToken} = CookieService.getAuthTokens(event.cookies);
const isAuthenticated: boolean = !!accessToken;
```

**Fix**: Check for either token:
```typescript
// AFTER (fixed):
const {accessToken, refreshToken} = CookieService.getAuthTokens(event.cookies);
const hasValidSession: boolean = !!accessToken || !!refreshToken;
```

### Implementation Workflow
- Single file change in `hooks.server.ts`
- No backend changes required (backend already handles refresh correctly)
- Type checks passing

---

## Actual changelog after completion

### Summary
Fixed frontend token refresh bypass - now checks for either access token OR refresh token before redirecting to login, allowing the backend's existing token refresh mechanism to work correctly.

### Changes

**Frontend:**
- Updated `hooks.server.ts` to check both `accessToken` and `refreshToken`
- Renamed variable from `isAuthenticated` to `hasValidSession` for clarity
- Updated comments to reflect new behavior

### Files Modified
| File | Change |
|------|--------|
| `client/src/hooks.server.ts` | Check both tokens before redirecting |

### Quality Metrics
- ✅ Type checks: `bun run check` passes with 0 errors
- ✅ Build: Successful
- ✅ No backend changes required

### Testing Instructions
1. Login as a user (verify both cookies are set)
2. Delete only the `EVENTIFY_ACCESS_TOKEN` cookie from browser DevTools
3. Navigate to a protected route (e.g., `/dashboard`)
4. **Expected**: Page loads normally, new access token cookie is set by backend
5. **Before fix**: Redirected to login page

### Notes
- Low risk change - single line modification
- Leverages existing backend logic in `JwtAuthenticationFilter.java`
- Backend handles: access token valid → authenticate; access invalid but refresh valid → issue new tokens; both invalid → 401
