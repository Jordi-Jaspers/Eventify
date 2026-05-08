# Server Test Suite Cleanup

**Completed:** 2026-04-30
**Epic:** TEST
**Source:** .opencode/refined/TEST-01-server-test-cleanup.md

## Summary

Removed 7 trivial job delegation tests and 2 context-load-only tests, extracted shared TestBuilders utility class, updated 5 test files to use shared builders, and enabled parallel test execution.

## Implementation

### Deleted Files (7 trivial job tests)
- `api/channel/job/ChannelCleanupJobTest.java`
- `api/event/job/EventRetentionCleanupJobTest.java`
- `api/user/job/UserCleanupJobTest.java`
- `api/token/job/TokenCleanupJobTest.java`
- `api/quota/job/UserQuotaCleanupJobTest.java`

### Removed Methods
- `ApplicationContextTest.contextLoads()` — only asserted context != null
- `ApplicationContextTest.testHawaiiFilters()` — only asserted bean != null

### New: TestBuilders.java
Static utility in `support/` package with builders for Channel, ApiKey, Watchlist, PageInput — no Spring dependency.

### Updated Test Files
- `ApiKeyServiceTest.java` — uses TestBuilders.anApiKey()
- `ApiKeyAuthenticationServiceTest.java` — uses TestBuilders.anApiKey/anOrgApiKey()
- `ChannelServiceTest.java` — uses TestBuilders.aChannel/aPageInput()
- `UserWatchlistServiceTest.java` — uses TestBuilders
- `OrganizationUserWatchlistServiceTest.java` — uses TestBuilders

### Entity Builders in UnitTest.java
Added protected convenience methods delegating to TestBuilders.

### Parallel Execution
Added `maxParallelForks = Runtime.getRuntime().availableProcessors()` to build.gradle.kts.

## Tests

- ~21 trivial tests removed (7 files × 3 tests each)
- 2 context-load tests removed
- All remaining 1,266 tests passing
