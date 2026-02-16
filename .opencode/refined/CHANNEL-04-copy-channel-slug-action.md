# Copy Channel Slug Action

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: XS
**Created Date**: 2026-02-16
**Depends On**: CHANNEL-02-slug-identifier-system.md

## 1. User Story
**As a** developer using Eventify
**I want** to quickly copy a channel's slug from the UI
**So that** I can paste it into my code or configuration without typing errors

## 2. Business Context & Value
Small quality-of-life improvement that:
- Reduces friction when setting up integrations
- Prevents typos in slugs
- Makes the slug the "go-to" identifier for developers

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Copy button in channel table
    * Given the channels table
    * When I click the copy icon next to a channel's slug
    * Then the slug is copied to my clipboard
    * And a toast confirms "Slug copied to clipboard"

* [ ] **Scenario 2**: Copy action in channel row actions menu
    * Given the channel row actions menu (⋮)
    * When I open the menu
    * Then there is a "Copy Slug" option
    * And clicking it copies the slug

* [ ] **Scenario 3**: Works on both personal and org channel tables
    * Given both channel table views
    * When viewing either table
    * Then copy functionality is available

* [ ] **Scenario 4**: Keyboard accessibility
    * Given focus on the copy button
    * When I press Enter or Space
    * Then the slug is copied

## 4. Technical Requirements

### Frontend Changes
| Component | Change |
|-----------|--------|
| `ChannelRow.svelte` | Add copy button/icon next to slug display |
| `ChannelActions.svelte` | Add "Copy Slug" menu item |

### Implementation
- Use `navigator.clipboard.writeText()`
- Use existing toast system for feedback
- Copy icon: Use clipboard icon from Lucide icons (e.g., `Copy` or `Clipboard`)

## 5. Design & UI/UX

### Copy Button Placement
- Small clipboard icon next to slug text in table row
- Icon size: Match other action icons
- Hover state: Slight opacity change or color shift
- Active state: Brief visual feedback on click

### Toast Message
- Text: "Slug copied to clipboard"
- Variant: Success (green)
- Duration: 2-3 seconds
- Position: Use existing toast positioning

### Actions Menu
- Add "Copy Slug" as first item in dropdown
- Icon: Clipboard icon
- Keyboard shortcut hint (optional): None needed

## 6. Implementation Notes / Research

### Clipboard Utility
Check if there's an existing clipboard utility in the codebase. If not, create a simple helper:

```typescript
// $lib/utils/clipboard.ts
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}
```

### Component Implementation
```svelte
<script lang="ts">
  import { Copy } from 'lucide-svelte';
  import { toast } from '$lib/stores/toast';
  import { copyToClipboard } from '$lib/utils/clipboard';

  export let slug: string;

  async function handleCopy() {
    const success = await copyToClipboard(slug);
    if (success) {
      toast.success('Slug copied to clipboard');
    } else {
      toast.error('Failed to copy slug');
    }
  }
</script>

<button 
  onclick={handleCopy}
  class="p-1 hover:bg-muted rounded"
  aria-label="Copy slug to clipboard"
>
  <Copy class="h-4 w-4" />
</button>
```

### Existing Patterns to Follow
- Check `ChannelActions.svelte` for dropdown menu pattern
- Check existing toast usage in the codebase
- Match styling with other icon buttons in tables

### Files to Modify
| File | Change |
|------|--------|
| `client/src/lib/components/channels/ChannelRow.svelte` | Add copy button next to slug |
| `client/src/lib/components/channels/ChannelActions.svelte` | Add "Copy Slug" menu item |
| `client/src/lib/utils/clipboard.ts` | Create if not exists |
