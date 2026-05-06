---
epic: "AUTH"
title: "OAuth2 Account Linking & Connected Accounts"
estimate: M
status: ready
created: 2026-04-04
updated: 2026-05-04
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

Worse, the implicit-only flow has a critical bug: when an authenticated user attempts to link an OAuth2 account with a different email, the system silently logs them out and into the OAuth2 account instead. Users need an explicit, intent-driven linking flow that can carry their authenticated identity through the OAuth2 redirect.

## 3. Acceptance Criteria

### Login flows (no current session)
* [ ] **L1: Login via OAuth2 — provider already linked**
    * Given user X has GOOGLE linked with provider email `g@x.com`
    * When they log in via Google as `g@x.com`
    * Then they are authenticated as user X (matched by `(provider, providerEmail)` lookup, NOT by primary email)

* [ ] **L2: Login via OAuth2 — email matches existing user, no provider record yet (legacy)**
    * Given user X exists with `user.email=x@example.com`, has no UserAuthProvider records
    * When they log in via Google as `x@example.com`
    * Then they are authenticated as user X AND a GOOGLE UserAuthProvider record is created (auto-link)

* [ ] **L3: Login via OAuth2 — email is new**
    * Given no user exists with the provider email
    * When they log in via Google
    * Then a new user is created with `hasPassword=false` AND a GOOGLE UserAuthProvider record is created

* [ ] **L4: Login via local email/password**
    * Existing flow unchanged

### Linking flows (user already authenticated, mode=link)
* [ ] **K1: Link OAuth2 with same email as current user**
    * Given user X (`user.email=x@example.com`) is logged in, no GOOGLE link
    * When they initiate Google linking and Google returns `x@example.com`
    * Then GOOGLE is linked to user X, session preserved, redirected to `/profile/connected-accounts?linked=google`

* [ ] **K2: Link OAuth2 with a different email (Y is unknown)**
    * Given user X is logged in, no GOOGLE link
    * When they initiate Google linking and Google returns `y@example.com` (no user has this email, no provider record uses this email)
    * Then GOOGLE is linked to user X with `providerEmail=y@example.com`, session preserved

* [ ] **K3: REJECT — Link OAuth2, email matches another user's primary email (E3)**
    * Given user X is logged in. Another user Z exists with `user.email=z@example.com`
    * When user X initiates Google linking and Google returns `z@example.com`
    * Then linking is REJECTED. User X's session preserved. Redirected to `/profile/connected-accounts?error=email_in_use`

* [ ] **K4: REJECT — Link OAuth2, (provider, providerEmail) already linked to another user (E2)**
    * Given user X is logged in. Another user Z has GOOGLE linked with `providerEmail=g@example.com`
    * When user X initiates Google linking and Google returns `g@example.com`
    * Then linking is REJECTED. Redirected to `/profile/connected-accounts?error=provider_linked_elsewhere`

* [ ] **K5: REJECT — Link OAuth2, current user already has same provider linked (B5)**
    * Given user X is logged in and already has GOOGLE linked (any provider email)
    * When they initiate Google linking again
    * Then linking is REJECTED. Redirected to `/profile/connected-accounts?error=already_linked`. Message instructs user to unlink first.

* [ ] **K6: Link different provider while same provider already linked is OK**
    * Given user X has GOOGLE linked
    * When they link GitHub
    * Then GITHUB is added (different provider, same user)

### Unlink flows
* [ ] **U1: Unlink OAuth2 provider, user has password**
    * Given user X has LOCAL + GOOGLE
    * When they unlink GOOGLE
    * Then GOOGLE removed, can still login via password

* [ ] **U2: Unlink OAuth2 provider, user has another OAuth2 provider**
    * Given user X has GOOGLE + GITHUB, no password
    * When they unlink GOOGLE
    * Then GOOGLE removed, can still login via GitHub

* [ ] **U3: REJECT — Unlink last auth method (no password, no other OAuth2)**
    * Given user X has only GOOGLE
    * When they attempt unlink
    * Then REJECT 409 LAST_AUTH_METHOD_ERROR

* [ ] **U4: REJECT — Unlink LOCAL provider (NEVER allowed)**
    * Given user X has LOCAL (with or without OAuth2 providers)
    * When they attempt to unlink LOCAL
    * Then REJECT 409 with message "Local password cannot be unlinked. Use 'Change password' to update."
    * Frontend MUST hide the Disconnect button for LOCAL provider entirely

### Connected accounts page (view + UX)
* [ ] **V1: View connected providers**
    * Given user navigates to `/profile/connected-accounts`
    * Then they see GOOGLE, GITHUB, LOCAL with connected/not-connected status and provider email per linked provider
    * LOCAL row shows "Password set" status, no Disconnect button

* [ ] **V2: Toast feedback on link success**
    * On `?linked={provider}` query param, show success toast and reload provider list

* [ ] **V3: Toast feedback on link error**
    * On `?error={code}` query param, show error toast with human-readable message per error code

## 4. Technical Requirements

### API Changes
- `GET /v1/user/providers` — list connected providers (per current user)
- `DELETE /v1/user/providers/{id}` — unlink provider by ID (with safeguards U3, U4)
- `GET /v1/oauth2/authorization/{provider}?mode=link` — initiate linking flow (reuses existing OAuth2 endpoint with new `mode` query param; default `mode=login` preserves login behavior)
- New error codes:
  - `PROVIDER_NOT_FOUND_ERROR` (ERR-0053) — already added
  - `LAST_AUTH_METHOD_ERROR` (ERR-0054) — already added
  - `LOCAL_PROVIDER_UNLINK_ERROR` — for U4
  - `EMAIL_IN_USE_ERROR` — for K3
  - `PROVIDER_LINKED_ELSEWHERE_ERROR` — for K4
  - `PROVIDER_ALREADY_LINKED_ERROR` — for K5

### Database
- `user_auth_provider` table (already created in Phase 2): `id`, `user_id` (FK), `provider`, `provider_email`, `linked_at`
- **NEW unique constraint** on `(provider, provider_email)` — DB-level guard for K4 and login lookup G1
- **NEW index** on `(provider, provider_email)` for login lookup performance
- Migration to add the above (separate changeset from the original)

### Backend logic — OAuth2 flow split
- `CustomOAuth2UserService.processOAuth2User` must branch based on `mode` and current SecurityContext:

  **Login mode (default):**
  1. Lookup `UserAuthProvider` by `(provider, providerEmail)` — if found, return that user (L1)
  2. Else lookup `User` by primary email — if found, auto-link provider, return user (L2)
  3. Else create new user + provider record (L3)

  **Link mode (mode=link param + authenticated SecurityContext):**
  1. Get current authenticated user X from SecurityContext
  2. Reject if X already has this provider linked (K5)
  3. Reject if `(provider, providerEmail)` belongs to another user (K4)
  4. Reject if providerEmail matches another user's primary email AND that user is not X (K3)
  5. Otherwise create UserAuthProvider for X (K1, K2)
  6. Throw a `LinkOAuth2Exception` (extends OAuth2AuthenticationException) carrying error code on rejection — handler maps to redirect with `?error={code}`

- **Mode propagation through OAuth2 redirect:** Use Spring's `OAuth2AuthorizationRequestResolver` to capture `mode` from initial request and store it in the OAuth2 state parameter (or in `OAuth2AuthorizationRequest.additionalParameters`). Read it back in `CustomOAuth2UserService` from `OAuth2UserRequest.getAdditionalParameters()`.

- **Success handler split:** `OAuth2AuthenticationSuccessHandler` reads mode:
  - `mode=login` → existing behavior (issue JWT cookies, redirect to dashboard)
  - `mode=link` → preserve existing session (do NOT issue new JWT), redirect to `/profile/connected-accounts?linked={provider}`

- **Failure handler split:** On `LinkOAuth2Exception`, redirect to `/profile/connected-accounts?error={code}` instead of the generic OAuth2 failure page.

- **`UserAuthProviderService.unlinkProvider` safeguards:**
  - If `provider == LOCAL` → throw `LocalProviderUnlinkException` (HttpException 409) — U4
  - If unlinking would leave 0 records → throw `LastAuthMethodException` (HttpException 409) — U3 (already implemented)

### Security
- `mode=link` MUST be ignored if SecurityContext has no authenticated user (treat as login)
- Mode parameter is server-validated, never blindly trusted from client
- Cross-user provider lookups (K3, K4) MUST NOT leak user identity in error messages — generic codes only

## 5. Design & UI/UX
- "Connected Accounts" lives at `/profile/connected-accounts`, registered as a tab in `SettingsNav.svelte` (icon: `Link2`)
- Card per provider: icon + provider name + status (Connected/Not connected) + connected email + Connect/Disconnect button
- LOCAL row: shows "Password set" status, NO Disconnect button (per U4)
- Toast notifications:
  - Link success → `toast.success('{Provider} connected')`
  - Link error → `toast.error({error_code_to_message})` mapping:
    - `email_in_use` → "That email is already used by another account"
    - `provider_linked_elsewhere` → "This {provider} account is linked to another Eventify account"
    - `already_linked` → "{Provider} is already connected. Unlink it first to link a different account."
    - default → "Could not link {provider}"
- Confirmation dialog before unlinking — `ConfirmDialog` with `destructive=true`

## 6. Implementation Notes

### Backend
- **Existing entity** `UserAuthProvider` (Phase 2): no schema change, but new unique constraint migration
- **Existing repository** `UserAuthProviderRepository`: add `findByProviderAndProviderEmail(AuthProvider, String)` for K3/K4/L1 lookups
- **Existing service** `UserAuthProviderService`: add `unlinkProvider` LOCAL guard
- **`CustomOAuth2UserService`**: refactor `processOAuth2User` into `processLogin` + `processLink` branches dispatched by mode
- **NEW** `CustomOAuth2AuthorizationRequestResolver` — extends Spring's `DefaultOAuth2AuthorizationRequestResolver` to capture `mode` from query string into `additionalParameters`
- **NEW exceptions** in `common/exception/`:
  - `LocalProviderUnlinkException extends HttpException` (409)
  - `LinkOAuth2Exception extends OAuth2AuthenticationException` carrying error code constant (handled by failure handler)
- **`OAuth2AuthenticationSuccessHandler`**: split based on mode; link path reads request param/session, skips JWT issuance
- **`OAuth2AuthenticationFailureHandler`**: detect `LinkOAuth2Exception`, redirect with `?error=` to settings page
- **`WebSecurityConfig`**: register the custom `OAuth2AuthorizationRequestResolver`

### Frontend
- **`ConnectedAccountsService.svelte.ts`** — `linkProvider(provider)` already redirects; append `?mode=link` to URL
- **`/profile/connected-accounts/+page.svelte`** — on `onMount`, read `$page.url.searchParams.get('linked')` and `get('error')`, show toast, then strip query params via `goto(currentPath, { replaceState: true, keepFocus: true })`
- **`ConnectedAccountRow.svelte`** — verify LOCAL provider has NO Disconnect button rendered (per U4) — even if backend sends id, frontend MUST gate on `provider !== 'LOCAL'`
- After backend changes, run `bun run sync:api` from `client/` to regenerate types

### Files to modify

| File | Change |
|------|--------|
| `server/.../api/user/repository/UserAuthProviderRepository.java` | Add `findByProviderAndProviderEmail` |
| `server/.../api/user/service/UserAuthProviderService.java` | Add LOCAL unlink guard; add `linkProviderForUser` for explicit link path with K3/K4/K5 checks |
| `server/.../common/security/oauth2/CustomOAuth2UserService.java` | Split into login/link branches based on mode |
| NEW: `server/.../common/security/oauth2/CustomOAuth2AuthorizationRequestResolver.java` | Capture `mode` query param into OAuth2 state |
| `server/.../common/security/oauth2/OAuth2AuthenticationSuccessHandler.java` | Branch on mode — link mode preserves session, redirects to settings |
| `server/.../common/security/oauth2/OAuth2AuthenticationFailureHandler.java` | Handle `LinkOAuth2Exception` with redirect to settings page with error code |
| `server/.../common/config/WebSecurityConfig.java` | Register custom `OAuth2AuthorizationRequestResolver` |
| NEW: `server/.../common/exception/LocalProviderUnlinkException.java` | HttpException 409 |
| NEW: `server/.../common/exception/LinkOAuth2Exception.java` | OAuth2AuthenticationException carrying error code |
| `server/.../common/exception/ApiErrorCode.java` | Add `LOCAL_PROVIDER_UNLINK_ERROR`, `EMAIL_IN_USE_ERROR`, `PROVIDER_LINKED_ELSEWHERE_ERROR`, `PROVIDER_ALREADY_LINKED_ERROR` |
| `server/src/main/resources/db/changelog/changesets/202605041XXX-PRD-user-auth-provider-unique-email.xml` | Add unique constraint + index on `(provider, provider_email)` |
| `client/src/lib/api/user/service/ConnectedAccountsService.svelte.ts` | `linkProvider` appends `?mode=link` |
| `client/src/routes/(authenticated)/profile/connected-accounts/+page.svelte` | Read query params on mount, show toasts, strip params |
| `client/src/lib/components/profile/ConnectedAccountRow.svelte` | Verify LOCAL has no Disconnect button |

## 7. Test Impact Analysis

### Existing tests to update
| Test File | Method | Current | Update To |
|-----------|--------|---------|-----------|
| `CustomOAuth2UserServiceAuthProviderTest` | `processOAuth2User_linksGoogleProvider_whenExistingLocalUserAuthenticatesViaGoogle` | Tests login auto-link by email match | Keep — covers L2 |
| `CustomOAuth2UserServiceAuthProviderTest` | All tests | Currently call `processOAuth2User(userRequest, oAuth2User)` without mode | Pass `mode=login` in additionalParameters; verify default behavior |
| `UserProviderControllerIT` | (existing) | DELETE only checks 204/404/409 last-method | Add 409 LOCAL_PROVIDER_UNLINK_ERROR test |

### New tests required
**Backend (Phase 1 extension):**
- `CustomOAuth2UserServiceLoginModeTest` — L1 (provider lookup wins over email lookup), L2 (email fallback), L3 (create)
- `CustomOAuth2UserServiceLinkModeTest` — K1, K2, K3, K4, K5, K6
- `UserAuthProviderServiceTest` — add U4 (LOCAL unlink rejected)
- `OAuth2AuthenticationSuccessHandlerTest` — mode=link does NOT issue JWT, redirects to settings with `?linked=`
- `OAuth2AuthenticationFailureHandlerTest` — `LinkOAuth2Exception` redirects to settings with `?error=`
- `CustomOAuth2AuthorizationRequestResolverTest` — captures mode from query into additionalParameters
- `UserProviderControllerIT` — DELETE LOCAL → 409

**Frontend (Phase 4 extension):**
- `ConnectedAccountsService.test.ts` — `linkProvider` includes `mode=link` in URL
- `+page.svelte` query param handling — extracted into a small reactive helper or tested via component test (toast on `?linked=`, toast on `?error=`, strip query params after)
- `ConnectedAccountRow.test.ts` (new) — LOCAL provider does NOT render Disconnect button

