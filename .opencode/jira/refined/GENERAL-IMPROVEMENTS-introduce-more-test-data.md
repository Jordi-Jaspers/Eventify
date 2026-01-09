# Introduce More Test Data

**Epic**: General Improvements
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-09

## 1. User Story
**As a** developer
**I want** comprehensive test data seeded into the development database
**So that** I can easily test and demonstrate all application features without manually creating data

## 2. Business Context & Value
The existing test data migration only includes users and organizations spread across 30 days for growth chart visualization. To effectively test and demonstrate the full application functionality (memberships, API keys, quotas), developers need a richer dataset that covers various edge cases and scenarios. This reduces manual setup time and ensures consistent testing environments.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Organization memberships exist with varied roles
    *   Given the test data migration runs
    *   When I query the `organization_membership` table
    *   Then I see the admin user (id=1) as OWNER of at least 3 organizations
    *   And I see other users as members with various roles (OWNER, ADMIN, MEMBER) across organizations
    *   And at least one organization has multiple members (5+)
    *   And at least one organization has only one member (the owner)

*   [ ] **Scenario 2**: User-scoped API keys exist with variety
    *   Given the test data migration runs
    *   When I query the `api_key` table for USER-scoped keys
    *   Then I see at least 3 API keys for the admin user (id=1)
    *   And at least one key is expired (`expires_at < NOW()`)
    *   And at least one key is approaching expiration (within 7 days)
    *   And at least one key has no expiration (`expires_at IS NULL`)
    *   And keys have varied `last_used_at` and `total_requests` values

*   [ ] **Scenario 3**: Organization-scoped API keys exist
    *   Given the test data migration runs
    *   When I query the `api_key` table for ORGANIZATION-scoped keys
    *   Then I see API keys for at least 3 different organizations
    *   And the keys are created by members with appropriate permissions
    *   And at least one org key is expired
    *   And at least one org key is approaching expiration

*   [ ] **Scenario 4**: API key audit trail exists
    *   Given the test data migration runs
    *   When I query the `api_key_audit` table
    *   Then I see at least 5 revoked key records
    *   And records show varied revocation dates across the last 30 days
    *   And records include both USER and ORGANIZATION scoped keys

*   [ ] **Scenario 5**: User event quotas exist
    *   Given the test data migration runs
    *   When I query the `user_event_quota` table
    *   Then I see quota records for multiple users
    *   And at least one user is near their quota limit (80%+)
    *   And at least one user has minimal usage

## 4. Technical Requirements
*   **Database**: Create new Liquibase migration files with `context="tst"`:
    *   `202601091000-TST-membership-test-data.xml` - Organization memberships
    *   `202601091001-TST-api-key-test-data.xml` - API keys (user + org scoped)
    *   `202601091002-TST-api-key-audit-test-data.xml` - Revoked key audit records
    *   `202601091003-TST-user-quota-test-data.xml` - User event quota data
*   **Naming Convention**: Follow existing pattern `YYYYMMDDHHMM-TST-<description>.xml`
*   **Dependencies**: Migrations must run AFTER:
    *   `202512171434-TST-growth-test-data.xml` (creates test users and orgs)
    *   `202601071000-PRD-api-key-tables.xml` (creates api_key tables)
    *   `202601081000-PRD-user-event-quota-table.xml` (creates quota table)
*   **Security**: Use the same BCrypt-hashed test password for consistency
*   **Performance**: N/A (test context only)

## 5. Design & UI/UX
N/A - This is backend/database work only. The data will be visible through existing UI pages.

## 6. Implementation Notes / Research
*   **Existing test data file**: `server/src/main/resources/db/changelog/changesets/202512171434-TST-growth-test-data.xml`
    *   Creates 30 users (IDs will be 2-31 assuming admin is ID 1)
    *   Creates 21 organizations (IDs will be 1-21)
*   **Admin user**: Assumed to have `id=1` (from bootstrap or initial setup)
*   **API Key hashing**: Use BCrypt hashing consistent with `ApiKeyService` - the test keys don't need to be usable, just present for display purposes. Use a placeholder hash.
*   **Key suffix**: Use realistic 4-character suffixes (e.g., `a1b2`, `x9y8`)
*   **Timestamps**: Spread data across the last 30 days for realistic display in date-based views
*   **Organization IDs**: Reference orgs created in growth-test-data (Acme Corp = ID 1, etc.)
*   **Membership roles**: Use enum values `OWNER`, `ADMIN`, `MEMBER` as defined in the application
