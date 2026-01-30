## [2026-01-03] - Consolidate Membership Validators

### Feature plan approved by user

**Requirements Summary**

- Combine multiple small validators into a single class following AuthenticationValidator pattern
- Reduce validator count from 4 separate classes to 1 consolidated class
- Maintain all existing validation logic and test coverage

**Technical Approach**

**Validators to Consolidate:**
- `AddMemberRequestValidator` → `validateAddMember()`
- `UpdateMemberRoleRequestValidator` → `validateUpdateRole()`
- `TransferOwnershipRequestValidator` → `validateTransferOwnership()`
- `AssignOwnerRequestValidator` → `validateAssignOwner()`

**Pattern:**
Follow `AuthenticationValidator` pattern with typed methods instead of generic `validate()`.

**Implementation Workflow**

Phase 1: Create consolidated validator
- Create `OrganizationMembershipValidator` with all validation methods
- Consolidate tests into single test class with nested classes

Phase 2: Update controllers
- Update `OrganizationMembershipController` to use new validator
- Update `AdminOrganizationController` to use new validator

Phase 3: Cleanup
- Delete old validator files
- Delete old test files

---

### Actual changelog after completion

#### Summary
Consolidated 4 membership validators into single `OrganizationMembershipValidator` class.

#### Changes

**Created:**
- `OrganizationMembershipValidator.java` - Consolidated validator with typed methods
- `OrganizationMembershipValidatorTest.java` - 28 tests in 4 nested classes

**Updated:**
- `OrganizationMembershipController.java` - Uses `membershipValidator.validateXxx()` methods
- `AdminOrganizationController.java` - Uses `membershipValidator.validateAssignOwner()`

**Deleted:**
- `AddMemberRequestValidator.java`
- `UpdateMemberRoleRequestValidator.java`
- `TransferOwnershipRequestValidator.java`
- `AssignOwnerRequestValidator.java`
- `AddMemberRequestValidatorTest.java`
- `UpdateMemberRoleRequestValidatorTest.java`
- `TransferOwnershipRequestValidatorTest.java`

#### Validation Methods

| Method | Request Type | Validations |
|--------|--------------|-------------|
| `validateAddMember()` | AddMemberRequest | Email required/format, role required, role != OWNER |
| `validateUpdateRole()` | UpdateMemberRoleRequest | Role required, role != OWNER |
| `validateTransferOwnership()` | TransferOwnershipRequest | Both user IDs required and positive |
| `validateAssignOwner()` | AssignOwnerRequest | Email or userId required, email format if provided |

#### Quality Metrics
- ✅ Tests: 28 written, 28 passing
- ✅ Build: Successful
- ✅ Consistent OWASP_EMAIL_REGEX usage

#### Notes
- Pattern matches `AuthenticationValidator` for consistency
- All error messages preserved from original validators
- Controllers inject single validator instead of 3-4 separate ones
