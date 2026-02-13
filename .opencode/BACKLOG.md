# Backlog

Raw ideas and future work. Items here need refinement before development.

---

## Epic: Bugs & Technical Debt
**Context**: Ongoing maintenance, bug fixes, and technical debt cleanup.

- [ ] **not loading correct filter values in timeline** - Fix bug when on the edit watchlist page and you clicked on default filters, then went back to timeline via "monitor" button, the filters would be wrong.
- [ ] **Fix monthly quota** - organisations should have no limits, but currently they do. Need to fix the logic. got complaint on TST environment.

---
## Epic: General Improvements
**Context**: Small improvements that don't fit into other epics but would enhance user experience.

- [ ] **Update Favicon** - Depending on local, TST, PRD the favicon should have a banner with the environment name to avoid confusion when users have multiple tabs open. This is especially important for support and debugging.
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Changelog** - Show modal on login after a new release with highlights of new features and improvements. or have a changelog page linked from the sidebar. should be discussed what is more professional and user-friendly way to show updates.

---

## Epic: Channel Management
**Context**: Users need better ways to manage their channels.

- [ ] **Change Channel ids to uuid** - Consider switching from numeric IDs to UUIDs for channels for better security and scalability. Requires DB schema change and API updates.
- [ ] **Copy channel id action in table** - Add channel ID column to events table for easier debugging and support.
- [ ] **Create channel via API** - Allow users to create channels programmatically using API keys, enabling automation and infrastructure-as-code setups.
- [ ] **Channel archiving | Deleting via API** - Consider deleting or arching channels via API. Archiving would keep data but hide from UI, deleting would remove all data.
- [ ] **Flag channels as "Stale"** - If a channel hasn't received events in X days, mark it as "Stale" in the UI to help users identify unused channels. Optionally send a notification to channel owner after Y days of inactivity. should also be able to filter on that in the channels table.
- [ ] **Mass channel actions** - Allow users to select multiple channels in the table and perform bulk actions like delete, pause, unpause. add select all checkbox in header, and bulk action buttons that appear when channels are selected.

---
## Epic: Organization Management
**Context**: Admins need better tools to manage organizations, especially around trial limitations and status

- [ ] **TRIAL account limitations** - Limit users, event quota, and API keys for organizations in TRIAL status. Enforce limits in backend, show upgrade prompts in UI.
- [ ] **Organization status change audit log** - Track when admin changes org status, with reason field. Part of broader admin audit log feature.
- [ ] **Organization status change notifications** - Notify org owner when their organization is suspended or reactivated.
- [ ] **Refactor user/org dashboards to something useful**
- [ ] **Configurable Event Quotas** - Allow users to configure their monthly event limit (tied to subscription tier). organisation have no limitations.

---

## Epic: Retention & Data Lifecycle
**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

- [ ] **Retention Policy Configuration**: See "User/Organization Retention Settings UI" under Event Channels epic for UI implementation. DB columns already exist with CHECK constraints (90-1825 days). This item covers backend service logic for applying retention during cleanup.
- [ ] **Global Retention Settings (Admin)**: Admin can set system-wide default retention, maximum retention (users can't exceed), view storage usage stats.

---

## Epic: Admin Global Oversight
**Context**: Platform administrators need visibility into all API keys, channels, and events across the system for support, security, and compliance.

- [ ] **Admin Events/Usage Dashboard**: High-level stats: total events today/week/month, events by severity. Top channels by volume. Users/orgs approaching or exceeding quotas. Storage usage trends.
- [ ] **Admin Audit Log**: Track admin actions: key revocations, channel archives, user impersonation. Searchable log with: action, target, admin user, timestamp.

---

## Epic: Infrastructure & Performance

**Context**: Cross-cutting concerns for caching, performance optimization, and infrastructure improvements.

- [ ] **Add TTL-based Caching Infrastructure**: Add Caffeine cache with `@EnableCaching` support. Required for caching dashboard stats (~30s TTL), and other frequently-accessed, slowly-changing data. Currently project only uses request-scoped caches. Dependencies needed: `com.github.ben-manes.caffeine:caffeine`. Create `CacheConfig.java` with cache definitions. Priority endpoints: dashboard stats, user quotas, org membership checks.

---

## Epic: OAUTH2 Authentication
**Context**: Support third-party authentication via OAUTH2 providers (Google, GitHub, etc.) in addition to existing JWT and API Key methods.

- [ ] **Review current authentication mechanisms** - Evaluate if current JWT-based auth meets all needs or if we should add OAuth2 for better SSO support.
- [ ] **Company Login SSO / SAML** - enterprise authentication, user not searchable by regular users / org.
- [ ] **Long-Lived Refresh Tokens / Remember me**
- [ ] **Multi-Token Support / Management up to 5**
- [ ] **Token Revocation - single/all**
- [ ] **OAuth2 Enhancements - account linking**

---

## Epic: Future Considerations

Ideas to keep in mind for architecture decisions but not for immediate development.

- [ ] **Webhooks/Notifications**: Alert users when specific events occur (ERROR severity, keyword match)
- [ ] **Real-time Updates**: WebSocket or SSE for live timeline updates
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Integrations**: Slack, Discord, PagerDuty notifications
- [ ] **Log Forwarding**: Splunk/Datadog/CloudWatch style log driver compatibility
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
- [ ] **Stripe Integration**: manage subscriptions/payments
