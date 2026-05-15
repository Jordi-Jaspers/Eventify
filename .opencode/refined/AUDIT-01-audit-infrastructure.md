---
epic: "AUDIT"
title: "Audit Infrastructure"
estimate: M
status: ready
created: 2026-05-14
depends_on: []
labels: [backend, security, infrastructure]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform administrator\
**I want** a full paper trail of all admin activity and sensitive operations\
**So that** I have accountability and can investigate who did what and when\

## 2. Business Context & Value
Admins have elevated privileges that affect all users and organizations. A complete audit trail provides accountability, aids debugging, and prepares the platform for future compliance requirements. Currently only API key revocations are tracked — all other admin actions leave no trace.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Admin GET request is audited
    * Given an authenticated admin user
    * When they make any GET request to an `/admin/**` endpoint
    * Then an audit entry is persisted with actor_id, method, full path (incl. query params), status_code, ip_address, created_at, and null request_body
* [ ] **Scenario 2**: Admin mutation is audited with request body
    * Given an authenticated admin user
    * When they make a POST/PUT/PATCH/DELETE request to an `/admin/**` endpoint
    * Then an audit entry is persisted with all metadata plus the request body as JSONB
* [ ] **Scenario 3**: Failed requests are audited
    * Given an admin request that results in 4xx or 5xx
    * When the response is returned
    * Then the audit entry captures the error status code (e.g., 403, 404, 500)
* [ ] **Scenario 4**: Non-admin sensitive action is audited
    * Given a regular user revoking their own API key or performing a bulk channel operation
    * When the action completes
    * Then an audit entry is written to the same audit_log table via ApplicationEvent
* [ ] **Scenario 5**: Audit is async and non-blocking
    * Given any auditable request
    * When the audit write fails (e.g., DB error)
    * Then the original request is NOT affected — audit failure is logged as a warning, not propagated

## 4. Technical Requirements
* **API Changes**: No new public endpoints in this story (Audit Log UI is a separate story)
* **Database**: New `audit_log` table:
    * `id` BIGSERIAL PK
    * `actor_id` BIGINT NOT NULL FK → user
    * `method` VARCHAR(7) NOT NULL
    * `path` VARCHAR(512) NOT NULL
    * `status_code` SMALLINT NOT NULL
    * `request_body` JSONB NULL (only for POST/PUT/PATCH/DELETE)
    * `ip_address` VARCHAR(45) NOT NULL
    * `created_at` TIMESTAMPTZ NOT NULL DEFAULT NOW()
    * Index on `created_at` (query by time range)
    * Index on `actor_id` (query by user)
* **Security**: Audit log table is write-only from application perspective. No delete/update endpoints.
* **Performance**: Async write via `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async`. Must not add latency to admin requests.

## 5. Design & UI/UX
N/A — this story is backend infrastructure only. Audit Log UI is a separate story.

## 6. Implementation Notes

**Approach — two mechanisms:**

1. **Spring `HandlerInterceptor`** registered on `/api/v1/admin/**` — captures all admin requests automatically. In `afterCompletion()`, publishes `AuditEvent` with method, path, status code, actor, IP, and request body (cached via `ContentCachingRequestWrapper` for mutations only).

2. **Explicit `ApplicationEvent` publishing** from service methods for non-admin sensitive actions (key revocation by owner, bulk channel ops). Same `AuditEvent` class, same listener.

**Listener:** `AuditEventListener` with `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async` → persists to `audit_log` table. Wraps in try/catch — failures logged as warnings, never propagated.

**Key files:**
| File | Purpose |
|------|---------|
| `common/audit/model/AuditLog.java` | Entity |
| `common/audit/event/AuditEvent.java` | Spring ApplicationEvent |
| `common/audit/listener/AuditEventListener.java` | Async listener, persists to DB |
| `common/audit/interceptor/AdminAuditInterceptor.java` | HandlerInterceptor for `/admin/**` |
| `common/audit/repository/AuditLogRepository.java` | JpaRepository |
| `common/config/WebMvcConfig.java` | Register interceptor (or add to existing config) |

**Patterns to follow:**
- Entity: implement `PageableItem` interface (for future search/pagination in UI story)
- Migration naming: `202505141000-PRD-audit-log-table.xml`
- Async: use existing `@EnableAsync` config (verify it exists, add if not)

**Request body caching:** Wrap `HttpServletRequest` in `ContentCachingRequestWrapper` only for mutation methods. Read body in `afterCompletion()` after controller has consumed it.

**Existing `api_key_audit`:** Keep as-is. The generic audit log will also record key revocations as summary entries — both coexist.

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `common/audit/model/AuditLog.java` | New entity |
| `common/audit/event/AuditEvent.java` | New event class |
| `common/audit/listener/AuditEventListener.java` | New async listener |
| `common/audit/interceptor/AdminAuditInterceptor.java` | New interceptor |
| `common/audit/repository/AuditLogRepository.java` | New repository |
| Web config class | Register interceptor on `/admin/**` |
| `resources/db/changelog/changesets/202505141000-PRD-audit-log-table.xml` | New migration |
| `resources/db/changelog/db.changelog-master.xml` | Include new changeset |
