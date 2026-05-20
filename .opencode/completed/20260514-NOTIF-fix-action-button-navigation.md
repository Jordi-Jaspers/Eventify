# Fix Notification Action Button Navigation

**Completed:** 2026-05-14
**Epic:** USER_CHANGE_REQUEST
**Source:** ad-hoc request

## Summary

Fixed notification action button not navigating when clicked. `goto()` only works for internal SvelteKit routes — external URLs silently failed. Added internal/external URL detection, external link indicator (↗ icon), and admin form URL validation with route type badge.

## Implementation

### Bug Fix (NotificationPanel.svelte)
- Added `isInternalUrl()` helper — checks if URL starts with `/`
- Internal URLs → `goto()` (SPA navigation)
- External URLs → `window.open(url, '_blank')` (new tab)
- External action labels show `ExternalLink` icon instead of `→`

### Admin Form UX (BroadcastSendService + send page)
- Added `isUrlFormatValid()` — must start with `/`, `http://`, or `https://`
- Added `isInternalUrl()` and `hasActionUrl()` helpers
- URL format validation blocks submit when invalid
- Shows "Internal route — opens in same tab" or "External link — opens in new tab" badge below URL input

## Files Modified

- `client/src/lib/components/notification/NotificationPanel.svelte` — navigation fix + external icon
- `client/src/lib/api/admin/service/BroadcastSendService.svelte.ts` — URL validation logic
- `client/src/routes/(authenticated)/admin/tools/notifications/send/+page.svelte` — validation UI + route type indicator
