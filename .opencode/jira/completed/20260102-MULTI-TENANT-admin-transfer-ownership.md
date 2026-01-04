## [2026-01-02] - Admin Transfer Ownership Enhancement

### Summary
Enhanced the transfer ownership functionality to allow both organization OWNERs and global ADMINs (with MANAGE_ORGANIZATIONS permission) to transfer organization ownership. This enables platform administrators to help organizations manage ownership changes without requiring the current owner to be available.

---

### Feature Requirements

**Business Need:**
- Organization owners may become unavailable (left company, inactive, etc.)
- Global admins need ability to transfer ownership on behalf of organizations
- Must maintain security - prevent unauthorized ownership transfers

**Implementation:**
- Added `currentOwnerUserId` field to TransferOwnershipRequest
- Backend validates caller has permission (either current owner OR global admin)
- If caller is owner: must provide their own ID as `currentOwnerUserId`
- If caller is global admin: can provide any valid owner's ID
- Frontend finds current owner from members list automatically

---

### Actual Changelog

#### Backend Changes

**TransferOwnershipRequest.java**
- Added `currentOwnerUserId` field (Long)
- Request now requires both current and new owner IDs

**TransferOwnershipRequestValidator.java**
- Added validation for `currentOwnerUserId`:
  - Required field (cannot be null)
  - Must be positive (> 0)
- New constants: `CURRENT_OWNER_USER_ID`, `CURRENT_OWNER_USER_ID_REQUIRED`, `CURRENT_OWNER_USER_ID_MUST_BE_POSITIVE`

**OrganizationMembershipService.java**
- Updated `transferOwnership` method: `transferOwnership(Long orgId, Long currentOwnerId, Long newOwnerId)`
- Gets caller from security context via `getLoggedInUser()`
- Permission logic:
  - If caller has MANAGE_ORGANIZATIONS permission (global admin) - skip caller validation
  - Otherwise, validate caller.getId() equals currentOwnerId
- Validates provided currentOwnerId is actually the current owner
- Validates newOwnerId is an existing member

**OrganizationMembershipController.java**
- Updated `@PreAuthorize` annotation:
  - From: `@orgSecurity.isOwner(#orgId, principal.user.id)`
  - To: `@orgSecurity.isOwner(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')`
- Now passes `request.getCurrentOwnerUserId()` to service method (not caller's ID)

#### Frontend Changes

**api.d.ts (regenerated from OpenAPI)**
- TransferOwnershipRequest now includes `currentOwnerUserId` field

**models.ts**
- Updated `TransferOwnershipRequest` interface:
```typescript
export interface TransferOwnershipRequest {
    currentOwnerUserId: number;
    newOwnerUserId: number;
}
```

**OrganizationMembershipService.svelte.ts**
- Updated `transfer()` function to:
  - Find current owner from members list
  - Pass `currentOwnerUserId` in request
  - Handle edge case where owner not found

---

### Testing

**TransferOwnershipRequestValidatorTest.java** (4 new tests)
- `shouldRejectNullCurrentOwnerUserId`
- `shouldRejectZeroCurrentOwnerUserId`
- `shouldRejectNegativeCurrentOwnerUserId`
- `shouldPassValidRequestWithBothUserIds`

**OrganizationMembershipServiceTest.java** (4 new tests)
- `shouldTransferOwnershipWhenCallerIsCurrentOwner`
- `shouldTransferOwnershipWhenCallerIsGlobalAdmin`
- `shouldRejectWhenCallerIsNotOwnerAndNotAdmin`
- `shouldRejectWhenCurrentOwnerIdDoesNotMatchActualOwner`

**OrganizationMembershipControllerTest.java** (3 new tests)
- `transferOwnershipSuccess` - Owner transfers with correct currentOwnerUserId
- `transferOwnershipWithWrongCurrentOwnerIdFails` - Validation error
- `globalAdminCanTransferOwnership` - Admin transfers any org's ownership

---

### Files Modified

**Backend:**
- `server/src/main/java/io/github/eventify/api/organization/model/request/TransferOwnershipRequest.java`
- `server/src/main/java/io/github/eventify/api/organization/model/validator/TransferOwnershipRequestValidator.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationMembershipService.java`
- `server/src/main/java/io/github/eventify/api/organization/controller/OrganizationMembershipController.java`

**Backend Tests:**
- `server/src/test/java/io/github/eventify/api/organization/model/validator/TransferOwnershipRequestValidatorTest.java`
- `server/src/test/java/io/github/eventify/api/organization/service/OrganizationMembershipServiceTest.java`
- `server/src/test/java/io/github/eventify/api/organization/controller/OrganizationMembershipControllerTest.java`

**Frontend:**
- `client/src/lib/types/api.d.ts` (regenerated)
- `client/src/lib/api/models.ts`
- `client/src/lib/api/organization/OrganizationMembershipService.svelte.ts`

---

### Quality Metrics

- Backend tests: All 70+ passing
- Frontend type check: 0 errors, 0 warnings
- Build: Successful
- Coverage: Maintained >90% line coverage

---

### Security Considerations

1. **Authorization check order:**
   - Controller checks: isOwner OR hasAnyAuthority('MANAGE_ORGANIZATIONS')
   - Service validates: caller must be the actual owner OR have global admin role

2. **Prevents impersonation:**
   - Non-admin callers must provide their own ID as currentOwnerUserId
   - Backend validates caller.getId() === currentOwnerUserId for non-admins

3. **Audit trail:**
   - Transfer operations are logged
   - Previous owner is demoted to ADMIN (not removed)

---

### Notes

- Service uses `getLoggedInUser()` to get caller from security context
- Tests mock security context for unit tests
- Frontend automatically detects current owner from loaded members list
- If members list not loaded, transfer will fail gracefully with error message
