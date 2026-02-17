# Send Events Help Popover

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-02-17
**Depends On**: CHANNEL-03-event-routing-and-creation-via-slug.md

## 1. User Story
**As a** developer using Eventify
**I want** a help button on the channels page showing how to send events
**So that** I can quickly see the cURL commands for single and batch event ingestion without leaving the page

## 2. Business Context & Value
After creating a channel, developers need to know how to send events to it. Currently they must:
1. Go to API key settings to get their key
2. Figure out the API endpoint
3. Construct the correct JSON payload

A help popover provides:
- **Immediate guidance**: See example code right where they need it
- **Reduced friction**: Copy-paste ready cURL commands
- **Better onboarding**: New users understand the API without reading docs
- **Complete picture**: Both single event and batch history import documented in one place

## 3. Acceptance Criteria

### Help Button Visibility
* [ ] **Scenario 1**: Help button visible on user channels page
    * Given the user channels page (`/channels`)
    * When viewing the page header
    * Then there is a help button next to the "New Channel" button
    * And the button has appropriate tooltip/label indicating its purpose

* [ ] **Scenario 2**: Help button visible on org channels page
    * Given the org channels page (`/organizations/{orgId}/channels`)
    * When viewing the page header
    * Then the same help button is available

### Popover Content - Overview
* [ ] **Scenario 3**: Popover has two sections
    * Given the help popover is open
    * When viewing the content
    * Then there are two clearly separated sections:
      1. **Single Event** - for sending individual events
      2. **Batch Insert** - for importing historical data

### Single Event Section
* [ ] **Scenario 4**: Single event cURL example
    * Given the "Single Event" section
    * When viewing the content
    * Then it shows:
      - Brief description of use case (real-time event ingestion)
      - Endpoint: `POST /v1/external/event`
      - cURL example with correct headers (`Authorization: Bearer`)
      - Request body showing: `channelId`, `severity`, `title`, `message`, `metadata`
      - Copy button for the cURL command

* [ ] **Scenario 5**: Copy single event cURL
    * Given the single event cURL example
    * When I click the copy button
    * Then the command is copied to clipboard
    * And a toast shows "Copied to clipboard"

### Batch Insert Section
* [ ] **Scenario 6**: Batch insert cURL example
    * Given the "Batch Insert" section
    * When viewing the content
    * Then it shows:
      - Brief description of use case (importing historical data, all-or-nothing semantics)
      - Endpoint: `POST /v1/external/event/batch`
      - cURL example with request body showing `events` array with multiple entries
      - Note that `timestamp` field can be provided for historical events
      - Copy button for the batch cURL command

* [ ] **Scenario 7**: Copy batch cURL
    * Given the batch cURL example
    * When I click the copy button
    * Then the command is copied to clipboard
    * And a toast shows "Copied to clipboard"

### API Key Link
* [ ] **Scenario 8**: Context-aware API key link (user channels)
    * Given the help popover on `/channels` (user channels page)
    * When viewing the API key reference
    * Then there is a link to `/developer` (Developer settings)

* [ ] **Scenario 9**: Context-aware API key link (org channels)
    * Given the help popover on `/organizations/{orgId}/channels`
    * When viewing the API key reference
    * Then there is a link to `/organizations/{orgId}/settings/api-keys`

## 4. Technical Requirements

### API Endpoints (from OpenAPI spec)
| Endpoint | Purpose | Request Body |
|----------|---------|--------------|
| `POST /v1/external/event` | Single event ingestion | `CreateEventRequest` |
| `POST /v1/external/event/batch` | Batch event import | `BatchEventRequest` (contains `events` array) |

### Request Body Schema (CreateEventRequest)
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `channelId` | number (int64) | Yes | Target channel ID |
| `severity` | enum | Yes | `CRITICAL`, `WARNING`, `OK`, `NO_DATA` |
| `title` | string | Yes | Event title |
| `message` | string | Yes | Event message |
| `metadata` | object | No | Free-form key/value pairs |
| `timestamp` | ISO datetime | No | Event timestamp (defaults to now) |

### Existing Utils
| File | Function | Notes |
|------|----------|-------|
| `$lib/utils/channel.ts` | `generateCurlCommand(channelId)` | Generates single event cURL - use or extend |
| `$lib/utils/channel.ts` | `copyCurlToClipboard(channelId)` | Copies cURL with toast feedback |

### New Component
| File | Purpose |
|------|---------|
| `$lib/components/channels/SendEventsHelpPopover.svelte` | Reusable popover, accepts context prop for API key link |

### Modified Files
| File | Change |
|------|--------|
| `/channels/+page.svelte` | Add help button in header |
| `/organizations/[orgId]/channels/+page.svelte` | Add help button in header |
| `$lib/utils/channel.ts` | Add `generateBatchCurlCommand()` function (optional - could be inline) |

### Component Props
The popover component needs to know the context to generate the correct API key link:
- `apiKeySettingsUrl`: string - the URL to navigate to for API key management

## 5. Design & UI/UX
- Follow project styling guide (`.opencode/STYLING-GUIDE.md`)
- Use existing Popover component from shadcn-svelte
- Two sections should be visually distinct (consider tabs, accordion, or stacked sections)
- Code blocks should be easily readable with copy functionality
- Frontend agent determines best visual approach for help button placement and popover layout

## 6. Implementation Notes

### cURL Generation
- Use `SERVER_BASE_URL` from `$lib/config/constants` for environment-aware URLs
- Leverage existing `generateCurlCommand` function or follow its pattern
- Use OpenAPI types (`CreateEventRequest`) for type-safe example bodies

### Context-Aware Linking
- User channels page: link to `/developer`
- Org channels page: extract `orgId` from route params, link to `/organizations/{orgId}/settings/api-keys`

### Reference Components
- Look at existing popovers in the codebase for styling patterns

## 7. Out of Scope
- Full API documentation page (separate epic)
- Code examples in multiple languages (Python, Node, etc.)
- SDK documentation
- Interactive "try it" functionality
- Channel selector within the popover

## 8. Test Considerations
- Screenshot tests for popover open state (both sections visible)
- Verify copy functionality works for both single and batch cURL
- Verify correct API key link based on page context
