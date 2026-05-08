## [2026-01-11] - Channel Entity & Database Schema

### Plan (approved)
Create Channel entity, ChannelStatus enum, ChannelRepository, and database migration for storing event channels with personal/organization ownership model.

### Actual Changes
**Backend:**
- `Channel.java` entity with ownership model (user_id always set, organization_id nullable for org channels)
- `ChannelStatus.java` enum: ACTIVE, PAUSED, PENDING_DELETION
- `ChannelRepository.java` with findAllByUserId, findAllByOrganizationId methods
- Default status: ACTIVE
- Added `retentionDays` field to `User.java` and `Organization.java` entities (default 90)

**Database:**
- New `channel` table with columns: id, name, description, user_id, organization_id, status, created_at, updated_at
- Partial indexes: idx_channel_user, idx_channel_org, idx_channel_status
- Case-insensitive partial unique constraints:
  - uq_channel_user_name (personal channels per user)
  - uq_channel_org_name (org channels per organization)
- FK constraints with ON DELETE CASCADE
- Table/column/index comments

**Retention Settings (user/org level):**
- Added `retention_days` column to `user` table (default 90, min 90, max 1825)
- Added `retention_days` column to `organization` table (default 90, min 90, max 1825)
- CHECK constraints: `chk_user_retention_days`, `chk_org_retention_days` (90-1825 days = 3 months to 5 years)

**Note:** Per-channel retention override added to Future Considerations backlog.

### Agents Used
- java-backend-agent: Entity, enum, repository, migration

### Files Created
- `server/src/main/java/io/github/eventify/api/channel/model/Channel.java`
- `server/src/main/java/io/github/eventify/api/channel/model/ChannelStatus.java`
- `server/src/main/java/io/github/eventify/api/channel/repository/ChannelRepository.java`
- `server/src/main/resources/db/changelog/changesets/202601111000-PRD-channel-table.xml`
- `server/src/main/resources/db/changelog/changesets/202601111001-PRD-retention-days-columns.xml`

### Files Modified
- `server/src/main/java/io/github/eventify/api/user/model/User.java` - added retentionDays field
- `server/src/main/java/io/github/eventify/api/organization/model/Organization.java` - added retentionDays field

### Quality Metrics
- Build: Successful
- Quality checks: Spotless, Checkstyle, PMD, SpotBugs passed
