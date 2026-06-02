# Organization Statistics Page

**Completed:** 2026-05-26
**Epic:** ORGMGMT
**Source:** `.opencode/refined/ORGMGMT-04-org-statistics-page.md`

## Summary

Full org statistics page with event volume chart, error rate chart (percentage), time range filter, summary stat cards, and API key stats. Split into 3 independent endpoints for component-based loading.

## Plan Approved by the user:

### Requirements Summary

- Event volume timeline chart (dynamic buckets: ≤7d→1h, 8-30d→6h, 31d+→1d)
- Error rate timeline chart (percentage per bucket, not raw counts)
- Time range filter (7d/30d/90d/180d + custom date range)
- Summary stat cards (total events, avg daily volume, error rate, active channels)
- API key stats (revoked/expiring this month, never used, top keys)
- Access restricted to org ADMIN/OWNER
- Split REST calls for independent loading/caching

### Technical Approach

- Backend: 3 endpoints (`/stats/timeline`, `/stats/summary`, `/stats/api-keys`), domain objects + MapStruct mapper, @Cacheable, @PreAuthorize
- Frontend: Pill toggle chart switcher, TimeRangePopover, StatCards, AreaChartCard, ApiKeyStatsSection

### Execution Order

| Phase | Agent | Task |
|-------|-------|------|
| 2.1 | spring-testing-agent | Tests for split endpoints |
| 2.2 | spring-backend-agent | Implementation |
| 2.3 | — | Backend review |
| 2.4 | backend-optimizer-agent | Refactor |
| 3.2 | svelte-frontend-agent | Frontend implementation (multiple rounds) |
| 3.3 | — | Frontend review (4 rounds) |
| 3.4 | frontend-optimizer-agent | Extract components |

## Implementation

### Backend

- `GET /v1/organization/{orgId}/stats/timeline?days=30` → event + error rate timelines
- `GET /v1/organization/{orgId}/stats/summary?days=30` → totals + averages
- `GET /v1/organization/{orgId}/stats/api-keys` → API key statistics
- Dynamic bucket intervals via `time_bucket(:interval, eth.bucket)`
- Error rate as SQL percentage: `COUNT(*) FILTER (WHERE severity IN ('CRITICAL','WARNING')) * 100.0 / NULLIF(COUNT(*), 0)`
- Domain → Response mapping via `OrgStatsMapper` (MapStruct)

### Frontend

- Page: `organizations/[orgId]/statistics` (307 lines)
- Pill toggle to switch between Event Volume / Error Rate charts
- TimeRangePopover (extracted reusable component)
- 4 StatCards + conditional API Key Stats section
- Per-section independent loading states
- Direct HSL chart colors (green events, red errors)

### Deviations from Plan

- Added API key stats (user request during review)
- Error rate as percentage per bucket (user request — "tells nothing if millions of events")
- Split single endpoint into 3 (user request — "better component-based loading")
- Multiple frontend review rounds for UX polish

## Agents Used

| Agent | Task | Result |
|-------|------|--------|
| spring-testing-agent | Tests (2 rounds) | Complete |
| spring-backend-agent | Implementation (3 rounds) | Complete |
| backend-optimizer-agent | Refactor (2 rounds) | Complete |
| svelte-frontend-agent | Frontend (5 rounds) | Complete |
| deep-research-agent | Research (4 rounds) | Complete |

## Files Modified

### Backend (new)
- `server/.../organization/controller/OrgStatisticsController.java`
- `server/.../organization/service/OrgStatsService.java`
- `server/.../organization/model/OrgStats.java`, `OrgTimeline.java`, `OrgSummary.java`, `OrgApiKeyStatistics.java`, `OrgTopKey.java`, `OrgTimelineBucketData.java`, `OrgErrorRateBucket.java`
- `server/.../organization/model/response/OrgStatsResponse.java`, `OrgTimelineResponse.java`, `OrgSummaryResponse.java`, `OrgTimelineBucketResponse.java`, `OrgErrorRateBucketResponse.java`, `OrgApiKeyStatsResponse.java`, `OrgTopApiKeyResponse.java`
- `server/.../organization/model/validator/OrgStatsValidator.java`
- `server/.../organization/model/mapper/OrgStatsMapper.java`
- `server/.../organization/model/projection/OrgErrorRateProjection.java`
- `server/.../admin/stats/model/projection/OrgTimelineProjection.java`

### Backend (modified)
- `server/.../api/Paths.java` — 4 new path constants
- `server/.../admin/stats/repository/EventTimelineRepository.java` — org-scoped queries
- `server/.../apikey/repository/ApiKeyRepository.java` — org-scoped counts
- `server/.../apikey/repository/ApiKeyAuditRepository.java` — org-scoped revoked count
- `server/.../event/model/Event.java` — removed NamedNativeQuery

### Frontend (new)
- `client/src/lib/api/organization/OrganizationStatisticsController.ts`
- `client/src/lib/components/ui/pill-toggle/`
- `client/src/lib/components/ui/time-range-popover/`
- `client/src/lib/components/organization/api-key-stats-section.svelte`

### Frontend (modified)
- `client/src/routes/(authenticated)/organizations/[orgId]/statistics/+page.svelte`
- `client/src/routes/(public)/dev-playbook/+page.svelte`
- `client/src/lib/api/models.ts`
- `client/src/lib/types/api.d.ts`

### Config
- `.opencode/METADATA.md` — added dev-playbook reference
- `.opencode/skills/eventify-svelte-standards/SKILL.md` — added new component locations

### Test data
- `server/src/main/resources/db/changelog/changesets/202605231000-TST-acme-org-event-data.xml`

## Tests

- 44 backend tests (13 service + 25 controller + 6 validator), all passing
- Frontend tests skipped per config
