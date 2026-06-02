---
epic: "NOTIF"
title: "Adapter Infrastructure & Filtering"
estimate: M
status: ready
created: 2026-06-02
depends_on: []
labels: [backend, infrastructure, notification]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** system architect\
**I want** a pluggable adapter infrastructure with per-dispatch filtering and credential storage\
**So that** notification delivery can be routed to specific adapters based on caller context and user configuration\

## 2. Business Context & Value

Currently `NotificationDispatchService` fans out to ALL registered adapters, ignoring the `subscription.adapters` field. This story builds the foundational layer: adapter registry with filtering, credential storage table, and the contracts that NOTIF-02 (implementations) and NOTIF-03 (UI) depend on.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Dispatch filters by target adapters
    * Given a notification dispatch call with `targetAdapters = ["MATTERMOST", "IN_APP"]`
    * When the dispatch executes
    * Then only adapters matching those identifiers are invoked
* [ ] **Scenario 2**: Dispatch with no filter sends to all adapters (backward compat)
    * Given a notification dispatch call with `targetAdapters = null`
    * When the dispatch executes
    * Then all registered adapters are invoked (existing behavior preserved)
* [ ] **Scenario 3**: Adapter config CRUD — user level
    * Given an authenticated user
    * When they create an adapter config (type=MATTERMOST, webhookUrl, label)
    * Then it is persisted with their user_id and organization_id=null
* [ ] **Scenario 4**: Adapter config CRUD — org level
    * Given an org owner/admin
    * When they create an adapter config for their org (type=SLACK, webhookUrl, label)
    * Then it is persisted with organization_id set and user_id of creator
* [ ] **Scenario 5**: Multiple configs per adapter type
    * Given a user with 2 existing Mattermost configs
    * When they create a 3rd Mattermost config with different webhook URL and label
    * Then all 3 coexist and are independently selectable
* [ ] **Scenario 6**: Adapter registry provides lookup by identifier
    * Given registered adapters [IN_APP, MATTERMOST, SLACK, EMAIL]
    * When the registry is queried for "MATTERMOST"
    * Then the Mattermost adapter bean is returned
* [ ] **Edge Case**: Unknown adapter identifier in target list
    * Given targetAdapters contains "TELEGRAM" (not registered)
    * When dispatch executes
    * Then TELEGRAM is skipped with a warning log, other adapters still fire

## 4. Technical Requirements

* **API Changes**:
    - `POST /v1/adapter-configs` — create config (body: type, label, config json)
    - `GET /v1/adapter-configs` — list user's configs (query: scope=personal|org, orgId)
    - `GET /v1/adapter-configs/{id}` — get single config
    - `PUT /v1/adapter-configs/{id}` — update config
    - `DELETE /v1/adapter-configs/{id}` — delete config
* **Database**:
    - New table `adapter_config`:
      ```sql
      id UUID PK DEFAULT gen_random_uuid(),
      user_id UUID NOT NULL REFERENCES "user"(id),
      organization_id UUID NULL REFERENCES organization(id),
      adapter_type VARCHAR(50) NOT NULL,  -- MATTERMOST, SLACK, EMAIL, IN_APP
      label VARCHAR(100) NOT NULL,
      config JSONB NOT NULL DEFAULT '{}',  -- webhookUrl, etc.
      enabled BOOLEAN NOT NULL DEFAULT true,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
      ```
    - Index: `(user_id, adapter_type)`, `(organization_id, adapter_type)`
* **Security**:
    - Webhook URLs stored in `config` jsonb — consider masking on read (show last 8 chars)
    - Org configs: only org OWNER/ADMIN can create/edit/delete
    - Users can only access their own personal configs
* **Performance**: N/A — low volume CRUD

## 5. Design & UI/UX

N/A — backend-only infrastructure story. UI comes in NOTIF-03.

## 6. Implementation Notes

- Extend `NotificationAdapter` interface: add `String getIdentifier()` method (e.g., returns "IN_APP", "MATTERMOST")
- New `AdapterRegistry` service: wraps `List<NotificationAdapter>`, provides `getByIdentifier(String)` and `filterByIdentifiers(List<String>)`
- Modify `NotificationDispatchService.dispatch()`: accept optional `List<String> targetAdapters` parameter. When non-null, filter via registry.
- `SeverityTransitionJob`: pass `subscription.getAdapters()` as targetAdapters to dispatch (wires up existing stored field)
- New domain: `api/adapter-config/` with standard layered structure (controller, service, model, repository)
- Entity: `AdapterConfig` with JPA mappings
- Existing `InAppNotificationAdapter`: add `getIdentifier()` returning `"IN_APP"`
- Liquibase migration for `adapter_config` table

## 7. Test Impact Analysis

### Existing tests affected by this change:

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| Tests for `NotificationDispatchService` | dispatch methods | Fans out to all adapters | YES | Update to verify filtering behavior |
| Tests for `SeverityTransitionJob` | severity transition dispatch | Calls dispatch without adapter filter | YES | Update to pass subscription.adapters |
| Tests for `InAppNotificationAdapter` | send method | Adapter sends notification | NO | Keep — add getIdentifier test |

### Test modification policy:

- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified: NotificationDispatchService tests, SeverityTransitionJob tests

### Potential files impacted by implementation

| File | Change |
|------|--------|
| `notification/adapter/NotificationAdapter.java` | Add `getIdentifier()` method |
| `notification/adapter/InAppNotificationAdapter.java` | Implement `getIdentifier()` |
| `notification/service/NotificationDispatchService.java` | Accept targetAdapters param, filter via registry |
| `subscription/job/SeverityTransitionJob.java` | Pass subscription.adapters to dispatch |
| New: `adapter-config/` domain package | Full CRUD stack |
| Liquibase changelog | New `adapter_config` table |
