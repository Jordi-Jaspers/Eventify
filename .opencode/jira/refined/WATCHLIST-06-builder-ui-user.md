# Watchlist Builder UI (User)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: L
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-02-crud-api-user.md

## 1. User Story
**As a** user
**I want** to create and edit watchlists using a drag-and-drop interface
**So that** I can easily configure which channels to monitor and in what order

## 2. Business Context & Value
The watchlist builder is the primary configuration interface for watchlists. It allows users to:
- Name and describe their watchlist
- Select channels from their available channels
- Arrange channels in a specific order via drag-and-drop
- Set default filter preferences

This is a core UX feature that needs to be intuitive and responsive.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Create new watchlist page loads
    *   Given an authenticated user
    *   When they navigate to `/watchlists/new`
    *   Then they see an empty builder with available channels on the right

*   [ ] **Scenario 2**: Edit existing watchlist page loads
    *   Given an authenticated user with a watchlist
    *   When they navigate to `/watchlists/{id}`
    *   Then they see the builder pre-populated with the watchlist's data

*   [ ] **Scenario 3**: Search available channels
    *   Given the builder page
    *   When they type in the channel search field
    *   Then the available channels list filters accordingly

*   [ ] **Scenario 4**: Drag channel to canvas
    *   Given an available channel in the right panel
    *   When they drag it to the canvas (left panel)
    *   Then the channel appears in the canvas at the drop position
    *   And the channel is removed from the available list

*   [ ] **Scenario 5**: Reorder channels on canvas
    *   Given channels on the canvas
    *   When they drag a channel to a new position
    *   Then the channels reorder accordingly

*   [ ] **Scenario 6**: Remove channel from canvas
    *   Given a channel on the canvas
    *   When they click the remove button (X)
    *   Then the channel is removed from the canvas
    *   And the channel reappears in the available list

*   [ ] **Scenario 7**: Auto-save on changes
    *   Given an existing watchlist being edited
    *   When any change is made (name, channels, order, defaults)
    *   Then the watchlist is automatically saved after a debounce period
    *   And a "Saved" indicator appears

*   [ ] **Scenario 8**: Create watchlist
    *   Given a new watchlist with valid data
    *   When they click "Create Watchlist"
    *   Then the watchlist is created
    *   And they are redirected to `/watchlists`

*   [ ] **Scenario 9**: Validation - name required
    *   Given the builder page
    *   When they try to save without a name
    *   Then a validation error is shown

*   [ ] **Scenario 10**: Validation - duplicate name
    *   Given a watchlist named "Production" exists
    *   When they try to create another with name "production"
    *   Then a validation error is shown (409 from API)

*   [ ] **Scenario 11**: Empty channels allowed
    *   Given the builder page
    *   When they save a watchlist with no channels
    *   Then the watchlist is saved successfully

*   [ ] **Scenario 12**: Configure default filters
    *   Given the builder page
    *   When they set default time range, only-critical, and sort options
    *   Then these defaults are saved with the watchlist

*   [ ] **Scenario 13**: Back navigation
    *   Given the builder page
    *   When they click "Back" or browser back
    *   Then they return to the watchlist list

## 4. Technical Requirements

### Frontend Routes
- Create: `/watchlists/new` → `client/src/routes/(authenticated)/watchlists/new/+page.svelte`
- Edit: `/watchlists/[id]` → `client/src/routes/(authenticated)/watchlists/[id]/+page.svelte`

### API Integration
- `GET /v1/user/watchlists/{id}` - Load existing watchlist
- `POST /v1/user/watchlists` - Create new watchlist
- `PUT /v1/user/watchlists/{id}` - Update watchlist (auto-save)
- `POST /v1/user/channels/search` - Load available channels

### Drag and Drop
- Install and use a Svelte 5-compatible DnD library
- Recommended: `svelte-dnd-action` or native HTML5 DnD API
- Ensure touch support for mobile

### State Management
```typescript
interface BuilderState {
    id: number | null;  // null for new
    name: string;
    description: string;
    selectedChannels: Channel[];  // Ordered
    availableChannels: Channel[];
    defaultTimeRange: '24h' | '7d' | '30d';
    defaultOnlyCritical: boolean;
    defaultSortBySeverity: boolean;
    isDirty: boolean;
    isSaving: boolean;
    lastSaved: Date | null;
}
```

### Auto-Save Logic
- Debounce: 1000ms after last change
- Only save if `isDirty` is true
- Show saving indicator during save
- Show "Saved" indicator on success
- Show error toast on failure

### Security
- User can only see their personal channels
- User can only edit their own watchlists

### Performance
- Load all user channels upfront (paginated if many)
- Optimistic UI updates during drag operations
- Debounced auto-save to prevent excessive API calls

## 5. Design & UI/UX

### Page Layout
```
┌─────────────────────────────────────────────────────────────────────┐
│  ← Back    Create Watchlist / Edit "Production"        [Saved ✓]   │
├─────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────┐  │  ┌───────────────────────────────┐ │
│  │ Watchlist Name*         │  │  │ Available Channels            │ │
│  │ [Production Services  ] │  │  │ [Search channels...]          │ │
│  ├─────────────────────────┤  │  ├───────────────────────────────┤ │
│  │ Description             │  │  │ ┌───────────────────────────┐ │ │
│  │ [Monitors all prod...] │  │  │ │ 📻 API Gateway      [drag]│ │ │
│  ├─────────────────────────┤  │  │ ├───────────────────────────┤ │ │
│  │ Default Settings        │  │  │ │ 📻 User Service     [drag]│ │ │
│  │ Time Range: [24h ▼]     │  │  │ ├───────────────────────────┤ │ │
│  │ □ Only show critical    │  │  │ │ 📻 Payment Service  [drag]│ │ │
│  │ ☑ Sort by severity      │  │  │ └───────────────────────────┘ │ │
│  ├─────────────────────────┤  │  └───────────────────────────────┘ │
│  │ Selected Channels       │  │                                    │
│  │ (Drag to reorder)       │  │                                    │
│  │ ┌───────────────────┐   │  │                                    │
│  │ │ ≡ Database    [×] │   │  │                                    │
│  │ ├───────────────────┤   │  │                                    │
│  │ │ ≡ Redis Cache [×] │   │  │                                    │
│  │ ├───────────────────┤   │  │                                    │
│  │ │ ≡ Auth Server [×] │   │  │                                    │
│  │ └───────────────────┘   │  │                                    │
│  │                         │  │                                    │
│  │ Drop channels here...   │  │                                    │
│  └─────────────────────────┘  │                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                              [Cancel] [Create]     │
└─────────────────────────────────────────────────────────────────────┘
```

### Visual Indicators
- **Drag handle**: `≡` icon on selected channels
- **Remove button**: `×` on selected channels
- **Drop zone**: Dashed border, highlight on drag over
- **Drag preview**: Semi-transparent card following cursor
- **Saving indicator**: Spinner + "Saving..."
- **Saved indicator**: Checkmark + "Saved" (fade out after 2s)

### Empty States
- **No available channels**: "No channels found. Create channels first."
- **No selected channels**: "Drag channels here to add them to your watchlist"

### Responsive Design
- On mobile: Stack panels vertically (selected on top, available below)
- Touch-friendly drag targets

## 6. Implementation Notes / Research

### File Locations
- Create page: `client/src/routes/(authenticated)/watchlists/new/+page.svelte`
- Edit page: `client/src/routes/(authenticated)/watchlists/[id]/+page.svelte`
- Shared builder component: `client/src/lib/components/watchlist/WatchlistBuilder.svelte`
- Channel card component: `client/src/lib/components/watchlist/DraggableChannelCard.svelte`

### Drag and Drop Library Options
1. **`svelte-dnd-action`**: Popular, Svelte-native, good for lists
   - `bun add svelte-dnd-action`
   - Supports flip animations, touch, accessibility
   
2. **Native HTML5 DnD**: No dependencies, more control
   - Requires more boilerplate
   - Less polished UX

**Recommendation**: Use `svelte-dnd-action` for faster implementation and better UX.

### Auto-Save Implementation
```typescript
import { debounce } from '$lib/utils/debounce';

const debouncedSave = debounce(async () => {
    if (!state.isDirty || !state.id) return;
    state.isSaving = true;
    try {
        await updateWatchlist(state.id, buildRequest());
        state.isDirty = false;
        state.lastSaved = new Date();
    } catch (error) {
        toast.error('Failed to save');
    } finally {
        state.isSaving = false;
    }
}, 1000);

// Call on any state change
$effect(() => {
    if (state.isDirty && state.id) {
        debouncedSave();
    }
});
```

### Patterns to Follow
- Use existing form patterns from channel creation
- Use Card components for the panels
- Use Badge for channel status indicators
- Follow glassmorphism styling

### Accessibility
- Keyboard navigation for drag and drop
- ARIA labels for drag handles
- Screen reader announcements for reordering
