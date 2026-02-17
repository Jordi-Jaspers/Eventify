# Send Events Help Popover

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-02-17
**Depends On**: CHANNEL-03-event-routing-and-creation-via-slug.md

## 1. User Story
**As a** developer using Eventify
**I want** a help button on the channels page showing how to send events
**So that** I can quickly see the cURL command and API usage without leaving the page

## 2. Business Context & Value
After creating a channel, developers need to know how to send events to it. Currently they must:
1. Go to API key settings to get their key
2. Figure out the API endpoint
3. Construct the correct JSON payload

A help popover provides:
- **Immediate guidance**: See example code right where they need it
- **Reduced friction**: Copy-paste ready cURL command
- **Better onboarding**: New users understand the API without reading docs

## 3. Acceptance Criteria

### User Channels Page
* [ ] **Scenario 1**: Help button visible in header
    * Given the user channels page (`/channels`)
    * When viewing the page header
    * Then there is a help button (HelpCircle icon) next to the "New Channel" button
    * And the button has a tooltip "How to send events"

* [ ] **Scenario 2**: Help popover content
    * Given the help button
    * When I click on it
    * Then a popover opens with:
      - Title: "Sending Events to Channels"
      - Brief explanation (2-3 sentences)
      - cURL example using `channelSlug` (not channelId)
      - Copy button for the cURL command
      - Note about getting API key from Developer settings
      - Link to Developer settings page

* [ ] **Scenario 3**: cURL example format
    * Given the help popover
    * When viewing the cURL example
    * Then it shows:
      ```bash
      curl -X POST https://api.eventify.io/v1/events \
        -H "X-API-Key: YOUR_API_KEY" \
        -H "Content-Type: application/json" \
        -d '{
          "channelSlug": "your.channel.slug",
          "severity": "OK",
          "title": "Event Title",
          "message": "Event message"
        }'
      ```

* [ ] **Scenario 4**: Copy button feedback
    * Given the cURL example in the popover
    * When I click the copy button
    * Then the command is copied to clipboard
    * And a toast shows "Copied to clipboard"

### Organization Channels Page
* [ ] **Scenario 5**: Same help available for org channels
    * Given the org channels page (`/organizations/{orgId}/channels`)
    * When viewing the page
    * Then the same help button and popover are available
    * And the copy functionality works identically

## 4. Technical Requirements

### New Component
| File | Purpose |
|------|---------|
| `SendEventsHelpPopover.svelte` | Reusable popover component for both pages |

### Modified Files
| File | Change |
|------|--------|
| `/channels/+page.svelte` | Add help button in header |
| `/organizations/[orgId]/channels/+page.svelte` | Add help button in header |
| `channel.ts` (utils) | Update `generateCurlCommand` to use slug instead of channelId |

### Component Location
`$lib/components/channels/SendEventsHelpPopover.svelte`

## 5. Design & UI/UX

### Header Layout
```
┌─────────────────────────────────────────────────────────────┐
│  My Channels                                                │
│  Manage your personal channels...                           │
│                                          [?] [+ New Channel]│
└─────────────────────────────────────────────────────────────┘
```

### Popover Design
```
┌────────────────────────────────────────────┐
│ 📤 Sending Events to Channels              │
├────────────────────────────────────────────┤
│ Use your API key to send events via the    │
│ REST API. Each event requires a channel    │
│ slug to route it correctly.                │
│                                            │
│ ┌────────────────────────────────────────┐ │
│ │ curl -X POST .../v1/events \           │ │
│ │   -H "X-API-Key: YOUR_API_KEY" \       │ │
│ │   -H "Content-Type: application/json"\ │ │
│ │   -d '{                                │ │
│ │     "channelSlug": "your.channel...",  │ │
│ │     "severity": "OK",                  │ │
│ │     "title": "Event Title",            │ │
│ │     "message": "Event message"         │ │
│ │   }'                                   │ │
│ └──────────────────────────────── [Copy] │ │
│                                            │
│ 🔑 Get your API key from Developer Settings│
│    → Go to Developer Settings              │
└────────────────────────────────────────────┘
```

### Styling
- Use existing Popover component from shadcn-svelte
- Code block with `font-mono`, `bg-muted/50`, rounded corners
- Copy button: ghost variant, small, positioned bottom-right of code block
- Link: text-primary with underline on hover
- Match existing glassmorphism aesthetic (see ConfigurePopover for reference)

## 6. Implementation Notes

### Component Structure
```svelte
<script lang="ts">
  import * as Popover from '$lib/components/ui/popover';
  import { Button } from '$lib/components/ui/button';
  import { HelpCircle, Copy, Key, ExternalLink } from '@lucide/svelte';
  import { toast } from 'svelte-sonner';
  import { SERVER_BASE_URL } from '$lib/config/constants';
  
  function getCurlExample(): string {
    return `curl -X POST ${SERVER_BASE_URL}/v1/events \\
  -H "X-API-Key: YOUR_API_KEY" \\
  -H "Content-Type: application/json" \\
  -d '{
    "channelSlug": "your.channel.slug",
    "severity": "OK",
    "title": "Event Title",
    "message": "Event message"
  }'`;
  }
  
  function copyToClipboard(): void {
    navigator.clipboard.writeText(getCurlExample())
      .then(() => toast.success('Copied to clipboard'))
      .catch(() => toast.error('Failed to copy'));
  }
</script>

<Popover.Root>
  <Popover.Trigger asChild let:builder>
    <Button builders={[builder]} variant="outline" size="icon" title="How to send events">
      <HelpCircle class="h-4 w-4" />
    </Button>
  </Popover.Trigger>
  <Popover.Content class="w-96" align="end">
    <!-- Content here -->
  </Popover.Content>
</Popover.Root>
```

### Update generateCurlCommand (channel.ts)
The existing function uses `channelId`. Update to use `channelSlug`:
```typescript
export function generateCurlCommand(channelSlug: string | undefined): string {
  const apiUrl = `${SERVER_BASE_URL}/v1/events`;
  const exampleBody = {
    channelSlug: channelSlug ?? 'your.channel.slug',
    severity: 'OK',
    title: 'Event Title',
    message: 'Event message here',
    metadata: {}
  };
  // ...
}
```

**Note**: Also update `copyCurlToClipboard` signature and any callers.

### Header Integration
Both channel pages follow the same pattern. Add the help button:
```svelte
<div class="flex items-center justify-between mb-8">
  <div>
    <h1>My Channels</h1>
    <p>...</p>
  </div>
  <div class="flex items-center gap-2">
    <SendEventsHelpPopover />
    <Button onclick={() => (showCreateSheet = true)}>
      <Plus class="mr-2 h-4 w-4" />
      New Channel
    </Button>
  </div>
</div>
```

## 7. Out of Scope
- Full API documentation page (separate epic)
- Code examples in multiple languages (Python, Node, etc.)
- SDK documentation
- Batch event documentation (can be added later)

## 8. Test Considerations
- Screenshot tests for popover open state
- Verify copy functionality works
- Verify link navigation to Developer settings
