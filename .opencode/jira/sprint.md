# Sprint: Event Ingestion Foundation

**Sprint Goal**: Establish the complete event ingestion infrastructure, enabling external systems to send events to channels with quota enforcement and automatic retention cleanup.

**Start Date**: 2026-01-20
**Duration**: 2 weeks

---

## Priority Order

| Priority | Story | Epic | Estimate | Dependencies |
|----------|-------|------|----------|--------------|
| 1 | [AUTHENTICATION-production-ready-auth-flow](./refined/AUTHENTICATION-production-ready-auth-flow.md) | Authentication | M (5 pts) | None |
| 2 | [EVENT-entity-database-schema](./refined/EVENT-entity-database-schema.md) | Event Ingestion | M (5 pts) | None |
| 3 | [EVENT-realtime-ingestion-api](./refined/EVENT-realtime-ingestion-api.md) | Event Ingestion | L (8 pts) | #2 |
| 4 | [EVENT-retention-cleanup-job](./refined/EVENT-retention-cleanup-job.md) | Event Ingestion | S (2 pts) | #2 |
| 5 | [EVENT-batch-ingestion-api](./refined/EVENT-batch-ingestion-api.md) | Event Ingestion | M (5 pts) | #2 |
| 6 | [EVENT-quota-enforcement](./refined/EVENT-quota-enforcement.md) | Event Ingestion | M (5 pts) | #3, #5 |

**Total Points**: 30

---

## Execution Strategy

### Week 1: Foundation

| Day | Priority 1 (Auth) | Priority 2-4 (Events) |
|-----|-------------------|----------------------|
| 1-2 | AUTH: API interceptor, auth store | SCHEMA: Event table, hypertable, indexes |
| 3 | AUTH: Layout guard, session validation | SCHEMA: Compression policy, entity, repository |
| 4-5 | AUTH: Login page, testing | REALTIME: Controller, service, validation |

### Week 2: Complete Ingestion

| Day | Tasks |
|-----|-------|
| 1-2 | REALTIME: Complete API, tests |
| 2-3 | RETENTION: Cleanup job (parallel with realtime completion) |
| 3-4 | BATCH: Batch endpoint, timestamp validation |
| 5 | QUOTA: Integrate with both endpoints, rate limit headers |

---

## Parallelization Opportunities

```
Week 1:
├── AUTH (frontend)────────────────────►
└── SCHEMA (backend)───► REALTIME (backend)───►

Week 2:
├── REALTIME (completion)───►
├── RETENTION (parallel)─────►
├── BATCH (after schema)─────────────►
└── QUOTA (after realtime + batch)────►
```

**Notes**:
- AUTH and SCHEMA can run in parallel (different workstreams: frontend vs backend)
- RETENTION and REALTIME can run in parallel (both only depend on SCHEMA)
- BATCH can start once SCHEMA is done, shares validation logic with REALTIME
- QUOTA must wait for both REALTIME and BATCH to be complete

---

## Definition of Done

- [ ] All acceptance criteria pass
- [ ] Unit tests written and passing (>80% coverage for new code)
- [ ] Integration tests for API endpoints
- [ ] Code quality checks pass (Spotless, Checkstyle, PMD, SpotBugs)
- [ ] Build succeeds
- [ ] PR reviewed and merged

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| TimescaleDB hypertable complexity | Medium | Refer to existing Liquibase patterns, test locally first |
| Quota race conditions | High | Use pessimistic locking, add concurrency tests |
| Batch validation complexity | Medium | Reuse single-event validator, add batch-specific tests |

---

## Success Metrics

After sprint completion:
- [ ] External systems can send events via `POST /v1/events`
- [ ] Offline systems can sync historical events via `POST /v1/events/batch`
- [ ] Users are blocked at 1000 events/month with clear error message
- [ ] Events older than retention period are automatically deleted
- [ ] Users stay logged in as long as refresh token is valid
