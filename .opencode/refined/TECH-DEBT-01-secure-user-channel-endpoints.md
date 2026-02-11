# Secure User Channel Endpoints

**Epic**: Bugs & Technical Debt
**Status**: Ready for Dev
**Estimate**: S (Small)
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** platform security administrator
**I want** controller-level authorization checks on user channel endpoints
**So that** we have defense-in-depth security and consistent patterns across all controllers

## 2. Business Context & Value
Currently, `UserChannelController` relies solely on service-layer security (filtering queries by `user.getId()`). While this works, it's inconsistent with `OrganizationChannelController` which uses `@PreAuthorize` annotations for defense-in-depth.

**Risks of current approach**:
- If a developer accidentally modifies a service method, there's no controller-level gate
- New endpoints might forget to add ownership checks
- Inconsistent patterns increase cognitive load and bug potential

**Benefits of this change**:
- Matches `OrganizationChannelController` pattern
- Defense-in-depth: security at both controller AND service layer
- Clear 403 Forbidden responses for unauthorized access
- Admin override capability for platform support

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Owner can access their own channel
    * Given I am an authenticated user
    * And I own a channel with ID 123
    * When I call GET /api/v1/channels/123
    * Then I receive 200 OK with channel details

* [ ] **Scenario 2**: Non-owner receives 403 Forbidden
    * Given I am an authenticated user
    * And another user owns channel ID 456
    * When I call GET /api/v1/channels/456
    * Then I receive 403 Forbidden

* [ ] **Scenario 3**: Admin can access any user's channel
    * Given I am an authenticated admin with MANAGE_CHANNELS authority
    * And another user owns channel ID 789
    * When I call GET /api/v1/channels/789
    * Then I receive 200 OK with channel details

* [ ] **Scenario 4**: All mutating endpoints are secured
    * Given I am an authenticated user who does NOT own channel ID 123
    * When I call any of these endpoints:
        - PUT /api/v1/channels/123
        - POST /api/v1/channels/123/pause
        - POST /api/v1/channels/123/resume
        - DELETE /api/v1/channels/123
    * Then I receive 403 Forbidden for each

* [ ] **Scenario 5**: Unauthenticated user receives 401
    * Given I am not authenticated
    * When I call GET /api/v1/channels/123
    * Then I receive 401 Unauthorized

* [ ] **Scenario 6**: Non-existent channel returns 404 (for owner)
    * Given I am an authenticated user
    * When I call GET /api/v1/channels/99999 (non-existent)
    * Then I receive 404 Not Found (not 403)

## 4. Technical Requirements

### Controller Changes

Add `@PreAuthorize` annotations to `UserChannelController` endpoints:

| Endpoint | Path | Security Expression |
|----------|------|---------------------|
| GET channel | `/api/v1/channels/{id}` | `@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_CHANNELS')` |
| PUT channel | `/api/v1/channels/{id}` | `@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_CHANNELS')` |
| POST pause | `/api/v1/channels/{id}/pause` | `@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_CHANNELS')` |
| POST resume | `/api/v1/channels/{id}/resume` | `@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_CHANNELS')` |
| DELETE channel | `/api/v1/channels/{id}` | `@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_CHANNELS')` |

**Endpoints NOT requiring channel-level checks** (they operate on logged-in user's context):
- `POST /api/v1/channels` (create) - creates for logged-in user
- `POST /api/v1/channels/search` - filters by logged-in user automatically

### Security Service

The method `ChannelSecurityService.canAccessChannelAsUser(Long channelId, UserTokenPrincipal principal)` already exists and can be reused:

```java
public boolean canAccessChannelAsUser(final Long channelId, final UserTokenPrincipal principal) {
    if (principal == null || channelId == null) {
        return false;
    }

    return channelRepository.findActiveChannelById(channelId)
        .filter(channel -> isPersonalChannel(channel) && isChannelOwner(channel, principal))
        .isPresent();
}
```

### Test Updates

Update `UserChannelControllerTest` to expect `403 FORBIDDEN` instead of `404 NOT_FOUND` for unauthorized access:

| Test Method | Current Expectation | New Expectation |
|-------------|---------------------|-----------------|
| `getChannelByIdFailsWhenNotOwner` | `SC_NOT_FOUND` (404) | `SC_FORBIDDEN` (403) |
| `deleteChannelFailsWhenNotOwner` | `SC_NOT_FOUND` (404) | `SC_FORBIDDEN` (403) |

**Add new tests**:
- `updateChannelFailsWhenNotOwner` → expects 403
- `pauseChannelFailsWhenNotOwner` → expects 403
- `resumeChannelFailsWhenNotOwner` → expects 403
- `adminCanAccessAnyUserChannel` → expects 200
- `adminCanUpdateAnyUserChannel` → expects 200
- `adminCanDeleteAnyUserChannel` → expects 200

### Authority Constant

Add or verify constant exists:
```java
// In Authority.java or similar
public static final String MANAGE_CHANNELS = "MANAGE_CHANNELS";
```

If this authority doesn't exist yet, create it and ensure it's assigned to admin roles.

## 5. Design & UI/UX

N/A - Backend security change only.

## 6. Implementation Notes / Research

### Existing Code References
- `UserChannelController`: `server/src/main/java/io/github/eventify/api/channel/controller/UserChannelController.java`
- `OrganizationChannelController` (pattern reference): `server/src/main/java/io/github/eventify/api/channel/controller/OrganizationChannelController.java`
- `ChannelSecurityService`: `server/src/main/java/io/github/eventify/api/channel/service/ChannelSecurityService.java`
- `UserChannelControllerTest`: `server/src/test/java/io/github/eventify/api/channel/controller/UserChannelControllerTest.java`

### Pattern Reference

Follow the `OrganizationChannelController` pattern:
```java
@GetMapping(path = ORGANIZATION_CHANNEL_PATH, produces = APPLICATION_JSON_VALUE)
@ResponseStatus(OK)
@PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
public ResponseEntity<ChannelDetailsResponse> getOrganizationChannel(...) {
```

Apply same pattern to `UserChannelController`:
```java
@GetMapping(path = USER_CHANNEL_PATH, produces = APPLICATION_JSON_VALUE)
@ResponseStatus(OK)
@PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_CHANNELS')")
public ResponseEntity<ChannelDetailsResponse> getChannel(@PathVariable final Long id) {
```

### Edge Cases

| Edge Case | Expected Behavior |
|-----------|-------------------|
| Channel exists but user doesn't own it | 403 Forbidden |
| Channel doesn't exist | 404 Not Found (from service layer after security passes for admin, or 403 for non-owner since security check fails) |
| User owns channel but it's PENDING_DELETION | 404 Not Found (service layer excludes deleted) |
| Org channel accessed via user endpoint | 403 Forbidden (security check verifies personal channel) |

### Note on Non-Existent Channels

For non-owners, non-existent channels will return 403 (since `canAccessChannelAsUser` returns false). This is acceptable as it doesn't leak information about which channel IDs exist.

For owners/admins, non-existent channels return 404 (security passes, service throws DataNotFoundException).
