---
epic: "ADMIN"
title: "Restructure Admin Routes with Tabbed Sections"
estimate: M
status: ready
created: 2026-05-08
depends_on: []
labels: [frontend, refactor, ux]
priority: P1
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** global admin\
**I want** the admin area organized into Statistics / Resources / Tools sections with tabs\
**So that** related management screens are grouped, the sidebar stays compact as we add more admin tools, and URLs reflect the conceptual structure of admin work.\

## 2. Business Context & Value
Today the admin sidebar lists four flat entries (Dashboard, Users, Organizations, API Keys). Upcoming work (NOTIF-01..05, AUDIT-01..02, future admin utilities) will balloon this to 8+ entries — unsustainable. Industry standard (Stripe, Vercel, Linear) groups admin under conceptual sections with tabs for siblings. This story rebuilds the URL structure and sidebar to that pattern. Foundation for NOTIF-02, AUDIT-02, and any future admin tool. No production deploy yet → no redirects needed; rename freely.

## 3. Acceptance Criteria
* [ ] **Scenario 1: New URL structure**
    * Given a logged-in global admin
    * When they navigate to `/admin/statistics`, `/admin/resources/users`, `/admin/resources/organizations`, `/admin/resources/api-keys`, or `/admin/tools`
    * Then each route renders the correct content (statistics page; resources page with the requested tab active; tools page with first available tab — Notifications added in NOTIF-02 — or an empty state if no tools exist yet)
* [ ] **Scenario 2: Old URLs removed**
    * Given the old admin routes (`/admin/dashboard`, `/admin/users`, `/admin/organizations`, `/admin/api-keys`)
    * When a user visits any of them
    * Then SvelteKit returns a 404 (routes are deleted, no redirects since not in production)
* [ ] **Scenario 3: Sidebar grouping**
    * Given the admin sidebar section
    * When rendered for a global admin
    * Then exactly three entries appear: Statistics (`/admin/statistics`), Resources (`/admin/resources` → defaults to Users tab), Tools (`/admin/tools` → defaults to first available tab)
    * And each entry uses an appropriate icon (Statistics=`ChartBar`, Resources=`Database` or `Boxes`, Tools=`Wrench`)
* [ ] **Scenario 4: Tab navigation under Resources**
    * Given a user on `/admin/resources/users`
    * When they click the "Organizations" tab
    * Then the URL changes to `/admin/resources/organizations` and the page content swaps without a full page reload (SvelteKit nested layout)
    * And the active tab visual state reflects the current URL
* [ ] **Scenario 5: Deep-link to nested entity pages**
    * Given a URL like `/admin/resources/users/{userId}` (user detail page)
    * When visited directly
    * Then the user detail page renders (no tab bar — drill-in pages are full-screen, not tabbed)
* [ ] **Scenario 6: Tools page empty state (this story only)**
    * Given the Tools page has no tools yet (NOTIF-02 not yet shipped)
    * When an admin visits `/admin/tools`
    * Then an empty state is shown: "No tools available yet" with a brief description; sidebar entry remains visible
* [ ] **Edge Case: Non-admin access**
    * Given a regular user (no global admin role)
    * When they attempt to navigate to any `/admin/*` route
    * Then existing admin auth guard redirects them away (existing behavior preserved — no changes to auth)

## 4. Technical Requirements
* **API Changes**: N/A — pure frontend route refactor.
* **Database**: N/A — no schema changes.
* **Security**: Existing admin route guard (in `(authenticated)/admin/+layout.svelte` or equivalent) must apply to all new routes. No new authorization logic.
* **Performance**: Tab switching uses SvelteKit nested layouts — no full page reload. Use `+layout.svelte` at `/admin/resources/+layout.svelte` and `/admin/tools/+layout.svelte` to host tab bars; child routes render below.

## 5. Design & UI/UX
**Sidebar (Admin section):**
```
Admin
├── 📊 Statistics      → /admin/statistics
├── 📦 Resources       → /admin/resources       (defaults to first tab: Users)
└── 🔧 Tools           → /admin/tools            (defaults to first tab: Notifications when NOTIF-02 lands; empty state otherwise)
```

**Tab bar (Resources page):** horizontal tabs at top of page using shadcn-svelte `Tabs` component or a custom `<TabsNav>`. Tabs: Users / Organizations / API Keys. Active tab from URL match.

**Tab bar (Tools page):** same pattern. NOTIF-02 will add the Notifications tab. This story leaves it empty (or with one disabled placeholder).

**Statistics page:** identical content to current `/admin/dashboard` — only the URL and sidebar entry change.

**Page titles (browser tab):** `Admin · Statistics`, `Admin · Resources · Users`, `Admin · Tools`.

**Layout:** `(authenticated)/admin/+layout.svelte` keeps the auth guard. New nested layouts:
- `(authenticated)/admin/resources/+layout.svelte` — renders tab bar + `<slot />`
- `(authenticated)/admin/tools/+layout.svelte` — renders tab bar (or empty state) + `<slot />`

## 6. Implementation Notes
**Files to create:**
- `client/src/routes/(authenticated)/admin/statistics/+page.svelte` (move from `dashboard/+page.svelte`)
- `client/src/routes/(authenticated)/admin/statistics/+page.ts` (move from `dashboard/+page.ts` if exists)
- `client/src/routes/(authenticated)/admin/resources/+layout.svelte` (new — tab bar)
- `client/src/routes/(authenticated)/admin/resources/+page.svelte` (redirect to `users` tab — `goto('/admin/resources/users')`)
- `client/src/routes/(authenticated)/admin/resources/users/+page.svelte` (move from `users/+page.svelte`)
- `client/src/routes/(authenticated)/admin/resources/users/[userId]/+page.svelte` (move from `users/[userId]/+page.svelte` if exists)
- `client/src/routes/(authenticated)/admin/resources/organizations/+page.svelte` (move from `organizations/+page.svelte`)
- `client/src/routes/(authenticated)/admin/resources/organizations/[orgId]/+page.svelte` (move from `organizations/[orgId]/+page.svelte` if exists)
- `client/src/routes/(authenticated)/admin/resources/api-keys/+page.svelte` (move from `api-keys/+page.svelte`)
- `client/src/routes/(authenticated)/admin/tools/+layout.svelte` (new — tab bar / empty state)
- `client/src/routes/(authenticated)/admin/tools/+page.svelte` (empty state for now, will redirect to first tab once tools exist)

**Files to delete:**
- All old `admin/dashboard/`, `admin/users/`, `admin/organizations/`, `admin/api-keys/` directories.

**Sidebar component:**
- `client/src/lib/components/layout/AppSidebar.svelte` (or wherever admin nav is rendered) — replace four entries with three new entries pointing to the new URLs.

**Tab bar component (suggested reusable):**
- `client/src/lib/components/admin/AdminTabsNav.svelte` — accepts `tabs: Array<{label: string, href: string, icon?: Component}>` and uses `$page.url.pathname` to determine active tab.

**Tests:**
- Update Playwright E2E tests under `client/tests/` that hit any old admin URL — replace paths.
- Add a small smoke test: visit each new URL, assert page renders correctly.

**Pitfalls:**
- Any internal `goto('/admin/users')` calls in code must be updated to `/admin/resources/users`. Grep for `/admin/` strings before opening PR.
- Breadcrumb components (if any) need updating.
- Nested `[userId]`/`[orgId]` detail pages keep existing data loaders — only paths change.

## 7. Test Impact Analysis
### Existing tests affected by this change:
| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `client/tests/admin/*.spec.ts` (any that exist) | Tests hitting `/admin/dashboard`, `/admin/users`, `/admin/organizations`, `/admin/api-keys` | URL navigation + page contents | YES | Update URLs to new structure |

### Test modification policy:
- [x] Existing tests MAY be updated where they assert URL paths (the URLs are the behavior being moved)
- [ ] No backend tests affected (frontend-only change)

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `client/src/routes/(authenticated)/admin/dashboard/*` | Move to `admin/statistics/*` | all |
| `client/src/routes/(authenticated)/admin/users/*` | Move to `admin/resources/users/*` | all |
| `client/src/routes/(authenticated)/admin/organizations/*` | Move to `admin/resources/organizations/*` | all |
| `client/src/routes/(authenticated)/admin/api-keys/*` | Move to `admin/resources/api-keys/*` | all |
| `client/src/lib/components/layout/AppSidebar.svelte` (or equivalent) | Replace 4 admin entries with 3 grouped entries | TBD |
| Internal `goto()` / `<a href>` references to old admin URLs | Update to new paths | grep first |
| `client/tests/admin/*.spec.ts` | Update URL paths in any failing tests | TBD |
