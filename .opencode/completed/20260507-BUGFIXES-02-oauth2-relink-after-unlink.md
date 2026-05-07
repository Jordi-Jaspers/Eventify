# OAuth2 Re-link After Unlink Bug Fix

**Completed:** 2026-05-07
**Epic:** BUGFIXES
**Source:** ad-hoc request

## Summary

Fixed bug where unlinking an OAuth2 provider from one account and linking it to another account failed with "email already associated with another account." Removed overly-strict EMAIL_IN_USE_ERROR check that blocked linking based on primary email match.

## Bug Description

1. User creates account Y via Google OAuth (email Y)
2. Links GitHub (email Y) to account Y
3. Unlinks GitHub from account Y
4. Logs into account X, tries to link GitHub (email Y) → ERROR: EMAIL_IN_USE_ERROR

Root cause: `linkProviderForUser()` had a check that rejected linking when the provider's email matched *any* other user's primary email — even if the provider was no longer linked to that user.

## Fix

Removed the EMAIL_IN_USE_ERROR check (lines 133-136). The remaining PROVIDER_LINKED_ELSEWHERE_ERROR check correctly handles the case where a provider is *actively* linked to another user.

## Files Modified

- `server/src/main/java/io/github/eventify/api/user/service/UserAuthProviderService.java` — removed EMAIL_IN_USE_ERROR check from `linkProviderForUser()`
- `server/src/test/java/io/github/eventify/api/user/service/UserAuthProviderServiceTest.java` — removed obsolete test, added 3 regression tests
- `client/src/routes/(authenticated)/profile/connected-accounts/+page.svelte` — added error toast display from redirect query param

## Tests

- 1 test removed (asserted incorrect behavior)
- 3 tests added:
  - Provider email matches another user's primary email but not linked elsewhere → succeeds
  - Provider actively linked to another user → throws PROVIDER_LINKED_ELSEWHERE_ERROR
  - Re-link after unlink scenario → succeeds
