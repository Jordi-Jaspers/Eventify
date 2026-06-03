# Backlog

## Epic: Bugs & Technical Debt
**Context**: Ongoing maintenance, bug fixes, and technical debt cleanup.
- [ ] **Improve error message in validators** -  The backend has custom validators and all the error messages are user-facing. verify that all validators have clear, user-friendly error messages. Also double check the frontend that errors are always shown via a toast (with.without retry) and not just an error block on the screen. report any discrepancies so we can also update the agent frontend skill if possible.
- 
- [ ] **Permission Gaps Fix** - Fix 12 identified permission inconsistencies across backend and frontend. Includes: missing server-side admin route guard in frontend, missing `MANAGE_ORGANIZATIONS` bypass on org settings/statistics/dashboard endpoints, missing `@PreAuthorize` on UserWatchlistController and UserApiKeyController, dead `SEND_EVENTS` authority, inconsistent frontend route guards, security logic mixed in service layer. Reference: `.opencode/.tmp/permission-gaps.md`

- [ ] **Refactor Security services** - the `AdapterConfigSecurityService`, `ChannelSecurityService`, ` EventSecurityService`, `OrganizationSecurityService`, and `WatchlistSecurityService` (Maybe other i forgot) have some overlapping logic and inconsistent patterns. Refactor to extract common patterns, ensure consistent method signatures, and improve readability. migrate all these services to the common.security package so they are all in one place and can share common patterns and utilities.

- [ ] **Remove all unecessary Hibernate properties** - Lots of models have (updatable = , length =, ...) or any other field in the @Column annotation. Remove all of them except if they should not be updateable. the classes need to look as clean as possible.
---

## Epic: Notification System
**Context**: Modular notification subsystem built from scratch. No backend notification entity exists today; "What's New" is purely a frontend changelog reader. Replaces the deprecated "Webhooks/Notifications" and "Integrations: Slack/Discord/PagerDuty" items in Future Considerations.

**Subscription model:** subscriptions are at the WATCHLIST level (not per-channel). When any channel in a subscribed watchlist transitions severity, the subscription evaluates. Subscriptions are global per user — fire regardless of which org context the user is currently viewing.

**Trigger model:** severity *transitions* only (NOT per-event firing). Configurable target severities — user picks which transitions trigger ("notify on transition to CRITICAL" or "notify on transition to WARNING or CRITICAL").

**Adapter pattern:** `NotificationAdapter` interface with pluggable destinations. MVP destinations: in-app + Telegram. Email and Slack/Discord/webhooks come later via the same abstraction.

- [ ] **Channel Rhythm Detection + Overdue Alerts** - Statistical (no LLM): period detection on inter-arrival times via FFT or simple periodicity over `event_timeline_hourly`. New trigger type `CHANNEL_OVERDUE` fires when expected next event is late by configurable margin. Severity drift detection (CRITICAL ratio anomaly vs baseline) as additional trigger type. All evaluated through existing dispatch path.

---

## Epic: Organization Enhancement: Team support (To be discussed)
**Context**: Organizations should support team creations and have members in teams with specific settings. maybe we should completely revisit our current setup. notifications adapters, watchlists on team level per org? team-based access control? how would this affect the current org structure? first we need possible high level options that would work for an enterprise. we should create a scheme of the all the old vs new flows first.

---

## Epic: Retention & Data Lifecycle
**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

- [ ] **Enterprise Audit Trail Enhancement** - Upgrade audit system from HTTP-level capture to semantic, enterprise-grade authorization auditing. Includes: authz denial logging (403s), semantic event types enum, org-scoped audit log (visible to org owners/admins), auth event semantics (login success/failure), actor snapshot (immutable), 1-year retention, API key usage logging. New `authorization_event` table. Compliance targets: SOC2 CC6.1/CC6.2/CC7.2, ISO27001 A.12.4.1/A.12.4.3. Reference: `.opencode/.tmp/audit-trail-enhancement-plan.md`

- [ ] **Retention Policy Configuration**: See "User/Organization Retention Settings UI" under Event Channels epic for UI implementation. DB columns already exist with CHECK constraints (90-1825 days). This item covers backend service logic for applying retention during cleanup.
- [ ] **Global Retention Settings (Admin)**: Admin can set system-wide default retention, maximum retention (users can't exceed), view storage usage stats.

---

---

## Epic: AI Monitoring
**Context**: configure AI per org / user and let user create API keys to use their AI with our application as proxy. We can use this for anomaly detection, alerting, and insights on event data, cost management, orgs should als have budget controls and usage alerts for AI calls.

key question: Should this be in this application or should we build a separate application for this?

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
## Epic: Future Considerations (NOT MVP)
**Context**: Ideas to keep in mind for architecture decisions but not for immediate development.

- [ ] **Opentelemtry in frontend** - Add opentelemetry instrumentation to the frontend for performance monitoring and debugging. also persist frontend traces to the same backend for unified observability. Deferred until backend tracing infrastructure is mature enough to consume frontend data without overwhelming it.
- [ ] **Monthly Quota Analytics Tracking** - Track monthly event counts for both personal users and organizations separately for analytics and reporting purposes. Even though organizations have no limits, we want visibility into usage patterns. Requires new database table or extending existing quota tracking.
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
- [ ] **Admin Tools Tab** - Admin-only page with system maintenance actions. First action: "Rebuild watchlist_channel junction" button (with confirmation warning) that triggers a full reconciliation of all watchlist-channel junction rows from JSONB + template references. Useful as a manual recovery tool. Future actions: cache invalidation, re-index, health checks. Discuss scope and what else belongs here.

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
