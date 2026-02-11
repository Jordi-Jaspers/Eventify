# Secure User Channel Endpoints

**Completed:** 2026-02-11
**Epic:** Bugs & Technical Debt
**Story:** TECH-DEBT-01

## Summary

Added `@PreAuthorize` annotations to `UserChannelController` endpoints for defense-in-depth security. This ensures authorization checks happen at the controller layer before service execution, matching the pattern used in `OrganizationChannelController`.

## Changes Made

### Controller Layer (`UserChannelController.java`)
Added `@PreAuthorize` annotations to 5 endpoints:
- `GET /channels/{id}` - Get channel by ID
- `PUT /channels/{id}` - Update channel
- `DELETE /channels/{id}` - Delete channel (soft delete)
- `POST /channels/{id}/pause` - Pause channel
- `POST /channels/{id}/resume` - Resume channel

Security expression: `@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')`

**Endpoints NOT requiring channel-level checks** (they operate on logged-in user's context):
- `POST /channels` (create) - creates for logged-in user
- `POST /channels/search` - filters by logged-in user automatically

### Service Layer (`ChannelService.java`)
- Added `getChannelById(Long id)` - fetches channel without user filtering (for admin access)
- Added `getChannelWithAdminFallback(Long id)` - checks if caller has `MANAGE_USERS` authority and uses appropriate method
- Updated `updateUserChannel`, `pauseUserChannel`, `resumeUserChannel`, `deleteUserChannel` to use `getChannelWithAdminFallback`

### Security Utility (`SecurityUtil.java`)
- Added `hasAuthority(String authority)` - checks if current user has a specific authority

### Tests (`UserChannelControllerTest.java`)
**Updated tests** (4):
- `getChannelByIdFailsWhenNotFound` - now expects 403 (security-first)
- `updateChannelFailsWhenNotFound` - now expects 403 (security-first)
- `deleteChannelFailsWhenNotFound` - now expects 403 (security-first)
- `deleteChannelFailsWhenAlreadyDeleted` - now expects 403 (security-first)

**New tests** (5):
- `updateChannelFailsWhenNotOwner` - expects 403
- `pauseChannelFailsWhenNotOwner` - expects 403
- `resumeChannelFailsWhenNotOwner` - expects 403
- `adminCanAccessAnyUserChannel` - expects 200
- `adminCanDeleteAnyUserChannel` - expects 200

## Security Behavior

| Scenario | Response |
|----------|----------|
| Owner accessing own channel | 200 OK |
| Admin (MANAGE_USERS) accessing any channel | 200 OK |
| Non-owner accessing other's channel | 403 Forbidden |
| Any user accessing non-existent channel | 403 Forbidden (security-first, doesn't leak existence) |
| Any user accessing deleted channel | 403 Forbidden |

## Files Modified

- `server/src/main/java/io/github/eventify/api/channel/controller/UserChannelController.java`
- `server/src/main/java/io/github/eventify/api/channel/service/ChannelService.java`
- `server/src/main/java/io/github/eventify/common/security/SecurityUtil.java`
- `server/src/test/java/io/github/eventify/api/channel/controller/UserChannelControllerTest.java`

## Test Results

- 27 tests in `UserChannelControllerTest` - all passing
- Full backend test suite - all passing
- Clean build successful

## Notes

- The security-first approach for non-existent channels (returning 403 instead of 404) is intentional to avoid leaking information about which channel IDs exist
- This matches the pattern used in `OrganizationChannelController` for consistency
- Admin users with `MANAGE_USERS` authority can access any user's channel for platform support purposes
