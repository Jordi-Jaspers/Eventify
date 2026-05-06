---
epic: "AUTH"
title: "Always assign password to OAuth users; remove hasPassword; add change-password UI"
estimate: M
status: ready
created: 2026-05-06
depends_on: ["UI-01-settings-reorg-security-merge"]
labels: [backend, frontend, auth, security, refactor, dx]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story

**As a** user of Eventify
**I want** every account to always have a working password (even if I signed up via OAuth)
**So that** I can change my password from the profile, reset it via email when needed, and unlink any OAuth provider without losing access to my account

## 2. Business Context & Value

Today the codebase carries a `hasPassword` boolean on `User` to distinguish "real" passwords from the random UUID password assigned to OAuth-created users. Investigation revealed:

- **Every OAuth user already has a hashed random UUID password** (`CustomOAuth2UserService.createNewUser`, line 201). The DB column is `NOT NULL`.
- **`hasPassword` is internal-only**: not exposed in any DTO, not used in any business gate (registration sets it `true`, OAuth sets it `false`, password reset sets it back to `true` — pure bookkeeping).
- **The "Change password" button on the profile is a dead link** — it routes back to the profile page itself; no UI flow exists.
- **The `LOCAL` provider is hardcoded as never unlinkable** via `LocalProviderUnlinkException` — correct invariant (every account keeps password access) but the UI still confusingly renders it with a "Connected/Not connected" badge, identical to optional OAuth providers.

Aligning the system around the invariant **"every account always has a password"** simplifies the domain (drop a column, ~6 lines of dead state mutation, one frontend rename), removes a class of "what does this flag mean?" questions for future devs, and unblocks a real user-facing change-password flow.

## 3. Acceptance Criteria

* [ ] **Scenario 1 — `hasPassword` field removed from domain**
    * Given a fresh database
    * When I run all migrations
    * Then the `users.has_password` column does not exist
    * And the `User` entity has no `hasPassword` field, getter, or setter
    * And no service references `setHasPassword(...)` or `isHasPassword()`
    * And `./gradlew build test` passes

* [ ] **Scenario 2 — OAuth-created users still receive a hashed random password**
    * Given a new user signs in via Google/GitHub for the first time
    * When the user record is created
    * Then `users.password` contains a BCrypt-hashed random UUID (not null, not the literal UUID)
    * And the user can immediately request a password reset via the existing forgot-password flow
    * And after reset, the user can authenticate with the new password via `/api/v1/auth/login`

* [ ] **Scenario 3 — Authenticated change-password modal works**
    * Given I am logged in with a known password
    * And I am on `/profile/security` (the combined Security page from UI-01)
    * When I click "Change password" on the Password row
    * Then a modal opens with three fields: current password, new password, confirm new password
    * And submitting valid values calls `POST /api/v1/password` with `{ oldPassword, newPassword }`
    * And on success the modal closes and a success toast appears
    * And on `PasswordIncorrectException` (HTTP 4xx) the form shows "Current password is incorrect" inline
    * And client-side validation enforces: new password matches confirmation, meets existing password complexity rules (reuse `ChangePasswordValidator` rules / shared password input component)

* [ ] **Scenario 4 — Reset-via-email button works for any user**
    * Given I am logged in (whether OAuth-only or password user)
    * When I click "Reset via email" on the Password row
    * Then `POST /api/v1/password/reset` is called with the user's email
    * And a toast confirms "Password reset link sent to your email"
    * And the user receives the standard password-reset email
    * And completing the reset works identically for OAuth-only and password users

* [ ] **Scenario 5 — LOCAL row UI: no badge, no Connect/Disconnect**
    * Given the `/profile/security` page (Sign-in methods section)
    * When the LOCAL provider row renders
    * Then it shows: "Password" label, no "Connected"/"Not connected" badge, no Connect/Disconnect button
    * And it shows two buttons: "Change password" (primary) and "Reset via email" (secondary/ghost)
    * And OAuth provider rows still show the existing "Connected"/"Not connected" badge and Connect/Disconnect button

* [ ] **Scenario 6 — LOCAL provider remains permanently linked (defense in depth)**
    * Given a user has both LOCAL and Google providers linked
    * When a request is made to `DELETE /api/v1/providers/{id}` with the LOCAL provider's id (e.g. via crafted request, since UI does not surface this)
    * Then the backend rejects with `LocalProviderUnlinkException` (HTTP 409 or matching status)
    * And no provider records are deleted

* [ ] **Edge Case — OAuth-only user attempting "Change password"**
    * Given an OAuth-only user (never set their own password) opens the Change Password modal
    * When they submit the form with any value as the current password
    * Then the request fails with `PasswordIncorrectException` (UUID won't match)
    * And the modal shows the standard inline error "Current password is incorrect"
    * And the modal continues to display the "Reset via email" button as the alternative path
    * Note: we deliberately do NOT detect this case server-side because doing so would re-introduce a `hasPassword` equivalent. Standard error + visible alternative button is sufficient.

## 4. Technical Requirements

* **API Changes**:
    * No new endpoints. Frontend adopts existing `POST /api/v1/password` (authenticated change) and `POST /api/v1/password/reset` (email reset request).
    * No DTO changes. `UpdatePasswordRequest` stays as-is (`oldPassword` + `newPassword`).
    * No new exceptions; existing `PasswordIncorrectException`, `LocalProviderUnlinkException`, `LastAuthMethodException` remain.

* **Database**:
    * New Liquibase changeset: `DROP COLUMN users.has_password;`
    * Use `<sql>` tag per `liquibase-migrations-standards` skill. Single forward-only changeset; no rollback definition needed (re-adding the column on rollback would re-introduce the dead concept).
    * Naming: follow existing pattern `YYYYMMDDHHMM-PRD-drop-has-password-column.xml` in `server/src/main/resources/db/changes/`.

* **Security**:
    * `oldPassword` verification stays REQUIRED on `POST /api/v1/password` (matches OWASP ASVS V6.2.4, NIST SP 800-63B, and every major provider).
    * `LocalProviderUnlinkException` rule stays (defense in depth even though UI no longer offers the action).
    * `canUnlink` "≥1 provider must remain" rule stays for OAuth providers.
    * No new attack surface: change-password endpoint pre-existed; we are only wiring the UI.

* **Performance**: N/A — single column drop, single modal, no new queries.

## 5. Design & UI/UX

### LOCAL row (Sign-in methods page)

```
┌────────────────────────────────────────────────────────────────────────┐
│  🔑  Password                    [Change password]  [Reset via email]   │
└────────────────────────────────────────────────────────────────────────┘
```

- No badge (LOCAL is permanent, badge would be visual noise).
- "Change password" — primary button style, opens modal.
- "Reset via email" — ghost/secondary button style, calls reset-request endpoint, shows toast.

### OAuth provider rows (unchanged)

```
┌────────────────────────────────────────────────────────────────────────┐
│  🔵  Google         user@gmail.com         [Connected]    [Disconnect]  │
│  🐙  GitHub                                [Not connected] [Connect]    │
└────────────────────────────────────────────────────────────────────────┘
```

### Change Password modal

- Title: "Change password"
- Three labelled inputs (all `type="password"`):
    1. Current password
    2. New password
    3. Confirm new password
- Inline validation: confirmation matches new; password complexity feedback if a shared component already exists (otherwise minimum length per existing backend `ChangePasswordValidator`).
- Footer: secondary "Cancel" + primary "Change password" (disabled while submitting / invalid).
- On `PasswordIncorrectException`: error displayed under the "Current password" field; modal stays open.
- On success: modal closes, toast "Password changed".

### Section rename

Done by UI-01 (predecessor story). The combined Security page already labels the providers section "Sign-in methods" — AUTH-01 only edits the LOCAL row inside that section.

### Toasts and copy

- "Password changed" — on successful change
- "Password reset link sent to your email" — on reset request
- Reuse existing toast utility (`svelte-sonner`).

## 6. Implementation Notes

### Backend

**Files to modify:**

| File | Change |
|------|--------|
| `server/src/main/java/io/github/eventify/api/user/model/User.java` | Remove `hasPassword` field (lines 73-76) and any constructor reference (line 141 sets `this.hasPassword = false`) |
| `server/src/main/java/io/github/eventify/common/security/oauth2/CustomOAuth2UserService.java` | Remove `user.setHasPassword(false)` (line 199); remove "preserve hasPassword" comments (lines 141, 166); remove `existingUser.isHasPassword()` reference (line 219) |
| `server/src/main/java/io/github/eventify/api/user/service/UserService.java` | Remove `newUser.setHasPassword(true)` (line 244) |
| `server/src/main/java/io/github/eventify/api/user/service/PasswordService.java` | Remove `user.setHasPassword(true)` (line 71) |
| `server/src/main/resources/db/changes/<new>.xml` | New Liquibase changeset: `ALTER TABLE users DROP COLUMN has_password;` |
| `server/src/main/resources/db/db.changelog-master.xml` (or equivalent include file) | Register new changeset |

**Files NOT to modify:**

- `UserAuthProviderService.java` — keep `LocalProviderUnlinkException` rule as-is (defense in depth)
- `PasswordController.java` — already correct
- `UpdatePasswordRequest.java` — already correct

### Frontend

**Files to modify:**

| File | Change |
|------|--------|
| `client/src/lib/components/profile/ConnectedAccountRow.svelte` | LOCAL branch: remove badge entirely, render two buttons (Change password → opens modal; Reset via email → calls service). Keep OAuth branches unchanged. |
| `client/src/lib/components/profile/` (new) | New component `ChangePasswordDialog.svelte` (modal) — uses shadcn-svelte Dialog primitive (existing pattern in codebase) |
| `client/src/lib/api/user/service/ConnectedAccountsService.svelte.ts` (or new sibling service) | Add methods: `openChangePasswordDialog()`, `submitPasswordChange({oldPassword, newPassword})`, `requestPasswordReset()` — wraps `POST /api/v1/password` and `POST /api/v1/password/reset` |
| `client/src/routes/(authenticated)/profile/security/+page.svelte` | Mount the `ChangePasswordDialog` (state lives in `accountsService`); no other structural changes — UI-01 already created this page |
| `client/src/lib/api/models/*` (auto-generated) | Run `bun run sync:api` after backend changes — should be a no-op for this story since no DTO changes |

**Patterns to follow:**

- Dialog: mirror existing unlink-confirmation dialog in `ConnectedAccountsService` (same opening/closing state pattern).
- Service methods: mirror existing `linkProvider` / `unlinkProvider` patterns — error handling via `handleError` util, toasts via `svelte-sonner`.
- API calls: use generated `openapi-fetch` client (`client/src/lib/api/`), not raw `fetch`.
- Form validation: check for an existing reusable password input component before rolling new validation.

**Pitfalls:**

- The `Change password` button in the existing `ConnectedAccountRow.svelte` (line 48) currently uses `href={CLIENT_ROUTES.PROFILE_PAGE.path}` — replace this entirely; do not leave the dead `href`.
- After backend column drop, any test seed/fixture that inserts `has_password` will break — see Test Impact below.
- DO NOT add a new field/flag (e.g., `passwordSetByUser`) to compensate for losing `hasPassword`. The whole point is to remove this concept. Rely on the `PasswordIncorrectException` failure path + visible "Reset via email" button.

## 7. Test Impact Analysis

This is a refactoring + UI feature story. Multiple existing tests assert behavior tied to the removed flag.

### Existing tests affected:

| Test File | What it asserts | Conflicts? | Action |
|-----------|-----------------|------------|--------|
| `server/.../CustomOAuth2UserServiceCreateUserTest.java` | OAuth user gets `hasPassword = false` | YES | Remove the `hasPassword` assertion; keep the password-is-set assertion |
| `server/.../PasswordServiceTest.java` | Password reset sets `hasPassword = true` | YES | Remove the `hasPassword` assertion; keep the password-changed assertion |
| `server/.../UserServiceRegisterAuthProviderTest.java` | Registration sets `hasPassword = true` | YES | Remove the `hasPassword` assertion |
| `server/.../CustomOAuth2UserServiceLoginModeTest.java` | L2 auto-link preserves `hasPassword` | YES | Remove preservation assertion; remaining auto-link behavior stays |
| `server/.../CustomOAuth2UserServiceUpdateUserTest.java` | `updateExistingUserIfNeeded` reads `hasPassword` | YES | Remove the field read; verify other update logic unaffected |
| `server/.../UserAuthProviderServiceTest.java` | LOCAL never unlinkable | NO | KEEP — invariant unchanged |
| `server/.../PasswordControllerTest.java` (if exists) | Existing change-password endpoint tests | NO | KEEP |
| Any DB fixture / `.sql` seed inserting `has_password` | Column existence | YES | Remove the column reference |
| `client/.../ConnectedAccountsService.test.ts` | Existing service tests | Possibly | Update to cover new `submitPasswordChange` and `requestPasswordReset` methods |

### New tests required:

| File | Coverage |
|------|----------|
| `client/.../ChangePasswordDialog.test.ts` (component test) | Form validation: confirmation mismatch shown; submit disabled when invalid; success closes dialog; `PasswordIncorrectException` shows inline error |
| `client/.../ConnectedAccountsService.test.ts` | `submitPasswordChange` calls correct endpoint with payload; `requestPasswordReset` calls correct endpoint and toasts |
| `client/.../ConnectedAccountRow.test.ts` (or Playwright) | LOCAL row: no badge, both buttons present, OAuth row still has badge + Connect/Disconnect |

### Action for testing agent BEFORE implementation:

- Run `grep -r "hasPassword\|has_password\|isHasPassword\|setHasPassword"` across `server/src/test` and `client/` and report any additional test files not listed above.
- Search seed/fixture files (`*.sql`, `data.sql`, `import.sql`, test `@Sql` annotations) for `has_password` references.

### Test modification policy:
- [x] Existing tests MAY be updated where they assert `hasPassword` behavior (which is being removed)
- [x] DB fixture/seed files MAY be updated to remove the dropped column

### Files to modify (MANDATORY):

| File | Change |
|------|--------|
| `server/src/main/java/io/github/eventify/api/user/model/User.java` | Drop `hasPassword` field + constructor line |
| `server/src/main/java/io/github/eventify/common/security/oauth2/CustomOAuth2UserService.java` | Drop `setHasPassword(false)` + `isHasPassword()` reference |
| `server/src/main/java/io/github/eventify/api/user/service/UserService.java` | Drop `setHasPassword(true)` |
| `server/src/main/java/io/github/eventify/api/user/service/PasswordService.java` | Drop `setHasPassword(true)` |
| `server/src/main/resources/db/changes/<new>.xml` | New: `DROP COLUMN users.has_password;` |
| `server/src/main/resources/db/db.changelog-master.xml` | Register new changeset |
| `client/src/lib/components/profile/ConnectedAccountRow.svelte` | LOCAL branch: drop badge, two buttons |
| `client/src/lib/components/profile/ChangePasswordDialog.svelte` | New component |
| `client/src/lib/api/user/service/ConnectedAccountsService.svelte.ts` | New methods for change/reset |
| `client/src/routes/(authenticated)/profile/security/+page.svelte` | Mount `ChangePasswordDialog` |
| Test files listed above | Per Test Impact table |

**Verification commands:**
- `./gradlew build test`
- `cd client && bun run check && bun run test`
- Manual happy paths:
    1. Sign in via Google (new user) → verify `users.password` is set in DB; verify `has_password` column does not exist
    2. Logged in: Profile → Security → Sign-in methods → Change password → submit valid → success
    3. Logged in: Profile → Security → Sign-in methods → Reset via email → email received → reset works
    4. OAuth-only user: Change password → wrong-password error → Reset via email works as fallback
    5. Try `DELETE /api/v1/providers/{LOCAL_id}` via curl → 409
