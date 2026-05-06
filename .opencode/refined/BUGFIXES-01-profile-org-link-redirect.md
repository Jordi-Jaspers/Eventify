---
epic: "BUGFIXES"
title: "Fix profile page org link redirecting to wrong location"
estimate: S
status: ready
created: 2026-05-06
depends_on: []
labels: [frontend, backend, bug, dx]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user viewing my profile page
**I want** clicking an organization name to take me to that organization's dashboard with the correct context
**So that** I can quickly navigate to my orgs without landing on a 404 or seeing a sidebar that shows a different org than the page content

## 2. Business Context & Value
The organization links on the user profile page (`OrganizationMembershipCard.svelte`) are broken: they build URLs from `organizationSlug` (e.g. `/organizations/acme-corp`), but every org route in the app expects a numeric `[orgId]` (e.g. `/organizations/12345/dashboard`). Users either hit a broken route or see inconsistent UI state.

Root cause is twofold:
1. The link uses the wrong field and wrong route shape (no `/dashboard` suffix, no store sync).
2. The backend OpenAPI schema marks every `UserOrganizationResponse` field as `NOT_REQUIRED`, even though the mapper always populates them. This forces consumers (sidebar, profile, dropdowns) to use non-null assertions (`organizationId!`) — masking real type-safety.

Fixing both yields a working profile link **and** removes a class of `!` non-null assertions across the frontend. Side effect: any future API consumer gets honest types.

## 3. Acceptance Criteria

* [ ] **Scenario 1 — Profile org link navigates correctly with context switch**
    * Given I am on `/profile` and a member of organization "Acme" (id=42)
    * When I click "Acme" in the organization memberships list
    * Then I am navigated to `/organizations/42/dashboard`
    * And `organizationStore.currentOrgId` equals `42`
    * And the `currentOrganizationId` cookie equals `"42"`
    * And the sidebar shows "Acme" as the active organization

* [ ] **Scenario 2 — Backend marks UserOrganizationResponse fields as required**
    * Given the backend `UserOrganizationResponse` DTO
    * When the OpenAPI schema is generated
    * Then `organizationId`, `organizationName`, `organizationSlug`, `role`, and `joinedAt` are all marked `required: true`
    * And the regenerated TypeScript type (`bun run sync:api`) declares these fields as non-optional

* [ ] **Scenario 3 — Frontend non-null assertions removed**
    * Given the regenerated `UserOrganizationResponse` TypeScript type
    * When `AppSidebarNav.svelte`, `AppSidebarUser.svelte`, `OrganizationMembershipCard.svelte`, and `organization.svelte.ts` are inspected
    * Then no `organizationId!` non-null assertions remain on `UserOrganizationResponse` field accesses
    * And the project still type-checks (`bun run check` / build passes)

* [ ] **Edge Case — Right-click "Open in new tab" still works**
    * Given the profile page is rendered
    * When I right-click an organization name and select "Open in new tab"
    * Then the new tab opens at `/organizations/{orgId}/dashboard` (the link is a real `<a href>`, not a button)
    * Note: store/cookie sync only fires on actual click in the current tab; new-tab navigation will fall back to standard initialization on the new page load.

## 4. Technical Requirements

* **API Changes**:
    * `UserOrganizationResponse.java`: change all five `@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)` to `Schema.RequiredMode.REQUIRED`
    * No endpoint signatures change; only OpenAPI metadata
    * Frontend regenerated via `bun run sync:api` (from `client/`)

* **Database**: N/A — no schema changes

* **Security**: N/A — no auth/permission changes; existing route guards already cover `/organizations/[orgId]/*`

* **Performance**: N/A — single-component change

## 5. Design & UI/UX

No visual changes. Behavioral change only: clicking an org link now switches active org context AND navigates, matching the established pattern from `AppSidebarUser.handleOrgSwitch`.

The `<a>` tag is preserved (not converted to `<button>`) for accessibility — keyboard users, screen readers, and right-click "Open in new tab" continue to work. The click handler runs `organizationStore.switchOrganization(id)` then SvelteKit's `goto()` for client-side navigation, while the `href` provides a valid fallback URL.

## 6. Implementation Notes

**Files involved (verified via research):**

| File | Change |
|------|--------|
| `server/src/main/java/io/github/eventify/api/organization/model/response/UserOrganizationResponse.java` | All 5 `@Schema` annotations: `NOT_REQUIRED` → `REQUIRED` |
| `client/src/lib/components/profile/OrganizationMembershipCard.svelte` | Replace broken `<a href="/organizations/{slug}">` with `<a href={CLIENT_ROUTES.ORGANIZATION_DASHBOARD_PAGE(membership.organizationId).path} onclick={handleClick}>` that calls `organizationStore.switchOrganization(membership.organizationId)` then `goto(...)` (with `event.preventDefault()`) |
| `client/src/lib/api/models` (generated) | Regenerate via `bun run sync:api` from `client/` |
| `client/src/lib/components/layout/AppSidebarNav.svelte` | Remove `!` from `currentOrganization.organizationId!` (lines 88, 89, 97, 98, 106, 107, 115, 116, 124, 125, 134) |
| `client/src/lib/components/layout/AppSidebarUser.svelte` | Remove `!` from `org.organizationId!` (line 232) |
| `client/src/lib/stores/organization.svelte.ts` | Remove any `!` on `UserOrganizationResponse` fields if present after regeneration |
| `client/src/lib/api/organization/service/OrganizationDetailsService.svelte.ts` | Remove `?? 'N/A'` fallback on `organizationSlug` if no longer needed (line 66) |

**Patterns to follow:**
- Org switch + navigate pattern lives in `AppSidebarUser.handleOrgSwitch` — mirror it
- Use `CLIENT_ROUTES.ORGANIZATION_DASHBOARD_PAGE(id).path` from `client/src/lib/config/routes.ts`, not hand-built strings
- Use `goto()` from `$app/navigation`, not `window.location`

**Pitfalls:**
- After backend change, you MUST run `bun run sync:api` from `client/` before frontend changes will type-check
- `.svelte` component must `event.preventDefault()` in `onclick` to avoid double navigation (browser anchor + `goto`)
- Verify the cookie write happens (in `setCurrentOrgId`) before `goto` triggers data loading on the destination page — `switchOrganization` is synchronous, so order in handler is enough

## 7. Test Impact Analysis

This is a refactoring + bug fix story. Existing tests asserting current behavior must be reviewed.

### Existing tests affected by this change:

| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| `server/src/test/.../organization/...UserOrganizationResponse*Test.java` (if any) | Schema/serialization tests | Field presence/types | Likely NO | Keep — required vs optional doesn't affect serialization |
| `server/src/test/.../OrganizationMembershipMapperTest.java` (if exists) | Mapper populates all fields | All fields non-null in mapped output | NO | Keep — already asserts non-null behavior; now schema matches |
| `client/tests/**/profile*.spec.ts` (Playwright) | Profile page rendering | Org card shows name | Possibly YES | Update if it asserts the broken `/organizations/{slug}` href; otherwise keep |
| `client/src/**/*.test.ts` (Vitest) for `OrganizationMembershipCard` | Card renders link | Likely none currently | NO | Add a new test asserting href matches `/organizations/{id}/dashboard` and click triggers `switchOrganization` |

**Action for the testing agent BEFORE implementation:**
- Search `server/src/test` for `UserOrganizationResponse` and `OrganizationMembershipMapper` references
- Search `client/tests/` and `client/src/` for `OrganizationMembershipCard`, `organizationSlug`, and `/organizations/` href assertions
- Report findings; update only those that explicitly assert the buggy behavior

### Test modification policy:
- [x] Existing tests MAY be updated where they assert behavior being moved (the broken URL shape)
- [x] Specific files that may be modified: only tests that hard-code the old `/organizations/{slug}` URL shape

### Files to modify (MANDATORY):

| File | Change |
|------|--------|
| `server/src/main/java/io/github/eventify/api/organization/model/response/UserOrganizationResponse.java` | 5 × `RequiredMode.NOT_REQUIRED` → `RequiredMode.REQUIRED` |
| `client/src/lib/components/profile/OrganizationMembershipCard.svelte` | Replace anchor + add click handler with store switch |
| `client/src/lib/components/layout/AppSidebarNav.svelte` | Remove `!` non-null assertions on `organizationId` |
| `client/src/lib/components/layout/AppSidebarUser.svelte` | Remove `!` non-null assertion on `organizationId` |
| `client/src/lib/api/models/*` (auto-generated) | Regenerate via `bun run sync:api` |

**Verification commands:**
- `./gradlew test` (server)
- `cd client && bun run check && bun run test` (frontend type-check + unit tests)
- Manual: log in → `/profile` → click any org name → confirm correct dashboard + sidebar shows that org
