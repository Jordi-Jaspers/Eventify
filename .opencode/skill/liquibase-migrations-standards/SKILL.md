---
name: liquibase-migrations-standards
description: Database migration patterns using Liquibase with raw SQL. Use when creating tables, adding columns, creating indexes, modifying schema, or any database changes. Always use <sql> tags, never Liquibase XML tags like <createTable>.
compatibility: opencode
metadata:
  language: SQL, XML
  build-tool: gradle
---

# Liquibase Migration Standards

All migrations use raw SQL in `<sql>` tags. Never use Liquibase XML tags (`<createTable>`, `<addColumn>`, etc.).

## File Structure

```
resources/db/changelog/
├── db.changelog-master.yaml     # Master changelog
├── db.changelog-plugin.yaml     # Plugin configurations
├── changesets/
│   ├── 202411122200-PRD-initial-tables-user-management.xml
│   ├── 202512111000-PRD-organization-table.xml
│   ├── 202512171434-TST-growth-test-data.xml    # Test data
│   └── 202601071000-PRD-api-key-tables.xml
└── triggers/
    └── update_membership_count.sql              # Trigger functions
```

## Naming Convention

```
YYYYMMDDHHMI-{ENV}-description.xml
```

- `YYYYMMDDHHMI` - Timestamp (year, month, day, hour, minute)
- `{ENV}` - Environment prefix:
    - `PRD` - Production migrations (schema, indexes, constraints)
    - `TST` - Test/seed data only
- `description` - Kebab-case description

## Base Template

```xml
<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd"
                   logicalFilePath="202501071430-PRD-description.xml">
    
    <changeSet id="202501071430-PRD-description-1" author="developer.name">
        <comment>Brief description of what this migration does.</comment>
        <sql>
            -- SQL here
        </sql>
    </changeSet>
</databaseChangeLog>
```

**Notes:**
- Use `<comment>` tag (not `<ext:documentation>`)
- Author: `firstname.lastname` format
- ChangeSet ID: `{timestamp}-{ENV}-{description}-{sequence}`

---

## Common Patterns

### Create Table

```xml
<changeSet id="202501071430-PRD-create-events-1" author="developer.name">
    <comment>Creating events table for storing user events.</comment>
    <sql>
        CREATE TABLE IF NOT EXISTS event (
            id          SERIAL       PRIMARY KEY,
            name        TEXT         NOT NULL,
            description TEXT,
            user_id     INTEGER      NOT NULL,
            status      TEXT         NOT NULL DEFAULT 'DRAFT',
            starts_at   TIMESTAMPTZ  NOT NULL,
            ends_at     TIMESTAMPTZ,
            created_at  TIMESTAMPTZ  NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
            updated_at  TIMESTAMPTZ  NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
            FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
        );

        CREATE INDEX idx_event_user_id ON event (user_id);
        CREATE INDEX idx_event_status ON event (status);
    </sql>
</changeSet>

<changeSet id="202501071430-PRD-create-events-2" author="developer.name">
    <comment>Adding documentation for events table.</comment>
    <sql>
        COMMENT ON TABLE event IS 'Stores event information created by users';
        COMMENT ON COLUMN event.id IS 'Unique identifier for the event';
        COMMENT ON COLUMN event.name IS 'Event name/title';
        COMMENT ON COLUMN event.user_id IS 'Reference to the user who created the event';
        COMMENT ON COLUMN event.status IS 'Event status (DRAFT, PUBLISHED, CANCELLED, COMPLETED)';
        COMMENT ON INDEX idx_event_user_id IS 'Index for finding events by user';
        COMMENT ON INDEX idx_event_status IS 'Index for filtering events by status';
    </sql>
</changeSet>
```

### Create Table with UUID Primary Key

```xml
<changeSet id="202501071430-PRD-create-tokens-1" author="developer.name">
    <comment>Creating password reset tokens table.</comment>
    <sql>
        CREATE TABLE IF NOT EXISTS password_reset_token (
            id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
            user_id    INTEGER      NOT NULL,
            token_hash TEXT         NOT NULL,
            expires_at TIMESTAMPTZ  NOT NULL,
            created_at TIMESTAMPTZ  NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
            FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
        );
    </sql>
</changeSet>
```

### Add Column

```xml
<changeSet id="202501071430-PRD-add-phone-column-1" author="developer.name">
    <comment>Adding phone number column to users table.</comment>
    <sql>
        ALTER TABLE "user" ADD COLUMN IF NOT EXISTS phone TEXT;
    </sql>
</changeSet>
```

### Add Column with Default (Non-Nullable)

```xml
<changeSet id="202501071430-PRD-add-enabled-column-1" author="developer.name">
    <comment>Adding enabled flag to users table with default value.</comment>
    <sql>
        ALTER TABLE "user" ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
    </sql>
</changeSet>
```

### Create Index

```xml
<changeSet id="202501071430-PRD-create-indexes-1" author="developer.name">
    <comment>Creating indexes for event queries.</comment>
    <sql>
        CREATE INDEX idx_event_user_id ON event (user_id);
        CREATE INDEX idx_event_status ON event (status);
        CREATE INDEX idx_event_starts_at ON event (starts_at);
    </sql>
</changeSet>
```

### Create Partial Index

```xml
<changeSet id="202501071430-PRD-partial-indexes-1" author="developer.name">
    <comment>Creating partial indexes for common query patterns.</comment>
    <sql>
        CREATE INDEX idx_user_enabled ON "user" (enabled) WHERE enabled = TRUE;
        CREATE INDEX idx_user_unvalidated ON "user" (validated) WHERE validated = FALSE;
    </sql>
</changeSet>
```

### Create Unique Constraint

```xml
<changeSet id="202501071430-PRD-unique-email-1" author="developer.name">
    <comment>Adding unique constraint on user email.</comment>
    <sql>
        CREATE UNIQUE INDEX idx_user_email_unique ON "user" (LOWER(email));
    </sql>
</changeSet>
```

### Create Composite Index

```xml
<changeSet id="202501071430-PRD-composite-index-1" author="developer.name">
    <comment>Creating composite index for event lookups by user and status.</comment>
    <sql>
        CREATE INDEX idx_event_user_status ON event (user_id, status);
    </sql>
</changeSet>
```

### Add Foreign Key to Existing Table

```xml
<changeSet id="202501071430-PRD-add-fk-1" author="developer.name">
    <comment>Adding foreign key constraint from event to category.</comment>
    <sql>
        ALTER TABLE event 
        ADD COLUMN IF NOT EXISTS category_id INTEGER,
        ADD CONSTRAINT fk_event_category 
            FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE SET NULL;
    </sql>
</changeSet>
```

### Add Check Constraint

```xml
<changeSet id="202501071430-PRD-add-check-1" author="developer.name">
    <comment>Adding check constraint for valid status values.</comment>
    <sql>
        ALTER TABLE event 
        ADD CONSTRAINT chk_event_status 
            CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED'));
    </sql>
</changeSet>
```

### Rename Column

```xml
<changeSet id="202501071430-PRD-rename-column-1" author="developer.name">
    <comment>Renaming start_date to starts_at for consistency.</comment>
    <sql>
        ALTER TABLE event RENAME COLUMN start_date TO starts_at;
    </sql>
</changeSet>
```

### Drop Column

```xml
<changeSet id="202501071430-PRD-drop-column-1" author="developer.name">
    <comment>Removing deprecated legacy_id column.</comment>
    <sql>
        ALTER TABLE event DROP COLUMN IF EXISTS legacy_id;
    </sql>
</changeSet>
```

### Create Junction Table (Many-to-Many)

```xml
<changeSet id="202501071430-PRD-create-junction-1" author="developer.name">
    <comment>Creating junction table for event attendees.</comment>
    <sql>
        CREATE TABLE IF NOT EXISTS event_attendee (
            event_id   INTEGER NOT NULL,
            user_id    INTEGER NOT NULL,
            rsvp_at    TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
            PRIMARY KEY (event_id, user_id),
            FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,
            FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
        );

        CREATE INDEX idx_event_attendee_user ON event_attendee (user_id);
    </sql>
</changeSet>
```

### Insert Seed Data (TST prefix)

```xml
<changeSet id="202501071430-TST-seed-roles-1" author="developer.name">
    <comment>Seeding default roles for testing.</comment>
    <sql>
        INSERT INTO role (name, description) VALUES 
            ('ROLE_USER', 'Standard user role'),
            ('ROLE_ADMIN', 'Administrator role'),
            ('ROLE_MODERATOR', 'Moderator role')
        ON CONFLICT (name) DO NOTHING;
    </sql>
</changeSet>
```

### Data Migration

```xml
<changeSet id="202501071430-PRD-migrate-data-1" author="developer.name">
    <comment>Migrating legacy status values to new format.</comment>
    <sql>
        UPDATE event SET status = 'PUBLISHED' WHERE status = 'ACTIVE';
        UPDATE event SET status = 'CANCELLED' WHERE status = 'DELETED';
    </sql>
</changeSet>
```

---

## TimescaleDB Patterns

### Enable Extensions (First Migration)

```xml
<changeSet id="202501071430-PRD-extensions-1" author="developer.name">
    <comment>Adding TimescaleDB extensions. Must be executed with superuser.</comment>
    <sql>
        CREATE EXTENSION IF NOT EXISTS timescaledb;
        CREATE EXTENSION IF NOT EXISTS timescaledb_toolkit;
        CREATE EXTENSION IF NOT EXISTS pg_trgm;
    </sql>
</changeSet>
```

### Create Hypertable

```xml
<changeSet id="202501071430-PRD-create-metrics-1" author="developer.name">
    <comment>Creating metrics hypertable for time-series data.</comment>
    <sql>
        CREATE TABLE IF NOT EXISTS metric (
            time       TIMESTAMPTZ      NOT NULL,
            device_id  INTEGER          NOT NULL,
            value      DOUBLE PRECISION NOT NULL,
            metadata   JSONB
        );

        SELECT create_hypertable('metric', 'time', if_not_exists => TRUE);
        
        CREATE INDEX idx_metric_device_time ON metric (device_id, time DESC);
    </sql>
</changeSet>
```

### Add Compression Policy

```xml
<changeSet id="202501071430-PRD-add-compression-1" author="developer.name">
    <comment>Adding compression policy for metrics older than 7 days.</comment>
    <sql>
        ALTER TABLE metric SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'device_id'
        );

        SELECT add_compression_policy('metric', INTERVAL '7 days', if_not_exists => TRUE);
    </sql>
</changeSet>
```

### Add Retention Policy

```xml
<changeSet id="202501071430-PRD-add-retention-1" author="developer.name">
    <comment>Adding retention policy to drop data older than 90 days.</comment>
    <sql>
        SELECT add_retention_policy('metric', INTERVAL '90 days', if_not_exists => TRUE);
    </sql>
</changeSet>
```

---

## Trigger Functions

Store trigger functions in `triggers/` folder, reference from changesets:

**triggers/update_membership_count.sql:**
```sql
CREATE OR REPLACE FUNCTION update_membership_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE organization 
    SET member_count = (
        SELECT COUNT(*) FROM organization_membership 
        WHERE organization_id = COALESCE(NEW.organization_id, OLD.organization_id)
    )
    WHERE id = COALESCE(NEW.organization_id, OLD.organization_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Reference in changeset:**
```xml
    <changeSet id="202512241500-PRD-organization-membership-table-1" author="developer.name">
    <comment>
        Create membership count trigger for organization_membership
    </comment>
    <sqlFile
            path="../triggers/update_membership_count.sql"
            relativeToChangelogFile="true"
            splitStatements="false"
            stripComments="false"/>
    <sql>
        DROP TRIGGER IF EXISTS organization_membership_trigger
        ON organization_membership;

        CREATE TRIGGER organization_membership_trigger
        AFTER INSERT OR DELETE
        ON organization_membership
        FOR EACH ROW
        EXECUTE FUNCTION update_membership_count();
    </sql>
</changeSet>
```

---

## Rules

1. **Always use `<sql>` tags** - Never `<createTable>`, `<addColumn>`, etc.
2. **Use `<comment>` tag** - Not `<ext:documentation>`
3. **Use `IF NOT EXISTS` / `IF EXISTS`** - Makes migrations idempotent
4. **One logical change per file** - Related changes use multiple changesets
5. **Quote reserved words** - Always `"user"`, `"role"`, `"token"`
6. **Use TIMESTAMPTZ** - Never TIMESTAMP without timezone
7. **Default to UTC** - `DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')`
8. **Add COMMENT ON** - Document tables, columns, indexes in separate changeset
9. **Foreign keys cascade** - `ON DELETE CASCADE` or `ON DELETE SET NULL`

## Column Type Reference

| Use Case | PostgreSQL Type |
|----------|-----------------|
| Primary key (auto) | `SERIAL PRIMARY KEY` |
| Primary key (UUID) | `UUID PRIMARY KEY DEFAULT gen_random_uuid()` |
| Foreign key | `INTEGER` |
| Short text | `TEXT` (not VARCHAR) |
| Boolean | `BOOLEAN` |
| Integer | `INTEGER` |
| Decimal/money | `NUMERIC(precision, scale)` |
| Timestamp | `TIMESTAMPTZ` |
| JSON data | `JSONB` |

## Commands

```bash
cd server/
./gradlew bootRun                    # Run (applies migrations)
./gradlew liquibaseStatus            # Check migration status
./gradlew liquibaseRollbackCount -PliquibaseCommandValue=1  # Rollback
```
