---
epic: "TMPL"
title: "Template Manager UI & Editor Integration"
estimate: L
status: ready
created: 2026-06-02
depends_on: ["TMPL-01-channel-template-entity-crud-propagation"]
labels: [frontend, watchlist, template]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user managing watchlists\
**I want** a template manager page and the ability to add template references in the watchlist editor\
**So that** I can create, browse, and use templates within my watchlists\

## 2. Business Context & Value

The backend (TMPL-01) provides template CRUD and propagation. This story delivers the user-facing interface: a dedicated template management page and integration into the existing `WatchlistBuilder` editor so users can reference templates alongside inline groups and channels.

## 3. Acceptance Criteria

* [ ] **Scenario 1**: Template manager page — personal
    * Given an authenticated user
    * When they navigate to the Templates page
    * Then they see a list of their personal templates with name, channel count, usage count, and last edited info
* [ ] **Scenario 2**: Template manager page — org (admin/owner only)
    * Given an org owner/admin navigating to `/org/[orgId]/templates`
    * When the page loads
    * Then they see org-scoped templates with full edit/delete capabilities
* [ ] **Scenario 2b**: Org template route hidden from members
    * Given an org member with MEMBER role
    * When they view the org navigation
    * Then the "Templates" nav item is not visible and the route returns 403 if accessed directly
* [ ] **Scenario 3**: Create template from manager
    * Given a user on the template manager page
    * When they click "New Template", enter name, select channels, and save
    * Then the template is created and appears in the list
* [ ] **Scenario 4**: Edit template — channel picker
    * Given an existing template
    * When user clicks edit
    * Then they see a channel picker to add/remove channels, with save triggering propagation
* [ ] **Scenario 5**: Delete template from manager
    * Given a template used by 2 watchlists
    * When user clicks delete
    * Then destructive confirmation shows "Used in 2 watchlists. Channels will be removed." + requires confirm
* [ ] **Scenario 6**: Add template reference in watchlist editor
    * Given a user editing a watchlist in WatchlistBuilder
    * When they click "Add Template" (new button alongside "Add Channel" / "Add Group")
    * Then a sheet/modal shows available templates (personal + org if applicable) to select
* [ ] **Scenario 7**: Template ref displayed in editor
    * Given a watchlist referencing template "Production Servers" with 5 channels
    * When the editor renders
    * Then the template appears as a distinct card/row showing: template icon, name, "5 channels", expand toggle
* [ ] **Scenario 8**: Template ref expand to show channels (read-only)
    * Given a template_ref item in the editor
    * When user expands it
    * Then the resolved channels are shown as read-only list (cannot edit inline — must edit via template manager)
* [ ] **Scenario 9**: Remove template reference from watchlist
    * Given a watchlist with a template_ref
    * When user removes it (X button or drag-to-remove)
    * Then the template_ref is removed from JSONB and junction rebuilt (template itself unchanged)
* [ ] **Edge Case**: Template with 0 channels
    * Given an empty template is referenced
    * When editor renders
    * Then it shows "0 channels — edit template to add channels" with link to template manager

## 4. Technical Requirements

* **API Changes**: Uses TMPL-01 endpoints. No new backend endpoints.
* **Database**: N/A
* **Security**: Org template route restricted to OWNER/ADMIN — nav hidden + 403 on direct access. Backend enforces same.
* **Performance**: Template channel resolution done server-side in watchlist GET (denormalized in response)

## 5. Design & UI/UX

- **Template Manager**: New page at `/templates` (personal) and `/org/[orgId]/templates` (org — admin/owner only)
  - Org nav item hidden for MEMBER role; route guarded (403)
  - Table/card list: name, scope badge, channel count pill, usage count, last edited by + date
  - Actions: Edit, Delete (with destructive confirm showing usage)
  - Create: modal/inline form with channel multi-select
- **WatchlistBuilder integration**:
  - New "Add Template" button in toolbar alongside existing "Add Channel" / "Add Group"
  - Template items render as a distinct card (different background/border) with template icon
  - Collapsed: shows name + channel count badge
  - Expanded: read-only channel list with "Edit in Template Manager" link
  - Drag-to-reorder works same as groups/channels
- **Frontend type extension**: `ConfigTemplateRefItem { id: string, type: 'template_ref', templateId: string, template: { name, channelCount, channels? } }`

## 6. Implementation Notes

- New frontend routes: `/templates`, `/templates/new`, `/org/[orgId]/templates`
- Extend `ConfigItem` union type: add `ConfigTemplateRefItem`
- New components: `TemplateList`, `TemplateCard`, `TemplateForm`, `ConfigTemplateRef` (for editor)
- `WatchlistBuilder`: add "Add Template" button → opens `TemplateSelectSheet` showing user's available templates
- API client: add `channelTemplate` resource to openapi-fetch typed client
- Watchlist GET response should include resolved template data (name, channel count) for each template_ref — may need backend DTO extension
- Existing `Configurator` component: handle `template_ref` type in the item rendering switch

## 7. Test Impact Analysis

### Test modification policy:

- [x] No existing tests should be modified (greenfield frontend + additive type extension)

### Potential files impacted by implementation

| File | Change |
|------|--------|
| `client/src/lib/components/watchlist/types.ts` | Add ConfigTemplateRefItem to union |
| `client/src/lib/components/watchlist/WatchlistBuilder.svelte` | Add template button + render template_ref |
| `client/src/lib/components/watchlist/Configurator.svelte` | Handle template_ref item type |
| New: `client/src/routes/(authenticated)/templates/` | Template manager pages |
| New: `client/src/lib/components/template/` | Template components |
| OpenAPI types | Regenerate for channel-template endpoints |
