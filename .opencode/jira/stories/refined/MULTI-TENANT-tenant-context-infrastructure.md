# Tenant Context Infrastructure

**Epic**: Multi-Tenant User & Organization Management
**Status**: Ready for Dev
**Estimate**: L
**Created Date**: 2024-12-23

## 1. User Story
**As a** developer
**I want** automatic tenant isolation with organization context management
**So that** data leakage is prevented by default and org-scoped queries are simplified

## 2. Business Context & Value
Multi-tenancy requires strict data isolation between organizations. By implementing infrastructure-level filtering, we prevent accidental data leakage and reduce the cognitive load on developers who would otherwise need to manually filter every query.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Automatic query filtering
    *   Given a Hibernate filter is enabled for organization_id
    *   When a query runs against an org-scoped entity
    *   Then only records matching the current organization context are returned

*   [ ] **Scenario 2**: Organization context from request
    *   Given a user has selected an organization via the org switcher
    *   When they make an API request
    *   Then the `X-Organization-Id` header (or cookie) is read
    *   And the TenantContext is populated for the request

*   [ ] **Scenario 3**: Membership validation
    *   Given a user attempts to access organization ID 5
    *   When the user is not a member of organization 5
    *   Then the request is rejected with 403 Forbidden
    *   And the error message is "You are not a member of this organization"

*   [ ] **Scenario 4**: No org context for non-org endpoints
    *   Given a user accesses `/v1/user/details` (user-private endpoint)
    *   When no organization context is provided
    *   Then the request succeeds (org context not required)

*   [ ] **Scenario 5**: @RequireOrgRole annotation
    *   Given an endpoint annotated with `@RequireOrgRole(ADMIN)`
    *   When a MEMBER of the organization accesses it
    *   Then they receive 403 Forbidden
    *   And when an ADMIN or OWNER accesses it
    *   Then the request proceeds

*   [ ] **Scenario 6**: Global admin org access
    *   Given I am a global admin
    *   When I select an organization in the org switcher
    *   Then I can access that organization's data (even without membership)
    *   And the tenant filter applies to scope my queries

## 4. Technical Requirements
*   **Hibernate Filter**:
    *   Define filter on org-scoped entities:
        ```java
        @FilterDef(name = "organizationFilter", parameters = @ParamDef(name = "organizationId", type = Long.class))
        @Filter(name = "organizationFilter", condition = "organization_id = :organizationId")
        ```
    *   Enable filter in `TenantContextFilter` for each request
*   **TenantContext**:
    *   ThreadLocal holder for current organization ID and role:
        ```java
        public class TenantContext {
            private static final ThreadLocal<Long> currentOrgId = new ThreadLocal<>();
            private static final ThreadLocal<OrganizationalRole> currentOrgRole = new ThreadLocal<>();
            // getters, setters, clear()
        }
        ```
*   **TenantContextFilter**:
    *   Servlet filter that:
        1. Reads `X-Organization-Id` header or `currentOrganizationId` cookie
        2. If present, validates user is member (or global admin)
        3. Populates TenantContext
        4. Enables Hibernate filter
        5. Clears context after request
*   **@RequireOrgRole Annotation**:
    *   Custom annotation + aspect for method-level org role checks:
        ```java
        @RequireOrgRole(OrganizationalRole.ADMIN)
        public void updateOrgSettings() { ... }
        ```
    *   Aspect reads TenantContext.currentOrgRole and validates
*   **Database**: No schema changes (uses existing `organization_membership` from Story 3)
*   **Security**:
    *   Membership check queries `organization_membership` table
    *   Global admins bypass membership check but still set org context

## 5. Design & UI/UX
*   N/A — This is backend infrastructure
*   Frontend must send `X-Organization-Id` header on org-scoped API calls

## 6. Implementation Notes / Research
*   **New Files Needed**:
    *   `TenantContext.java` — ThreadLocal holder
    *   `TenantContextFilter.java` — Servlet filter
    *   `RequireOrgRole.java` — Annotation
    *   `RequireOrgRoleAspect.java` — AOP aspect for role checking
    *   `TenantAwareRepository.java` — Base interface for org-scoped repos (optional)
*   **Existing Files to Modify**:
    *   `WebSecurityConfig.java` — Register TenantContextFilter
    *   Future org-scoped entities — Add `@Filter` annotations
*   **Filter Order**:
    *   TenantContextFilter must run AFTER JwtAuthenticationFilter (needs authenticated user)
*   **Testing**:
    *   Integration tests proving isolation:
        *   User A in Org 1 cannot see Org 2 data
        *   Query returns empty when Org has no matching data
        *   Global admin can access any org after selecting context
*   **Pitfalls**:
    *   Ensure ThreadLocal is cleared in finally block to prevent leakage
    *   Hibernate filter must be enabled per-session, not globally
