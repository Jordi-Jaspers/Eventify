# Bug Fixes
- [x] ~~**The avatar does not fit in the sidebar when minimized.**~~ (Fixed 2026-01-04)
- [x] ~~**User details are not retrieved during login, they are using the auth response**~~ (Fixed 2026-01-04)
- [x] ~~**Consider retrieving the user organisations (LEFT JOIN) during details retrieval to avoid multiple calls later.**~~ (Fixed 2026-01-04)
- [x] ~~**Global admin should be able manage the users in a organisation alongside the owner and admins**~~ (Fixed 2026-01-04)
- [x] ~~**Update user details page with their organtisations and their role in it.**~~ (Fixed 2026-01-04)

# Epic: User Management
- [ ] **User Dashboard**:
    - Create filterable and sortable user list view
    - basic table actions (lock, unlock, view)
    - use the custom data table component

# Epic: General Improvements
- [ ] **OpenApi annotations**:
    - Add missing OpenApi annotations to request and response models for better API documentation.
    - Ensure they all have proper descriptions, examples, and constraints.
    - If there is javadoc on the field, use that as the description and remove the javadoc to avoid duplication.
    - Example:
    ```
      @Schema(
        description = "Name of the field to search on",
        requiredMode = Schema.RequiredMode.REQUIRED,
        example = "name"
    )
  ```

# Epic: Security Enhancements for Authentication

- [ ] **Long-Lived Refresh Tokens**:
    - Add a remember-me option during login for longer refresh token validity

- [ ] **Multi-Token Support**:
    - Allow multiple active refresh tokens per user (for different devices)
    - User can retrieve list of active refresh tokens

- [ ] **Token Revocation**:
    - User can revoke specific refresh tokens
    - User can revoke all refresh tokens (logout from all devices, including current one, logout)

- [ ] **OAuth2 Enhancements**:
    - Implement account linking strategy (when user registers with email, then tries OAuth2 with same email)

---
