---
epic: "UI"
title: "Reorganize user settings: merge Sessions + Connected Accounts into Security, reorder tabs"
estimate: S
status: ready
created: 2026-05-06
depends_on: []
labels: [frontend, ux, refactor]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** signed-in user managing my account\
**I want** a clearer settings layout where security-related controls live together and tabs follow a sensible order\
**So that** I can find sign-in methods, sessions, and developer tools quickly without scanning five disconnected tabs\

## 2. Business Context & Value
The current settings nav has 5 tabs in a non-intuitive order (Profile, Developer, Data & Storage, Sessions, Connected Accounts). "Sessions" and "Connected Accounts" are both security concerns shown as peers to unrelated tabs, while "Developer" — a power-user/edge feature — sits in the second slot. Consolidating Sessions + Connected Accounts into a single **Security** page reduces nav width, groups related concerns (matching industry conventions: GitHub, Google, GitLab all use a single Security hub), and lets us push Developer to the end where it belongs. This story is a prerequisite for AUTH-01, which extends the new Security page with change-password controls.

## 3. Acceptance Criteria
* [ ] **Scenario 1**: Settings nav renders the new four-tab order
    * Given I am a signed-in user on any settings page
    * When the `SettingsNav` component renders
    * Then I see exactly four tabs in this order: **Profile** → **Security** → **Data & Storage** → **Developer**
    * And no tab labeled "Sessions" or "Connected Accounts" appears
* [ ] **Scenario 2**: Security page combines both sections in one scrollable view
    * Given I navigate to `/profile/security`
    * When the page loads
    * Then I see two stacked Card sections in this order: **Sign-in methods** (the providers list) followed by **Active sessions** (the sessions table)
    * And both sections load their data independently on mount
    * And the page `<title>` is `Security - Eventify`
* [ ] **Scenario 3**: Sign-in methods section preserves existing connected-accounts behaviour
    * Given the Security page is open
    * When the sign-in methods section finishes loading
    * Then each provider row renders via the existing `ConnectedAccountRow` component
    * And the unlink confirm `AlertDialog` continues to work as before (open/close, confirm, cancel)
* [ ] **Scenario 4**: Active sessions section preserves existing sessions behaviour
    * Given the Security page is open
    * When the sessions section finishes loading
    * Then sessions render via the existing `SessionsTable` component
    * And the "Revoke all other sessions" button appears in the section header when more than one session exists
    * And revoking flows (single + all-others) continue to work as before
* [ ] **Scenario 5**: Old routes are removed entirely
    * Given the codebase after this change
    * When I search for `/profile/connected-accounts` or `/profile/sessions`
    * Then I find zero references (route folders deleted, route constants removed, no redirects added — feature is not in production)
* [ ] **Scenario 6**: Tab active-state highlighting works on the new Security page
    * Given I am on `/profile/security`
    * When `SettingsNav` evaluates `currentPath`
    * Then the **Security** tab is highlighted as active (`border-primary text-primary font-semibold`)
* [ ] **Edge Case**: Independent loading states do not block each other
    * Given the Security page is loading
    * When the sign-in methods request is slower than the sessions request (or vice versa)
    * Then each section shows its own loading skeleton independently and renders its content as soon as its own data arrives

## 4. Technical Requirements
* **API Changes**: N/A — no backend changes; reuses existing `ConnectedAccountsService` and `SessionService`.
* **Database**: N/A — no schema changes.
* **Security**: N/A — no auth changes; routes remain `RouteType.PRIVATE`.
* **Performance**: Both data fetches must be issued in parallel on mount (do not chain). Each section owns its own loading state and skeleton — neither blocks the other from rendering.

## 5. Design & UI/UX
**Tab order & icons** (`SettingsNav.svelte`):

| Order | Label          | Route                  | Icon (lucide)    |
|-------|----------------|------------------------|------------------|
| 1     | Profile        | `/profile`             | `User`           |
| 2     | Security       | `/profile/security`    | `Shield`         |
| 3     | Data & Storage | `/profile/data-storage`| `Database`       |
| 4     | Developer      | `/developer`           | `Code2`          |

Drop these imports/usages from `SettingsNav.svelte`: `Link2` icon, `PROFILE_SESSIONS_PAGE`, `PROFILE_CONNECTED_ACCOUNTS_PAGE`.

**Security page layout** (`/profile/security/+page.svelte`):
- Reuse the existing wrapper pattern from current `connected-accounts/+page.svelte` and `sessions/+page.svelte`:
  - `<SettingsNav currentPath={CLIENT_ROUTES.PROFILE_SECURITY_PAGE.path} />`
  - `<main class="container mx-auto px-4 py-8">` → `<div class="max-w-4xl mx-auto space-y-6 animate-fade-in">`
- Two `Card` sections inside the wrapper, stacked with the existing `space-y-6` gap:
  1. **Sign-in methods** — copy CardHeader/CardContent block from `connected-accounts/+page.svelte` (lines 27–65) verbatim. Title: `Sign-in methods` (renamed from "Connected Accounts"). Icon: `Link2`. Description unchanged.
  2. **Active sessions** — copy CardHeader/CardContent block from `sessions/+page.svelte` (lines 26–63) verbatim. Title and description unchanged. Icon: `Shield`. Keep the conditional "Revoke all other sessions" button in the header.
- Both services instantiated in `<script>`: `const accountsService = createConnectedAccountsService(); const sessionService = createSessionService();`
- Both load on mount in parallel: `onMount(() => { accountsService.load(); sessionService.load(); });`
- Move the unlink `AlertDialog.Root` block (current `connected-accounts/+page.svelte` lines 69–95) to the bottom of the new security page, bound to `accountsService`.
- Preserve the gradient overlay treatment on each Card (the `absolute inset-0 bg-gradient-to-br ...` div is already part of each section copy).

**No redirects** — feature is pre-production per user direction. Update all internal links directly.

## 6. Implementation Notes
**Route constant changes** in `client/src/lib/config/routes.ts`:
- **Add** (line ~70):
  ```ts
  PROFILE_SECURITY_PAGE: {
      path: '/profile/security',
      type: RouteType.PRIVATE
  },
  ```
- **Remove**: `PROFILE_SESSIONS_PAGE` (lines 70–73) and `PROFILE_CONNECTED_ACCOUNTS_PAGE` (lines 74–77).

**Files to create:**
- `client/src/routes/(authenticated)/profile/security/+page.svelte` — combined page per Design section.

**Files to delete (entire folders):**
- `client/src/routes/(authenticated)/profile/connected-accounts/` (and its `+page.svelte`)
- `client/src/routes/(authenticated)/profile/sessions/` (and its `+page.svelte`)

**Files to edit:**
- `client/src/lib/components/settings/SettingsNav.svelte` — replace tabs array per the table in §5; remove `Link2` import; move `Code2` to last; reorder imports if linter requires.
- `client/src/lib/config/routes.ts` — add `PROFILE_SECURITY_PAGE`, remove the two old constants.

**Patterns to follow:**
- Keep using the project's existing `Card`/`CardHeader`/`CardContent` shadcn-svelte primitives.
- Keep `animate-fade-in`, `backdrop-blur-xl shadow-2xl`, and the gradient overlay div — these are the established eventify glassmorphism pattern (see `eventify-svelte-standards` skill).
- Lucide icon imports: `Link2` for Sign-in methods section header, `Shield` for Active sessions section header AND the Security tab in nav.

**Pitfalls:**
- The `service.providerToUnlink` reference inside the AlertDialog must point to `accountsService.providerToUnlink` after the merge. Same for `service.showUnlinkDialog`, `service.setShowUnlinkDialog`, `service.confirmUnlink`. Pass `service={accountsService}` to `ConnectedAccountRow`.
- Do NOT chain the two `service.load()` calls — fire both on mount so the slower request never blocks the faster one.
- Verify no other files import the removed `PROFILE_SESSIONS_PAGE` / `PROFILE_CONNECTED_ACCOUNTS_PAGE` constants (research confirmed only `SettingsNav.svelte` + the two deleted pages reference them — no other consumers).

**Coordination with AUTH-01:** AUTH-01 (`always-assign-password-remove-haspassword`) extends the **Sign-in methods** section with change-password controls. Its `depends_on` will be updated to `["UI-01-settings-reorg-security-merge"]` after this story is approved, and its scope trimmed (no separate "rename Connected Accounts → Sign-in methods" step needed — done here).

## 7. Test Impact Analysis
### Existing tests affected by this change:
| Test File | Test Method | What it asserts | Conflicts? | Action |
|-----------|------------|-----------------|------------|--------|
| (none found) | — | No frontend route-path or nav-label assertions exist for these routes | NO | None |

Frontend test coverage for `SettingsNav`, the connected-accounts page, and the sessions page is currently absent (no `*.test.ts` / `*.spec.ts` files reference `PROFILE_SESSIONS_PAGE`, `PROFILE_CONNECTED_ACCOUNTS_PAGE`, or `/profile/sessions`/`/profile/connected-accounts`). No existing tests need updating.

### Test modification policy:
- [x] No existing tests should be modified (no tests cover the affected paths)
- [ ] Existing tests MAY be updated where they assert behavior being moved
- [ ] Specific files that may be modified: N/A

### Files to modify (MANDATORY):
| File | Change | Lines |
|------|--------|-------|
| `client/src/lib/components/settings/SettingsNav.svelte` | Replace `tabs` array with new 4-entry ordered list; remove `Link2` import | 2, 11–17 |
| `client/src/lib/config/routes.ts` | Add `PROFILE_SECURITY_PAGE`; remove `PROFILE_SESSIONS_PAGE` and `PROFILE_CONNECTED_ACCOUNTS_PAGE` | 70–77 |
| `client/src/routes/(authenticated)/profile/security/+page.svelte` | **Create** — combined page per §5 spec | new file |
| `client/src/routes/(authenticated)/profile/connected-accounts/+page.svelte` | **Delete** | full file |
| `client/src/routes/(authenticated)/profile/sessions/+page.svelte` | **Delete** | full file |
