---
epic: "ORGMGMT"
title: "Sidebar Restructure and Route Renames"
estimate: M
status: ready
created: 2026-05-22
depends_on: []
labels: [frontend]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user\
**I want** the sidebar navigation to prioritize monitoring and watchlists\
**So that** I can quickly access the most important operational views\

## 2. Business Context & Value
Current sidebar order buries Monitor under Watchlists. Reordering puts operational views first. Route renames (`/admin/resources` → `/admin/manage`) improve clarity. Removing the org dashboard route eliminates a redundant page.

## 3. Acceptance Criteria
* [ ] **User workspace sidebar order**
    * Given a logged-in user
    * When viewing the sidebar
    * Then items appear in order: Dashboard → Monitor → Watchlists → Channels
* [ ] **Org workspace sidebar order**
    * Given a user viewing an org context
    * When viewing the sidebar
    * Then items appear: Monitor → Watchlists → Channels → Members (admin/owner) → Statistics (admin/owner) → Settings (admin/owner)
* [ ] **Admin sidebar order and rename**
    * Given an admin user
    * When viewing the admin section
    * Then items appear: Manage → Tools → Statistics
    * And "Manage" links to `/admin/manage`
* [ ] **Monitor route promoted**
    * Given the user workspace
    * When navigating to Monitor
    * Then the route is `/monitor` (not `/watchlists/monitor`)
    * And org monitor is `/organizations/[orgId]/monitor`
* [ ] **Admin resources route renamed**
    * Given an admin navigating to resource management
    * When clicking "Manage"
    * Then the route is `/admin/manage` with sub-routes `/admin/manage/users`, `/admin/manage/organizations`, `/admin/manage/api-keys`
* [ ] **Org dashboard route removed**
    * Given the route `/organizations/[orgId]/dashboard`
    * When accessed
    * Then it returns 404 (route deleted, no redirect needed)
* [ ] **All internal links updated**
    * Given any component referencing old routes (share buttons, breadcrumbs, programmatic navigation)
    * When the app runs
    * Then no broken links exist — all references point to new routes

## 4. Technical Requirements
* **API Changes**: N/A — frontend-only routing changes
* **Database**: N/A
* **Security**: N/A — same auth guards apply to new paths
* **Performance**: N/A

## 5. Design & UI/UX
* Sidebar visual structure unchanged — only order and labels change
* "Resources" label → "Manage" with same `Database` icon (or consider `FolderCog`)
* Monitor uses `Activity` icon, Statistics uses `BarChart3` icon

## 6. Implementation Notes
* **Sidebar config**: `client/src/lib/components/layout/AppSidebarNav.svelte` — reorder items
* **Routes config**: `client/src/lib/config/routes.ts` — update path constants
* **File moves**:
  - `client/src/routes/(authenticated)/watchlists/monitor/` → `client/src/routes/(authenticated)/monitor/`
  - `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/` → `client/src/routes/(authenticated)/organizations/[orgId]/monitor/`
  - `client/src/routes/(authenticated)/admin/resources/` → `client/src/routes/(authenticated)/admin/manage/`
  - Delete `client/src/routes/(authenticated)/organizations/[orgId]/dashboard/`
* **Grep for old routes**: Search entire client for `/watchlists/monitor`, `/admin/resources`, `/organizations/[orgId]/dashboard` and update all references
* **Add org Statistics placeholder route**: `client/src/routes/(authenticated)/organizations/[orgId]/statistics/` (empty page with "Coming Soon" or gate for ORGMGMT-04)

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| `client/src/lib/components/layout/AppSidebarNav.svelte` | Reorder items, update paths, rename "Resources" → "Manage" |
| `client/src/lib/config/routes.ts` | Update route constants |
| `client/src/routes/(authenticated)/watchlists/monitor/` | Move to `/monitor/` |
| `client/src/routes/(authenticated)/organizations/[orgId]/watchlists/monitor/` | Move to `/organizations/[orgId]/monitor/` |
| `client/src/routes/(authenticated)/admin/resources/` | Move to `/admin/manage/` |
| `client/src/routes/(authenticated)/organizations/[orgId]/dashboard/` | Delete |
| All files referencing old routes | Update paths |
