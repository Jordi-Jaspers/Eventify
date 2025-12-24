# Epic: Multi-Tenant User & Organization Management

- [ ] **User Search Endpoint with Owner Selector UI** (S):
    - Backend: `GET /admin/users/search?query={email}` endpoint
    - Returns enabled users matching query (min 3 chars)
    - Response: `List<{ id, email, firstName, lastName }>`
    - Requires ADMIN role
    - Frontend: Replace owner text input with autocomplete/combobox component
    - Displays user email + name in dropdown
    - Debounced search (300ms)
    - Max 10 results returned
    - Used in: Organization provisioning form (`/admin/organizations/new`)

---

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
