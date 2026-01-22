# Epic: Event Timeline & Visualization

**Context**: Users need to view their events in a real-time or near-real-time timeline. This is the core UI experience.

## Backlog Items

- [ ] **monitoring dashboard creation**:
- There should be drag and drop functionality to configure dashboards.
- right side panel with available channels to add to the dashboard with search functionality. (just like channel page)
- left side a canvas where channels can be dropped to create a dashboard.
- the channels keep the same order as they were added.
- channels can be reordered via drag and drop on the canvas.
- dashboards can be named and is unique per user / organisation.
- dashboards are saved automatically when changes are made.
- dashboards can be deleted.

- [ ] **timeline monitoring**:
- there should be a monitoring page with a selector for dashboards.
- a dashboard contains a list of channels to monitor.
- each channel has a timeline preview represented as a line with colors for event severities over time.
- left of the timeline is the channel name with underneath the name a status icon (active / paused) and the last event severity.
- paused channels are greyed out and do not show any events in the timeline.
- clicking on a channel in the dashboard opens up a channel modal with the details of that channel.
- clicking on a duration within the channel timeline shows the events that happened in that duration for that channel. not an extra modal but expands the channel card to show the events.
- the dashboard also has a timeline which is the combined timeline of all channels in the dashboard.
- On the monitoring page there should be date range selector.
- there should be 2 types for dates: Live data options (Last 24h, Last 7d, last month) and fixed date range (calendar picker).
- the live data options refresh automatically every minute.
- timelines are sorted with highest last happend severity events at the top.
- there should be a filter option button to turn off the sorting and show the channels in the order they were added to the dashboard.

---

# Epic: Retention & Data Lifecycle

**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

## Backlog Items

- [ ] **Retention Policy Configuration**:
    - See "User/Organization Retention Settings UI" under Event Channels epic for UI implementation
    - DB columns already exist with CHECK constraints (90-1825 days)
    - This item covers backend service logic for applying retention during cleanup

- [x] **Retention Cleanup Job**: → REFINED (see `EVENT-retention-cleanup-job.md`)

- [ ] **Global Retention Settings (Admin)**:
    - Admin can set system-wide default retention
    - Admin can set maximum retention (users can't exceed)
    - Admin can view storage usage stats

- [ ] **Retention Warning Notifications**:
    - Notify users before significant data deletion (e.g., if reducing retention)
    - Show "data will be deleted in X days" warning in UI

---

# Epic: Admin Global Oversight

**Context**: Platform administrators need visibility into all API keys, channels, and events across the system for support, security, and compliance.

## Backlog Items

- [ ] **Admin Events/Usage Dashboard**:
    - High-level stats: total events today/week/month, events by severity
    - Top channels by volume
    - Users/orgs approaching or exceeding quotas
    - Storage usage trends

- [ ] **Admin Audit Log**:
    - Track admin actions: key revocations, channel archives, user impersonation
    - Searchable log with: action, target, admin user, timestamp
  
---

# Epic: Future Considerations (Not for immediate development)

These are ideas to keep in mind for architecture decisions but not to implement now:

- [ ] **Completely refactor landing page** - make it more user friendly, modern, and informative.
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

---
