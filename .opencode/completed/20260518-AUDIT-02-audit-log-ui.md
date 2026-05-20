# Audit Log UI

**Completed:** 2026-05-18
**Epic:** AUDIT
**Source:** .opencode/refined/AUDIT-02-audit-log-ui.md

## Summary

Searchable audit log page for platform admins with KPI cards, hourly sparkline chart, path exclusion filtering, and paginated DataTable with expandable request body rows.

## Plan Approved by the user:

### Requirements Summary

- Admin page at `/admin/tools/audit-log` with paginated DataTable
- `POST /api/v1/admin/audit-log/search` with JFrame `SortablePageInput`
- `GET /api/v1/admin/audit-log/stats` with time_bucket aggregation
- Filters: Actor (FUZZY_TEXT), Method (MULTI_ENUM), Path (FUZZY_TEXT), Status (MULTI_ENUM: 2xx/4xx/5xx), Timestamp (DATE), ExcludePath (TEXT, NOT LIKE)
- Expandable row for request body JSON
- Security: `@PreAuthorize("hasAuthority('MANAGE_USERS')")`
- Default sort: created_at DESC, page size 20
- KPI cards (Total Requests, Error Rate, Mutations, Unique Actors) with click-to-filter
- Hourly sparkline chart with tooltip
- "Hide noise" preset for health/refresh endpoints
- TimescaleDB hypertable conversion for audit_log table

### Technical Approach

- Backend: Search endpoint + Stats endpoint, MetaData with custom status range spec + exclude path spec, EntityGraph for actor join, MapStruct mappers
- Frontend: DataTable page with KPI cards component, sparkline component using reusable AreaChartCard, path exclusion via DataTable filter bar
- Database: Hypertable migration (1-day chunks, 7-day compression, 90-day retention)
- Bonus: Fixed Organization N+1 query (batch owner lookup)

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 1 | spring-testing-agent | Backend test suite (controller, service, metadata) |
| 2 | spring-backend-agent | Implementation to pass tests |
| 3 | spring-backend-agent | N+1 fixes (AuditLog actor join, Org batch owners) |
| 4 | spring-backend-agent | Stats endpoint + exclude path filter |
| 5 | svelte-frontend-agent | Page, controller, sidebar, KPI, sparkline |
| 6 | backend-optimizer-agent | Remove redundant javadocs, fix visibility |
| 7 | frontend-optimizer-agent | Bug fix, dead state removal, dedup tooltip |

## Implementation

### Backend

- `POST /v1/admin/audit-log/search` — paginated search with JFrame spec
- `GET /v1/admin/audit-log/stats?from=&to=` — aggregate stats with hourly buckets (time_bucket)
- `AuditLog` entity refactored: `@ManyToOne User actor` (removed raw actorId), `@EntityGraph` on findAll
- `AdminAuditLogMetaData` — actor via dot-notation MULTI_COLUMN_FUZZY, status range filter (2xx/4xx/5xx → BETWEEN), excludePath (NOT LIKE with comma-split)
- `AuditLogStatsMapper` — MapStruct for domain→response separation
- NPE fix: `isEmptySearchInput()` filters null-valued inputs before toSearchCriteria
- Organization N+1 fix: batch owner lookup via `findAllByOrganizationIdInAndRole` + Map join

### Frontend

- Page: `/admin/tools/audit-log` with DataTable, expandable rows
- `AuditLogKpiCards.svelte` — 4 stat cards with click-to-filter + "Hide noise" button
- `AuditLogSparkline.svelte` — hourly activity chart using reusable AreaChartCard
- `AreaChartCard` — new reusable chart component (`$lib/components/ui/chart/`)
- `StatCard` — redesigned to minimalistic style
- Sidebar: added Audit Log tab under Tools
- Types from generated OpenAPI spec (no manual interfaces)

### Deviations from Plan

- Added stats endpoint (not in original story — user requested visual graph)
- Added exclude path filter (user requested noise reduction)
- Added TimescaleDB hypertable migration (user requested)
- Fixed Organization N+1 (discovered during review)
- Refactored AreaChartCard as reusable component (also used on statistics page)
- StatCard redesigned to minimalistic style (user preference)

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| deep-research-agent | Backend patterns research | Complete |
| deep-research-agent | Frontend patterns research | Complete |
| spring-testing-agent | Backend test suite | Complete |
| spring-backend-agent | Implementation (multiple rounds) | Complete |
| deep-research-agent | N+1 optimization research | Complete |
| spring-backend-agent | Stats + exclude path + fixes | Complete |
| svelte-frontend-agent | Page + components (multiple rounds) | Complete |
| backend-optimizer-agent | Code cleanup | Complete |
| frontend-optimizer-agent | Bug fix + cleanup | Complete |

## Files Modified

### Backend (new)
- `server/src/main/java/io/github/eventify/api/admin/controller/AdminAuditLogController.java`
- `server/src/main/java/io/github/eventify/api/admin/model/AdminAuditLogMetaData.java`
- `server/src/main/java/io/github/eventify/api/admin/model/mapper/AuditLogMapper.java`
- `server/src/main/java/io/github/eventify/api/admin/model/mapper/AuditLogStatsMapper.java`
- `server/src/main/java/io/github/eventify/api/admin/model/response/AuditLogResponse.java`
- `server/src/main/java/io/github/eventify/api/admin/model/response/AuditLogStatsResponse.java`
- `server/src/main/java/io/github/eventify/api/admin/model/response/AuditLogStatsData.java`
- `server/src/main/java/io/github/eventify/api/admin/model/response/AuditLogStatsProjection.java`
- `server/src/main/java/io/github/eventify/api/admin/model/response/HourlyBucketProjection.java`
- `server/src/main/java/io/github/eventify/api/admin/service/AdminAuditLogService.java`
- `server/src/main/resources/db/changelog/changesets/202605181000-PRD-audit-log-hypertable.xml`

### Backend (modified)
- `server/src/main/java/io/github/eventify/common/audit/model/AuditLog.java`
- `server/src/main/java/io/github/eventify/common/audit/event/AuditEvent.java`
- `server/src/main/java/io/github/eventify/common/audit/interceptor/AdminAuditInterceptor.java`
- `server/src/main/java/io/github/eventify/common/audit/listener/AuditEventListener.java`
- `server/src/main/java/io/github/eventify/common/audit/repository/AuditLogRepository.java`
- `server/src/main/java/io/github/eventify/api/Paths.java`
- `server/src/main/java/io/github/eventify/api/organization/service/OrganizationService.java`
- `server/src/main/java/io/github/eventify/api/organization/repository/OrganizationMembershipRepository.java`

### Frontend (new)
- `client/src/routes/(authenticated)/admin/tools/audit-log/+page.svelte`
- `client/src/routes/(authenticated)/admin/tools/audit-log/AuditLogKpiCards.svelte`
- `client/src/routes/(authenticated)/admin/tools/audit-log/AuditLogSparkline.svelte`
- `client/src/lib/api/admin/AdminAuditLogController.ts`
- `client/src/lib/components/ui/chart/area-chart-card.svelte`
- `client/src/lib/components/ui/chart/index.ts`

### Frontend (modified)
- `client/src/lib/config/routes.ts`
- `client/src/routes/(authenticated)/admin/tools/+layout.svelte`
- `client/src/lib/components/ui/stat-card/stat-card.svelte`
- `client/src/lib/components/data-table/filters/DateFilter.svelte`
- `client/src/routes/(authenticated)/admin/statistics/+page.svelte`
- `client/src/lib/api/models.ts`

### Tests
- `server/src/test/java/io/github/eventify/api/admin/controller/AdminAuditLogControllerTest.java`
- `server/src/test/java/io/github/eventify/api/admin/service/AdminAuditLogServiceTest.java`
- `server/src/test/java/io/github/eventify/api/admin/model/AdminAuditLogMetaDataTest.java`
- `server/src/test/java/io/github/eventify/common/audit/AuditIntegrationTest.java`
- `server/src/test/java/io/github/eventify/common/audit/interceptor/AdminAuditInterceptorTest.java`
- `server/src/test/java/io/github/eventify/common/audit/listener/AuditEventListenerTest.java`

### Other
- `client/src/lib/api/admin/PasswordController.ts` (bug fix)
- `client/src/lib/api/admin/service/ChangePasswordService.svelte.ts` (bug fix)
- `.opencode/agents/svelte-frontend-agent.md` (added rule 17: no lint suppression)

## Tests

- 22+ backend tests written, all passing
- Frontend: `bun run check` — 0 errors, 0 warnings
