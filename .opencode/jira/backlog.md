# Epic: Event Ingestion

**Context**: The core functionality - receiving and storing events via API. Events are immutable log entries with metadata, severity, and payload. This should be optimized for high write throughput. high availability, high scalability. just imagine external systems sending a couple of events per second or more.

## Backlog Items

- [ ] **Event Entity & Database Schema**:
    - New `event` table with: id (UUID recommended for distributed systems), channel_id, severity (INFO, WARN, ERROR, DEBUG, CRITICAL), title, message/data (TEXT or JSONB), metadata (JSONB - for custom key-value pairs), source/application (optional string), received_at, client_timestamp (optional - what the sender says the time is)
    - TimescaleDB hypertable for time-series optimization
    - default retention policy applied at DB level (e.g., drop chunks older than 5 years)
    - think about timescale optimizations (compression, chunk time interval, ...)
    - Indexes: channel_id + received_at (for timeline queries), severity, full-text on message
    - Partitioning consideration: by received_at for efficient retention cleanup
    - Really think about write performance and storage efficiency and discuss story thoroughly.

- [ ] **Event Ingestion API Endpoint**:
    - `POST /v1/events` with body `{ channelId, severity, title, data, metadata, timestamp }`
    - Authentication: API key only (not JWT - this is for programmatic use)
    - Channel access validation: API key scope must match channel scope
    - Response: 201 with event ID, or 202 if async processing
    - Validation: channel exists, API key has access, payload size limits
    - must scale to high throughput, consider event bus (e.g., RabbitMQ) for decoupling ingestion from storage

- [ ] **Event Ingestion Quotas & Usage Tracking**:
    - New `usage_quota` table: user_id, organization_id, period (DAILY/MONTHLY), event_count, limit, period_start
    - Background job to reset quotas at period boundaries
    - User can view their current usage vs limit in dashboard
    - Warning notifications when approaching limit (80%, 100%)

---

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

- [ ] **Retention Cleanup Job**:
    - Scheduled background job (daily or hourly)
    - Delete events where `received_at < NOW() - owner's retention_days`
    - Look up retention from user (personal channel) or organization (org channel)
    - Batch deletion to avoid locking issues
    - Logging/metrics: how many events deleted per run

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
- [ ] **Batch Event Ingestion**: Useful for log aggregators sending in batches, Max batch size limit (e.g., 100 events)
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
