---
name: eventify-frontend-patterns
description: Eventify frontend code patterns for SvelteKit 5 + Svelte runes. Use when writing frontend components, pages, stores, or API integration in the Eventify client. Extends spring-boot-standards for backend awareness. Trigger when asked about Svelte components, stores, API controllers, forms, auth flow, or UI patterns.
metadata:
  skill-type: patterns
  language: typescript
  framework: sveltekit, svelte5
---

# Eventify Frontend Patterns

Extends global `spring-boot-standards` (for backend awareness). SvelteKit 5 + Svelte runes, openapi-fetch typed client, shadcn-svelte UI.

---

## 1. Route Structure

Two route groups — guard lives in `hooks.server.ts`:

```
routes/
  (authenticated)/          ← protected, server hook redirects to /login if no cookie
    +layout.svelte           ← validates session on mount, inits organizationStore
    organizations/[orgId]/  ← dynamic org routes
  (public)/                 ← no auth required
    login/  register/  ...
```

**Route constants** — always use `CLIENT_ROUTES` from `$lib/config/routes.ts`:

```ts
// Static route
CLIENT_ROUTES.DASHBOARD.path

// Dynamic factory
CLIENT_ROUTES.ORGANIZATION_CHANNELS_PAGE(orgId).path
```

Never hardcode path strings. Use `isPublicPath(path)` for guard checks.

---

## 2. API Client Pattern

**Single client** at `$lib/api/client.ts`:

```ts
export const client = createClient<paths>({ baseUrl, credentials: 'include' });
// 401 middleware: clears localStorage user, redirects to /login?expired=true
```

**Controller layer** — thin wrappers only, no business logic:

```ts
// src/lib/api/channels/ChannelController.ts
export async function getChannel(orgId: number, channelId: number): Promise<ChannelResponse> {
  const { data, error } = await client.GET('/v1/organizations/{orgId}/channels/{channelId}', {
    params: { path: { orgId, channelId } }
  });
  if (error) throw error;
  return data;
}
```

Rules:
- Destructure `{ data, error }` — never access response directly
- Throw `error` if present, return `data`
- File: `XxxController.ts` (PascalCase + Controller suffix)

**Service layer** — reactive state, calls controllers:

```ts
// src/lib/api/channels/service/ChannelService.svelte.ts  ← .svelte.ts required for runes
export function createChannelService(orgId: () => number) {
  let channels = $state<ChannelResponse[]>([]);
  let loading = $state(false);

  async function load() {
    loading = true;
    try { channels = await getChannels(orgId()); }
    finally { loading = false; }
  }

  return {
    get channels() { return channels; },
    get loading() { return loading; },
    load
  };
}
```

---

## 3. State Management

### Modern (preferred) — rune-based class singletons

```ts
// src/lib/stores/organizationStore.svelte.ts
class OrganizationStore {
  #orgId = $state<number | null>(null);
  currentOrg = $derived(/* ... */);

  get orgId() { return this.#orgId; }
  setOrg(id: number) { this.#orgId = id; }
}

export const organizationStore = new OrganizationStore();
```

### Legacy (auth only) — writable stores

```ts
// src/lib/stores/auth.ts — keep existing pattern, don't migrate
export const authStore = createAuthStore();
export const isAuthenticated = derived(authStore, ($s) => $s.user !== null && $s.user.validated);
```

### Component-scoped state — service factories

```ts
// Prefer factory pattern for component-local reactive state
const tableService = createDataTableService<ChannelResponse>({ ... });
const eventService = createEventService();

// Always use getter accessors for reactivity
tableService.items   // ✅ reactive
```

---

## 4. Component Patterns

**Feature folders** always have `index.ts` barrel:

```ts
// src/lib/components/channels/index.ts
export { default as CreateChannelSheet } from './CreateChannelSheet.svelte';
export { default as ChannelCard } from './ChannelCard.svelte';
```

**Naming:**

| Type | Convention | Example |
|------|-----------|---------|
| Feature component | `PascalCase.svelte` | `CreateChannelSheet.svelte` |
| shadcn primitive | `kebab-case.svelte` | `dialog-content.svelte` |
| Service file | `XxxService.svelte.ts` | `ChannelService.svelte.ts` |
| Config/utils | `kebab-case.ts` | `channel-table-columns.ts` |
| Controller | `XxxController.ts` | `ChannelController.ts` |

**Class merging** — always use `cn()`:

```ts
import { cn } from '$lib/utils';

<div class={cn('base-class', isActive && 'active-class', className)} />
```

**shadcn primitives** — never rewrite, customize via props or extend:

```svelte
<script>
  import { Button } from '$lib/components/ui/button';
  import { Dialog } from '$lib/components/ui/dialog';
</script>
```

---

## 5. Form Pattern

No form library. Manual `$state` + `$derived`:

```svelte
<script lang="ts">
  let name = $state('');
  let slug = $state('');
  let nameError = $state('');
  let slugError = $state('');

  const canSubmit = $derived(
    name.trim().length > 0 && slug.trim().length > 0 && !nameError && !slugError
  );

  function validateSlugInput() {
    slugError = validateSlug(slug) ? '' : 'Invalid slug format';
  }

  function handleOpenChange(open: boolean) {
    if (!open) { name = ''; slug = ''; nameError = ''; slugError = ''; }  // reset on close
  }
</script>

<Input bind:value={name} oninput={() => nameError = name.trim() ? '' : 'Required'} />
{#if nameError}<p class="text-xs text-destructive">{nameError}</p>{/if}

<Button disabled={!canSubmit} onclick={props.onSubmit}>Create</Button>
```

Rules:
- `bind:value` on all inputs
- `oninput` for real-time validation
- `onSubmit` passed as prop from parent (not internal)
- Reset all `$state` vars in `handleOpenChange(false)`
- Inline errors: `text-xs text-destructive`

---

## 6. Auth Flow

**Server hook** (cookie check — no API call):

```ts
// hooks.server.ts
const tokens = CookieService.getAuthTokens(event.cookies);
if (!tokens && !isPublicPath(event.url.pathname)) {
  redirect(302, '/login');
}
```

**Client layout guard** (`(authenticated)/+layout.svelte`):

```svelte
<script lang="ts">
  onMount(async () => {
    await authStore.validateSession();          // hits /v1/users/me
    organizationStore.init();
    setInterval(() => authStore.validateSession(), 10 * 60 * 1000);
  });
</script>
```

**401 handling** — automatic via client middleware (do not duplicate):

```ts
// Already in client.ts — do NOT add manual 401 checks in controllers
client.use({
  onResponse: async ({ response }) => {
    if (response.status === 401 && !isRedirecting) {
      isRedirecting = true;
      localStorage.removeItem('user');
      goto('/login?expired=true');
    }
  }
});
```

---

## 7. Reactive Utilities

**Persistent state** — use existing utilities, never raw `localStorage`:

```ts
import { Localstorage } from '$lib/utils/localstorage.svelte';
import { PersistentCookie } from '$lib/utils/persistent-cookie.svelte';

// Reactive, auto-serializes JSON
const userStore = new Localstorage<UserDetailsResponse>('user');
userStore.value = user;           // write
const user = userStore.value;     // read (reactive $state)

// Cookie-backed reactive value
const sidebarOpen = new PersistentCookie<boolean>('sidebar-open');
```

**Mobile detection:**

```svelte
<script lang="ts">
  import { IsMobile } from '$lib/hooks/is-mobile.svelte';
  const isMobile = new IsMobile();  // optional breakpoint: new IsMobile(1024)
</script>

{#if isMobile.matches} ... {/if}
```

---

## 8. Error Handling

Use `handleError` utility for API errors:

```ts
import { handleError } from '$lib/utils/error-handler';

try {
  await channelService.create(payload);
} catch (err) {
  const { message, fieldErrors } = handleError(err, 'Failed to create channel');
  errorMessage = message;
  // fieldErrors: Record<string, string> for field-level validation errors
}
```

Handles `ValidationErrorResponseResource`, `ApiErrorResponseResource`, and generic errors.

---

## Key Imports Reference

```ts
import { cn } from '$lib/utils';
import { CLIENT_ROUTES, isPublicPath } from '$lib/config/routes';
import { authStore, isAuthenticated } from '$lib/stores/auth';
import { organizationStore } from '$lib/stores/organizationStore.svelte';
import { handleError } from '$lib/utils/error-handler';
import { Localstorage } from '$lib/utils/localstorage.svelte';
import { IsMobile } from '$lib/hooks/is-mobile.svelte';
```
