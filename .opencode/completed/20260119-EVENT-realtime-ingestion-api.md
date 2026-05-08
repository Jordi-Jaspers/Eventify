## [2026-01-19] - Real-time Event Ingestion API

### Plan (approved)
Implement `POST /v1/events` endpoint for real-time event ingestion from SDK/CLI clients using API key authentication.

**Requirements:**
- API key authentication via X-Api-Key header
- Validate channel access (API key must own the channel)
- Support severity levels: OK, WARNING, CRITICAL
- Server-assigned timestamps
- Channel status validation (reject paused channels)
- Metadata support (max 10KB JSON)

### Actual Changes

**Backend:**
- `EventIngestionController` - REST endpoint with `@PreAuthorize("@channelSecurity.canAccess(#request.channelId, principal)")`
- `EventIngestionService` - Creates events with server timestamp, fetches channel from repository
- `CreateEventRequest` - Request DTO with channelId, severity, title, message, metadata
- `CreateEventValidator` - JFrame validator for title length (255), message size (10KB), required fields
- `EventCreatedResponse` - Response with event ID and timestamp
- Fixed `ApiKeyAuthenticationFilter` to properly extract error message from `ApiException.getApiError().getReason()`

**Security:**
- Controller-level `@PreAuthorize` using existing `ChannelSecurityService`
- Channel ownership/scope validation (personal key → personal channel, org key → org channel)
- Expired API key returns proper error message

**Testing:**
- 16 integration tests for EventIngestionController (all scenarios: success, validation, auth, access control)
- 5 unit tests for EventIngestionService
- 10 unit tests for ChannelSecurityService (existing)
- 14 unit tests for ApiKeyAuthenticationFilter (existing + fixed)

### Agents Used
- Orchestrator: Test fixes, filter improvements, assertion corrections

### Files Modified

**New Files:**
- `server/src/main/java/io/github/eventify/api/event/controller/EventIngestionController.java`
- `server/src/main/java/io/github/eventify/api/event/service/EventIngestionService.java`
- `server/src/main/java/io/github/eventify/api/event/model/request/CreateEventRequest.java`
- `server/src/main/java/io/github/eventify/api/event/model/response/EventCreatedResponse.java`
- `server/src/main/java/io/github/eventify/api/event/model/validator/CreateEventValidator.java`
- `server/src/test/java/io/github/eventify/api/event/controller/EventIngestionControllerTest.java`
- `server/src/test/java/io/github/eventify/api/event/service/EventIngestionServiceTest.java`
- `server/src/test/java/io/github/eventify/api/event/model/validator/CreateEventValidatorTest.java`

**Modified Files:**
- `server/src/main/java/io/github/eventify/common/security/filter/ApiKeyAuthenticationFilter.java` - Added `extractErrorMessage()` to properly get reason from ApiException
- `server/src/main/java/io/github/eventify/common/config/WebSecurityConfig.java` - Added filter to security chain

### Quality Metrics
- Tests: 911 total, 0 failures
- Build: Successful
- Coverage: Full coverage for new code
