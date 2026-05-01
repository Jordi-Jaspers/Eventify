---
epic: "LAUNCH"
title: "Enable Swagger UI in Production"
estimate: S
status: ready
created: 2026-05-01
depends_on: [ ]
labels: [ backend, config ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** developer using Eventify\
**I want** to access the API documentation in production\
**So that** I can explore and test the API without needing a local dev setup\

## 2. Business Context & Value
API docs are currently disabled in production. Enabling Swagger UI gives developers self-serve API exploration, reducing support burden and improving developer experience.

## 3. Acceptance Criteria
* [ ] **Swagger UI accessible in production**:
    * Given the production environment
    * When a user navigates to `/v1/public/docs/openapi.html`
    * Then the Swagger UI loads with all documented endpoints
* [ ] **OpenAPI spec accessible**:
    * Given the production environment
    * When a client requests `/v1/public/docs`
    * Then the OpenAPI JSON spec is returned
* [ ] **No auth required**:
    * Given an unauthenticated user
    * When they access the docs endpoints
    * Then they can view the documentation (existing permitAll config)

## 4. Technical Requirements
* **API Changes**: N/A — endpoints already exist, just disabled
* **Database**: N/A
* **Security**: Docs endpoints are already `permitAll` in `WebSecurityConfig.java`. No change needed.
* **Performance**: N/A

## 5. Design & UI/UX
N/A — Swagger UI is provided by springdoc library.

## 6. Implementation Notes
- **File:** `server/src/main/resources/application-prd.yml`
- Change `springdoc.api-docs.enabled` from `false` to `true` (or remove the override to inherit default `true`)
- Security already configured: `/v3/api-docs/**` is `permitAll` in `WebSecurityConfig.java`

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `server/src/main/resources/application-prd.yml` | Set `springdoc.api-docs.enabled: true` | ~line with api-docs.enabled |
