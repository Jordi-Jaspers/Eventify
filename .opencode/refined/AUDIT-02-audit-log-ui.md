---
epic: "AUDIT"
title: "Audit Log UI"
estimate: M
status: ready
created: 2026-05-14
depends_on: ["AUDIT-01-audit-infrastructure"]
labels: [backend, frontend]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** platform administrator\
**I want** a searchable audit log interface\
**So that** I can investigate admin activity and trace who did what\

## 2. Business Context & Value
The audit infrastructure (AUDIT-01) captures all admin requests. Without a UI to query it, the data is only accessible via direct DB queries. A searchable admin page makes the audit trail actionable for day-to-day accountability and incident investigation.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: View audit log with pagination
    * Given an admin navigates to the audit log page
    * When the page loads
    * Then the most recent audit entries are displayed in a paginated table, sorted by created_at DESC
* [ ] **Scenario 2**: Filter by actor
    * Given the audit log page is loaded
    * When the admin types a username in the actor filter
    * Then only entries by that actor are shown (fuzzy text match)
* [ ] **Scenario 3**: Filter by HTTP method
    * Given the audit log page is loaded
    * When the admin selects one or more methods (GET/POST/PATCH/DELETE)
    * Then only entries matching those methods are shown
* [ ] **Scenario 4**: Filter by date range
    * Given the audit log page is loaded
    * When the admin selects a from/to date range
    * Then only entries within that range are shown
* [ ] **Scenario 5**: Filter by status code category
    * Given the audit log page is loaded
    * When the admin filters by status (e.g., "Success 2xx", "Client Error 4xx", "Server Error 5xx")
    * Then only entries matching that category are shown
* [ ] **Scenario 6**: View request body for mutations
    * Given a POST/PATCH/DELETE audit entry
    * When the admin clicks to expand/view details
    * Then the request body JSON is displayed in a readable format

## 4. Technical Requirements
* **API Changes**:
    * `POST /api/v1/admin/audit-log/search` — paginated search with JFrame `SortablePageInput`
    * Response: `PageResource<AuditLogResponse>` with fields: id, actorName, method, path, statusCode, ipAddress, createdAt, requestBody (nullable)
* **Database**: N/A — table created in AUDIT-01
* **Security**: `@PreAuthorize("hasAnyAuthority('MANAGE_USERS')")` or a new `VIEW_AUDIT_LOG` permission
* **Performance**: Index on `created_at` and `actor_id` (from AUDIT-01) supports the primary query patterns

## 5. Design & UI/UX
- New admin page at `/admin/tools/audit-log` (under "Tools" section in sidebar)
- Uses existing `DataTable` component with columns:
    * Actor (FUZZY_TEXT filter, colSpan 2)
    * Method (MULTI_ENUM: GET/POST/PUT/PATCH/DELETE, colSpan 1)
    * Path (FUZZY_TEXT filter, colSpan 3)
    * Status (MULTI_ENUM: "2xx"/"4xx"/"5xx", colSpan 1)
    * IP Address (colSpan 2)
    * Timestamp (DATE filter, colSpan 2)
    * Actions — expand button for request body (colSpan 1)
- Request body shown in expandable row or sheet/modal with JSON syntax highlighting
- Default sort: `created_at DESC`
- Page size: 20

## 6. Implementation Notes

**Backend:**
| File | Purpose |
|------|---------|
| `api/admin/controller/AdminAuditLogController.java` | Search endpoint |
| `api/admin/model/AdminAuditLogMetaData.java` | JFrame search metadata (FUZZY_TEXT for actor/path, MULTI_ENUM for method/status, DATE for createdAt) |
| `api/admin/model/mapper/AuditLogMapper.java` | PageMapper — maps entity to response, resolves actor_id → username |
| `api/admin/model/response/AuditLogResponse.java` | Response DTO |
| `common/audit/repository/AuditLogRepository.java` | Add `JpaSpecificationExecutor` (if not already) |

**Frontend:**
| File | Purpose |
|------|---------|
| `client/src/routes/(authenticated)/admin/tools/audit-log/+page.svelte` | Page component |
| `client/src/lib/api/admin/AdminAuditLogController.ts` | API client function |
| Sidebar config | Add "Audit Log" entry under admin tools section |

**Patterns to follow:**
- Exact same pattern as admin organizations page: `DataTable` + `createDataTableService` + column config
- Status filter: map MULTI_ENUM values ("2xx", "4xx", "5xx") to backend range query (200-299, 400-499, 500-599) — may need custom `SearchType` or handle in specification
- Actor filter: join to `user` table for username fuzzy search (use `@EntityGraph` or specification join)

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `api/admin/controller/AdminAuditLogController.java` | New controller |
| `api/admin/model/AdminAuditLogMetaData.java` | New metadata |
| `api/admin/model/mapper/AuditLogMapper.java` | New mapper |
| `api/admin/model/response/AuditLogResponse.java` | New response DTO |
| `common/audit/repository/AuditLogRepository.java` | Add JpaSpecificationExecutor |
| `client/src/routes/(authenticated)/admin/tools/audit-log/+page.svelte` | New page |
| `client/src/lib/api/admin/AdminAuditLogController.ts` | New API client |
| Sidebar navigation config | Add audit log link |
