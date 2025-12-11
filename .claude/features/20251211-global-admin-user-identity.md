## [2025-12-11] - Global Admin & User Identity

### Feature plan approved by user
**Requirements Summary**

- Global admin capability via `Role.ADMIN` enum value
- Bootstrap listener creates first admin from ENV vars on startup
- JWT includes `global_role` claim (renamed from `role`)
- Email uniqueness (already enforced)
- ApplicationStartedEvent listener checks for existing admin
- ENV vars: GLOBAL_ADMIN_EMAIL, GLOBAL_ADMIN_PASSWORD, GLOBAL_ADMIN_FIRST_NAME, GLOBAL_ADMIN_LAST_NAME

**Technical Approach**

**Backend Changes:**
- Add GLOBAL_ROLE constant to JWTClaimNames
- Update JwtService.generateAccessToken() to use GLOBAL_ROLE claim instead of ROLE
- Create GlobalAdminBootstrap component (ApplicationStartedEvent listener)
- Bootstrap checks if any Role.ADMIN exists in DB
- If not + ENV vars present → create first admin
- Password hashing via PasswordEncoder (BCrypt)
- User created with: role=ADMIN, enabled=true, validated=true

**Backlog Updates:**
- Update Story #1 (Global Admin & User Identity) - mark as completed
- Update Story #2 (Organization Provisioning) - use permission-based guards
- Update Story #4 (Global Admin Dashboard) - specify permission checks
- Update Story #7 (User Context & Permissions) - document permission-based approach
- Rationale: Fine-grained permissions > role checks (extensible, clear intent)

**Implementation Workflow**

Phase 1: Tests First
Agent: java-testing-agent
Task: Create comprehensive test suite for GlobalAdminBootstrap
Deliverable: 22 tests covering all edge cases, 100% passing

Phase 2: Backend Implementation
Agent: java-backend-agent
Task: Implement JWT claim rename
Deliverable: GLOBAL_ROLE constant added, JwtService updated, all tests passing

Phase 3: Documentation Update
Agent: Orchestrator
Task: Update backlog with permission-based approach
Deliverable: Backlog stories updated with permission guards

**Success Criteria**

✅ JWT includes `global_role` claim (not `role`)
✅ Bootstrap listener creates first admin from ENV vars
✅ Tests passing (>90% coverage)
✅ Build successful
✅ Backlog updated with permission-based guards
✅ No breaking changes to existing auth flow

**Estimated Effort**

~45-60 minutes

---

### Actual changelog after completion
#### Summary
Implemented global admin capability using `Role.ADMIN` enum value, JWT claim rename to `global_role`, and ApplicationStartedEvent bootstrap listener for first admin creation. Updated backlog stories to use permission-based security guards instead of role checks.

#### Changes
**Backend:**
- Added `GLOBAL_ROLE` constant to JWTClaimNames
- Updated JwtService.generateAccessToken() to use `GLOBAL_ROLE` claim instead of `ROLE`
- Created GlobalAdminBootstrap component with ApplicationStartedEvent listener
- Bootstrap checks if any User with Role.ADMIN exists in database
- If no admin + ENV vars present → creates first admin with hashed password
- User created with: role=ADMIN, enabled=true, validated=true
- Added `existsByRole(Role role)` method to UserRepository

**Documentation:**
- Updated `.claude/backlog.md`:
  - Story #1 marked as completed with implementation notes
  - Story #2 updated to use `@RequirePermission(PROVISION_ORGANIZATIONS)`
  - Story #4 updated with permission-based guards (VIEW_ALL_ORGANIZATIONS, MANAGE_ORGANIZATIONS)
  - Story #7 updated with permission-based approach and correct JWT structure
- Rationale documented: Fine-grained permissions over role checks

**Testing:**
- 22 unit tests created for GlobalAdminBootstrap (100% passing)
- Coverage: 100% line, 100% branch
- All 238 tests passing (including existing tests)

**Security:**
- Password hashed using PasswordEncoder.encode() before storage
- No plain text passwords stored
- Role explicitly set to ADMIN
- User enabled and validated by default
- Graceful handling of missing/empty ENV vars

#### Agents Used
- java-testing-agent (GlobalAdminBootstrap test suite creation)
- java-backend-agent (JWT claim rename implementation)
- Orchestrator (backlog documentation updates)

#### Files Modified
- `/opt/hawaii/workspace/eventify/server/src/main/java/io/github/eventify/api/token/model/JWTClaimNames.java` - Added GLOBAL_ROLE constant
- `/opt/hawaii/workspace/eventify/server/src/main/java/io/github/eventify/api/token/service/JwtService.java` - Updated to use GLOBAL_ROLE claim
- `/opt/hawaii/workspace/eventify/server/src/main/java/io/github/eventify/api/bootstrap/GlobalAdminBootstrap.java` - New component (84 lines)
- `/opt/hawaii/workspace/eventify/server/src/test/java/io/github/eventify/api/bootstrap/GlobalAdminBootstrapTest.java` - New test suite (575 lines, 22 tests)
- `/opt/hawaii/workspace/eventify/server/src/main/java/io/github/eventify/api/user/repository/UserRepository.java` - Added existsByRole method
- `/opt/hawaii/workspace/eventify/.claude/backlog.md` - Updated stories #1, #2, #4, #7 with permission-based approach

#### Quality Metrics
- ✅ Tests: 22 new tests, 238 total passing
- ✅ Coverage: 100% line, 100% branch (GlobalAdminBootstrap)
- ✅ Build: Successful
- ✅ Quality checks: Passed (Checkstyle, PMD, SpotBugs, Spotless)
- ✅ No warnings or violations
- ✅ Test duration: 0.599 seconds

#### Notes
- Using `Role` enum (not separate boolean flag) for clean multi-tenant design
- JWT claim renamed to `global_role` for clarity (later: `org_role` will be added)
- Permission-based guards preferred over role checks (fine-grained, extensible)
- Bootstrap is idempotent (skips if admin already exists)
- ENV vars are optional (graceful handling if missing)
- Default names: "Global" (first), "Admin" (last)
- ROLE constant retained for potential backward compatibility needs
- Refresh tokens unaffected (don't contain role claims)
- No breaking changes to existing authentication flow
- Prepares JWT structure for multi-tenant role management
- Future work: Add specific permissions (PROVISION_ORGANIZATIONS, VIEW_ALL_ORGANIZATIONS, etc.)

---

### Post-Implementation Refactor: Bootstrap Properties via application.yml

**Date:** 2025-12-11 (same day)

**Changes Made:**

**1. BootstrapProperties Refactor**
- Removed `@Configuration` annotation (conflicted with nested ConfigurationProperties)
- Removed `@Value` annotations (Spring Boot auto-binds fields from parent ConfigurationProperties)
- Changed to simple POJO with fields: `email`, `password`, `firstName`, `lastName`
- Added default values: `firstName = "Global"`, `lastName = "Admin"`
- Now properly bound via SecurityProperties (ConfigurationProperties parent)

**2. GlobalAdminBootstrap Updated**
- Replaced `Environment` dependency with `SecurityProperties`
- Now reads properties via `securityProperties.getBootstrap().getEmail()` etc.
- Cleaner, type-safe property access
- Updated Javadoc to reflect configuration via application.yml

**3. application.yml Configuration**
```yaml
security:
  bootstrap:
    email: ${GLOBAL_ADMIN_EMAIL:jordijaspers@gmail.com}
    password: ${GLOBAL_ADMIN_PASSWORD:admin123!}
    first-name: ${GLOBAL_ADMIN_FIRST_NAME:Global}
    last-name: ${GLOBAL_ADMIN_LAST_NAME:Admin}
```

**4. Test Updates**
- Updated all 22 GlobalAdminBootstrapTest tests
- Mock `SecurityProperties` and `BootstrapProperties` instead of `Environment`
- All tests passing ✅

**Files Modified:**
- `BootstrapProperties.java` - Refactored to proper nested properties format
- `GlobalAdminBootstrap.java` - Uses SecurityProperties instead of Environment
- `GlobalAdminBootstrapTest.java` - Updated mocks to use SecurityProperties
- `application.yml` - Added first-name and last-name properties

**Benefits:**
- Centralized configuration in application.yml
- Type-safe property access via BootstrapProperties
- Still supports ENV var overrides (Spring Boot convention)
- Cleaner code (no manual Environment.getProperty() calls)
- Better IDE support (autocomplete for properties)

**Usage:**

Via application.yml (default):
```yaml
security:
  bootstrap:
    email: admin@mycompany.com
    password: securePassword123!
    first-name: Platform
    last-name: Administrator
```

Via ENV vars (override):
```bash
GLOBAL_ADMIN_EMAIL=admin@mycompany.com
GLOBAL_ADMIN_PASSWORD=securePassword123!
GLOBAL_ADMIN_FIRST_NAME=Platform
GLOBAL_ADMIN_LAST_NAME=Administrator
```

**Quality Metrics:**
- ✅ All 238 tests passing
- ✅ Build successful
- ✅ Properties properly bound from application.yml
- ✅ Backward compatible (ENV vars still work)