# Channel Creation via API

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-02-11
**Depends On**: None

## 1. User Story
**As a** developer integrating with Eventify
**I want** to create channels programmatically via API using my API key
**So that** I can automate channel setup in my deployment scripts, CI/CD pipelines, and test suites without logging into the UI

## 2. Business Context & Value
Currently, channels can only be created via the UI (JWT-authenticated endpoints). This forces developers to:
1. Log into the web UI
2. Manually create channels
3. Copy channel IDs into their code/config

This is friction for:
- **CI/CD pipelines** that need to provision channels as part of deployment
- **Test scripts** that want to create dedicated test channels
- **Infrastructure-as-code** setups where everything should be automated
- **Multi-environment deployments** (dev, staging, prod) each needing their own channels

With API-based channel creation, developers can fully automate their Eventify setup using just their API key.

## 3. Acceptance Criteria

### Backend
* [ ] **Scenario 1**: Create personal channel with personal API key
    * Given a user with a personal API key
    * When they call `POST /api/v1/channels` with `{"name": "my-errors", "description": "Error logs"}`
    * Then a personal channel is created owned by the user
    * And the response returns the created channel with ID

* [ ] **Scenario 2**: Create organization channel with org API key
    * Given a user with an organization API key
    * When they call `POST /api/v1/channels` with `{"name": "prod-errors"}`
    * Then an organization channel is created owned by the organization
    * And the response returns the created channel with ID

* [ ] **Scenario 3**: Name is required
    * Given a valid API key
    * When they call `POST /api/v1/channels` with `{}` (no name)
    * Then they receive 400 Bad Request with validation error

* [ ] **Scenario 4**: Duplicate name rejected
    * Given a user with a personal channel named "my-errors"
    * When they try to create another channel with `{"name": "my-errors"}`
    * Then they receive 400 Bad Request with "Channel name already exists" error

* [ ] **Scenario 5**: Invalid API key rejected
    * Given an invalid or expired API key
    * When they attempt to create a channel
    * Then they receive 401 Unauthorized

* [ ] **Scenario 6**: Description is optional
    * Given a valid API key
    * When they call `POST /api/v1/channels` with `{"name": "minimal-channel"}`
    * Then the channel is created with null/empty description

## 4. Technical Requirements

### API Changes
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/channels` | API Key | Create a channel |

**Request Body**:
```json
{
  "name": "my-channel",        // Required, 3-100 chars
  "description": "Optional"    // Optional
}
```

**Response**: `201 Created` with `ChannelDetailsResponse`

**Authentication**: API Key (via `X-API-Key` header) - same as event ingestion

### Backend Implementation

**New Files**:
- `ApiChannelController.java` - New controller for API key authenticated channel operations

**Modified Files**:
| File | Change |
|------|--------|
| `Paths.java` | Add `API_CHANNELS_PATH = "/api/v1/channels"` |
| `ChannelService.java` | Add `createChannelFromApiKey(CreateChannelRequest, ApiKeyPrincipal)` method |
| `SecurityConfig.java` | Ensure `/api/v1/channels` uses API key filter (should already be covered by existing config) |

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

### Service Logic
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

### Database
No schema changes required.

### Security
- Uses existing `ApiKeyAuthenticationFilter`
- No additional authorization needed (API key ownership determines channel ownership)

## 5. Design & UI/UX
N/A - API-only feature, no UI changes.

## 6. Implementation Notes / Research

### Existing Patterns to Follow
- **API Key auth pattern**: See `EventIngestionController.java` - uses `@AuthenticationPrincipal ApiKeyPrincipal`
- **Channel creation**: See `ChannelService.createUserChannel()` and `OrganizationChannelService.createOrganizationChannel()`
- **Validation**: Reuse existing `ChannelValidator`
- **Duplicate name handling**: Already handled by existing service methods (throws exception -> 400)

### Reuse Existing Code
The existing `CreateChannelRequest` DTO can be reused as-is:
```java
public class CreateChannelRequest {
    private String name;        // Required
    private String description; // Optional
}
```

### Test Files to Create
- `ApiChannelControllerTest.java` - Integration tests for new endpoint

### Example Usage (for documentation)
```bash
# Create personal channel
curl -X POST https://api.eventify.io/api/v1/channels \
  -H "X-API-Key: evf_personal_xxxxx" \
  -H "Content-Type: application/json" \
  -d '{"name": "my-app-errors", "description": "Production error logs"}'

# Create organization channel
curl -X POST https://api.eventify.io/api/v1/channels \
  -H "X-API-Key: evf_org_xxxxx" \
  -H "Content-Type: application/json" \
  -d '{"name": "backend-errors"}'
```

### Future Considerations (not in scope)
- `GET /api/v1/channels` - List channels via API key
- `GET /api/v1/channels/{id}` - Get channel details via API key
- `DELETE /api/v1/channels/{id}` - Delete channel via API key
- Auto-create channel on event ingestion (with `createIfNotExists` flag)
