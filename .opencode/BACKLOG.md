# Backlog

Raw ideas and future work. Items here need refinement before development.

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
- [ ] **Monthly Quota Analytics Tracking** - Track monthly event counts for both personal users and organizations separately for analytics and reporting purposes. Even though organizations have no limits, we want visibility into usage patterns. Requires new database table or extending existing quota tracking.

---

## Epic: Audit System
**Context**: Cross-cutting audit trail for security, compliance, and debugging. Identified during Channel Management refinement.

**NOTE** something like Axiom's frontend logging to capture user interactions for audits?

- [ ] **Audit infrastructure** - Create audit log table, service, and base patterns for tracking user actions across the platform.
- [ ] **Bulk action audit trail** - Record bulk operations (channel deletes, etc.) with user, action, targets, timestamp.
- [ ] **Admin action audit** - Track admin-specific actions: status changes, user management, system configuration.
- [ ] **Audit log UI** - Admin interface to search and view audit trail.

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
## Epic: Bugs & Technical Debt
**Context**: Ongoing maintenance, bug fixes, and technical debt cleanup.

- [ ] **Redirect to org incorrect** - on user profile page clicking on organization name redirects to wrong location. Fix routing logic to redirect to correct org dashboard.
- [ ] **Adjust "how it works" landing page** - Update copy and design of the landing page to better explain the product and guide users to sign up or explore features. Consider adding screenshots, diagrams, or a video demo.
- [ ] **Landing page curl command** - Update the example curl command on the landing page to reflect the current API structure and authentication method. Ensure it works with the latest API version and provides a clear example for developers.
- [ ] **Update curl command API key generation** -- during creation of the api key, the curl command example should be updated to reflect the new API key format and endpoint structure. This ensures that users can easily copy and use the command without confusion. there is a util method used in the channel.

---
## Epic: General Improvements
**Context**: Small improvements that don't fit into other epics but would enhance user experience.

- [ ] **Native Compilation** - Explore using GraalVM native image compilation for faster startup times and lower memory usage, especially for serverless deployments.
- [ ] **Improve server Test suite** - currently 1200+ tests which is taking around 5 minutes to run. Explore ways to optimize test execution time, such as parallel test execution, test selection based on code changes, or refactoring slow tests. extracting util methods to base classes. most culprits are unit tests having duplicate helper methods. our method is to always create a valid object and adjust the relevant field for the test instead of creating multiple variations of the same object with different fields set.

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

- [ ] **Webhooks/Notifications**: Alert users when specific events occur (ERROR severity, keyword match)
- [ ] **Real-time Updates**: WebSocket or SSE for live timeline updates
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Integrations**: Slack, Discord, PagerDuty notifications
- [ ] **Log Forwarding**: Splunk/Datadog/CloudWatch style log driver compatibility
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
- [ ] **Stripe Integration**: manage subscriptions/payments
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Add TTL-based Caching Infrastructure**: Add `@Cacheable` annotations or something similar for frequently accessed data like dashboard calls.
