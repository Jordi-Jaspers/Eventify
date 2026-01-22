# Watchlist CRUD API (User)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-01-entity-database-schema.md

## 1. User Story
**As a** user
**I want** to create, read, update, and delete my personal watchlists via API
**So that** I can manage groups of channels for monitoring

## 2. Business Context & Value
Users need to manage their personal watchlists through a RESTful API. This enables the frontend to perform CRUD operations and provides a searchable list of watchlists for the monitoring page selector.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Create a watchlist
    *   Given an authenticated user
    *   When they POST to `/v1/user/watchlists` with a valid payload
    *   Then a new watchlist is created with the specified channels and defaults
    *   And the response contains the created watchlist details

*   [ ] **Scenario 2**: Create watchlist with duplicate name fails
    *   Given an authenticated user with an existing watchlist named "Production"
    *   When they POST to create another watchlist named "production" (case-insensitive)
    *   Then a 409 Conflict error is returned

*   [ ] **Scenario 3**: Search watchlists with JFrame
    *   Given an authenticated user with multiple watchlists
    *   When they POST to `/v1/user/watchlists/search` with filters/pagination
    *   Then they receive a paginated list of their watchlists matching the criteria

*   [ ] **Scenario 4**: Get single watchlist
    *   Given an authenticated user with a watchlist
    *   When they GET `/v1/user/watchlists/{id}`
    *   Then they receive the full watchlist details including channels

*   [ ] **Scenario 5**: Update watchlist
    *   Given an authenticated user with a watchlist
    *   When they PUT to `/v1/user/watchlists/{id}` with updated data
    *   Then the watchlist is updated (name, description, channels, defaults)

*   [ ] **Scenario 6**: Update channel order
    *   Given a watchlist with channels [A, B, C]
    *   When they PUT with channels in order [C, A, B]
    *   Then the channel positions are updated to reflect the new order

*   [ ] **Scenario 7**: Delete watchlist
    *   Given an authenticated user with a watchlist
    *   When they DELETE `/v1/user/watchlists/{id}`
    *   Then the watchlist is permanently deleted

*   [ ] **Scenario 8**: Access another user's watchlist fails
    *   Given user A has a watchlist
    *   When user B tries to GET/PUT/DELETE that watchlist
    *   Then a 404 Not Found is returned (don't leak existence)

*   [ ] **Scenario 9**: Add non-existent channel fails
    *   Given an authenticated user creating a watchlist
    *   When they include a channel ID that doesn't exist or isn't theirs
    *   Then a 400 Bad Request is returned with details

*   [ ] **Scenario 10**: Add organization channel to personal watchlist fails
    *   Given a user with access to an organization channel
    *   When they try to add that org channel to a personal watchlist
    *   Then a 400 Bad Request is returned (strict separation)

## 4. Technical Requirements

### API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/user/watchlists` | Create new watchlist |
| POST | `/v1/user/watchlists/search` | Search watchlists (JFrame) |
| GET | `/v1/user/watchlists/{id}` | Get watchlist by ID |
| PUT | `/v1/user/watchlists/{id}` | Update watchlist |
| DELETE | `/v1/user/watchlists/{id}` | Delete watchlist |

### Request/Response Models

**CreateWatchlistRequest**
```java
public record CreateWatchlistRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    @NotNull List<Long> channelIds,  // Ordered list
    @NotBlank String defaultTimeRange,  // "24h", "7d", "30d"
    @NotNull Boolean defaultOnlyCritical,
    @NotNull Boolean defaultSortBySeverity
) {}
```

**UpdateWatchlistRequest**
```java
public record UpdateWatchlistRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    @NotNull List<Long> channelIds,  // Ordered list - full replacement
    @NotBlank String defaultTimeRange,
    @NotNull Boolean defaultOnlyCritical,
    @NotNull Boolean defaultSortBySeverity
) {}
```

**WatchlistDetailsResponse**
```java
public record WatchlistDetailsResponse(
    Long id,
    String name,
    String description,
    List<WatchlistChannelResponse> channels,
    String defaultTimeRange,
    Boolean defaultOnlyCritical,
    Boolean defaultSortBySeverity,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
```

**WatchlistChannelResponse**
```java
public record WatchlistChannelResponse(
    Long id,
    String name,
    String status,
    Integer position
) {}
```

**WatchlistSearchResponse** (for list view)
```java
public record WatchlistSearchResponse(
    Long id,
    String name,
    String description,
    Integer channelCount,
    OffsetDateTime createdAt
) {}
```

### Path Constants
Add to `Paths.java`:
```java
public static final String WATCHLISTS_PART = "/watchlists";

public static final String USER_WATCHLISTS_PATH = USERS_PATH + WATCHLISTS_PART;
public static final String USER_WATCHLISTS_SEARCH_PATH = USER_WATCHLISTS_PATH + SEARCH_PART;
public static final String USER_WATCHLIST_PATH = USER_WATCHLISTS_PATH + ID_PART;
```

### Database
- Uses entities from WATCHLIST-01

### Security
- All endpoints require authentication
- Users can only access their own personal watchlists
- Channel validation ensures users can only add their own personal channels

### Performance
- Search uses JFrame for efficient pagination
- Channel count can be computed via COUNT subquery for list view

## 5. Design & UI/UX
- N/A (API only)

## 6. Implementation Notes / Research

### File Locations
- Controller: `server/src/main/java/io/github/eventify/api/watchlist/controller/UserWatchlistController.java`
- Service: `server/src/main/java/io/github/eventify/api/watchlist/service/WatchlistService.java`
- Repository: `server/src/main/java/io/github/eventify/api/watchlist/repository/WatchlistRepository.java`
- Mapper: `server/src/main/java/io/github/eventify/api/watchlist/model/mapper/WatchlistMapper.java`
- Validator: `server/src/main/java/io/github/eventify/api/watchlist/model/validator/WatchlistValidator.java`
- Requests: `server/src/main/java/io/github/eventify/api/watchlist/model/request/`
- Responses: `server/src/main/java/io/github/eventify/api/watchlist/model/response/`

### Patterns to Follow
- Follow `UserChannelController` for controller structure
- Follow `ChannelService` for service patterns
- Use `ChannelMapper` as reference for MapStruct mapping
- Use `ChannelValidator` as reference for validation

### Channel Update Strategy
When updating channels, use **full replacement**:
1. Remove all existing `WatchlistChannel` entries
2. Create new entries with correct positions based on `channelIds` array order

This simplifies the API (no PATCH operations) and matches the drag-drop UI behavior.

### Validation Rules
- `name`: Required, max 100 chars, unique per user (case-insensitive)
- `description`: Optional, max 500 chars
- `channelIds`: Can be empty (watchlist with no channels), no duplicates
- `defaultTimeRange`: Must be one of: "24h", "7d", "30d"
- All channels must belong to the authenticated user (personal channels only)

### Test Cases
- Create watchlist with/without channels
- Create with invalid channel ID (non-existent)
- Create with another user's channel
- Create with organization channel (should fail)
- Search with various filters
- Update name, description, channels, defaults
- Reorder channels
- Delete watchlist
- Access control tests (user isolation)
