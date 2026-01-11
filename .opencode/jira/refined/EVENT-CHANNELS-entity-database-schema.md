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
Channels are the core organizational unit for events in Eventify. Users and organizations create channels to group related events (e.g., "Production Errors", "User Signups"). This schema must support both personal and organization-owned channels with proper access control.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Channel entity is created with all required fields
    *   Given the new Channel entity
    *   When I inspect the database schema
    *   Then I see columns for: id, name, description, scope, owner_id, organization_id, status, retention_days, created_at, created_by, updated_at

*   [ ] **Scenario 2**: Personal channel uniqueness enforced
    *   Given a user creates a channel named "My Channel"
    *   When the same user tries to create another channel with the same name
    *   Then a unique constraint violation occurs
    *   And a different user can create a channel with the same name

*   [ ] **Scenario 3**: Organization channel uniqueness enforced
    *   Given an organization has a channel named "Prod Errors"
    *   When someone tries to create another channel with the same name in that org
    *   Then a unique constraint violation occurs
    *   And a different organization can have a channel with the same name

*   [ ] **Scenario 4**: Channel status transitions
    *   Given a channel with status ACTIVE
    *   When the status is changed to PAUSED or PENDING_DELETION
    *   Then the change is persisted correctly

## 4. Technical Requirements
*   **New Entity**: `io.github.eventify.api.channel.model.Channel`
*   **Database Table**: `channel`
*   **Columns**:
    | Column | Type | Constraints |
    |--------|------|-------------|
    | id | BIGINT | PK, AUTO_INCREMENT |
    | name | VARCHAR(100) | NOT NULL |
    | description | VARCHAR(500) | NULLABLE |
    | scope | VARCHAR(20) | NOT NULL (ENUM: USER, ORGANIZATION) |
    | owner_id | BIGINT | NOT NULL, FK → user(id) |
    | organization_id | BIGINT | NULLABLE, FK → organization(id) |
    | status | VARCHAR(20) | NOT NULL, DEFAULT 'ACTIVE' (ENUM: ACTIVE, PAUSED, PENDING_DELETION) |
    | retention_days | INT | NOT NULL, DEFAULT 30 |
    | created_at | TIMESTAMP WITH TZ | NOT NULL, DEFAULT NOW() |
    | created_by | BIGINT | NOT NULL, FK → user(id) |
    | updated_at | TIMESTAMP WITH TZ | NULLABLE |
*   **Indexes**:
    *   `idx_channel_owner` on (owner_id) for user channel lookups
    *   `idx_channel_organization` on (organization_id) for org channel lookups
    *   `idx_channel_status` on (status) for filtering active channels
*   **Unique Constraints**:
    *   `uq_channel_user_name` on (owner_id, name) WHERE scope = 'USER'
    *   `uq_channel_org_name` on (organization_id, name) WHERE scope = 'ORGANIZATION'
*   **Liquibase Migration**: New changeset in appropriate migration file

## 5. Design & UI/UX
N/A - Backend infrastructure story

## 6. Implementation Notes / Research
*   **Follow existing patterns**: See `ApiKey.java` for similar scope-based entity design
*   **Scope enum**: Create `ChannelScope` enum similar to `ApiKeyScope`
*   **Status enum**: Create `ChannelStatus` enum with ACTIVE, PAUSED, PENDING_DELETION
*   **Repository**: Create `ChannelRepository` with JpaRepository
*   **Retention default**: Use a constant `DEFAULT_RETENTION_DAYS = 30` that can later be made configurable
*   **created_by vs owner_id**: `owner_id` is the user who owns personal channels; `created_by` tracks who created it (same for personal, may differ for org channels)
