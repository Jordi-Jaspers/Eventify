# Backlog

Raw ideas and future work. Items here need refinement before development.

---

## Epic: Bugs & Technical Debt
**Context**: Ongoing maintenance, bug fixes, and technical debt cleanup.

- [ ] **Hovering configure button in watchlist has bad contrast** - Fix CSS to ensure the button is visible on hover in all themes.
- [ ] **The stat cards may be redesigned**
- [ ] **Slow admin dashboard load times** - Investigate and optimize backend queries, consider adding caching for frequently accessed data, split calls maybe.

---

## Epic: Organization Management
**Context**: Admins need better tools to manage organizations, especially around trial limitations and status

- [ ] **Organization status change audit log** - Track when admin changes org status, with reason field. Part of broader admin audit log feature.
- [ ] **Refactor user/org dashboards to something useful**

---

## Epic: Watchlist Composition
**Context**: Templates and additional views to make watchlists more reusable and informative. Templates are a new first-class concept distinct from inline groups: live-linked, editable, propagating across all consumers.

- [ ] **Reusable Channel/Group Templates** - New entity separate from inline groups. Templates contain channels, groups, or groups-with-channels (no nesting templates inside templates). Inline groups CAN reference templates. Personal scope (user-owned, used in personal watchlists with personal channels) and org scope (org-shared, used in org watchlists with org channels). Edits propagate to every watchlist using the template — show confirmation modal warning ("dangerous operation: used in N watchlists"). Org template edits restricted to org owner/admin. Channel deletion cascades: removed channels are auto-removed from referencing templates. Template detail page shows usage count + list of consuming watchlists.
- [ ] **Save Group as Template** - Action on a watchlist group in the editor. Modal: name + scope (personal/org) + propagation warning. On confirm: create template from group's current channels, replace inline group with template reference in the watchlist. Subsequent edits to that group go through the template editor and propagate.


---

## Epic: Notification System
**Context**: Modular notification subsystem built from scratch. No backend notification entity exists today; "What's New" is purely a frontend changelog reader. Replaces the deprecated "Webhooks/Notifications" and "Integrations: Slack/Discord/PagerDuty" items in Future Considerations.

**Subscription model:** subscriptions are at the WATCHLIST level (not per-channel). When any channel in a subscribed watchlist transitions severity, the subscription evaluates. Subscriptions are global per user — fire regardless of which org context the user is currently viewing.

**Trigger model:** severity *transitions* only (NOT per-event firing). Configurable target severities — user picks which transitions trigger ("notify on transition to CRITICAL" or "notify on transition to WARNING or CRITICAL").

**Adapter pattern:** `NotificationAdapter` interface with pluggable destinations. MVP destinations: in-app + Telegram. Email and Slack/Discord/webhooks come later via the same abstraction.

- [ ] **NOTIF-07: Telegram Adapter + Personal Telegram Linking** - `TelegramAdapter` implementation. User settings page for linking via bot `/link <code>` flow associating Telegram chat ID with user. Per-subscription destination preferences (which adapters fire for this subscription). Subscriptions can fan out to multiple destinations.
- [ ] **NOTIF-08: Org Shared Telegram Destinations + Routing Rule** - Org settings page for adding shared Telegram chats (group chats: bot added to chat, `/link <orgcode>`). Org-level routing rule entity: "send transitions to CRITICAL on any org watchlist to this shared destination." Start with a single global org rule; per-watchlist or per-channel rules deferred. Acknowledged: a user with personal subscription on watchlist X plus org rule routing X to shared Telegram = both fire (different audiences, intentional).
- [ ] **NOTIF-09 (deferred, post-MVP): Email Adapter** - Plug into existing email infra. Throttling/digest logic to prevent email floods. Deferred until email infra is more mature.
- [ ] **NOTIF-10 (deferred, post-MVP): Channel Rhythm Detection + Overdue Alerts** - Statistical (no LLM): period detection on inter-arrival times via FFT or simple periodicity over `event_timeline_hourly`. New trigger type `CHANNEL_OVERDUE` fires when expected next event is late by configurable margin. Severity drift detection (CRITICAL ratio anomaly vs baseline) as additional trigger type. All evaluated through existing dispatch path.

---

## Epic: Retention & Data Lifecycle
**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

- [ ] **Retention Policy Configuration**: See "User/Organization Retention Settings UI" under Event Channels epic for UI implementation. DB columns already exist with CHECK constraints (90-1825 days). This item covers backend service logic for applying retention during cleanup.
- [ ] **Global Retention Settings (Admin)**: Admin can set system-wide default retention, maximum retention (users can't exceed), view storage usage stats.

---

## Epic: Admin Global Oversight
**Context**: Platform administrators need visibility into all API keys, channels, and events across the system for support, security, and compliance.

- [ ] **Admin Audit Log**: Track admin actions: key revocations, channel archives, user impersonation. Searchable log with: action, target, admin user, timestamp.
- [ ] **Monthly Quota Analytics Tracking** - Track monthly event counts for both personal users and organizations separately for analytics and reporting purposes. Even though organizations have no limits, we want visibility into usage patterns. Requires new database table or extending existing quota tracking.

---

## Epic: Audit System
**Context**: Cross-cutting audit trail for security, compliance, and debugging. Identified during Channel Management refinement.

**NOTE** something like Axiom's frontend logging / pocketbase monitoring to capture user interactions for audits? This should be something that can be monitored in the application as an admin.

- [ ] **Bulk action audit trail** - Record bulk operations (channel deletes, etc.) with user, action, targets, timestamp.
- [ ] **Admin action audit** - Track admin-specific actions: status changes, user management, system configuration.

---

## Epic: Billing & Subscription Tiers (NOT MVP)
**Context**: Commercial pricing model with tier-based limits. Replaces removed TRIAL concept. All quota/limit enforcement should be driven by the user's subscription tier.

- [ ] **Pricing Model & Plan Entity** - Define plan tiers (Free/Pro/Enterprise or similar). Plan entity with limits: members, monthly events, API keys, retention days. Admin can assign plans to users/orgs.
- [ ] **Configurable Event Quotas** - Monthly event limit driven by subscription tier (replaces hardcoded 1000). Orgs inherit tier from their plan. Upgrade prompts when approaching limit.
- [ ] **Max Retention Per Tier** - Retention days capped by plan tier. Lower tiers get shorter retention. Enforce on cleanup job + show limit in retention settings UI.
- [ ] **Subscription Info Tab (User Details)** - User settings page showing: current plan, usage vs limits (events, API keys, members, retention), billing period, upgrade CTA.
- [ ] **Subscription Info Tab (Org Details)** - Org settings page showing: current plan, usage vs limits for the org, managed by org owner/admin.
- [ ] **Stripe Integration** - Payment processing, plan upgrades/downgrades, webhook handling for subscription lifecycle events.
- [ ] **Pricing/Upgrade Page** - Authenticated users can view plans, compare features, and initiate upgrade from within the app.

---
## Epic: Developer API Documentation (NOT MVP)
**Context**: Developers integrating with Eventify need comprehensive documentation to understand the API and get started quickly.

- [ ] **Getting Started Guide** - Step-by-step guide: create API key, create channel, send first event. Interactive examples with copy-paste commands.
- [ ] **API Reference Page** - Full endpoint documentation: Events (single, batch), Channels (CRUD). Request/response schemas, authentication headers, error codes.
- [ ] **Code Examples** - cURL, Python, Node.js, Go examples for common operations. Copyable snippets with syntax highlighting.
- [ ] **Authentication Guide** - API key types (personal vs org), header format, security best practices, key rotation.
- [ ] **Rate Limits & Quotas** - Document rate limits, quota system, error responses (429), best practices for high-volume senders.
- [ ] **SDKs (future)** - Official client libraries for popular languages. Auto-generated from OpenAPI spec.
- [ ] **Changelog/Versioning** - API versioning strategy, deprecation policy, changelog for breaking changes.

---
## Epic: Future Considerations (NOT MVP)
**Context**: Ideas to keep in mind for architecture decisions but not for immediate development.

- [ ] **Basic Tracing** - Consuming tracing data from a opentelemetry collector. Could be used for debugging and performance monitoring. (custom jeager, configurable per organization/user)
- [ ] **Growthbook** - https://www.growthbook.io/ for feature flagging and A/B testing. Could be useful for gradual rollouts and testing new features.
- [ ] **Admin User can create dashboards from every org channel** - Admins can create dashboards that pull in data from any channel across the organization, even if they are not the channel owner. This allows for cross-channel monitoring and insights.
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Add TTL-based Caching Infrastructure**: Add `@Cacheable` annotations or something similar for frequently accessed data like dashboard calls.
- [ ] **CSRF protection for cookie-based auth** - Currently disabled. Auth cookies use `SameSite=Lax` which already blocks cross-origin state-changing requests in modern browsers, so CSRF is defense-in-depth rather than a real gap. Consider enabling when pursuing SOC2 / ISO 27001 / enterprise sales. Scope: enable Spring Security CSRF with `CookieCsrfTokenRepository`, exclude `/v1/external/**` (API-key endpoints), update every SPA fetch to echo `X-CSRF-Token`, update every integration test. ~1–2 days.
- [ ] **Refresh token theft detection (token reuse → family revocation)** - When a previously-rotated refresh token is presented again, treat as a theft signal and revoke the entire token family for that user. RFC 6819 §5.2.2.3 / OAuth 2.0 Security BCP §4.13.2. Standard at Auth0, Okta, Cognito, Clerk. Pre-req: AUTH-04 (introduces `family_id` column on `token`). Implementation needs: `revoked_at TIMESTAMPTZ NULL` column, preserve old rows on rotation (delete-on-presentation only), grace-period logic to absorb network races (just-rotated token valid for ~30s after rotation), daily cleanup job for revoked rows past max session lifetime. Deferred from AUTH-04 because pre-MVP has no concrete threat and false-positive race conditions add behavioural complexity.
- [ ] **Company Login SSO / SAML** - EntraID authentication, configuring IdP during org creation. User not searchable by regular users / org. Requires SAML library, org-level IdP config, JIT provisioning, admin setup UI. (XL — consider as sub-epic)
- [ ] **SSE / WebSocket push for notifications** - Replace 30s polling (NOTIF-03) with server push for real-time delivery. SSE preferred (one-way, simple, behind cookie auth). Adds backend `/api/v1/notifications/stream` endpoint, frontend `EventSource` integration. Polling remains as fallback. Consider when notification volume justifies it or for "presence"-type features.

