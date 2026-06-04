# Adapter Client Factory & Dispatch Refactoring

**Completed:** 2026-06-04
**Epic:** NOTIF
**Source:** .opencode/refined/NOTIF-02-adapter-implementations.md

## Summary

Refactored notification adapter infrastructure: per-adapter tracing via AdapterClientFactory, cleaned dispatch service to always-async, simplified NotificationAdapter interface to 3-arg only.

## Plan Approved by the user:

### Requirements Summary

- Each adapter (Mattermost, Slack) must have its own service name for distributed tracing
- NotificationDispatchService should be clean: required deps, always async via gateway
- NotificationAdapter interface: single 3-arg `send(User, NotificationPayload, AdapterConfig)`
- AdapterClient as top-level class, factory creates per-adapter instances

### Technical Approach

- Backend: AdapterClientFactory + AdapterClient, adapter constructors, dispatch service refactor
- Frontend: N/A

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | orchestrator | Implement AdapterClientFactory with per-adapter service names |
| 2 | orchestrator | Update adapters to use factory |
| 3 | orchestrator | Refactor dispatch service (required deps, always async) |
| 4 | orchestrator | Fix all tests |
| 5 | orchestrator | Optimize: extract AdapterClient to top-level class |

## Implementation

### Backend

- `AdapterClientFactory` — Spring `@Component` factory creating `AdapterClient` instances with per-adapter tracing service names
- `AdapterClient` — top-level class, package-private constructor, thin `RestClient` wrapper
- `MattermostNotificationAdapter` — injects factory, creates with `"Mattermost Client"`
- `SlackNotificationAdapter` — injects factory, creates with `"Slack Client"`
- `NotificationDispatchService` — required deps only, always dispatches via `AdapterSendGateway.sendAsync()`
- `AdapterSendGateway` — `@Async` + try-catch isolation per adapter call
- Removed old `AdapterClient` `@Component`, removed 2-arg `send()` from interface

### Deviations from Plan

- None

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| orchestrator | All implementation | Complete |

## Files Modified

- `server/src/main/java/io/github/eventify/api/notification/adapter/client/AdapterClientFactory.java` — new factory
- `server/src/main/java/io/github/eventify/api/notification/adapter/client/AdapterClient.java` — new top-level client
- `server/src/main/java/io/github/eventify/api/notification/adapter/adapters/MattermostNotificationAdapter.java` — use factory
- `server/src/main/java/io/github/eventify/api/notification/adapter/adapters/SlackNotificationAdapter.java` — use factory
- `server/src/main/java/io/github/eventify/api/notification/adapter/adapters/EmailNotificationAdapter.java` — 3-arg only
- `server/src/main/java/io/github/eventify/api/notification/adapter/adapters/NotificationAdapter.java` — 3-arg interface
- `server/src/main/java/io/github/eventify/api/notification/adapter/service/AdapterSendGateway.java` — async gateway
- `server/src/main/java/io/github/eventify/api/notification/core/service/NotificationDispatchService.java` — clean dispatch
- `server/src/test/java/io/github/eventify/api/notification/adapter/MattermostNotificationAdapterTest.java` — factory mock
- `server/src/test/java/io/github/eventify/api/notification/adapter/SlackNotificationAdapterTest.java` — factory mock
- `server/src/test/java/io/github/eventify/api/notification/adapter/EmailNotificationAdapterTest.java` — 3-arg
- `server/src/test/java/io/github/eventify/api/notification/adapter/service/AdapterSendGatewayTest.java` — new test
- `server/src/test/java/io/github/eventify/api/notification/core/service/NotificationDispatchServiceTest.java` — gateway mock

## Tests

- 29 notification tests passing
