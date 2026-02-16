# Event Routing & Channel Creation via Slug

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-02-16
**Depends On**: CHANNEL-02-slug-identifier-system.md

## 1. User Story
**As a** developer integrating with Eventify
**I want** to send events using channel slugs and create channels via API
**So that** I can fully automate my Eventify integration without using numeric IDs or the UI

## 2. Business Context & Value
This story completes the developer experience by:
1. **Event routing by slug**: Send events to `myapp.prod.errors` instead of channel ID `123`
2. **Channel creation via API**: Provision channels in CI/CD pipelines
3. **Self-service automation**: Full infrastructure-as-code support

Combined with CHANNEL-02, developers never need to know or use numeric IDs.

## 3. Acceptance Criteria

### Event Routing
* [ ] **Scenario 1**: Send event by slug
    * Given a channel with slug `myapp.errors` owned by my API key
    * When I send `POST /api/v1/events` with `{"channelSlug": "myapp.errors", "message": "test"}`
    * Then the event is created in that channel

* [ ] **Scenario 2**: Slug not found
    * Given no channel with slug `nonexistent`
    * When I send an event with `{"channelSlug": "nonexistent", ...}`
    * Then I receive 404 Not Found with error "Channel not found"

* [ ] **Scenario 3**: Slug belongs to different owner
    * Given user B has a channel with slug `their.errors`
    * When I (user A) send an event with `{"channelSlug": "their.errors", ...}`
    * Then I receive 404 Not Found (not 403, to prevent enumeration)

* [ ] **Scenario 4**: Batch events by slug
    * Given multiple events in a batch request
    * When each event specifies `channelSlug`
    * Then events are routed to correct channels

* [ ] **Scenario 5**: Mixed slug and ID in batch rejected
    * Given a batch where some events use `channelSlug` and others use `channelId`
    * When I send the batch
    * Then I receive 400 Bad Request with message "Cannot mix channelSlug and channelId in batch"

* [ ] **Scenario 6**: Neither slug nor ID provided
    * Given an event without `channelSlug` or `channelId`
    * When I send the event
    * Then I receive 400 Bad Request with message "Either channelSlug or channelId is required"

### Channel Creation via API
* [ ] **Scenario 7**: Create personal channel with personal API key
    * Given a personal API key
    * When I call `POST /api/v1/channels` with `{"name": "My Errors", "slug": "my.errors"}`
    * Then a personal channel is created
    * And the response includes the slug

* [ ] **Scenario 8**: Create org channel with org API key
    * Given an organization API key
    * When I call `POST /api/v1/channels` with `{"name": "Backend Errors", "slug": "backend.errors"}`
    * Then an org channel is created under that organization

* [ ] **Scenario 9**: Duplicate slug rejected
    * Given I already have a channel with slug `errors`
    * When I try to create another with the same slug
    * Then I receive 400 Bad Request with message "Channel with this slug already exists"

* [ ] **Scenario 10**: Invalid slug format rejected
    * Given a slug with invalid characters like `My-Errors`
    * When I try to create a channel
    * Then I receive 400 Bad Request with validation error

* [ ] **Scenario 11**: Invalid API key
    * Given an invalid API key
    * When I attempt any operation
    * Then I receive 401 Unauthorized

* [ ] **Scenario 12**: Description is optional
    * Given a valid API key
    * When I call `POST /api/v1/channels` with `{"name": "Minimal", "slug": "minimal"}`
    * Then the channel is created with null/empty description

## 4. Technical Requirements

### API Changes

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/events` | API Key | Modified: accept `channelSlug` field |
| `POST` | `/api/v1/channels` | API Key | New: create channel via API |

### Event Request (Updated)
```json
{
  "channelSlug": "myapp.prod.errors",  // Use this OR channelId
  "severity": "ERROR",
  "message": "Connection failed",
  "payload": {}
}
```

Validation: Exactly one of `channelSlug` or `channelId` must be provided.

### Channel Creation Request
```json
{
  "name": "My Application Errors",  // Required, display name
  "slug": "myapp.errors",           // Required, unique identifier
  "description": "Production error logs"  // Optional
}
```

### Channel Creation Response
`201 Created` with `ChannelDetailsResponse`

### New Files
| File | Purpose |
|------|---------|
| `ApiChannelController.java` | Channel CRUD via API key auth |

### Modified Files
| File | Change |
|------|--------|
| `CreateEventRequest.java` | Add optional `channelSlug` field |
| `EventIngestionService.java` | Resolve channel by slug if provided |
| `ChannelRepository.java` | Add `findBySlugAndUserId()`, `findBySlugAndOrganizationId()` |
| `ChannelSecurityService.java` | Add slug-based access check |
| `Paths.java` | Add `API_CHANNELS_PATH = "/api/v1/channels"` |

## 5. Design & UI/UX
N/A - API-only feature

## 6. Implementation Notes / Research

### Controller Pattern
```java
@RestController
@RequiredArgsConstructor
@Tag(name = "Channel API", description = "Programmatic channel management via API key")
public class ApiChannelController {

    private final ChannelService channelService;
    private final ChannelMapper channelMapper;
    private final ChannelValidator channelValidator;

    @PostMapping(path = API_CHANNELS_PATH, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    @Operation(summary = "Create channel", description = "Creates a new channel. Personal API key creates personal channel, org API key creates org channel.")
    public ResponseEntity<ChannelDetailsResponse> createChannel(
            @RequestBody final CreateChannelRequest request,
            @AuthenticationPrincipal final ApiKeyPrincipal principal) {
        channelValidator.validateAndThrow(request);
        final Channel channel = channelService.createChannelFromApiKey(request, principal);
        return ResponseEntity.status(CREATED).body(channelMapper.toResourceObject(channel));
    }
}
```

### Channel Resolution Logic
```java
public Channel resolveChannel(CreateEventRequest request, ApiKeyPrincipal principal) {
    if (request.getChannelSlug() != null) {
        return resolveBySlug(request.getChannelSlug(), principal);
    } else {
        return resolveById(request.getChannelId(), principal);
    }
}

private Channel resolveBySlug(String slug, ApiKeyPrincipal principal) {
    if (principal.getOrganizationId() != null) {
        return channelRepository.findBySlugAndOrganizationId(slug, principal.getOrganizationId())
            .orElseThrow(() -> new NotFoundException("Channel not found"));
    } else {
        return channelRepository.findBySlugAndUserId(slug, principal.getUserId())
            .orElseThrow(() -> new NotFoundException("Channel not found"));
    }
}
```

### Service Logic for API Key Creation
```java
public Channel createChannelFromApiKey(CreateChannelRequest request, ApiKeyPrincipal principal) {
    if (principal.getOrganizationId() != null) {
        // Org API key -> create org channel
        return createOrganizationChannel(principal.getOrganizationId(), request);
    } else {
        // Personal API key -> create personal channel
        return createUserChannel(request);  // Uses principal's user
    }
}
```

### Security Considerations
- Return 404 (not 403) when channel exists but belongs to another owner (prevent enumeration)
- Validate slug format before database lookup
- Reuse existing `ApiKeyAuthenticationFilter`

### Example Usage
```bash
# Create channel
curl -X POST https://api.eventify.io/api/v1/channels \
  -H "X-API-Key: evf_xxxxx" \
  -H "Content-Type: application/json" \
  -d '{"name": "Production Errors", "slug": "prod.errors"}'

# Send event by slug
curl -X POST https://api.eventify.io/api/v1/events \
  -H "X-API-Key: evf_xxxxx" \
  -H "Content-Type: application/json" \
  -d '{"channelSlug": "prod.errors", "severity": "ERROR", "message": "DB connection lost"}'
```

### Test Files to Create
- `ApiChannelControllerTest.java` - Integration tests for channel creation endpoint
- Update `EventIngestionControllerTest.java` - Add tests for slug-based routing
