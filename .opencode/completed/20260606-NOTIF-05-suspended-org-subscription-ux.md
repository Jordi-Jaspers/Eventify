# Suspended Organization Subscription UX

**Completed:** 2026-06-06
**Epic:** NOTIF
**Source:** `.opencode/refined/NOTIF-05-suspended-org-subscription-ux.md`

## Summary

Subscriptions to watchlists owned by suspended organizations now display a "Blocked" indicator, prevent editing (with tooltip), and are excluded from watchlist search. Delete remains allowed. IN_APP adapter now appears in org subscription dialogs.

## Plan Approved by the user:

### Requirements Summary

- Blocked badge on subscription list (tooltip: "Organization suspended — notifications paused")
- Remove allowed on blocked subscription (standard delete)
- Edit disallowed on blocked subscription (disabled + tooltip)
- Suspended org watchlists excluded from subscription search
- Reactivated org → subscriptions resume (already works via SeverityTransitionJob)
- Edge: All subscriptions blocked → all show blocked indicators

### Technical Approach

- Backend: `SubscriptionResponse` + `blocked`/`blockedReason` fields, `SubscriptionMapper` derives from org status, update guard on personal+org subscriptions, watchlist search excludes suspended orgs
- Frontend: Status column with Blocked/Active badge, disabled edit button with tooltip, IN_APP adapter merged into org dialog

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1.2 | deep-research-agent | Scan existing tests for conflicts |
| 2.1 | backend-testing-agent | Write/update tests (14 new/updated) |
| 2.2 | backend-agent | Implement backend changes |
| 2.4 | backend-optimizer-agent | Extract applyFields helper, fix syntax error |
| 3.2 | frontend-agent | Build blocked indicators + status column |
| 3.2b | frontend-agent | Fix IN_APP adapter + layout overflow |
| 3.4 | frontend-optimizer-agent | Accessibility + dedup |

## Implementation

### Backend

- `SubscriptionResponse`: +`Boolean blocked`, +`String blockedReason`
- `SubscriptionMapper`: `@AfterMapping deriveBlockedFields()` — blocked when watchlist.org.status == SUSPENDED
- `SubscriptionService`: `verifyOrgNotSuspended` on update (personal + org); delete unchanged
- `SubscriptionRepository`: extended `@EntityGraph` to eager-load watchlist.organization
- `WatchlistMetaData`: LEFT JOIN organization + exclude SUSPENDED from personal watchlist search

### Frontend

- `SubscriptionTableRow.svelte`: 12-col grid with dedicated Status column (amber Blocked badge + Lock icon OR green Active badge), disabled edit with tooltip
- `NotificationSettingsPanel.svelte`: guard in onEdit, updated table columns (Watchlist:4, Status:2, Severities:2, Adapters:2, Created:1, Actions:1)
- `SubscriptionDialog.svelte`: org scope now fetches personal IN_APP config and merges into adapter list
- `api.d.ts`: added `blocked?` + `blockedReason?` to SubscriptionResponse type

### Deviations from Plan

- HTTP status for suspended-org update rejection is 400 (not 409) — follows existing `OrganizationSuspendedException extends ApiException` pattern
- Added dedicated Status column instead of inline badge in Watchlist column (user feedback)
- Fixed IN_APP adapter missing in org subscription dialog (discovered during review)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent (×4) | Subscription domain, org status, frontend UI, test conflicts | Complete |
| backend-testing-agent | 14 tests (6 updated + 8 new across 4 files) | Complete |
| backend-agent | Implement mapper, service guard, repository, spec | Complete |
| backend-optimizer-agent | Extract applyFields, fix import typo | Complete |
| frontend-agent (×3) | Blocked UI, layout fix, IN_APP fix | Complete |
| frontend-optimizer-agent | Accessibility, dedup | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/subscription/model/response/SubscriptionResponse.java` — +blocked, +blockedReason
- `server/src/main/java/io/github/eventify/api/subscription/model/mapper/SubscriptionMapper.java` — @AfterMapping deriveBlockedFields
- `server/src/main/java/io/github/eventify/api/subscription/service/SubscriptionService.java` — update guard + applyFields helper
- `server/src/main/java/io/github/eventify/api/subscription/repository/SubscriptionRepository.java` — extended EntityGraph
- `server/src/main/java/io/github/eventify/api/watchlist/model/WatchlistMetaData.java` — exclude suspended orgs from search
- `server/src/test/.../subscription/controller/SubscriptionControllerTest.java` — 6 updated + 3 new
- `server/src/test/.../subscription/service/SubscriptionServiceTest.java` — 4 new
- `server/src/test/.../watchlist/service/UserWatchlistServiceTest.java` — 1 new
- `server/src/test/.../subscription/model/mapper/SubscriptionMapperTest.java` — NEW FILE, 4 tests
- `server/src/main/resources/db/changelog/changesets/202606061000-TST-suspended-org-test-data.xml` — 6 test data changesets
- `client/src/lib/components/subscriptions/SubscriptionTableRow.svelte` — status column, blocked UI
- `client/src/lib/components/notification-settings/NotificationSettingsPanel.svelte` — table columns, edit guard
- `client/src/lib/components/subscriptions/SubscriptionDialog.svelte` — IN_APP merge for org scope
- `client/src/lib/types/api.d.ts` — blocked fields on SubscriptionResponse
- `.opencode/BACKLOG.md` — added suspended org switcher item

## Tests

- 14 backend tests written/updated, all passing
- 89 total backend tests passing
- Frontend build clean (0 errors)
