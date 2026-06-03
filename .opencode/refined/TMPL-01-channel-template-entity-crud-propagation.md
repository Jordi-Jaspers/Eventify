---
epic: "TMPL"
title: "Channel Template Entity, CRUD & Propagation"
estimate: L
status: ready
created: 2026-06-02
depends_on: []
labels: [backend, watchlist, template]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user or org admin\
**I want** to create reusable channel templates that propagate changes to consuming watchlists\
**So that** I can maintain shared channel collections without manually updating every watchlist\

## 2. Business Context & Value

Groups are currently embedded JSONB — edits require manual updates in every watchlist. Templates introduce live-linked, propagating channel collections. Edit once, propagate everywhere. This reduces maintenance burden for power users managing many watchlists with overlapping channel sets.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Create personal template
    * Given an authenticated user
    * When they create a template with name "Production Servers" and channel IDs [1, 5, 12]
    * Then a `channel_template` row is persisted with scope=PERSONAL, user_id set, organization_id=null
* [ ] **Scenario 2**: Create org template
    * Given an org owner/admin
    * When they create a template with name "Critical Infra" scoped to their org
    * Then it is persisted with scope=ORG, organization_id set, accessible to all org members
* [ ] **Scenario 3**: Edit template — channels propagate
    * Given template T1 with channels [1, 2, 3] used by watchlists W1 and W2
    * When user adds channel 4 to T1
    * Then W1 and W2's `watchlist_channel` junction rows are asynchronously rebuilt to include channel 4
* [ ] **Scenario 4**: Edit template — metadata tracked
    * Given template T1 edited by user with email "dev@example.com"
    * When the edit is saved
    * Then `last_edited_at` and `last_edited_by` (email string) are updated on the template
* [ ] **Scenario 5**: Delete template — removes from consuming watchlists
    * Given template T1 used by watchlists W1 and W2
    * When user deletes T1 and confirms the destructive action
    * Then T1 is deleted, template_ref pointers removed from W1/W2 JSONB, junction rows rebuilt
* [ ] **Scenario 6**: Delete template — confirmation shows usage count
    * Given template T1 used by 3 watchlists
    * When user initiates delete
    * Then confirmation shows "This template is used in 3 watchlists. Channels will be removed from all."
* [ ] **Scenario 7**: List templates with usage count
    * Given user has 3 personal templates
    * When they call GET /v1/channel-templates
    * Then response includes each template with `usageCount` (number of referencing watchlists)
* [ ] **Scenario 8**: Nightly reconciliation job
    * Given a template was edited but async sync failed (edge case)
    * When the nightly reconciliation job runs
    * Then all `watchlist_channel` junction rows are rebuilt from current JSONB + resolved templates
* [ ] **Scenario 9**: Duplicate channels within template rejected
    * Given a template with channels [1, 2, 3]
    * When user tries to add channel 2 again
    * Then validation rejects with "Channel already exists in this template"
* [ ] **Edge Case**: Channel deleted — cascade removes from template
    * Given template T1 containing channel 5
    * When channel 5 is deleted from the system
    * Then channel 5 is removed from T1's channel list (and propagation triggers)

## 4. Technical Requirements

* **API Changes**:
    - `POST /v1/channel-templates` — create (body: name, scope, organizationId?, channelIds)
    - `GET /v1/channel-templates` — list user's templates (query: scope=personal|org, orgId)
    - `GET /v1/channel-templates/{id}` — get single with usage count
    - `PUT /v1/channel-templates/{id}` — update (name, channelIds)
    - `DELETE /v1/channel-templates/{id}` — delete with cascade
    - `GET /v1/channel-templates/{id}/watchlists` — list consuming watchlists
* **Database**:
    - New table `channel_template`:
      ```sql
      id UUID PK DEFAULT gen_random_uuid(),
      name VARCHAR(100) NOT NULL,
      scope VARCHAR(20) NOT NULL CHECK (scope IN ('PERSONAL', 'ORG')),
      user_id UUID NOT NULL REFERENCES "user"(id),
      organization_id UUID NULL REFERENCES organization(id),
      channel_ids JSONB NOT NULL DEFAULT '[]',
      last_edited_by VARCHAR(255),  -- email string, not FK
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
      ```
    - New junction `template_watchlist`:
      ```sql
      template_id UUID NOT NULL REFERENCES channel_template(id) ON DELETE CASCADE,
      watchlist_id BIGINT NOT NULL REFERENCES watchlist(id) ON DELETE CASCADE,
      PRIMARY KEY (template_id, watchlist_id)
      ```
    - Unique constraint: `(user_id, lower(name))` WHERE scope='PERSONAL'
    - Unique constraint: `(organization_id, lower(name))` WHERE scope='ORG'
* **Security**:
    - Personal templates: owner only
    - Org templates: OWNER/ADMIN can create/edit/delete, all org members can read/reference
    - Validate channelIds belong to user (personal) or org (org scope)
* **Performance**:
    - Async propagation via Spring `@Async` or application events
    - Nightly job: `@Scheduled(cron = "0 0 3 * * *")` — full junction rebuild
    - Template usage count: COUNT on `template_watchlist` junction

## 5. Design & UI/UX

N/A — backend-only. UI in TMPL-02.

## 6. Implementation Notes

- New domain package: `api/channel-template/` with standard layered structure
- Entity: `ChannelTemplate` with JPA mappings
- Propagation: on template update → query `template_watchlist` → for each watchlist, call `syncWatchlistChannels()`
- Extend `WatchlistConfiguration` model: add `ConfigTemplateRefItem` type alongside groups/channels
- Extend `syncWatchlistChannels()`: resolve template_ref items → flatten channelIds from referenced templates
- Junction maintenance: when watchlist is saved with template_ref → upsert `template_watchlist` rows
- Delete cascade: remove template → find consuming watchlists → remove template_ref from JSONB → rebuild junction
- Channel deletion cascade: add listener/hook — when channel deleted, remove from all templates containing it, trigger propagation
- Reconciliation job: iterate all watchlists, rebuild junction from scratch (idempotent DELETE + reinsert)

## 7. Test Impact Analysis

### Existing tests affected by this change:

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `WatchlistServiceTest` | syncWatchlistChannels | Syncs from groups+standalone only | YES | Update to also resolve template_refs |
| `UserWatchlistServiceTest` | create/update | Validates config structure | YES | Accept template_ref items |
| `SeverityTransitionJobTest` | finds watchlists for channel | Uses junction table | NO | Keep — junction still works same way |

### Test modification policy:

- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified: WatchlistService tests (syncWatchlistChannels), UserWatchlistService tests

### Potential files impacted by implementation

| File | Change |
|------|--------|
| `watchlist/model/WatchlistConfiguration.java` | Add template_ref item type |
| `watchlist/service/WatchlistService.java` | Extend syncWatchlistChannels to resolve templates |
| New: `channel-template/` domain package | Full CRUD + propagation |
| Liquibase changelog | New tables |
| Channel deletion handler | Remove from templates on cascade |
