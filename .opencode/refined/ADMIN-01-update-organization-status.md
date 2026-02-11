# Update Organization Status

**Epic**: Admin Global Oversight
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** platform administrator
**I want** to update an organization's status (TRIAL, ACTIVE, SUSPENDED)
**So that** I can manage organization lifecycles and restrict access when needed

## 2. Business Context & Value
Currently, organizations are created with `TRIAL` status and there's no way to change it. Admins need to:
- Activate organizations after payment/verification (`TRIAL` → `ACTIVE`)
- Suspend organizations for policy violations or non-payment
- Reactivate suspended organizations after issues are resolved

**Critical side effect**: When an organization is `SUSPENDED`, its members can no longer see or access it. The organization and its data remain intact (admin-visible), but users experience it as if they are not a member (403 Forbidden - consistent with existing behavior for non-members).

## 3. Acceptance Criteria

### Backend
* [ ] **Scenario 1**: Admin updates organization status
    * Given an admin with `MANAGE_ORGANIZATIONS` authority
    * When they call `PATCH /api/v1/admin/organizations/{orgId}/status` with `{"status": "ACTIVE"}`
    * Then the organization's status is updated to ACTIVE
    * And the response returns the updated organization

* [ ] **Scenario 2**: Non-admin cannot update status
    * Given a regular user (without admin authority)
    * When they attempt to update an organization's status
    * Then they receive 403 Forbidden

* [ ] **Scenario 3**: Invalid status rejected
    * Given an admin attempts to set an invalid status
    * When they call the endpoint with `{"status": "INVALID"}`
    * Then they receive 400 Bad Request with validation error

### Side Effects (Suspension)
* [ ] **Scenario 4**: Suspended org hidden from user's organization list
    * Given a user is a member of Organization X
    * When Organization X is suspended
    * Then calling `GET /api/v1/user/organizations` does NOT include Organization X

* [ ] **Scenario 5**: Suspended org inaccessible to members
    * Given a user is a member of a suspended organization
    * When they attempt to access any endpoint under `/api/v1/org/{orgId}/*`
    * Then they receive 403 Forbidden (consistent with non-member behavior)

* [ ] **Scenario 6**: Admin can still access suspended org
    * Given an admin with `MANAGE_ORGANIZATIONS` authority
    * When they access a suspended organization via admin endpoints
    * Then they can view and manage it normally

### Frontend
* [ ] **Scenario 7**: Edit button in admin org table
    * Given an admin views the organizations table
    * When they click the Edit (pencil) button on an organization row
    * Then a sheet/modal opens with a status dropdown

* [ ] **Scenario 8**: Status update via UI
    * Given an admin has the edit sheet open
    * When they select a new status and click Save
    * Then the status is updated and the table refreshes

## 4. Technical Requirements

### API Changes
| Method | Path | Description |
|--------|------|-------------|
| `PATCH` | `/api/v1/admin/organizations/{orgId}/status` | Update organization status |

**Request Body**:
```json
{
  "status": "ACTIVE"  // TRIAL, ACTIVE, or SUSPENDED
}
```

**Response**: `200 OK` with `OrganizationResponse`

**Security**: `@PreAuthorize("hasAuthority('MANAGE_ORGANIZATIONS')")`

### Backend Implementation

**New Files**:
- `UpdateOrganizationStatusRequest.java` - Request DTO with `@NotNull OrganizationStatus status`

**Modified Files**:
| File | Change |
|------|--------|
| `Paths.java` | Add `ADMIN_ORGANIZATION_STATUS_PATH` constant |
| `AdminOrganizationController.java` | Add `updateStatus()` endpoint |
| `OrganizationService.java` | Add `updateStatus(Long orgId, OrganizationStatus status)` method |
| `OrganizationSecurityService.java` | Modify `isMember()`, `isOwner()`, `isAdmin()`, `isOwnerOrAdmin()` to return `false` for SUSPENDED orgs |
| `OrganizationMembershipService.java` | Modify `getUserOrganizations()` to filter out SUSPENDED orgs |

### Security Service Changes (Critical)
```java
// In OrganizationSecurityService.java
public boolean isMember(final Long orgId, final Long userId) {
    final Organization org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR));
    
    // Suspended orgs behave as if user is not a member (403 Forbidden)
    if (org.getStatus() == OrganizationStatus.SUSPENDED) {
        return false;
    }
    
    return membershipRepository.existsByOrganizationIdAndUserId(orgId, userId);
}
```

### Frontend Implementation

**New Files**:
- `EditOrganizationSheet.svelte` - Sheet with status dropdown

**Modified Files**:
| File | Change |
|------|--------|
| `OrganizationController.ts` | Add `updateOrganizationStatus(orgId, status)` function |
| `admin/organizations/+page.svelte` | Add Edit button, wire up sheet |

### Database
No schema changes required - `status` column already exists.

## 5. Design & UI/UX

### Admin Organizations Table - New Edit Button
Add a pencil/edit icon button next to existing Members and API Keys buttons:
```
[Key] [Users] [Pencil]
  |      |       └── NEW: Edit org (opens sheet)
  |      └── Existing: View members
  └── Existing: View API keys
```

### Edit Organization Sheet
```
┌─────────────────────────────────────┐
│ Edit Organization                   │
├─────────────────────────────────────┤
│                                     │
│ Organization: Acme Corp             │
│ Slug: acme-corp                     │
│                                     │
│ Status                              │
│ ┌─────────────────────────────────┐ │
│ │ ACTIVE                        v │ │
│ └─────────────────────────────────┘ │
│   o TRIAL - Organization in trial   │
│   * ACTIVE - Active subscription    │
│   o SUSPENDED - Access restricted   │
│                                     │
│ Warning: Suspending will hide       │
│ this org from all members.          │
│                                     │
├─────────────────────────────────────┤
│        [Cancel]    [Save Changes]   │
└─────────────────────────────────────┘
```

## 6. Implementation Notes / Research

### Existing Patterns to Follow
- **Controller pattern**: See `AdminOrganizationController.java` for existing admin endpoints
- **Sheet pattern**: See `CreateOrganizationSheet.svelte` for sheet component structure
- **Security check pattern**: See `OrganizationSecurityService.java` - add status check to existing methods

### Key Decision: 403 for Suspended Orgs
Return **403 Forbidden** when users try to access suspended orgs - consistent with existing behavior when a user tries to access an org they're not a member of.

### Test Files to Update
- `AdminOrganizationControllerTest.java` - Add status update tests
- `OrganizationSecurityServiceTest.java` - Add suspended org access tests
- `OrganizationMembershipServiceTest.java` - Test getUserOrganizations filters suspended

### Future Considerations (not in scope)
- Audit log for status changes
- Notification to org owner when suspended
- **TRIAL account limitations**: Users, event quota, API keys limits for trial organizations
