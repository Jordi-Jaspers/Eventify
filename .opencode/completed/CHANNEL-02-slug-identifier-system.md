# Channel Slug Identifier System

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-02-16
**Depends On**: None

## 1. User Story
**As a** developer integrating with Eventify
**I want** channels to have human-readable slug identifiers
**So that** I can reference channels by meaningful names like `myapp.prod.errors` instead of numeric IDs

## 2. Business Context & Value
Currently, channels are identified only by auto-increment numeric IDs (e.g., `123`). This creates friction:
- Developers must look up IDs before sending events
- IDs are meaningless and easy to confuse (`123` vs `132`)
- No semantic meaning in logs or debugging

Slugs provide:
- **Self-documenting code**: `channelSlug: "payments.prod.errors"` is immediately clear
- **Easier onboarding**: Developers can use meaningful names from day one
- **Better debugging**: Logs show which channel by name, not ID

## 3. Acceptance Criteria

### Backend
* [ ] **Scenario 1**: Slug column exists with constraints
    * Given the database schema
    * When I inspect the `channel` table
    * Then there is a `slug` column (varchar 100, not null)
    * And unique index exists per owner (user_id or organization_id)

* [ ] **Scenario 2**: Slug format validation
    * Given a channel creation request
    * When the slug contains invalid characters (uppercase, spaces, special chars except dots)
    * Then the request is rejected with 400 Bad Request
    * And error message explains valid format

* [ ] **Scenario 3**: Slug format - valid examples
    * Given valid slugs: `errors`, `myapp.errors`, `myapp.prod.backend.errors`
    * When creating channels with these slugs
    * Then all are accepted

* [ ] **Scenario 4**: Slug uniqueness per owner
    * Given user A has a channel with slug `errors`
    * When user A tries to create another channel with slug `errors`
    * Then the request is rejected with 400 Bad Request

* [ ] **Scenario 5**: Same slug allowed for different owners
    * Given user A has a channel with slug `errors`
    * When user B creates a channel with slug `errors`
    * Then the channel is created successfully

* [ ] **Scenario 6**: Migration backfills existing channels
    * Given existing channels without slugs
    * When the migration runs
    * Then all channels have slugs generated from their names
    * And slugs are sanitized (lowercase, spaces→dots, special chars removed)

* [ ] **Scenario 7**: TST seed data includes slugs
    * Given the TST migration scripts
    * When inserting channel test data
    * Then all INSERT statements include valid `slug` values
    * And the local dev environment starts successfully with `./gradlew bootRun`

### Frontend
* [ ] **Scenario 8**: Slug displayed in channel table
    * Given the channels table
    * When viewing the table
    * Then the slug is visible (new column or replacing ID display)

* [ ] **Scenario 9**: Slug input on channel creation
    * Given the create channel form
    * When creating a channel
    * Then there is a required slug input field
    * And validation feedback shows format requirements

* [ ] **Scenario 10**: Slug shown in channel details
    * Given a channel details view/modal
    * When viewing channel info
    * Then the slug is prominently displayed

## 4. Technical Requirements

### Database Changes
```sql
-- Add slug column
ALTER TABLE channel ADD COLUMN slug VARCHAR(100);

-- Backfill existing channels (slug from name)
UPDATE channel SET slug = /* sanitized name */;

-- Make not null after backfill
ALTER TABLE channel ALTER COLUMN slug SET NOT NULL;

-- Unique indexes per owner type
CREATE UNIQUE INDEX uq_channel_user_slug 
  ON channel (user_id, slug) WHERE organization_id IS NULL;
  
CREATE UNIQUE INDEX uq_channel_org_slug 
  ON channel (organization_id, slug) WHERE organization_id IS NOT NULL;
```

### API Changes
| Endpoint | Change |
|----------|--------|
| `POST /v1/user/channel` | Add required `slug` field to request |
| `POST /v1/organization/{orgId}/channels` | Add required `slug` field to request |
| All channel responses | Include `slug` in response DTOs |

### Validation Rules
- Pattern: `^[a-z0-9]+(\.[a-z0-9]+)*$`
- Length: 1-100 characters
- Examples valid: `errors`, `myapp.errors`, `prod.backend.v2`
- Examples invalid: `My-Errors`, `my_errors`, `my errors`, `.errors`, `errors.`

### Entity Changes
```java
@Column(name = "slug", nullable = false, length = 100)
private String slug;
```

## 5. Design & UI/UX
- Channel table: Add "Slug" column (or rename "Name" column behavior)
- Create channel form: Add slug input with format hint ("lowercase letters, numbers, dots")
- Channel details: Show slug prominently, consider it the "primary identifier" for developers

## 6. Implementation Notes / Research

### Existing Files to Modify
| File | Change |
|------|--------|
| `Channel.java` | Add `slug` field |
| `CreateChannelRequest.java` | Add `slug` field (required) |
| `ChannelDetailsResponse.java` | Add `slug` field |
| `ChannelValidator.java` | Add slug format validation |
| `ChannelService.java` | Validate slug uniqueness |
| `OrganizationChannelService.java` | Validate slug uniqueness |
| `ChannelRepository.java` | Add `existsByUserIdAndSlug()`, `existsByOrganizationIdAndSlug()` |
| `202601121000-TST-channel-test-data.xml` | Add `slug` column to all channel INSERTs |
| `202601271000-ACC-admin-data.xml` | Add `slug` column to channel INSERTs |

### Migration Strategy
1. Add nullable `slug` column
2. Backfill from name: `lower(regexp_replace(name, '[^a-z0-9]+', '.', 'g'))`
3. Handle collisions by appending `.1`, `.2`, etc.
4. Set NOT NULL constraint
5. Add unique indexes

### Test/Seed Data Migrations (TST)
The following seed data files insert channel records and **must be updated** to include the `slug` column:

| File | Context | Action Required |
|------|---------|-----------------|
| `202601121000-TST-channel-test-data.xml` | TST | Add `slug` to all INSERT statements |

**Suggested slug values for seeded channels:**

| Channel Name | Suggested Slug |
|--------------|----------------|
| Backend Errors | `backend.errors` |
| Frontend Logs | `frontend.logs` |
| API Gateway | `api.gateway` |
| Database Events | `database.events` |
| Auth Service | `auth.service` |
| Payment Processing | `payment.processing` |
| Test Channel | `test.channel` |
| (User-specific channels) | Derive from name, e.g., `alice.errors` |
| (Org channels for Acme, Beta, Gamma) | `{org}.{service}`, e.g., `acme.backend` |

**Dependent TST scripts** (no changes needed, but verify they still work):
- `202601241000-TST-watchlist-test-data.xml` - References channels by name
- `202601251000-TST-watchlist-groups-test-data.xml` - References channels by name
- `202601221000-TST-event-test-data.xml` - Queries channels for event generation

### Breaking Change Notice
- Existing channels will have auto-generated slugs
- API now requires `slug` on creation
- Add to release notes: "Channel creation now requires a `slug` field. Existing channels have been assigned auto-generated slugs based on their names."
