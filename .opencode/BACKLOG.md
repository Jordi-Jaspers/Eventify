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
- [ ] **Webhooks/Notifications**: Alert users when specific events occur (ERROR severity, keyword match)
- [ ] **Real-time Updates**: WebSocket or SSE for live timeline updates
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Integrations**: Slack, Discord, PagerDuty notifications
- [ ] **Log Forwarding**: Splunk/Datadog/CloudWatch style log driver compatibility
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
- [ ] **Stripe Integration**: manage subscriptions/payments
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Pricing/Upgrade page for authenticated users** - Logged-in users should be able to view pricing and upgrade their plan from within the app (e.g., sidebar link or settings page).
- [ ] **Add TTL-based Caching Infrastructure**: Add `@Cacheable` annotations or something similar for frequently accessed data like dashboard calls.
- [ ] **Evaluate Keycloak Migration** - Assess whether migrating to Keycloak would be more efficient than building session management, SSO/SAML, account linking, and MFA individually. Compare: ops overhead vs feature velocity.
- [ ] **CSRF protection for cookie-based auth** - Currently disabled. Auth cookies use `SameSite=Lax` which already blocks cross-origin state-changing requests in modern browsers, so CSRF is defense-in-depth rather than a real gap. Consider enabling when pursuing SOC2 / ISO 27001 / enterprise sales. Scope: enable Spring Security CSRF with `CookieCsrfTokenRepository`, exclude `/v1/external/**` (API-key endpoints), update every SPA fetch to echo `X-CSRF-Token`, update every integration test. ~1–2 days.
- [ ] **Refresh token theft detection (token reuse → family revocation)** - When a previously-rotated refresh token is presented again, treat as a theft signal and revoke the entire token family for that user. RFC 6819 §5.2.2.3 / OAuth 2.0 Security BCP §4.13.2. Standard at Auth0, Okta, Cognito, Clerk. Pre-req: AUTH-04 (introduces `family_id` column on `token`). Implementation needs: `revoked_at TIMESTAMPTZ NULL` column, preserve old rows on rotation (delete-on-presentation only), grace-period logic to absorb network races (just-rotated token valid for ~30s after rotation), daily cleanup job for revoked rows past max session lifetime. Deferred from AUTH-04 because pre-MVP has no concrete threat and false-positive race conditions add behavioural complexity.
- [ ] **Company Login SSO / SAML** - EntraID authentication, configuring IdP during org creation. User not searchable by regular users / org. Requires SAML library, org-level IdP config, JIT provisioning, admin setup UI. (XL — consider as sub-epic)

