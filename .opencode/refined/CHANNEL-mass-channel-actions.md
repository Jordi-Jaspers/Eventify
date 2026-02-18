# Mass Channel Actions with Confirmation

**Epic**: Channel Management
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-02-16
**Depends On**: None

## 1. User Story
**As a** channel owner or administrator
**I want** to select multiple channels and perform bulk actions
**So that** I can efficiently manage large numbers of channels without repetitive clicking

## 2. Business Context & Value
Users with many channels (especially after identifying stale ones) need efficient management:
- Delete 20 unused test channels at once
- Pause all channels for a deprecated service
- Resume channels after maintenance

Without bulk actions, users must click through each channel individually - tedious and error-prone.

## 3. Acceptance Criteria

### Selection
* [ ] **Scenario 1**: Checkbox on each row
    * Given the channels table
    * When viewing channels
    * Then each row has a selection checkbox

* [ ] **Scenario 2**: Select all on current page
    * Given the channels table with multiple pages
    * When I click "Select All" checkbox in the header
    * Then all channels on the current page are selected
    * And a count shows "X selected"

* [ ] **Scenario 3**: Deselect all
    * Given selected channels
    * When I click the header checkbox again (or "Clear selection")
    * Then all channels are deselected

* [ ] **Scenario 4**: Selection cleared on filter/search change
    * Given selected channels
    * When I apply a filter or change search
    * Then my selection is cleared (to avoid confusion with hidden items)

### Bulk Actions Toolbar
* [ ] **Scenario 5**: Toolbar appears when channels selected
    * Given no channels selected
    * When I select one or more channels
    * Then a bulk actions toolbar appears
    * And it shows: "X channels selected" + action buttons

* [ ] **Scenario 6**: Toolbar actions available
    * Given the bulk actions toolbar
    * Then I see buttons for: Delete, Pause, Resume

* [ ] **Scenario 7**: Toolbar disappears when selection cleared
    * Given channels selected and toolbar visible
    * When I clear selection
    * Then the toolbar disappears

### Confirmation & Execution
* [ ] **Scenario 8**: Delete confirmation dialog
    * Given 5 channels selected
    * When I click "Delete"
    * Then a confirmation dialog appears
    * And title shows "Delete 5 channels?"
    * And body warns "This will permanently delete 5 channels and all their events. This action cannot be undone."

* [ ] **Scenario 9**: Cancel does nothing
    * Given the delete confirmation dialog
    * When I click "Cancel"
    * Then nothing is deleted
    * And dialog closes
    * And selection remains

* [ ] **Scenario 10**: Confirm executes delete
    * Given the delete confirmation dialog
    * When I click "Delete" (confirm button)
    * Then channels are deleted
    * And table refreshes
    * And selection is cleared

* [ ] **Scenario 11**: Pause confirmation (lighter)
    * Given channels selected
    * When I click "Pause"
    * Then action executes (or simple confirmation)
    * And toast shows "X channels paused"

* [ ] **Scenario 12**: Resume confirmation (lighter)
    * Given paused channels selected
    * When I click "Resume"
    * Then action executes
    * And toast shows "X channels resumed"

* [ ] **Scenario 13**: Success feedback
    * Given a bulk action completed
    * Then a toast shows "X channels deleted/paused/resumed"
    * And the table refreshes automatically

* [ ] **Scenario 14**: Partial failure handling
    * Given a bulk delete where some channels fail (e.g., permissions)
    * Then toast shows "X of Y channels deleted. Z failed."
    * And failed channels remain in table

### Audit Logging
* [ ] **Scenario 15**: Bulk actions logged
    * Given a bulk delete
    * When the action completes
    * Then an entry is written to application logs (structured JSON) with:
        * Action type (BULK_DELETE, BULK_PAUSE, BULK_RESUME)
        * User ID who performed action
        * List of affected channel IDs
        * Timestamp
        * Success/failure counts

## 4. Technical Requirements

### API Changes

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/v1/user/channel/bulk/delete` | Bulk delete personal channels |
| `POST` | `/v1/user/channel/bulk/pause` | Bulk pause personal channels |
| `POST` | `/v1/user/channel/bulk/resume` | Bulk resume personal channels |
| `POST` | `/v1/organization/{orgId}/channels/bulk/delete` | Bulk delete org channels |
| `POST` | `/v1/organization/{orgId}/channels/bulk/pause` | Bulk pause org channels |
| `POST` | `/v1/organization/{orgId}/channels/bulk/resume` | Bulk resume org channels |

### Request Body
```json
{
  "channelIds": [1, 2, 3, 4, 5]
}
```

### Response Body
```json
{
  "succeeded": [1, 2, 3],
  "failed": [
    {"id": 4, "reason": "Channel not found"},
    {"id": 5, "reason": "Insufficient permissions"}
  ]
}
```

### New DTOs
```java
public record BulkChannelRequest(List<Long> channelIds) {}

public record BulkActionResponse(
    List<Long> succeeded,
    List<BulkActionFailure> failed
) {}

public record BulkActionFailure(Long id, String reason) {}
```

### Backend Implementation Pattern
```java
@PostMapping("/v1/user/channel/bulk/delete")
@PreAuthorize("isAuthenticated()")
public BulkActionResponse bulkDelete(
        @RequestBody BulkChannelRequest request,
        @AuthenticationPrincipal UserPrincipal principal) {
    
    List<Long> succeeded = new ArrayList<>();
    List<BulkActionFailure> failed = new ArrayList<>();
    
    for (Long channelId : request.channelIds()) {
        try {
            // Validate ownership and delete
            channelService.deleteChannel(channelId, principal.getUserId());
            succeeded.add(channelId);
        } catch (NotFoundException e) {
            failed.add(new BulkActionFailure(channelId, "Channel not found"));
        } catch (AccessDeniedException e) {
            failed.add(new BulkActionFailure(channelId, "Access denied"));
        }
    }
    
    // Audit log
    logBulkAction("BULK_DELETE", principal.getUserId(), succeeded, failed);
    
    return new BulkActionResponse(succeeded, failed);
}
```

### Audit Logging Format
```java
log.info("AUDIT: {}", objectMapper.writeValueAsString(Map.of(
    "action", "BULK_DELETE",
    "userId", userId,
    "succeededChannelIds", succeeded,
    "failedChannelIds", failed.stream().map(BulkActionFailure::id).toList(),
    "timestamp", OffsetDateTime.now()
)));
```

Example log output:
```json
{"action":"BULK_DELETE","userId":123,"succeededChannelIds":[1,2,3],"failedChannelIds":[4,5],"timestamp":"2026-02-16T10:30:00Z"}
```

## 5. Design & UI/UX

### Selection Checkboxes
- Checkbox column: First column (left side of table)
- Header checkbox: Select/deselect all on current page
- Row highlight: Selected rows have subtle background highlight
- Checkbox style: Match existing checkbox components

### Bulk Actions Toolbar
- Position: Above table, below filters
- Appearance: Slides in when selection > 0
- Sticky: Yes, visible during scroll
- Layout: `[X channels selected] [Pause] [Resume] [Delete] [Clear selection]`
- Delete button: Red/danger variant
- Clear selection: Text link or icon button

### Confirmation Dialog (Delete)
- Title: "Delete X channels?"
- Body: "This will permanently delete X channels and all their events. This action cannot be undone."
- Buttons: [Cancel] [Delete]
- Delete button: Red/danger variant
- Focus: Cancel button by default (safer)

### Confirmation (Pause/Resume)
- Lighter approach: Execute immediately with toast feedback
- Or simple dialog: "Pause X channels?" with [Cancel] [Pause]

### Toast Messages
- Success: "5 channels deleted" (success variant)
- Partial: "3 of 5 channels deleted. 2 failed." (warning variant)
- Full failure: "Failed to delete channels" (error variant)

## 6. Implementation Notes / Research

### Existing DataTable Component
- Check if shadcn-svelte DataTable supports row selection
- May need to implement selection state management in page component
- Selection state: `let selectedIds = $state<Set<number>>(new Set())`

### Frontend State Management
```typescript
let selectedIds = $state<Set<number>>(new Set());

function toggleSelect(id: number) {
  if (selectedIds.has(id)) {
    selectedIds.delete(id);
  } else {
    selectedIds.add(id);
  }
  selectedIds = new Set(selectedIds); // Trigger reactivity
}

function selectAll(channels: Channel[]) {
  selectedIds = new Set(channels.map(c => c.id));
}

function clearSelection() {
  selectedIds = new Set();
}
```

### Transaction Handling
- Process each channel individually (not in single transaction)
- This allows partial success
- Each delete/pause/resume is atomic

### Security
- Validate each channel ID belongs to the authenticated user/org
- Don't expose which IDs exist vs don't (generic failure message)
- Rate limiting consideration for bulk delete

### Future: Full Audit System
- Current approach: Structured JSON logs
- Future epic will implement proper audit table with UI
- Code comment: `// TODO: Replace with AuditService when Audit System epic is implemented`

### Files to Create/Modify

**Backend**
| File | Change |
|------|--------|
| New: `BulkChannelRequest.java` | Request DTO |
| New: `BulkActionResponse.java` | Response DTO |
| `UserChannelController.java` | Add bulk endpoints |
| `OrganizationChannelController.java` | Add bulk endpoints |
| `ChannelService.java` | Add bulk operation methods |
| `Paths.java` | Add bulk endpoint paths |

**Frontend**
| File | Change |
|------|--------|
| Channel table pages | Add selection state, bulk toolbar |
| New: `BulkActionsToolbar.svelte` | Toolbar component |
| New: `BulkDeleteDialog.svelte` | Confirmation dialog |
| `user-channel-service.ts` | Add bulk API methods |
| `channel-service.ts` | Add bulk API methods (org) |

### Test Scenarios
- Bulk delete succeeds for all channels
- Bulk delete with partial failure
- Bulk pause/resume
- Selection persists during pagination (same page)
- Selection clears on filter change
- Confirmation dialog cancel does nothing
- Audit log entries created
