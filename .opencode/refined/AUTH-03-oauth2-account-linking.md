---
epic: "AUTH"
title: "OAuth2 Account Linking & Connected Accounts"
estimate: M
status: ready
created: 2026-04-04
depends_on: [ ]
labels: [ backend, frontend, security, database ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** user with both email/password and OAuth2 accounts\
**I want** to see which login providers are connected to my account, link new ones, and unlink existing ones\
**So that** I have control over how I authenticate and can consolidate my login methods\

> **AUTH-01 baseline (shipped 2026-05-04):** Profile uses `SettingsNav.svelte` for sub-route tabs (NOT in-page tabs). Sessions tab lives at `/profile/sessions`. Connected Accounts SHOULD follow the same pattern as a sibling sub-route. Generic `ConfirmDialog` primitive exists at `$lib/components/ui/confirm-dialog/` — use it for unlink confirmation.

## 2. Business Context & Value
Currently, OAuth2 account linking is implicit — if a Google login matches an existing email, it silently uses that account. But users have no visibility into which providers are connected, can't explicitly link new providers, and can't unlink providers they no longer use. This creates confusion and reduces trust in the authentication system.

## 3. Acceptance Criteria
* [ ] **Scenario 1: View connected providers**
    * Given a user navigates to Profile → Connected Accounts tab (`/profile/connected-accounts`)
    * When the page loads
    * Then they see a list of providers (Google, GitHub) with connected/not-connected status and the email used for each connected provider

* [ ] **Scenario 2: Link a new provider**
    * Given a user has only email/password login
    * When they click "Connect" next to Google
    * Then they are redirected to Google OAuth2, and upon success, the provider is linked to their account

* [ ] **Scenario 3: Unlink a provider (with password set)**
    * Given a user has both email/password AND Google connected
    * When they click "Disconnect" on Google
    * Then Google is unlinked, and they can still log in via email/password

* [ ] **Scenario 4: Cannot unlink last auth method**
    * Given a user has ONLY Google connected (no password set — created via OAuth2 with random UUID password)
    * When they attempt to unlink Google
    * Then the action is blocked with message: "You must have at least one login method. Set a password first or connect another provider."

* [ ] **Scenario 5: Implicit linking on first OAuth2 login (existing behavior preserved)**
    * Given a user registered with email/password
    * When they log in via Google using the same email
    * Then the Google provider is automatically linked and appears in Connected Accounts

* [ ] **Scenario 6: Provider shows connected email**
    * Given a user linked Google with `user@gmail.com`
    * When they view Connected Accounts
    * Then Google shows as connected with `user@gmail.com` displayed

* [ ] **Edge case: OAuth2-only user sets password**
    * Given a user created their account via Google (has random UUID password)
    * When they want to unlink Google
    * Then they must first set a password via the existing password reset flow, after which unlinking is allowed

## 4. Technical Requirements
* **API Changes**:
  - `GET /v1/user/providers` — list connected providers with status
  - `POST /v1/user/providers/{provider}/link` — initiate linking flow (redirect to OAuth2)
  - `DELETE /v1/user/providers/{provider}` — unlink provider (with safeguard check)
  - Update `CustomOAuth2UserService` to persist provider info on OAuth2 login
  - Add path constants to `api/Paths.java` (e.g. `USER_PROVIDERS_PATH`, `USER_PROVIDER_PATH`)
  - Add `ApiErrorCode.PROVIDER_NOT_FOUND_ERROR` and `ApiErrorCode.LAST_AUTH_METHOD_ERROR`
  - Note: base path is `/v1/user` (singular), not `/v1/users` — confirmed by AUTH-01

* **Database**:
  - New `user_auth_provider` table: `id`, `user_id` (FK), `provider` (enum: GOOGLE, GITHUB, LOCAL), `provider_email`, `linked_at`
  - Liquibase migration
  - On OAuth2 login, insert/update provider record
  - On registration (email/password), insert LOCAL provider record
  - Unique constraint on `(user_id, provider)`

* **Security**:
  - Unlink safeguard: check user has ≥2 providers OR has a non-UUID password before allowing unlink
  - Linking flow must verify OAuth2 email matches or user explicitly consents
  - Provider endpoints require authenticated user, scoped to own data

* **Performance**: N/A

## 5. Design & UI/UX
- "Connected Accounts" lives as its own sub-route `/profile/connected-accounts`, registered as a tab in `SettingsNav.svelte` (icon: `Link2` from lucide). Sibling to existing Profile / Sessions / Data & Storage tabs.
- Card per provider: icon (Google/GitHub) + provider name + status (Connected/Not connected) + connected email + Connect/Disconnect button
- "Local" provider (email/password) shown with password-set status
- Disconnect button disabled with tooltip when it's the last auth method
- Toast notification on successful link/unlink (via `svelte-sonner`)
- Confirmation dialog before unlinking — use the generic `ConfirmDialog` primitive from `$lib/components/ui/confirm-dialog/` (shipped in AUTH-01) with `destructive=true`

## 6. Implementation Notes

### Backend
- **New entity**: `UserAuthProvider` in `api/user/model/` with `provider` enum (`GOOGLE`, `GITHUB`, `LOCAL`)
- **New repository**: `UserAuthProviderRepository`
- **CustomOAuth2UserService** (`server/.../common/security/CustomOAuth2UserService.java`): On OAuth2 login, upsert `UserAuthProvider` record
- **AuthenticationService**: On email/password registration, create `LOCAL` provider record
- **New controller/service**: Provider management endpoints
- **Unlink safeguard**: `UserAuthProviderService.canUnlink(userId, provider)` checks remaining providers + password status
- To check if user has a "real" password vs UUID: could add `hasPassword` boolean to User entity, or check if password matches UUID pattern (less clean)

### Frontend
- **New route**: `client/src/routes/(authenticated)/profile/connected-accounts/+page.svelte` (sibling to `/profile/sessions`)
- **Update**: `client/src/lib/components/settings/SettingsNav.svelte` — add `{ label: 'Connected Accounts', path: '/profile/connected-accounts', icon: Link2 }` to tabs array
- **Update**: `client/src/lib/config/routes.ts` — add `PROFILE_CONNECTED_ACCOUNTS_PAGE` route constant
- **New component**: `client/src/lib/components/profile/ConnectedProviderCard.svelte`
- **New service**: `client/src/lib/api/user/service/ProviderService.svelte.ts` (factory pattern with `$state`, matches `SessionService` style from AUTH-01)
- **New controller**: `client/src/lib/api/user/UserProviderController.ts` (matches `UserSessionController` style)
- **Reuse**: `ConfirmDialog` from `$lib/components/ui/confirm-dialog/` for unlink confirmation
- **Link flow**: Redirect to OAuth2 provider URL (same as login, but with `linking=true` state parameter)
- After backend changes, run `bun run sync:api` from `client/` to regenerate types

### Data migration
- Backfill `user_auth_provider` for existing users: all users get `LOCAL` record; users who have logged in via OAuth2 need provider records (may need to infer from login history or skip — only future logins will populate)

### Files to modify (MANDATORY):
| File | Change |
|------|--------|
| New: `server/.../api/user/model/UserAuthProvider.java` | New entity |
| New: `server/.../api/user/repository/UserAuthProviderRepository.java` | New repository |
| `server/.../common/security/CustomOAuth2UserService.java` | Upsert provider on OAuth2 login |
| `server/.../api/authentication/service/AuthenticationService.java` | Create LOCAL provider on registration |
| New: Provider management controller + service | List, link, unlink endpoints |
| `server/.../api/Paths.java` | Add `USER_PROVIDERS_PATH`, `USER_PROVIDER_PATH` constants |
| `server/.../common/exception/ApiErrorCode.java` | Add `PROVIDER_NOT_FOUND_ERROR`, `LAST_AUTH_METHOD_ERROR` |
| `server/src/main/resources/db/changelog/` | Migration for `user_auth_provider` table + backfill (raw `<sql>` only per liquibase-migrations-standards) |
| New: `client/src/routes/(authenticated)/profile/connected-accounts/+page.svelte` | New tab route |
| `client/src/lib/components/settings/SettingsNav.svelte` | Add Connected Accounts tab |
| `client/src/lib/config/routes.ts` | Add route constant |
| New: `client/src/lib/components/profile/ConnectedProviderCard.svelte` | Provider card UI |
| New: `client/src/lib/api/user/UserProviderController.ts` | API client |
| New: `client/src/lib/api/user/service/ProviderService.svelte.ts` | Reactive service |
