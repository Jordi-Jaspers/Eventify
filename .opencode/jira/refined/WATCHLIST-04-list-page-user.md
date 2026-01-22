# Watchlist List Page (User)

**Epic**: Event Timeline & Visualization
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-01-22
**Depends On**: WATCHLIST-02-crud-api-user.md

## 1. User Story
**As a** user
**I want** to view a list of my personal watchlists in a searchable table
**So that** I can manage and navigate to my watchlists

## 2. Business Context & Value
Users need a central place to view all their watchlists before creating new ones or editing existing ones. This follows the established DataTable pattern used throughout the application for consistent UX.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: View watchlists list
    *   Given an authenticated user with watchlists
    *   When they navigate to `/watchlists`
    *   Then they see a DataTable with their watchlists

*   [ ] **Scenario 2**: Empty state
    *   Given an authenticated user with no watchlists
    *   When they navigate to `/watchlists`
    *   Then they see an empty state with a call-to-action to create one

*   [ ] **Scenario 3**: Search watchlists
    *   Given an authenticated user with multiple watchlists
    *   When they type in the search field
    *   Then the list filters by watchlist name

*   [ ] **Scenario 4**: Navigate to create
    *   Given the watchlists list page
    *   When they click "New Watchlist" button
    *   Then they navigate to `/watchlists/new`

*   [ ] **Scenario 5**: Navigate to edit
    *   Given a watchlist in the list
    *   When they click the edit action
    *   Then they navigate to `/watchlists/{id}`

*   [ ] **Scenario 6**: Delete watchlist
    *   Given a watchlist in the list
    *   When they click the delete action and confirm
    *   Then the watchlist is deleted and the list refreshes

*   [ ] **Scenario 7**: Pagination
    *   Given more than 10 watchlists
    *   When viewing the list
    *   Then pagination controls are available

## 4. Technical Requirements

### Frontend Route
- Path: `/watchlists`
- File: `client/src/routes/(authenticated)/watchlists/+page.svelte`

### API Integration
- Uses `POST /v1/user/watchlists/search` for list data
- Uses `DELETE /v1/user/watchlists/{id}` for deletion

### DataTable Columns
| Column | Key | Sortable | Filterable | ColSpan |
|--------|-----|----------|------------|---------|
| Watchlist | search | No | Yes (FUZZY_TEXT) | 3 |
| Description | description | No | No | 4 |
| Channels | channelCount | Yes | No | 2 |
| Created | createdAt | Yes | No | 2 |
| Actions | actions | No | No | 1 |

### Actions
- Edit (pencil icon) → Navigate to `/watchlists/{id}`
- Delete (trash icon) → Confirm dialog, then delete

### Security
- Page requires authentication
- Only shows user's personal watchlists

### Performance
- Uses DataTable's built-in pagination (page size: 10)
- Lazy loading via search endpoint

## 5. Design & UI/UX

### Page Layout
```
┌─────────────────────────────────────────────────────────┐
│  My Watchlists                          [New Watchlist] │
│  Manage your personal watchlists for monitoring         │
├─────────────────────────────────────────────────────────┤
│  [Search watchlists...]                                 │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────┐│
│  │ 📋 Production Services    | No description | 5 | ...││
│  ├─────────────────────────────────────────────────────┤│
│  │ 📋 Staging Environment   | Testing...     | 3 | ...││
│  └─────────────────────────────────────────────────────┘│
│                                          < 1 2 3 >      │
└─────────────────────────────────────────────────────────┘
```

### Empty State
- Icon: ClipboardList or similar
- Title: "No watchlists yet"
- Description: "Create your first watchlist to start monitoring channels"
- CTA Button: "Create Watchlist" → `/watchlists/new`

### Row Design
- Icon: List or ClipboardList icon with gradient background (similar to channels)
- Name: Bold, primary text
- Description: Muted, truncated at ~60 chars
- Channel count: Badge showing number of channels
- Created: Formatted date
- Actions: Edit and Delete icon buttons

### Delete Confirmation
- Use existing confirm dialog pattern
- Message: "Are you sure you want to delete '{name}'? This action cannot be undone."

## 6. Implementation Notes / Research

### File Locations
- Page: `client/src/routes/(authenticated)/watchlists/+page.svelte`
- API Client: `client/src/lib/api/watchlist/UserWatchlistController.ts` (generated from OpenAPI)

### Patterns to Follow
- Follow `client/src/routes/(authenticated)/channels/+page.svelte` for DataTable integration
- Use same styling patterns (glassmorphism, gradients)
- Use `createDataTableService` for state management

### Navigation
Add to sidebar navigation:
- Icon: `ClipboardList` or `ListChecks` from Lucide
- Label: "Watchlists"
- Path: `/watchlists`
- Position: After "Channels"

### API Client Generation
After backend is complete, run:
```bash
bun run sync:api
```
This will generate the TypeScript client from OpenAPI spec.

### Component Reuse
- `DataTable` component
- `Badge` for channel count
- `Button` for actions
- Existing toast patterns for success/error
