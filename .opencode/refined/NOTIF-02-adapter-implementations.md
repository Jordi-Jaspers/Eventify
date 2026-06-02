---
epic: "NOTIF"
title: "Adapter Implementations — Mattermost, Slack, Email"
estimate: M
status: ready
created: 2026-06-02
depends_on: ["NOTIF-01-adapter-infrastructure"]
labels: [backend, notification, integration]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user with linked notification adapters\
**I want** notifications delivered to Mattermost, Slack, and Email\
**So that** I receive alerts on the platforms I actively use\

## 2. Business Context & Value

NOTIF-01 provides the infrastructure; this story delivers the actual adapter implementations. Users can receive severity transition alerts on their preferred chat platforms or email without checking Eventify directly.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Mattermost adapter delivers formatted message
    * Given a user with a Mattermost adapter config containing a valid webhook URL
    * When a notification is dispatched targeting MATTERMOST
    * Then a formatted message (severity icon, watchlist name, channel, old→new severity, action link) is POSTed to the webhook URL
* [ ] **Scenario 2**: Slack adapter delivers formatted message
    * Given a user with a Slack adapter config containing a valid webhook URL
    * When a notification is dispatched targeting SLACK
    * Then a Slack Block Kit message (severity color sidebar, structured fields, action button) is POSTed to the webhook URL
* [ ] **Scenario 3**: Email adapter sends notification email
    * Given a user with EMAIL adapter enabled
    * When a notification is dispatched targeting EMAIL
    * Then an HTML email is sent to the user's account email address with severity details and action link
* [ ] **Scenario 4**: Test connection — success
    * Given a valid adapter config ID
    * When `POST /v1/adapter-configs/{id}/test` is called
    * Then a test message is sent via that adapter and 200 OK returned with `{success: true}`
* [ ] **Scenario 5**: Test connection — failure
    * Given an adapter config with an invalid/unreachable webhook URL
    * When `POST /v1/adapter-configs/{id}/test` is called
    * Then 200 OK returned with `{success: false, error: "Connection refused"}` (no 5xx)
* [ ] **Scenario 6**: Adapter delivery failure does not block other adapters
    * Given a dispatch targeting [MATTERMOST, SLACK, IN_APP] where Mattermost webhook is down
    * When dispatch executes
    * Then Slack and IN_APP still deliver successfully; Mattermost failure is logged as warning
* [ ] **Edge Case**: Email adapter — user has no verified email
    * Given a user with unverified email and EMAIL adapter enabled
    * When dispatch targets EMAIL
    * Then email is skipped with warning log (no error thrown)

## 4. Technical Requirements

* **API Changes**:
    - `POST /v1/adapter-configs/{id}/test` — sends test notification via specified config
* **Database**: N/A — uses `adapter_config` from NOTIF-01
* **Security**:
    - Webhook URLs never logged in full (mask in logs)
    - Email adapter uses account email only — no user-configurable recipient
    - Test endpoint rate-limited: max 5 calls per config per minute
* **Performance**:
    - All adapter sends are async (`@Async`) — dispatch does not block on delivery
    - HTTP timeout for webhook calls: 10s connect, 30s read

## 5. Design & UI/UX

N/A — backend adapters. UI test button wired in NOTIF-03.

## 6. Implementation Notes

- Each adapter: new class in `notification/adapter/` implementing `NotificationAdapter`
- `MattermostNotificationAdapter`: HTTP POST to webhook, markdown body format
- `SlackNotificationAdapter`: HTTP POST to webhook, Block Kit JSON payload
- `EmailNotificationAdapter`: bridges to existing `EmailService.sendEmail(User, MailMessage)`, uses new Thymeleaf template for notification emails
- All adapters annotated `@Component` → auto-registered in Spring DI
- Each returns identifier: `"MATTERMOST"`, `"SLACK"`, `"EMAIL"`
- Adapters need access to `AdapterConfigService` to resolve webhook URL for the target user — dispatch must pass user + adapter config context
- Consider `NotificationAdapter.send(User user, NotificationPayload payload, AdapterConfig config)` signature extension or wrapper
- New email template: `notification-alert.html` in templates directory
- Test endpoint: `AdapterConfigController.testConnection()` → resolves adapter, sends canned test payload

## 7. Test Impact Analysis

### Existing tests affected by this change:

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| N/A — greenfield adapters | — | — | — | — |

### Test modification policy:

- [x] No existing tests should be modified (greenfield)

### Potential files impacted by implementation

| File | Change |
|------|--------|
| New: `notification/adapter/MattermostNotificationAdapter.java` | Full implementation |
| New: `notification/adapter/SlackNotificationAdapter.java` | Full implementation |
| New: `notification/adapter/EmailNotificationAdapter.java` | Full implementation |
| New: `templates/notification-alert.html` | Email template |
| `adapter-config/controller/AdapterConfigController.java` | Add test endpoint |
| `notification/adapter/NotificationAdapter.java` | Possibly extend send signature |
