# Channel Entity & Database Schema

**Epic**: Event Channels
**Status**: Ready for Dev
**Estimate**: M (Medium)
**Created Date**: 2026-01-11

## 1. User Story
**As a** developer building the Event Channels feature
**I want** a well-designed Channel entity and database schema
**So that** I have a solid foundation for storing and querying channels

## 2. Business Context & Value
Channels are the core organizational unit for events in Eventify. Users and organizations create channels to group related events (e.g., "Production Errors", "User Signups"). A channel is an accumulation of time-based events which are later visualized in a timeline. This schema must support both personal and organization-owned channels with proper access control.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Channel entity is created with all required fields
    *   Given the new Channel entity
    *   When I inspect the database schema
    *   Then I see columns for: id, name, description, user_id, organization_id, status, retention_days, created_at, updated_at

*   [ ] **Scenario 2**: Personal channel ownership
    *   Given a user creates a personal channel
    *   When the channel is saved
    *   Then user_id is set to the creator
    *   And organization_id is NULL

*   [ ] **Scenario 3**: Organization channel ownership
    *   Given a user creates an organization channel
    *   When the channel is saved
    *   Then user_id is set to the creator (for audit)
    *   And organization_id is set to the organization

*   [ ] **Scenario 4**: Personal channel name uniqueness enforced
    *   Given a user creates a channel named "My Channel"
    *   When the same user tries to create another personal channel with the same name
    *   Then a unique constraint violation occurs
    *   And a different user can create a personal channel with the same name

*   [ ] **Scenario 5**: Organization channel name uniqueness enforced
    *   Given an organization has a channel named "Prod Errors"
    *   When someone tries to create another channel with the same name in that org
    *   Then a unique constraint violation occurs
    *   And a different organization can have a channel with the same name

*   [ ] **Scenario 6**: Channel status transitions
    *   Given a channel with status ACTIVE
    *   When the status is changed to PAUSED or PENDING_DELETION
    *   Then the change is persisted correctly

## 4. Technical Requirements
*   **New Entity**: `io.github.eventify.api.channel.model.Channel`
*   **Database Table**: `channel`
*   **Columns**:
    | Column | Type | Constraints |
    |--------|------|-------------|
    | id | SERIAL | PK |
    | name | VARCHAR(100) | NOT NULL |
    | description | VARCHAR(500) | NULLABLE |
    | user_id | INTEGER | NOT NULL, FK → user(id) |
    | organization_id | INTEGER | NULLABLE, FK → organization(id) |
    | status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' |
    | retention_days | INTEGER | NOT NULL, DEFAULT 30 |
    | created_at | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |
    | updated_at | TIMESTAMPTZ | NULLABLE |
*   **Ownership Model** (follows ApiKey pattern):
    *   Personal channel: `user_id` = owner, `organization_id` = NULL
    *   Organization channel: `user_id` = creator (audit), `organization_id` = owning org
*   **Status Enum**: `ChannelStatus` with values: ACTIVE, PAUSED, PENDING_DELETION
*   **Indexes**:
    *   `idx_channel_user` on (user_id) WHERE organization_id IS NULL — for personal channel lookups
    *   `idx_channel_org` on (organization_id) WHERE organization_id IS NOT NULL — for org channel lookups
    *   `idx_channel_status` on (status) — for filtering by status
*   **Unique Constraints**:
    *   `uq_channel_user_name` on (user_id, name) WHERE organization_id IS NULL
    *   `uq_channel_org_name` on (organization_id, name) WHERE organization_id IS NOT NULL
*   **Liquibase Migration**: New changeset following existing patterns

## 5. Design & UI/UX
N/A - Backend infrastructure story

## 6. Implementation Notes / Research
*   **Follow existing patterns**: See `ApiKey.java` for similar ownership model (user_id always set, organization_id nullable)
*   **No scope column needed**: Ownership type is derived from `organization_id IS NULL`
*   **Status enum**: Create `ChannelStatus` enum with ACTIVE, PAUSED, PENDING_DELETION
*   **Repository**: Create `ChannelRepository` with JpaRepository
*   **Retention default**: Use a constant `DEFAULT_RETENTION_DAYS = 30` that can later be made configurable
*   **user_id semantics**: For personal channels, this is the owner. For org channels, this tracks who created it (audit trail).
*   **Future enhancement**: When Event Ingestion is implemented, consider adding `last_event_at` and `last_event_severity` columns for dashboard display (updated on each event ingestion).
