---
epic: "TMPL"
title: "Save Group as Template"
estimate: M
status: ready
created: 2026-06-02
depends_on: ["TMPL-01-channel-template-entity-crud-propagation", "TMPL-02-template-manager-ui-editor-integration"]
labels: [fullstack, watchlist, template]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user with an existing inline group in a watchlist\
**I want** to convert it into a reusable template with one action\
**So that** I can reuse that channel collection across other watchlists without recreating it\

## 2. Business Context & Value

Users already have well-curated groups in their watchlists. This feature provides a friction-free path to adopt templates by converting existing work rather than starting from scratch.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Save group as template action available
    * Given a watchlist with an inline group "DB Servers" containing channels [1, 5, 8]
    * When user opens group context menu / action button
    * Then "Save as Template" option is visible
* [ ] **Scenario 2**: Save as template — modal flow
    * Given user clicks "Save as Template" on group "DB Servers"
    * When the modal opens
    * Then it pre-fills: name = "DB Servers", scope = PERSONAL (default), shows channel list, propagation warning
* [ ] **Scenario 3**: Propagation warning in modal
    * Given the save-as-template modal
    * When displayed
    * Then it shows: "Future edits to this template will propagate to all watchlists using it."
* [ ] **Scenario 4**: On confirm — template created + group replaced
    * Given user confirms with name "DB Servers" and scope PERSONAL
    * When they click Save
    * Then a new template is created with channels [1, 5, 8] AND the inline group in the watchlist JSONB is replaced with a `template_ref` pointing to the new template
* [ ] **Scenario 5**: Org scope selection
    * Given user is an org admin editing an org watchlist group
    * When they open save-as-template modal
    * Then scope options show: PERSONAL and ORG (org name) — org pre-selected for org watchlists
* [ ] **Scenario 6**: Name conflict
    * Given user already has a template named "DB Servers"
    * When they try to save another group with the same name
    * Then validation shows "Template name already exists" and they must choose a different name
* [ ] **Edge Case**: Empty group
    * Given a group with 0 channels
    * When user tries "Save as Template"
    * Then action is disabled with tooltip "Add channels to group before saving as template"

## 4. Technical Requirements

* **API Changes**:
    - `POST /v1/channel-templates/from-group` — create template + return ID (body: name, scope, orgId?, channelIds)
    - OR reuse `POST /v1/channel-templates` and handle replacement client-side (simpler — recommended)
* **Database**: N/A — uses existing channel_template table from TMPL-01
* **Security**: Same as template creation — scope-based validation
* **Performance**: N/A — single operation

## 5. Design & UI/UX

- **Trigger**: Context menu item or icon button on group row in WatchlistBuilder → "Save as Template"
- **Modal contents**:
  - Name (text input, pre-filled with group name)
  - Scope (radio: Personal / Org — org option only if in org watchlist context and user is admin)
  - Channel list (read-only, showing what will be in template)
  - Warning banner: "Future edits to this template will propagate to all watchlists using it."
  - Buttons: Cancel / "Create Template & Replace Group"
- **After confirm**: Group visually transforms into template_ref card (animation optional)

## 6. Implementation Notes

- Frontend-only orchestration (recommended approach):
  1. Call `POST /v1/channel-templates` with group's channels + name + scope
  2. On success, replace the `ConfigGroupItem` in local state with `ConfigTemplateRefItem { templateId: newId }`
  3. Auto-save triggers, persisting the updated JSONB to backend
- New component: `SaveAsTemplateModal.svelte`
- Add "Save as Template" to `ConfigGroup` component's action menu
- Disable if group has 0 channels

## 7. Test Impact Analysis

### Test modification policy:

- [x] No existing tests should be modified (additive feature)

### Potential files impacted by implementation

| File | Change |
|------|--------|
| `client/src/lib/components/watchlist/ConfigGroup.svelte` (or equivalent) | Add "Save as Template" action |
| New: `client/src/lib/components/template/SaveAsTemplateModal.svelte` | Modal component |
| `client/src/lib/components/watchlist/WatchlistBuilder.svelte` | Handle group→template replacement in state |
