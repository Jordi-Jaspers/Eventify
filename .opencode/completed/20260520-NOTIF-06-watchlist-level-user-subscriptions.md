# Watchlist-Level User Subscriptions

**Completed:** 2026-05-20
**Epic:** NOTIF
**Source:** .opencode/refined/NOTIF-06-watchlist-level-user-subscriptions.md

## Summary

Users can subscribe to severity transitions (CRITICAL/WARNING/OK) on watchlists via a bell icon dialog in the monitor page. A scheduled job detects severity changes and dispatches in-app notifications to matching subscribers.

## Plan Approved by the user:

### Requirements Summary

- Bell icon in monitor page header opens subscription dialog
- Dialog: severity checkboxes (CRITICAL default), extensible notification channels (IN_APP always on)
- POST/GET/DELETE `/api/v1/user/watchlist/{watchlistId}/subscription`
- Validation: targetSeverities non-empty subset of [CRITICAL, WARNING, OK], adapters must include IN_APP
- `SeverityTransitionJob` @Scheduled(fixedDelay=60s) detects severity transitions, dispatches ALERT notifications
- Junction table `watchlist_channel` for performant channel→watchlist lookups
- Security: reuse existing watchlist access checks via @PreAuthorize

### Technical Approach

- Backend: subscription CRUD, severity transition job, junction table with sync
- Frontend: SubscribeWatchlistDialog component with Dialog pattern
- Database: subscription table, watchlist_channel junction table, channel severity columns, trigger update

### Execution Order

| Phase | Agent | Task |
| ----- | ----- | ---- |
| 1 | spring-testing-agent | Create backend test suite (37 tests) |
| 2 | spring-backend-agent | Implement subscription module + junction table |
| 3 | backend-optimizer-agent | Refactor for maintainability |
| 4 | svelte-frontend-agent | Build subscription dialog UI |
| 5 | frontend-optimizer-agent | Refactor frontend |

## Implementation

### Backend

- Endpoints: POST/GET/DELETE `/v1/user/watchlist/{watchlistId}/subscription`
- Entity: Subscription with JSONB targetSeverities/adapters
- Service: SubscriptionService (upsert subscribe, get, unsubscribe)
- Job: SeverityTransitionJob (60s interval, dispatches ALERT notifications)
- Junction: watchlist_channel table with sync on watchlist create/update
- Migrations: subscription table, junction table, channel severity columns, trigger update

### Frontend

- SubscribeWatchlistDialog.svelte — Dialog with severity checkboxes + extensible adapters
- WatchlistSubscriptionController.ts — API client functions
- MonitorCanvas.svelte — Bell icon (outline/filled based on subscription state)

### Deviations from Plan

- Added watchlist_channel junction table for performance (not in original story)
- Used Dialog instead of Popover for better UX extensibility
- Frontend validation added for empty severity selection

## Agents Used

| Agent | Task | Result |
| ----- | ---- | ------ |
| deep-research-agent | Backend patterns research | Complete |
| deep-research-agent | Frontend patterns research | Complete |
| spring-testing-agent | 37 backend tests | Complete |
| spring-backend-agent | Full subscription module | Complete |
| spring-backend-agent | Junction table + review fixes | Complete |
| backend-optimizer-agent | Refactor backend | Complete |
| svelte-frontend-agent | Subscription dialog UI | Complete |
| frontend-optimizer-agent | Refactor frontend | Complete |

## Files Modified

### Backend (new)
- `api/subscription/controller/SubscriptionController.java`
- `api/subscription/service/SubscriptionService.java`
- `api/subscription/model/Subscription.java`
- `api/subscription/model/request/SubscribeRequest.java`
- `api/subscription/model/response/SubscriptionResponse.java`
- `api/subscription/model/mapper/SubscriptionMapper.java`
- `api/subscription/model/validator/SubscriptionValidator.java`
- `api/subscription/repository/SubscriptionRepository.java`
- `api/subscription/job/SeverityTransitionJob.java`
- `api/watchlist/model/WatchlistChannel.java`
- `api/watchlist/model/WatchlistChannelId.java`
- `api/watchlist/repository/WatchlistChannelRepository.java`

### Backend (modified)
- `api/watchlist/service/WatchlistService.java` — syncWatchlistChannels, validateFoundChannelIds
- `api/watchlist/service/UserWatchlistService.java` — sync calls
- `api/watchlist/service/OrganizationWatchlistService.java` — sync calls
- `api/watchlist/repository/WatchlistRepository.java` — findWatchlistsContainingChannel
- `api/channel/model/Channel.java` — currentSeverity + lastNotifiedSeverity persisted
- `api/channel/repository/ChannelRepository.java` — findChannelsWithSeverityChange
- `api/Paths.java` — SUBSCRIPTION_PART, USER_WATCHLIST_SUBSCRIPTION_PATH
- `support/util/TestDataCleanupService.java` — subscription cleanup

### Database
- `202605201000-PRD-subscription-table.xml`
- `202605201100-PRD-watchlist-channel-junction.xml`
- `update_channel_last_event.sql` — sets current_severity

### Frontend (new)
- `client/src/lib/components/monitor/SubscribeWatchlistDialog.svelte`
- `client/src/lib/api/watchlist/WatchlistSubscriptionController.ts`

### Frontend (modified)
- `client/src/lib/components/monitor/MonitorCanvas.svelte`
- `client/src/lib/components/monitor/index.ts`
- `client/src/lib/api/models.ts`

### Tests (new)
- `src/test/java/.../subscription/controller/SubscriptionControllerTest.java`
- `src/test/java/.../subscription/service/SubscriptionServiceTest.java`
- `src/test/java/.../subscription/model/validator/SubscriptionValidatorTest.java`
- `src/test/java/.../subscription/job/SeverityTransitionJobTest.java`

## Tests

- 37 tests written, 36 passing (1 stub removed during implementation)
