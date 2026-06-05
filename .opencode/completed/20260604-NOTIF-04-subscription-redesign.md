# Subscription Redesign — Personal & Org-Shared

**Completed:** 2026-06-04
**Epic:** NOTIF
**Source:** .opencode/refined/NOTIF-04-subscription-redesign.md

## Summary

Redesigned subscriptions from watchlist-scoped single-adapter model to personal + org-shared scopes with adapter config UUID references. New CRUD+search endpoints, Alerts tab integration, enhanced quick-subscribe modal with severity pills and adapter selection.

## Plan Approved by the user:

### Requirements Summary

- Personal CRUD at `/v1/subscriptions` + search
- Org CRUD at `/v1/organizations/{orgId}/subscriptions` + search with admin security
- Entity gains `organization` field + `adapterConfigIds` (replaces `List<AdapterType> adapters`)
- SeverityTransitionJob dispatches via adapter config IDs
- Frontend: Alerts tab in NotificationSettingsPanel for subscription management
- Enhanced quick-subscribe modal on watchlist detail with adapter multi-select
- Watchlist fuzzy search picker in create dialog
- Colorful severity pill toggles

### Technical Approach

- Backend: New UserSubscriptionController + OrgSubscriptionController, SubscriptionService rewrite, migration to drop adapters column and add adapter_config_ids + organization_id
- Frontend: Alerts tab content, SeverityPicker component, AdapterChecklist/AdapterTypeIcon shared components, watchlist search picker
- Security: `@orgSecurity.isOwnerOrAdmin` for org endpoints

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | backend-testing-agent | Rewrite all 3 test files (42 tests) |
| 2.2 | backend-agent | Implement backend to pass tests |
| 2.4 | backend-optimizer-agent | Extract shared methods (-8.3%) |
| 2.5 | backend-agent | Remove legacy adapters, add dispatchByAdapterConfigIds |
| 3.2 | frontend-agent | Initial UI + move to Alerts tab |
| 3.2b | frontend-agent | UI polish (search, severities, email, modal redesign) |
| 3.4 | frontend-optimizer-agent | Extract AdapterChecklist + AdapterTypeIcon |

## Implementation

### Backend

- Endpoints: POST/PUT/DELETE/SEARCH for personal (`/v1/subscriptions`) and org (`/v1/organizations/{orgId}/subscriptions`)
- Services: SubscriptionService with personal/org CRUD + search, org suspension checks
- DB: Migration `202606041000-PRD-subscription-redesign.xml` — drop adapters, add adapter_config_ids JSONB + organization_id FK
- NotificationDispatchService.dispatchByAdapterConfigIds for UUID-based routing
- SeverityTransitionJob uses adapterConfigIds

### Frontend

- Alerts tab in NotificationSettingsPanel (both profile + org contexts)
- SeverityPicker.svelte — colorful pill toggles (red/amber/green/gray)
- AdapterChecklist.svelte + AdapterTypeIcon.svelte — shared components
- SubscriptionDialog.svelte — watchlist fuzzy search picker, severity pills, adapter icons
- SubscribeWatchlistDialog.svelte — enhanced with severity pills + adapter selection
- SubscriptionTableRow.svelte — severity badges + adapter count
- NotificationSettingsPanel.svelte — fuzzy search on watchlistName field

### Deviations from Plan

- Subscriptions moved from separate pages to existing "Alerts" tab (user feedback)
- `dispatchByAdapterConfigIds` method name (not overload) due to Java generics erasure

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| backend-testing-agent | Rewrite test suite (42 tests) | Complete |
| backend-agent | Implement backend | Complete |
| backend-optimizer-agent | Optimize backend | Complete (-8.3%) |
| backend-agent | Remove legacy adapters | Complete |
| frontend-agent | UI implementation + alerts tab | Complete |
| frontend-agent | UI polish (search, severities, modal) | Complete |
| frontend-optimizer-agent | Extract shared components | Complete |

## Files Modified

### Backend
- `Subscription.java` — added organization, adapterConfigIds; removed adapters
- `SubscriptionService.java` — full rewrite with personal/org CRUD + search
- `SubscriptionRepository.java` — new queries + JpaSpecificationExecutor
- `SubscriptionValidator.java` — rewritten for adapterConfigIds
- `SubscriptionMapper.java` — extends PageMapper
- `SubscriptionResponse.java` — added adapterConfigIds, organizationId
- `UserSubscriptionController.java` — new (personal endpoints)
- `OrgSubscriptionController.java` — new (org endpoints)
- `CreateSubscriptionRequest.java` — new
- `UpdateSubscriptionRequest.java` — new
- `SubscriptionMetaData.java` — new (watchlistName fuzzy search)
- `OrganizationSuspendedException.java` — new
- `NotificationDispatchService.java` — added dispatchByAdapterConfigIds
- `SeverityTransitionJob.java` — uses adapterConfigIds
- `ApiErrorCode.java` — added SUBSCRIPTION_NOT_FOUND, ORGANIZATION_SUSPENDED_ERROR
- `Paths.java` — added subscription path constants
- `202606041000-PRD-subscription-redesign.xml` — migration

### Frontend
- `NotificationSettingsPanel.svelte` — alerts tab with subscription list
- `SubscriptionDialog.svelte` — full redesign with watchlist picker
- `SubscribeWatchlistDialog.svelte` — severity pills + adapter icons
- `SubscriptionTableRow.svelte` — improved display
- `SeverityPicker.svelte` — new shared component
- `AdapterChecklist.svelte` — new shared component
- `AdapterTypeIcon.svelte` — new shared component
- `SubscriptionController.ts` — personal subscription API
- `OrgSubscriptionController.ts` — org subscription API
- `DeleteSubscriptionDialog.svelte` — confirmation dialog
- `models.ts` — added request/response types

### Tests
- `SubscriptionControllerTest.java` — 17 tests (rewritten)
- `SubscriptionServiceTest.java` — 16 tests (rewritten)
- `SubscriptionValidatorTest.java` — 9 tests (rewritten)
- `SeverityTransitionJobTest.java` — updated for adapterConfigIds

## Tests

- 42 backend tests written, all passing
- Old SubscriptionController deleted (replaced by User/Org controllers)
