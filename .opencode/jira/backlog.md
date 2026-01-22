# Epic: Imporovements

```
  insert into event (check_id, created, "timestamp", check_result, message)
  SELECT ((random() * 500) + 4000)::int, stamp,
         stamp,
         (ARRAY['HARD_OK', 'HARD_OK', 'HARD_OK', 'HARD_OK', 'HARD_OK', 'HARD_OK', 'SOFT_OK', 'HARD_UNKNOWN', 'SOFT_UNKNOWN',
          'HARD_WARNING', 'SOFT_WARNING', 'HARD_CRITICAL', 'SOFT_CRITICAL' ])[round(random() * 12) + 1] as "check_result",
 'Test Event ' || stamp                                                                                                                                                                                      as "message"
  FROM generate_series((NOW() - interval '5 month'):: timestamp, (NOW() - interval '1 month'):: timestamp, '5 SECOND':: interval) as stamp
  ON CONFLICT DO NOTHING;
```



# Epic: Event Timeline & Visualization

**Context**: Users need to view their events in a real-time or near-real-time timeline. This is the core UI experience.

## Backlog Items

- [ ] **Channel Timeline View (Frontend)**:
    - New page: `/dashboard/channels/{id}` or `/organizations/{orgId}/channels/{id}`
    - Display events in reverse chronological order (newest first)
    - Each event shows: severity badge, title, timestamp, collapsible details
    - Auto-refresh / real-time updates (polling initially, WebSocket later)
    - Infinite scroll or pagination for older events

- [ ] **Channel Timeline API Endpoint**:
    - `GET /v1/channels/{id}/events` - list events for a channel
    - Query params: limit, before (cursor), after (cursor), severity[], search, from_date, to_date
    - Response: paginated list with cursor for infinite scroll
    - Authorization: user must own channel or be member of org that owns it

- [ ] **Event Detail View**:
    - Expandable event card or slide-over panel
    - Show full message/data payload (formatted JSON if applicable)
    - Show all metadata key-value pairs
    - Copy event ID, copy payload buttons

- [ ] **Event Filtering & Search**:
    - Filter by severity (multi-select)
    - Filter by date range
    - Full-text search on title and message
    - Filter by source/application (if provided)
    - Save filter presets (future enhancement)

- [ ] **Timeline Aggregations & Stats**:
    - Show event count by severity for current view
    - Mini chart showing event volume over time (last 24h, 7d, 30d)
    - Helpful for spotting anomalies (spike in errors)

- [ ] **Channel Health Status from Event Series**:
    - Derive channel "health status" (OK, WARNING, CRITICAL, UNINITIALIZED) from the most recent event
    - Show at-a-glance health indicators on channel cards in dashboard
    - Consider duration tracking (how long has a channel been in CRITICAL state?)

---

# Epic: Dashboard & Channel Management UI

**Context**: Users need a home for managing their channels, viewing aggregate stats, and quick access to recent activity.

## Backlog Items

- [ ] **Personal Dashboard - Channels Overview**:
    - New section on user dashboard: "My Channels"
    - List all personal channels with: name, status, event count (last 24h), last event time
    - Quick action buttons: view timeline, pause, delete
    - "Create Channel" button

- [ ] **Organization Dashboard - Channels Overview**:
    - Similar to personal dashboard but for org channels
    - Show on organization dashboard page
    - Role-based visibility of management actions

- [ ] **Channel Creation Modal/Page**:
    - Form: name, description
    - Preview of API endpoint and example curl command
    - Show API key selector (which key to use in example)

- [ ] **Channel Settings Page**:
    - Edit name, description
    - Pause/Resume channel toggle
    - Danger zone: permanently delete channel and all events
    - Note: Per-channel retention override is in Future Considerations

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
