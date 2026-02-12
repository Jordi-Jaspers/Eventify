# Backlog

Raw ideas and future work. Items here need refinement before development.

---

## Epic: Retention & Data Lifecycle
**Context**: Events should not be stored forever. Configurable retention policies help manage storage costs and comply with data governance requirements.

### Items

- [ ] **Retention Policy Configuration**: See "User/Organization Retention Settings UI" under Event Channels epic for UI implementation. DB columns already exist with CHECK constraints (90-1825 days). This item covers backend service logic for applying retention during cleanup.
- [ ] **Global Retention Settings (Admin)**: Admin can set system-wide default retention, maximum retention (users can't exceed), view storage usage stats.
- [ ] **Retention Warning Notifications**: Notify users before significant data deletion (e.g., if reducing retention). Show "data will be deleted in X days" warning in UI.

---

## Epic: Admin Global Oversight
**Context**: Platform administrators need visibility into all API keys, channels, and events across the system for support, security, and compliance.

### Items

- [ ] **Admin Events/Usage Dashboard**: High-level stats: total events today/week/month, events by severity. Top channels by volume. Users/orgs approaching or exceeding quotas. Storage usage trends.
- [ ] **Admin Audit Log**: Track admin actions: key revocations, channel archives, user impersonation. Searchable log with: action, target, admin user, timestamp.

---

## Epic: Infrastructure & Performance

**Context**: Cross-cutting concerns for caching, performance optimization, and infrastructure improvements.

### Items

- [ ] **Add TTL-based Caching Infrastructure**: Add Caffeine cache with `@EnableCaching` support. Required for caching dashboard stats (~30s TTL), and other frequently-accessed, slowly-changing data. Currently project only uses request-scoped caches. Dependencies needed: `com.github.ben-manes.caffeine:caffeine`. Create `CacheConfig.java` with cache definitions. Priority endpoints: dashboard stats, user quotas, org membership checks.

---

## Epic: OAUTH2 Authentication
**Context**: Support third-party authentication via OAUTH2 providers (Google, GitHub, etc.) in addition to existing JWT and API Key methods.

### Items

- [ ] **Review current authentication mechanisms** - Evaluate if current JWT-based auth meets all needs or if we should add OAuth2 for better SSO support.
- [ ] **Company Login SSO / SAML** - enterprise authentication, user not searchable by regular users / org.
- [ ] **Long-Lived Refresh Tokens / Remember me**
- [ ] **Multi-Token Support / Management up to 5**
- [ ] **Token Revocation - single/all**
- [ ] **OAuth2 Enhancements - account linking**

---
## Epic: Bugs & Technical Debt
**Context**: Ongoing maintenance, bug fixes, and technical debt cleanup.

### Items


---

## Epic: Future Considerations

Ideas to keep in mind for architecture decisions but not for immediate development.

### Items

- [ ] **What is new message** - Show modal on login after a new release with highlights of new features and improvements.
- [ ] **TRIAL account limitations** - Limit users, event quota, and API keys for organizations in TRIAL status. Enforce limits in backend, show upgrade prompts in UI.
- [ ] **Organization status change audit log** - Track when admin changes org status, with reason field. Part of broader admin audit log feature.
- [ ] **Organization status change notifications** - Notify org owner when their organization is suspended or reactivated.
- [ ] **Refactor user/org dashboards to something useful**
- [ ] **Support / Help buttons** - guide users to docs or support chat from the avatar modal in sidebar.
- [ ] **Stripe Integration - manage subscriptions/payments**
- [ ] **Configurable Event Quotas** - Allow users to configure their monthly event limit (tied to subscription tier). organisation have no limitations.
- [ ] **Webhooks/Notifications**: Alert users when specific events occur (ERROR severity, keyword match)
- [ ] **Real-time Updates**: WebSocket or SSE for live timeline updates
- [ ] **Event Enrichment**: Auto-detect JSON payloads, extract fields for filtering
- [ ] **Integrations**: Slack, Discord, PagerDuty notifications
- [ ] **Log Forwarding**: Splunk/Datadog/CloudWatch style log driver compatibility
- [ ] **Multi-region**: Consider event ingestion in multiple regions
- [ ] **Export**: Download events as CSV/JSON for compliance/backup
