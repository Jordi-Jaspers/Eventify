# Watchlist CRUD API (User)

**Completed:** 2026-01-23
**Updated:** 2026-01-24 (JSONB refactor)
**Vibe Kanban Task:** WATCHLIST - 02 CRUD API User
**Task ID:** d7563415-fdf2-4f80-909b-ee75c9054bb5

## Summary
Implemented personal watchlist CRUD API with 5 endpoints for creating, searching, getting, updating, and deleting user watchlists. Includes comprehensive validation, ownership checking, and duplicate name prevention.

**JSONB Refactor (2026-01-24):** Refactored from flat columns + join table to JSONB columns for configuration and filters. Simplified controller/service interface with MapStruct handling request-to-entity mapping.

## Agents Used
| Agent | Task |
|-------|------|
| backend-agent | Implemented service, controller, repositories, DTOs, mapper, validator |
| testing-agent | Created validator, service, and controller tests |

## API Endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/user/watchlists` | Create new watchlist |
| POST | `/v1/user/watchlists/search` | Search watchlists (JFrame) |
| GET | `/v1/user/watchlists/{id}` | Get watchlist by ID |
| PUT | `/v1/user/watchlists/{id}` | Update watchlist |
| DELETE | `/v1/user/watchlists/{id}` | Delete watchlist |

## Architecture (Post-Refactor)

### JSONB Structure
- **WatchlistConfiguration**: `channelIds` (List<Long>) - stored as JSONB, never queried individually
- **WatchlistFilters**: `onlyCritical`, `sortBySeverity`, `timeRange` - stored as JSONB with defaults

### Controller Pattern (Clean, Single Mapper Call)
```java
public ResponseEntity<WatchlistDetailsResponse> createWatchlist(@RequestBody final CreateWatchlistRequest request) {
    watchlistValidator.validateAndThrow(request);
    final Watchlist watchlist = watchlistService.createWatchlist(watchlistMapper.toWatchlist(request));
    return ResponseEntity.status(CREATED).body(watchlistMapper.toResourceObject(watchlist));
}
```

### Service Pattern (Accepts Watchlist, Applies Defaults)
```java
public Watchlist createWatchlist(final Watchlist watchlist) {
    // Set user, validate channels, apply defaults for null config/filters
    if (watchlist.getConfiguration() == null) {
        watchlist.setConfiguration(WatchlistConfiguration.empty());
    }
    if (watchlist.getFilters() == null) {
        watchlist.setFilters(WatchlistFilters.defaults());
    }
    return watchlistRepository.save(watchlist);
}
```

### Channel Cleanup Integration
- When channels are deleted, `WatchlistRepository.removeChannelFromAllConfigurations(channelId)` is called
- Uses native JSONB query to remove channel ID from all watchlist configurations
- `ChannelCleanupService` updated to call this before hard-deleting channels

## Files Modified

### New Files (18)
- `server/src/main/java/io/github/eventify/api/watchlist/controller/UserWatchlistController.java`
- `server/src/main/java/io/github/eventify/api/watchlist/service/WatchlistService.java`
- `server/src/main/java/io/github/eventify/api/watchlist/repository/WatchlistRepository.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/mapper/WatchlistMapper.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/validator/WatchlistValidator.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistMetaData.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistConfiguration.java` (JSONB)
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistFilters.java` (JSONB)
- `server/src/main/java/io/github/eventify/api/watchlist/model/request/CreateWatchlistRequest.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/request/UpdateWatchlistRequest.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/request/WatchlistConfigurationRequest.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/request/WatchlistFiltersRequest.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/response/WatchlistDetailsResponse.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/response/WatchlistConfigurationResponse.java`
- `server/src/main/java/io/github/eventify/api/watchlist/model/response/WatchlistFiltersResponse.java`
- `server/src/main/java/io/github/eventify/common/exception/DuplicateWatchlistNameException.java`
- `server/src/test/java/io/github/eventify/api/watchlist/controller/UserWatchlistControllerTest.java`
- `server/src/test/java/io/github/eventify/api/watchlist/service/WatchlistServiceTest.java`
- `server/src/test/java/io/github/eventify/api/watchlist/model/validator/WatchlistValidatorTest.java`

### Modified Files (5)
- `server/src/main/java/io/github/eventify/api/Paths.java` - Added watchlist path constants
- `server/src/main/java/io/github/eventify/api/channel/repository/ChannelRepository.java` - Added findByIdAndUserId
- `server/src/main/java/io/github/eventify/api/channel/service/ChannelCleanupService.java` - Calls watchlist cleanup before channel deletion
- `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java` - Added DUPLICATE_WATCHLIST_NAME
- `server/src/test/java/io/github/eventify/api/channel/service/ChannelCleanupServiceTest.java` - Added WatchlistRepository mock
- `server/src/test/java/io/github/eventify/support/util/TestDataCleanupService.java` - Added watchlist cleanup

### Deleted Files (3)
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistChannel.java` - Replaced by JSONB
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistChannelId.java` - Replaced by JSONB
- `server/src/main/java/io/github/eventify/api/watchlist/repository/WatchlistChannelRepository.java` - Replaced by JSONB

## Tests
- 57 watchlist tests written, all passing
  - 28 validator tests
  - 14 service tests
  - 15 controller tests
- ChannelCleanupServiceTest updated (12 tests passing)

## Design Decisions
1. **JSONB for configuration/filters** - Never queried individually, only fetched with watchlist
2. **No channel groups** - Only `channelIds` list in configuration
3. **MapStruct for all mapping** - `CreateWatchlistRequest` -> `Watchlist` directly
4. **Service accepts `Watchlist`** - Not individual fields
5. **Primitive `boolean`** for filter flags (`onlyCritical`, `sortBySeverity`)
6. **`@Builder.Default`** for WatchlistFilters defaults

## Notes
- Followed TDD approach: tests written first, then implementation
- Used JFrame patterns for search/pagination/sorting
- Ownership validation ensures users can only access their own watchlists
- Duplicate watchlist name prevention within same user scope
- Channel validation ensures only user's own channels can be added to personal watchlists
- JSONB refactor eliminates join table complexity and simplifies queries
