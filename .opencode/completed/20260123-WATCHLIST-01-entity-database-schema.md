# Watchlist Entity & Database Schema

**Completed:** 2026-01-23
**Vibe Kanban Task:** WATCHLIST - 01 Entity Database Schema
**Task ID:** c2285de5-b130-42fe-9bc7-c250daec0545

## Summary
Created database schema and JPA entities for watchlists - the foundation for timeline monitoring feature. Watchlists allow users to group channels together for consolidated event monitoring.

## Agents Used
| Agent | Task |
|-------|------|
| backend-agent | Created migration + entities |

## Database Schema

**Table: watchlist**
- id (SERIAL PK)
- name (VARCHAR 100, NOT NULL)
- description (VARCHAR 500, NULL)
- user_id (FK to user, CASCADE DELETE)
- organization_id (FK to org, CASCADE DELETE, NULL for personal)
- default_time_range (VARCHAR 20, default '24h')
- default_only_critical (BOOLEAN, default false)
- default_sort_by_severity (BOOLEAN, default true)
- created_at, updated_at (TIMESTAMPTZ)

**Table: watchlist_channel (Junction)**
- watchlist_id, channel_id (Composite PK)
- position (INTEGER, for ordering)
- CASCADE DELETE on both FKs

**Indexes:**
- Partial indexes for user/org queries
- Unique case-insensitive name constraints per user/org
- Reverse lookup index for channel → watchlists

## Files Created
- `server/src/main/resources/db/changelog/changesets/202601221000-PRD-watchlist-tables.xml`
- `server/src/main/java/io/github/eventify/api/watchlist/model/Watchlist.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistChannel.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistChannelId.java`

## Tests
- No tests required (entities/migrations have no business logic)
- Compile verified ✅
- Build verified ✅

## Notes
- Follows Channel entity pattern exactly
- Implements PageableItem for JFrame search compatibility
- Junction entity uses @EmbeddedId with @MapsId pattern
- Migration uses <sql> tags per Liquibase standards
