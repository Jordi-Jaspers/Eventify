# Backlog

Raw ideas and future work. Items here need refinement before development.

---

## Epic: Bugs & Technical Debt
**Context**: Ongoing maintenance, bug fixes, and technical debt cleanup.

---

## Epic: Organization Management
**Context**: Admins need better tools to manage organizations, especially around trial limitations and status

- [ ] **Update Organization Status** - Admin can update org status (TRIAL, ACTIVE, SUSPENDED). Suspended orgs hidden from members (403). Admin edit sheet with status dropdown. Endpoint: `PATCH /api/v1/admin/organizations/{orgId}/status`.
- [ ] **TRIAL account limitations** - Limit users, event quota, and API keys for organizations in TRIAL status. Enforce limits in backend, show upgrade prompts in UI.
- [ ] **Organization status change audit log** - Track when admin changes org status, with reason field. Part of broader admin audit log feature.
- [ ] **Organization status change notifications** - Notify org owner when their organization is suspended or reactivated.
- [ ] **Refactor user/org dashboards to something useful**
- [ ] **Configurable Event Quotas** - Allow users to configure their monthly event limit (tied to subscription tier). organisations have no limitations.

---

## Epic: Channel Management
**Context**: Channel owners and admins need efficient tools to manage channels at scale.

- [ ] **Mass Channel Actions** - Select multiple channels and perform bulk actions (delete, pause, resume) with confirmation dialogs and partial failure handling. Bulk API endpoints + selection UI with toolbar.

---

## Epic: Watchlist Composition
**Context**: Templates and additional views to make watchlists more reusable and informative. Templates are a new first-class concept distinct from inline groups: live-linked, editable, propagating across all consumers.

- [ ] **Reusable Channel/Group Templates** - New entity separate from inline groups. Templates contain channels, groups, or groups-with-channels (no nesting templates inside templates). Inline groups CAN reference templates. Personal scope (user-owned, used in personal watchlists with personal channels) and org scope (org-shared, used in org watchlists with org channels). Edits propagate to every watchlist using the template — show confirmation modal warning ("dangerous operation: used in N watchlists"). Org template edits restricted to org owner/admin. Channel deletion cascades: removed channels are auto-removed from referencing templates. Template detail page shows usage count + list of consuming watchlists.
- [ ] **Save Group as Template** - Action on a watchlist group in the editor. Modal: name + scope (personal/org) + propagation warning. On confirm: create template from group's current channels, replace inline group with template reference in the watchlist. Subsequent edits to that group go through the template editor and propagate.
- [ ] **Event Feed View** - New tab on the watchlist monitor page showing chronological raw event list across all channels in the current watchlist (NOT org-wide, NOT all channels). Sibling to the existing severity timeline view. Reuses the event detail rendering from `DurationDetailsModal`.

---

## Epic: Notification System
**Context**: Modular notification subsystem built from scratch. No backend notification entity exists today; "What's New" is purely a frontend changelog reader. Replaces the deprecated "Webhooks/Notifications" and "Integrations: Slack/Discord/PagerDuty" items in Future Considerations.

**Subscription model:** subscriptions are at the WATCHLIST level (not per-channel). When any channel in a subscribed watchlist transitions severity, the subscription evaluates. Subscriptions are global per user — fire regardless of which org context the user is currently viewing.

**Trigger model:** severity *transitions* only (NOT per-event firing). Configurable target severities — user picks which transitions trigger ("notify on transition to CRITICAL" or "notify on transition to WARNING or CRITICAL").

**Adapter pattern:** `NotificationAdapter` interface with pluggable destinations. MVP destinations: in-app + Telegram. Email and Slack/Discord/webhooks come later via the same abstraction.

- [ ] **NOTIF-01: Dispatch Core + Adapter Pattern + In-App Adapter** - `NotificationAdapter` interface, dispatch service, `Notification` entity (audit trail of what was sent), in-app adapter writing to existing notification bell surface. No subscriptions yet — internal dispatch only as a vertical slice foundation.
- [ ] **NOTIF-02: Watchlist-Level User Subscriptions** - `Subscription` entity: user + watchlist + target severities (configurable set). Subscribe/unsubscribe UI on watchlist pages. Hook into event ingestion: severity transition on any channel in a subscribed watchlist enqueues dispatch. Delivers via in-app adapter only at this stage. Severity transitions fire once per transition, not per-event.
- [ ] **NOTIF-03: Telegram Adapter + Personal Telegram Linking** - `TelegramAdapter` implementation. User settings page for linking via bot `/link <code>` flow associating Telegram chat ID with user. Per-subscription destination preferences (which adapters fire for this subscription). Subscriptions can fan out to multiple destinations.
- [ ] **NOTIF-04: Org Shared Telegram Destinations + Routing Rule** - Org settings page for adding shared Telegram chats (group chats: bot added to chat, `/link <orgcode>`). Org-level routing rule entity: "send transitions to CRITICAL on any org watchlist to this shared destination." Start with a single global org rule; per-watchlist or per-channel rules deferred. Acknowledged: a user with personal subscription on watchlist X plus org rule routing X to shared Telegram = both fire (different audiences, intentional).
- [ ] **NOTIF-05 (deferred, post-MVP): Email Adapter** - Plug into existing email infra. Throttling/digest logic to prevent email floods. Deferred until email infra is more mature.
- [ ] **NOTIF-06 (deferred, post-MVP): Channel Rhythm Detection + Overdue Alerts** - Statistical (no LLM): period detection on inter-arrival times via FFT or simple periodicity over `event_timeline_hourly`. New trigger type `CHANNEL_OVERDUE` fires when expected next event is late by configurable margin. Severity drift detection (CRITICAL ratio anomaly vs baseline) as additional trigger type. All evaluated through existing dispatch path.

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
- [ ] **Monthly Quota Analytics Tracking** - Track monthly event counts for both personal users and organizations separately for analytics and reporting purposes. Even though organizations have no limits, we want visibility into usage patterns. Requires new database table or extending existing quota tracking.

---

## Epic: Audit System
**Context**: Cross-cutting audit trail for security, compliance, and debugging. Identified during Channel Management refinement.

**NOTE** something like Axiom's frontend logging / pocketbase monitoring to capture user interactions for audits? This should be something that can be monitored in the application as an admin.

- [ ] **Audit infrastructure** - Create audit log table, service, and base patterns for tracking user actions across the platform.
- [ ] **Bulk action audit trail** - Record bulk operations (channel deletes, etc.) with user, action, targets, timestamp.
- [ ] **Admin action audit** - Track admin-specific actions: status changes, user management, system configuration.
- [ ] **Audit log UI** - Admin interface to search and view audit trail.

---
## Epic: Developer API Documentation
**Context**: Developers integrating with Eventify need comprehensive documentation to understand the API and get started quickly.

- [ ] **Getting Started Guide** - Step-by-step guide: create API key, create channel, send first event. Interactive examples with copy-paste commands.
- [ ] **API Reference Page** - Full endpoint documentation: Events (single, batch), Channels (CRUD). Request/response schemas, authentication headers, error codes.
- [ ] **Code Examples** - cURL, Python, Node.js, Go examples for common operations. Copyable snippets with syntax highlighting.
- [ ] **Authentication Guide** - API key types (personal vs org), header format, security best practices, key rotation.
- [ ] **Rate Limits & Quotas** - Document rate limits, quota system, error responses (429), best practices for high-volume senders.
- [ ] **SDKs (future)** - Official client libraries for popular languages. Auto-generated from OpenAPI spec.
- [ ] **Changelog/Versioning** - API versioning strategy, deprecation policy, changelog for breaking changes.

---
## Epic: Future Considerations
**Context**: Ideas to keep in mind for architecture decisions but not for immediate development.

- [ ] **Growthbook** - https://www.growthbook.io/ for feature flagging and A/B testing. Could be useful for gradual rollouts and testing new features.
- [ ] **Admin User can create dashboards from every org channel** - Admins can create dashboards that pull in data from any channel across the organization, even if they are not the channel owner. This allows for cross-channel monitoring and insights.
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
- [ ] **Stripe Integration**: manage subscriptions/payments
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Pricing/Upgrade page for authenticated users** - Logged-in users should be able to view pricing and upgrade their plan from within the app (e.g., sidebar link or settings page).
- [ ] **Add TTL-based Caching Infrastructure**: Add `@Cacheable` annotations or something similar for frequently accessed data like dashboard calls.
- [ ] **CSRF protection for cookie-based auth** - Currently disabled. Auth cookies use `SameSite=Lax` which already blocks cross-origin state-changing requests in modern browsers, so CSRF is defense-in-depth rather than a real gap. Consider enabling when pursuing SOC2 / ISO 27001 / enterprise sales. Scope: enable Spring Security CSRF with `CookieCsrfTokenRepository`, exclude `/v1/external/**` (API-key endpoints), update every SPA fetch to echo `X-CSRF-Token`, update every integration test. ~1–2 days.
- [ ] **Refresh token theft detection (token reuse → family revocation)** - When a previously-rotated refresh token is presented again, treat as a theft signal and revoke the entire token family for that user. RFC 6819 §5.2.2.3 / OAuth 2.0 Security BCP §4.13.2. Standard at Auth0, Okta, Cognito, Clerk. Pre-req: AUTH-04 (introduces `family_id` column on `token`). Implementation needs: `revoked_at TIMESTAMPTZ NULL` column, preserve old rows on rotation (delete-on-presentation only), grace-period logic to absorb network races (just-rotated token valid for ~30s after rotation), daily cleanup job for revoked rows past max session lifetime. Deferred from AUTH-04 because pre-MVP has no concrete threat and false-positive race conditions add behavioural complexity.
- [ ] **Company Login SSO / SAML** - EntraID authentication, configuring IdP during org creation. User not searchable by regular users / org. Requires SAML library, org-level IdP config, JIT provisioning, admin setup UI. (XL — consider as sub-epic)

