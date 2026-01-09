# Epic: Event Channels (formerly "Checks")

**Context**: A "Channel" is a named destination for events. Think of it like a topic or stream. Users can have personal channels, and organizations can have shared channels. Events are sent to a specific channel via API.

## Naming Considerations
- Alternatives considered: "Check", "Stream", "Timeline", "Feed", "Topic", "Log"
- **Recommendation**: "Channel" - familiar (Slack channels), implies a stream of data, enterprise-friendly
- Other viable options: "Stream" (AWS Kinesis-like), "Feed" (activity feed), "Log" (developer-friendly)

## Backlog Items

- [ ] **Channel Entity & Database Schema**:
    - New `channel` table with: id, name, slug, description, scope (USER or ORGANIZATION), owner_id (user), organization_id (nullable), status (ACTIVE, PAUSED, ARCHIVED), retention_days (default 30), created_at, created_by, updated_at, archived_at
    - Unique constraint on (scope, owner_id, slug) for personal channels
    - Unique constraint on (scope, organization_id, slug) for org channels

- [ ] **User Channel CRUD**:
    - User can create a personal channel (name, optional description)
    - User can list their personal channels
    - User can update channel name/description
    - User can archive a channel (soft delete, events retained until retention expires)
    - User can pause a channel (stops accepting new events, useful for debugging)

- [ ] **Organization Channel CRUD**:
    - Org OWNER/ADMIN can create organization channels
    - Org members can view organization channels (based on role permissions TBD)
    - Org OWNER/ADMIN can update/archive/pause channels
    - Shared visibility across all org members

- [ ] **Channel Access via API Key**:
    - API request to send event must specify channel (by slug or id)
    - Personal API key -> can only send to user's personal channels
    - Organization API key -> can only send to that org's channels
    - Validation: 403 if key scope doesn't match channel scope

- [ ] **Admin: Global Channel Overview**:
    - Admin can view all channels across the platform
    - Filter by: user, organization, status, created date
    - Admin can archive any channel (with reason)
    - Stats: total channels, events per channel, most active channels

---

# Epic: Event Ingestion

**Context**: The core functionality - receiving and storing events via API. Events are immutable log entries with metadata, severity, and payload.

## Backlog Items

- [ ] **Event Entity & Database Schema**:
    - New `event` table with: id (UUID recommended for distributed systems), channel_id, severity (INFO, WARN, ERROR, DEBUG, CRITICAL), title, message/data (TEXT or JSONB), metadata (JSONB - for custom key-value pairs), source/application (optional string), received_at, client_timestamp (optional - what the sender says the time is)
    - Indexes: channel_id + received_at (for timeline queries), severity, full-text on message
    - Partitioning consideration: by received_at for efficient retention cleanup

- [ ] **Event Ingestion API Endpoint**:
    - `POST /v1/events` or `POST /v1/channels/{slug}/events`
    - Request body: { channel: "my-channel", severity: "INFO", title: "...", data: "...", metadata: {...}, timestamp: "..." }
    - Authentication: API key only (not JWT - this is for programmatic use)
    - Response: 201 with event ID, or 202 if async processing
    - Validation: channel exists, API key has access, payload size limits

- [ ] **Batch Event Ingestion**:
    - `POST /v1/events/batch` - accept array of events
    - Useful for log aggregators sending in batches
    - Partial success handling: return which events succeeded/failed
    - Max batch size limit (e.g., 100 events)

- [ ] **Event Ingestion Rate Limiting**:
    - Personal accounts: X events per day/month (configurable limit)
    - Organization accounts: unlimited (or very high limit)
    - Track usage per API key and per user/org
    - Return 429 with Retry-After header when limit exceeded
    - Consider: burst limits vs sustained rate limits

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
    - New page: `/dashboard/channels/{slug}` or `/organizations/{orgId}/channels/{slug}`
    - Display events in reverse chronological order (newest first)
    - Each event shows: severity badge, title, timestamp, collapsible details
    - Auto-refresh / real-time updates (polling initially, WebSocket later)
    - Infinite scroll or pagination for older events

- [ ] **Channel Timeline API Endpoint**:
    - `GET /v1/channels/{slug}/events` - list events for a channel
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
    - Quick action buttons: view timeline, pause, archive
    - "Create Channel" button

- [ ] **Organization Dashboard - Channels Overview**:
    - Similar to personal dashboard but for org channels
    - Show on organization dashboard page
    - Role-based visibility of management actions

- [ ] **Channel Creation Modal/Page**:
    - Form: name (generates slug), description, retention period (dropdown: 7d, 30d, 90d, 1yr)
    - Preview of API endpoint and example curl command
    - Show API key selector (which key to use in example)

- [ ] **Channel Settings Page**:
    - Edit name, description
    - Change retention period (warning if reducing - data will be deleted)
    - Pause/Resume channel toggle
    - Archive channel (with confirmation)
    - Danger zone: permanently delete channel and all events

---

# Epic: Retention & Data Lifecycle

**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

## Backlog Items

- [ ] **Retention Policy Configuration**:
    - Each channel has a `retention_days` setting
    - Default: 30 days (configurable at system level)
    - Options: 3 months min - 5 years max
    - Organization channels may have different defaults

- [ ] **Retention Cleanup Job**:
    - Scheduled background job (daily or hourly)
    - Delete events where `received_at < NOW() - retention_days`
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

- [ ] **Admin Channels Dashboard**:
    - List all channels across all users and orgs
    - Columns: name, owner, scope, status, event count, created date
    - Actions: archive channel, view timeline
    - Filters: scope, status, owner type

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

- [ ] **Company Login SSO / SAML** - enterprise authentication, user not searchable by regular users / org.
- [ ] **Long-Lived Refresh Tokens / Remember me**
- [ ] **Multi-Token Support / Management up to 5**
- [ ] **Token Revocation - single/all**
- [ ] **OAuth2 Enhancements - account linking**
- [ ] **Stripe Integration - manage subscriptions/payments**
- [ ] **Webhooks/Notifications**: Alert users when specific events occur (ERROR severity, keyword match)
- [ ] **Real-time Updates**: WebSocket or SSE for live timeline updates
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Integrations**: Slack, Discord, PagerDuty notifications
- [ ] **Log Forwarding**: Splunk/Datadog/CloudWatch style log driver compatibility
- [ ] **API Key Scopes**: Fine-grained permissions (read-only keys, write-only keys)
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup


---
