# Watchlist Entity & Database Schema

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-01-22

## 1. User Story
**As a** developer
**I want** a database schema and JPA entity for watchlists
**So that** watchlists can be persisted and managed by the application

## 2. Business Context & Value
Watchlists are the foundation of the timeline monitoring feature. They allow users to group channels together for consolidated monitoring. This story establishes the data model that all subsequent watchlist features will build upon.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Watchlist table exists with correct structure
    *   Given the application starts
    *   When Liquibase migrations run
    *   Then the `watchlist` table exists with all required columns

*   [ ] **Scenario 2**: Watchlist-channel junction table exists
    *   Given the application starts
    *   When Liquibase migrations run
    *   Then the `watchlist_channel` table exists to store the many-to-many relationship with ordering

*   [ ] **Scenario 3**: JPA entity maps correctly
    *   Given a Watchlist entity is created
    *   When it is persisted to the database
    *   Then all fields are correctly stored and retrievable

*   [ ] **Scenario 4**: Cascade delete from channel
    *   Given a channel is part of a watchlist
    *   When the channel is deleted
    *   Then the channel is automatically removed from all watchlists (but watchlist remains)

*   [ ] **Scenario 5**: Cascade delete from user/organization
    *   Given a user or organization has watchlists
    *   When the user or organization is deleted
    *   Then all their watchlists are also deleted

## 4. Technical Requirements

### Database Schema

**Table: `watchlist`**
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | SERIAL | NO | - | Primary key |
| `name` | VARCHAR(100) | NO | - | Watchlist name |
| `description` | VARCHAR(500) | YES | NULL | Optional description |
| `user_id` | INTEGER | NO | - | FK to user (owner or creator) |
| `organization_id` | INTEGER | YES | NULL | FK to organization (null for personal) |
| `default_time_range` | VARCHAR(20) | NO | '24h' | Default time range (24h, 7d, 30d) |
| `default_only_critical` | BOOLEAN | NO | false | Default filter: show only critical |
| `default_sort_by_severity` | BOOLEAN | NO | true | Default filter: sort by severity |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMPTZ | YES | NULL | Last update timestamp |

**Table: `watchlist_channel`** (Junction table)
| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `watchlist_id` | INTEGER | NO | FK to watchlist |
| `channel_id` | INTEGER | NO | FK to channel |
| `position` | INTEGER | NO | Order position (0-based) |

**Indexes:**
- `idx_watchlist_user` - Partial index on `user_id` WHERE `organization_id IS NULL`
- `idx_watchlist_org` - Partial index on `organization_id` WHERE `organization_id IS NOT NULL`
- `uq_watchlist_user_name` - Unique on `(user_id, lower(name))` WHERE `organization_id IS NULL`
- `uq_watchlist_org_name` - Unique on `(organization_id, lower(name))` WHERE `organization_id IS NOT NULL`
- `uq_watchlist_channel` - Unique on `(watchlist_id, channel_id)` - prevent duplicates
- `idx_watchlist_channel_watchlist` - Index on `watchlist_id` for efficient lookups

**Constraints:**
- FK `watchlist.user_id` -> `user.id` ON DELETE CASCADE
- FK `watchlist.organization_id` -> `organization.id` ON DELETE CASCADE
- FK `watchlist_channel.watchlist_id` -> `watchlist.id` ON DELETE CASCADE
- FK `watchlist_channel.channel_id` -> `channel.id` ON DELETE CASCADE

### JPA Entities

**Watchlist.java**
```java
@Entity
@Table(name = "watchlist")
public class Watchlist implements PageableItem, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "default_time_range", nullable = false, length = 20)
    private String defaultTimeRange;

    @Column(name = "default_only_critical", nullable = false)
    private Boolean defaultOnlyCritical;

    @Column(name = "default_sort_by_severity", nullable = false)
    private Boolean defaultSortBySeverity;

    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<WatchlistChannel> channels = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
```

**WatchlistChannel.java**
```java
@Entity
@Table(name = "watchlist_channel")
public class WatchlistChannel implements Serializable {
    @EmbeddedId
    private WatchlistChannelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("watchlistId")
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("channelId")
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "position", nullable = false)
    private Integer position;
}
```

**WatchlistChannelId.java** (Composite key)
```java
@Embeddable
public class WatchlistChannelId implements Serializable {
    @Column(name = "watchlist_id")
    private Long watchlistId;

    @Column(name = "channel_id")
    private Long channelId;
}
```

### API Changes
- N/A (this story is database/entity only)

### Security
- N/A (this story is database/entity only)

### Performance
- Indexes ensure efficient lookups by user/organization
- Position column allows ordered retrieval without additional sorting

## 5. Design & UI/UX
- N/A (backend only)

## 6. Implementation Notes / Research

### File Locations
- Migration: `server/src/main/resources/db/changelog/changesets/202601221000-PRD-watchlist-tables.xml`
- Entity: `server/src/main/java/io/github/eventify/api/watchlist/model/Watchlist.java`
- Entity: `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistChannel.java`
- Entity: `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistChannelId.java`

### Patterns to Follow
- Follow the channel table migration pattern in `202601111000-PRD-channel-table.xml`
- Follow the Channel entity pattern for structure and annotations
- Use `PageableItem` interface for JFrame search compatibility

### Time Range Values
The `default_time_range` column should accept: `24h`, `7d`, `30d`
Consider adding a CHECK constraint or handling validation in the application layer.

### Notes
- The `position` field in `watchlist_channel` enables drag-and-drop reordering
- When channels are deleted, ON DELETE CASCADE removes them from watchlists automatically
- The junction table approach (vs. JSON array) ensures referential integrity
