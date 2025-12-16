# Epic: Multi-Tenant User & Team Management (Admin-Provisioned)

## Stories:

### Global Admin & User Identity ✅ COMPLETED
**As a** system architect
**I want** user accounts with global admin capability
**So that** platform administrators can provision organizations

**Acceptance Criteria:**
- ✅ Email uniqueness (already enforced)
- ✅ Global admin via `Role.ADMIN` enum value
- ✅ Bootstrap listener creates first admin from ENV vars on startup
- ✅ JWT includes `global_role` claim (renamed from `role`)
- ✅ ApplicationStartedEvent listener checks for existing admin
- ✅ ENV vars: GLOBAL_ADMIN_EMAIL, GLOBAL_ADMIN_PASSWORD, GLOBAL_ADMIN_FIRST_NAME, GLOBAL_ADMIN_LAST_NAME

**Implementation Notes:**
- Using `Role` enum (not separate boolean flag) for clean multi-tenant design
- JWT claim renamed to `global_role` for clarity (later: `org_role` will be added)
- Permission-based guards preferred over role checks (fine-grained, extensible)

---

### Organization Provisioning (Admin Only) ✅ COMPLETED
**As a** global admin
**I want** to create organizations
**So that** I can onboard new clients/customers

**Acceptance Criteria:**
- ✅ Organization table (id, name, slug, status, created_by, created_at)
- ✅ POST `/admin/organizations` endpoint (global admin only)
- ✅ Status: ACTIVE, SUSPENDED, TRIAL
- ✅ Slug generation/validation
- ✅ Security: Permission-based guard (e.g., `@RequirePermission(PROVISION_ORGANIZATIONS)`)
  - ✅ Permission attached to `Role.ADMIN` (global admins)
  - ✅ More fine-grained than role checks, extensible for future needs

---

### Global Admin Dashboard - Phase 1: Stats ✅ COMPLETED

**As a** global admin
**I want** to view platform statistics
**So that** I can monitor growth and activity

**Acceptance Criteria:**
- ✅ Add hamburger menu item "Admin Dashboard" visible only to global admins
- ✅ Dashboard page at `/admin/dashboard`
- ✅ Stats displayed:
  - ✅ Total orgs, total users, active users
  - ✅ New orgs/users (7d, 30d)
  - ✅ Recent logins (7d), inactive users (>30d no login)
- ✅ GET `/admin/dashboard/stats` endpoint
- ✅ Security: `MANAGE_USERS` permission
- ✅ Responsive stat cards with glassmorphism design

**Implementation Notes:**
- Backend: AdminStatsService + AdminDashboardController
- Frontend: /admin/dashboard page with 9 stat cards
- 19 tests passing (11 unit, 8 integration)
- Details: `.claude/features/20251216-admin-dashboard-phase1-stats.md`

---

### Global Admin Dashboard - Phase 2: Organization Table

**As a** global admin
**I want** to view and manage all organizations in a table
**So that** I can see org details at a glance

**Acceptance Criteria:**
- Table listing all organizations with columns:
  - Name, slug, status, created date, member count
- Search/filter by name, status
- Click org to view details
- GET `/admin/organizations` endpoint with pagination
- Security: `MANAGE_USERS` permission

**Dependencies:** Phase 1 complete

---

### Global Admin Dashboard - Phase 3: Actions & User List

**As a** global admin
**I want** to perform actions on organizations and view all users
**So that** I can manage the platform effectively

**Acceptance Criteria:**
- Suspend/activate organizations
- View platform-wide user list with search
- View org details and memberships
- POST `/admin/organizations/{id}/suspend` endpoint
- POST `/admin/organizations/{id}/activate` endpoint
- GET `/admin/users` endpoint with pagination
- Security: `MANAGE_USERS` permission for all actions

**Dependencies:** Phase 1 + 2 complete

---

### Organization Owner Assignment

**As a** global admin  
**I want** to assign an owner to a newly created organization  
**So that** they can manage their workspace independently

**Acceptance Criteria:**
- Assign owner during org creation or separately
- Creates `organization_membership` with OWNER role
- Owner receives email notification with access details
- Owner must be an existing user
- Can reassign owner if needed

---

### Organization Membership Management

**As an** organization owner  
**I want** to invite existing users and assign roles  
**So that** I can build my team

**Acceptance Criteria:**
- organization_memberships table (user_id, org_id, role, invited_by)
- Roles: OWNER, ADMIN, MEMBER
- Invite only ACTIVE users (search by email)
- Endpoints: invite, revoke, list members, update role
- Users can belong to multiple orgs
- Permission: OWNER/ADMIN can invite

---

### Tenant Context Filtering

**As a** developer  
**I want** automatic tenant isolation in all queries  
**So that** data leakage is prevented by default

**Acceptance Criteria:**
- Hibernate filter for organization_id
- TenantContext ThreadLocal extracting org_id from JWT
- Global admins can bypass filter for platform operations
- Applied automatically to all tenant-scoped entities
- Integration tests proving isolation

---

### User Context & Permissions

**As the** system
**I want** layered permission validation (global → org → team)
**So that** access control is properly enforced

**Acceptance Criteria:**
- Permission service checking: global_role permissions → org_role → team_role
- JWT structure: `{user_id, global_role, permissions[], org_id?, org_role?, team_ids[]}`
- Guard annotations: `@RequirePermission(...)`, `@RequireOrgRole(...)`, `@RequireTeamRole(...)`
- Permission-based approach (not role checks):
  - Global: Check user has specific permission (e.g., PROVISION_ORGANIZATIONS)
  - Org: Check user has org role + permission (e.g., MANAGE_ORG_MEMBERS)
  - Team: Check user has team role + permission (e.g., ASSIGN_TASKS)
- Clear hierarchy: Role.ADMIN (global) → OrganizationalRole.OWNER → ADMIN → MEMBER → TeamRole.LEAD → MEMBER
- Permissions attached to roles, guards check permissions (fine-grained, extensible)

---

### Team Creation & Management

**As an** organization admin  
**I want** to create teams within my organization  
**So that** I can organize users by project/department

**Acceptance Criteria:**
- teams table (id, organization_id, name, created_at)
- Unique constraint on (organization_id, name)
- CRUD endpoints for teams
- Permission: ADMIN/OWNER only
- Automatic tenant filtering applied

---

### Team Membership

**As a** team lead or admin  
**I want** to add/remove members from teams  
**So that** I can control team composition

**Acceptance Criteria:**
- team_memberships table (user_id, team_id, organization_id, role)
- Roles: LEAD, MEMBER
- Users must be org members before joining teams
- Denormalized organization_id for efficiency
- Endpoints: add/remove members, list team members

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
