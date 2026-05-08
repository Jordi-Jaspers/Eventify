# Batch Event Ingestion API - Completed

**Date**: 2026-01-20
**Epic**: Event Ingestion
**Status**: Completed

## Plan (Approved)

Implement `POST /v1/events/batch` endpoint for batch event ingestion with:
- Client-provided timestamps (required for batch, unlike real-time)
- All-or-nothing semantics via `@Transactional`
- Multi-channel support in single batch
- Unified validation approach

## Actual Changes

### Backend

**DTOs (Simplified/Unified)**:
- Extended `CreateEventRequest` with optional `timestamp` field for reuse in batch
- `BatchEventRequest` uses `List<CreateEventRequest>` instead of separate DTO
- Batch endpoint returns `List<EventCreatedResponse>` (reuses existing response DTO)

**Unified Validator**:
- Single `EventValidator` handles both single and batch validation
- Batch validation: timestamp required, not in future, max 100 events
- Single validation: timestamp optional (server generates)
- Validator only validates data - no repository dependencies, no security checks

**Security**:
- `@PreAuthorize("@channelSecurityService.canAccessBatch()")` for batch endpoint
- `ChannelSecurityService.canAccessBatch()` validates API key access to ALL channels before insert
- Personal API keys → personal channels only
- Organization API keys → org channels only

**Event Entity**:
- Two constructors for explicit timestamp control:
  - `Event(request, channel)` - always uses server timestamp (single events)
  - `Event(request, channel, timestamp)` - uses explicit timestamp (batch events)
- Prevents accidental use of client timestamp for single events

**Controller/Service**:
- `POST /v1/events/batch` endpoint in `EventIngestionController`
- `EventIngestionService.ingestBatch()` with `@Transactional`
- Service uses 3-parameter Event constructor for batch to use client timestamps

### Testing
- 21 unit tests for `EventValidator` (unified)
- 16 integration tests for `BatchEventIngestionControllerTest`
- All existing tests still pass (928 total)

### Acceptance Criteria Status
- ✅ Scenario 1: Successfully ingest batch of events
- ✅ Scenario 2: Reject entire batch if any event has future timestamp
- ⏭️ Scenario 3: Retention period validation - deferred (simplified scope)
- ✅ Scenario 4: Reject batch if any channel is inaccessible
- ✅ Scenario 5: Reject batch exceeding max size (100 events)
- ✅ Scenario 6: Require timestamp for all events
- ✅ Scenario 7: Handle empty batch
- ✅ Scenario 8: Events across multiple channels (same owner)

## Files Modified

### Phase 1: Batch Event Ingestion API

**New Files**:
- `BatchEventRequest.java`
- `EventValidator.java`
- `EventValidatorTest.java`
- `BatchEventIngestionControllerTest.java`

**Modified Files**:
- `CreateEventRequest.java` - added optional `timestamp` field
- `Event.java` - two constructors for timestamp handling
- `EventIngestionController.java` - added batch endpoint
- `EventIngestionService.java` - added `ingestBatch()` method
- `ChannelSecurityService.java` - added `canAccessBatch()` method
- `ChannelRepository.java` - removed unused method

**Deleted Files**:
- `CreateEventValidator.java` (unified)
- `BatchEventValidator.java` (unified)
- `CreateEventValidatorTest.java` (unified)

### Phase 2: Request-Scoped Caching

**New Files**:
- `common/cache/RequestScopedCache.java` - Generic request-scoped cache base class
- `common/cache/RequestScopedCacheTest.java` - 20 unit tests

**Modified Files**:
- `ChannelCache.java` - Extends `RequestScopedCache<Long, Channel>`
- `ChannelSecurityService.java` - Populates cache, refactored for quality
- `ChannelSecurityServiceTest.java` - Added cache mock
- `EventIngestionService.java` - Uses cache with `getOrLoad()`/`getAllOrLoad()`
- `EventIngestionServiceTest.java` - Added cache mock with delegation
- `EventValidator.java` - Refactored for quality (reduced return count)

## Quality Metrics
- ✅ Tests: 948 total passing (20 new for RequestScopedCache)
- ✅ Checkstyle: 0 violations
- ✅ PMD: 0 violations
- ✅ Build: Successful
- ✅ All-or-nothing semantics verified
- ✅ Client timestamps properly used in batch

## Design Decisions

1. **Unified CreateEventRequest**: Single DTO with optional timestamp field, reused for both endpoints
2. **Validator without repositories**: Pure data validation, security handled via `@PreAuthorize`
3. **Explicit constructor for timestamps**: Two constructors in Event entity prevent accidental misuse
4. **No retention period validation**: Simplified scope - can add later as enhancement

---

## Phase 2: Request-Scoped Caching Optimization

**Date**: 2026-01-21

### Problem
Duplicate database queries within the same HTTP request:
- Security layer (`ChannelSecurityService`) fetches channels to validate access
- Service layer (`EventIngestionService`) fetches same channels again for the insert

### Solution
Request-scoped caching infrastructure to share fetched entities within a request.

### Backend Changes

**New Files**:
- `common/cache/RequestScopedCache.java` - Generic abstract base class for request-scoped caching
- `common/cache/RequestScopedCacheTest.java` - 20 unit tests

**Modified Files**:
- `ChannelCache.java` - Now extends `RequestScopedCache<Long, Channel>` (simplified to 6 lines)
- `ChannelSecurityService.java` - Populates cache after security validation; refactored for code quality (max 2 returns per method)
- `EventIngestionService.java` - Uses `channelCache.getOrLoad()` and `channelCache.getAllOrLoad()` with repository fallback
- `ChannelSecurityServiceTest.java` - Added `ChannelCache` mock
- `EventIngestionServiceTest.java` - Added `ChannelCache` mock with delegation
- `EventValidator.java` - Refactored for code quality (max returns per method)

### Cache API

```java
// Single entity - get from cache or load
channelCache.getOrLoad(id, channelRepository::findById)

// Batch - get cached + batch load missing
channelCache.getAllOrLoad(ids, channelRepository::findAllById)
```

### Code Quality Fixes

**RequestScopedCache.java**:
- Added `final` to all loop variables
- Extracted helper methods to reduce cognitive complexity (PMD)
  - `partitionCachedAndMissing()` - separates cached vs missing IDs
  - `loadAndCacheMissing()` - loads and caches missing entities
  - `cacheAndAddToResult()` - caches single entity

**ChannelSecurityService.java**:
- Refactored `canAccess()` from 3 returns → 2 returns using `Optional` chaining
- Refactored `canAccessBatch()` from 5 returns → 2 returns by extracting:
  - `isEmptyBatch()` - checks for empty/null batch
  - `hasAccessToAllChannels()` - validates access to all channels
- Fixed PMD boolean simplification

**EventValidator.java**:
- Refactored `validate(BatchEventRequest)` from 3 returns → 1 return
- Extracted `getBasicBatchValidationError()` helper

**RequestScopedCacheTest.java**:
- Refactored to follow project testing standards:
  - Package-private class and methods
  - Descriptive Given-When-Then comments
  - Factory methods with `a` prefix: `aTestEntity()`, `aTestEntityWithNullId()`, `aTestEntityList()`
  - Removed section comment headers

### Performance Impact
- Single event: 1 fewer DB query per request
- Batch event (N channels): Up to N fewer DB queries per request
