# Notification Bell with What's New Panel

**Completed:** 2026-05-01
**Epic:** GEN
**Source:** .opencode/refined/GEN-01-notification-bell-whats-new.md

## Summary

Replaced sidebar "What's New" sparkles button with a Bell notification icon that opens a right-side Sheet panel showing unread changelog versions. "What's New" moved to user dropdown Account Actions.

## Approved Plan

### Requirements Summary

- Bell icon replaces Sparkles in sidebar quick actions
- Pulsing dot + "New" badge when unread versions exist
- Click opens right-side Sheet panel with unread changelog entries (up to 5)
- "What's New" item added to user dropdown between Profile and Theme toggle
- Versions marked as read when user visits the Changelog page (existing behavior)
- Panel only shows unread versions; empty state when all seen

### Technical Approach

- Frontend-only (no backend changes)
- New notification store wrapping existing versionStore with semver comparison
- New NotificationPanel component using Sheet pattern
- Modified AppSidebarUser for bell + dropdown changes

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | typescript-testing-agent | Create screenshot test suite |
| 2 | svelte-frontend-agent | Implement bell, store, panel, sidebar changes |
| 2b-d | svelte-frontend-agent | Multi-version support, read-on-visit behavior |
| 3 | frontend-optimizer-agent | Indentation fixes |
| 4 | ui-validator | UI polish (blocked by model availability) |

## Implementation

### Frontend

- Bell icon with relative pulsing dot indicator + "New" badge
- NotificationPanel: right-side Sheet with glassmorphism, changelog cards, "View Full Changelog" footer
- NotificationStore: filters unread versions via semver comparison, up to 5 entries
- What's New in user dropdown Account Actions section

### Deviations from Plan

- Added multi-version support (up to 5 changelog entries) per user feedback
- Changed to read-on-visit only (panel no longer marks as read on open)
- UI polish blocked due to claude-opus-4.5 model unavailability

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent | Research sidebar, store, sheet, badge patterns | Complete |
| typescript-testing-agent | Create 4 screenshot tests (×2 themes) | Complete |
| svelte-frontend-agent | Implement notification bell feature | Complete |
| svelte-frontend-agent | Multi-version + read/unread support | Complete |
| svelte-frontend-agent | Read-on-visit only behavior | Complete |
| frontend-optimizer-agent | Indentation cleanup | Complete |

## Files Modified

- `client/src/lib/types/notification.ts` - NotificationType and NotificationItem types (new)
- `client/src/lib/stores/notification.svelte.ts` - NotificationStore wrapping versionStore (new)
- `client/src/lib/components/notification/NotificationPanel.svelte` - Right-side Sheet panel (new)
- `client/src/lib/components/layout/AppSidebarUser.svelte` - Bell replaces Sparkles, What's New in dropdown
- `client/test/components/notification-bell.spec.ts` - 6 screenshot tests (new)

## Tests

- 6 tests written, 6 passing (4 scenarios × dark/light themes, minus 2 merged)
