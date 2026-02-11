# Backlog

Raw ideas and future work. Items here need refinement before development.

---
## Epic: Bugs & Technical Debt

**Context**: Address known bugs and technical debt to improve system stability and maintainability.

### Items
- [ ] **Unsecured channel endpoints**: There is no check in the controller to verify if the user owns the channel. This could allow unauthorized access if someone guesses a channel ID. Add ownership check in controller methods.

- [ ] **No security tests on the `/search` endpoint**: The search endpoint lacks tests to verify that users cannot access items they do not own. Add security tests to ensure proper access control.

- [ ] **Duration Details API Endpoint**: Create a new REST endpoint to retrieve the full duration details for a channel. Currently, the modal uses the clamped timeline data from the monitor page (e.g., a 2-hour duration is clamped to the 1-hour visible window). The new endpoint should return: (1) The selected duration with its ACTUAL start/end times from the database, (2) Adjacent durations (previous/next) with their actual times. This will allow the frontend MiniTimeline to show accurate time ranges even when durations extend beyond the monitor's visible window. Related: Events are now correctly fetched using the duration's actual times, but the timeline visualization still uses clamped data. 
---

## Epic: Retention & Data Lifecycle

**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

### Items

- [ ] **Retention Policy Configuration**: See "User/Organization Retention Settings UI" under Event Channels epic for UI implementation. DB columns already exist with CHECK constraints (90-1825 days). This item covers backend service logic for applying retention during cleanup.
- [ ] **Global Retention Settings (Admin)**: Admin can set system-wide default retention, maximum retention (users can't exceed), view storage usage stats.
- [ ] **Retention Warning Notifications**: Notify users before significant data deletion (e.g., if reducing retention). Show "data will be deleted in X days" warning in UI.

---

## Epic: Admin Global Oversight

**Context**: Platform administrators need visibility into all API keys, channels, and events across the system for support, security, and compliance.

### Items

- [ ] **Admin Events/Usage Dashboard**: High-level stats: total events today/week/month, events by severity. Top channels by volume. Users/orgs approaching or exceeding quotas. Storage usage trends.
- [ ] **Admin Audit Log**: Track admin actions: key revocations, channel archives, user impersonation. Searchable log with: action, target, admin user, timestamp.

---

## Epic: Dashboard Enhancements

**Context**: The user/org dashboards are currently basic. Adding visual stats and insights will make the app feel more professional and provide immediate value to users.

### Items

- [ ] **Dashboard Stats Cards**: Add 3-4 stat cards at the top of the user dashboard showing key metrics at a glance: Events Today (count), Active Channels (count), Error Rate (percentage, last 24h), Last Event (relative time). Cards should use the glassmorphism style from the design system. Error Rate card should change color based on threshold (green <1%, amber 1-5%, red >5%). Responsive layout: 4 columns on desktop, 2x2 on tablet, stacked on mobile. Create a reusable `StatCard` component. Backend needs a new endpoint `GET /api/dashboard/stats` that aggregates these metrics. *Note: Good candidate for live demo - visual impact, full-stack, ~25 min.*

---

## Epic: Future Considerations

Ideas to keep in mind for architecture decisions but not for immediate development.

### Items

- [ ] **Admin update org status**
- [ ] **Update favicon with logo**
- [ ] **Refactor user/org dashboards to something useful**
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Company Login SSO / SAML** - enterprise authentication, user not searchable by regular users / org.
- [ ] **Long-Lived Refresh Tokens / Remember me**
- [ ] **Multi-Token Support / Management up to 5**
- [ ] **Token Revocation - single/all**
- [ ] **OAuth2 Enhancements - account linking**
- [ ] **Stripe Integration - manage subscriptions/payments**
- [ ] **Configurable Event Quotas** - Allow users/orgs to configure their monthly event limit (tied to subscription tier). Default 1000, configurable up to 1M for enterprise.
- [ ] **Webhooks/Notifications**: Alert users when specific events occur (ERROR severity, keyword match)
- [ ] **Real-time Updates**: WebSocket or SSE for live timeline updates
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Integrations**: Slack, Discord, PagerDuty notifications
- [ ] **Log Forwarding**: Splunk/Datadog/CloudWatch style log driver compatibility
- [ ] **API Key Scopes**: Fine-grained permissions (read-only keys, write-only keys)
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
- [ ] **Per-Channel Retention Override**: Allow channels to override user/org retention_days setting. Add optional `retention_days` column to channel table. UI: Channel settings page with retention slider. Falls back to user/org setting if not specified.
