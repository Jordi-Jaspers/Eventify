# Adapter Infrastructure & Filtering

**Completed:** 2026-06-03
**Epic:** NOTIF
**Source:** `.opencode/refined/NOTIF-01-adapter-infrastructure-filtering.md`

## Summary

Pluggable adapter infrastructure with per-dispatch filtering and credential storage (AdapterConfig CRUD).

## Plan Approved by the user:

### Requirements Summary

- `NotificationAdapter` gains `getAdapterType()` method
- `AdapterRegistry` service (lookup/filter by adapter types)
- `NotificationDispatchService.dispatch()` requires `List<AdapterType> targetAdapters`
- `SeverityTransitionJob` passes `subscription.getAdapters()` to dispatch
- AdapterConfig CRUD (personal + org-scoped) with security
- Liquibase migration for `adapter_config` table

### Technical Approach

- Backend only (no frontend)
- Refactoring classification (existing tests updated)
- Package restructure: `notification/core/` + `notification/adapter/`

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | backend-testing-agent | Create/update test suite |
| 2.2 | backend-agent | Implement production code |
| 2.3 | backend-agent | Address review feedback (2 rounds) |
| 2.4 | backend-optimizer-agent | Restructure test packages, remove dead code |

## Implementation

### Backend

- `NotificationAdapter.getAdapterType()` — identifier method
- `AdapterRegistry` — Spring bean that filters adapters by type
- `NotificationDispatchService.dispatch(audience, payload, targetAdapters)` — 3-arg dispatch
- `SeverityTransitionJob` — passes `subscription.getAdapters()` to dispatch
- `AdapterConfig` entity with Long SERIAL ID, `getMaskedWebhookUrl()`
- `UserAdapterConfigController` + `OrganizationAdapterConfigController`
- `UserAdapterConfigService` + `OrganizationAdapterConfigService`
- `AdapterConfigSecurityService` with `@PreAuthorize`
- `AdapterType` enum: IN_APP, MATTERMOST, SLACK, EMAIL
- `NoOpNotificationAdapter` for testing
- `Subscription.targetSeverities` changed from `List<String>` to `List<Severity>`
- `Subscription.adapters` changed from `List<String>` to `List<AdapterType>`
- User-facing validation messages

### Deviations from Plan

- Entity ID changed from UUID to Long SERIAL (user feedback)
- Added EMAIL adapter type (user feedback)
- Package restructure to `core/` + `adapter/` (user feedback)
- Severity enum adoption for subscriptions (user feedback)
- NoOpNotificationAdapter in production source (user feedback)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent | Existing patterns, tests, structure | Complete |
| backend-testing-agent | Create test suite (57 tests) | Complete |
| backend-agent | Implement all production code | Complete |
| backend-agent | Address review feedback (2 rounds) | Complete |
| backend-optimizer-agent | Package restructure + cleanup | Complete |

## Files Modified

- 97 files changed, 3160 insertions, 327 deletions
- New: adapter infrastructure (registry, config CRUD, controllers, services, validator, mapper, entity, migration)
- Restructured: entire notification package into core/ + adapter/
- Updated: subscription model/validator/tests to use Severity and AdapterType enums

## Tests

- ~57 new/updated tests, all passing
- Full build: BUILD SUCCESSFUL
