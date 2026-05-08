---
name: eventify-svelte-standards
description: Project-specific conventions for the eventify frontend.
metadata:
  skill-type: frontend
  language: svelte
  framework: sveltekit
  build-tool: vite, bun
---

## TypeScript Rules

**Explicit types on ALL declarations:**

```typescript
// CORRECT
let loading: boolean = $state(false);
const user: UserDetailsResponse = await fetchUser();
function handleSubmit(event: SubmitEvent): Promise<void> { }

// WRONG - no inference
let loading = $state(false);
const user = await fetchUser();
```

**Always use OpenAPI generated types:**

```typescript
import type { UserDetailsResponse, OrganizationResponse } from '$lib/api/models';
```

---

## Project Structure

```
src/
├── routes/
│   ├── (public)/           # Login, register, etc.
│   ├── (authenticated)/    # Dashboard, profile, etc.
│   └── +layout.svelte
├── lib/
│   ├── api/
│   │   ├── client.ts                    # openapi-fetch client
│   │   ├── models.ts                    # Generated types
│   │   └── [domain]/
│   │       ├── [Name]Controller.ts      # API calls
│   │       └── service/
│   │           └── [Name]Service.svelte.ts  # Business logic + state
│   ├── components/
│   │   ├── ui/              # shadcn-svelte components
│   │   ├── layout/          # AppBackground, AppSidebar, AppLogo
│   │   ├── data-table/      # Paginated table system
│   │   └── [feature]/       # Feature-specific components
│   ├── config/
│   │   ├── routes.ts        # CLIENT_ROUTES
│   │   └── constants.ts
│   ├── stores/              # Global state
│   └── utils/               # Helpers (date, error-handler, etc.)
```

---

## API Architecture

### Layer 1: Controller (API calls only)

```typescript
// $lib/api/user/UserController.ts
import { client } from '$lib/api/client';
import type { UserDetailsResponse } from '$lib/api/models';

export async function getUserDetails(): Promise<UserDetailsResponse> {
    const { data, error } = await client.GET('/v1/user/details');
    if (error) throw error;
    return data;
}

export async function updateUserDetails(
    firstName: string, 
    lastName: string
): Promise<UserDetailsResponse> {
    const { data, error } = await client.POST('/v1/user/details', {
        body: { firstName, lastName }
    });
    if (error) throw error;
    return data;
}
```

### Layer 2: Service (state + business logic)

```typescript
// $lib/api/user/service/ProfileService.svelte.ts
import { updateUserDetails } from '../UserController';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

export function createProfileService() {
    let saving: boolean = $state(false);
    let firstName: string = $state('');

    async function save(): Promise<void> {
        saving = true;
        try {
            await updateUserDetails(firstName, '');
            toast.success('Profile updated');
        } catch (err: unknown) {
            const { message } = handleError(err, 'Failed to save');
            toast.error(message);
        } finally {
            saving = false;
        }
    }

    return {
        get saving(): boolean { return saving; },
        get firstName(): string { return firstName; },
        set firstName(v: string) { firstName = v; },
        save
    };
}

export type ProfileService = ReturnType<typeof createProfileService>;
```

### Layer 3: Page (minimal, uses service)

```svelte
<script lang="ts">
    import { onMount } from 'svelte';
    import { createProfileService } from '$lib/api/user/service/ProfileService.svelte';

    const service = createProfileService();

    onMount(() => service.load());
</script>

<Input bind:value={service.firstName} disabled={service.saving} />
<Button onclick={service.save} disabled={service.saving}>Save</Button>
```

---

## Routing

**Never hardcode paths:**

```typescript
import { CLIENT_ROUTES } from '$lib/config/routes';

// Static routes
goto(CLIENT_ROUTES.DASHBOARD_PAGE.path);
goto(CLIENT_ROUTES.LOGIN_PAGE.path);

// Dynamic routes (functions)
goto(CLIENT_ROUTES.ORGANIZATION_MEMBERS_PAGE(orgId).path);
goto(CLIENT_ROUTES.ORGANIZATION_SETTINGS_API_KEYS_PAGE(orgId).path);
```

**Route types:**
- `CLIENT_ROUTES.[NAME]_PAGE` - Static routes with `.path` and `.type`
- `CLIENT_ROUTES.[NAME]_PAGE(id)` - Dynamic routes (functions)

---

## Error Handling

```typescript
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

try {
    await apiCall();
} catch (err: unknown) {
    const { message, validationErrors } = handleError(err, 'Default message');
    toast.error(message);
    
    // For form validation errors
    if (validationErrors) {
        errors = getValidationErrorMap(validationErrors);
    }
}
```

---

## Component Patterns

### Props Interface

```svelte
<script lang="ts">
    interface Props {
        open: boolean;
        creating: boolean;
        onOpenChange: (open: boolean) => void;
        onSubmit: (name: string) => void;
    }

    let { open, creating, onOpenChange, onSubmit }: Props = $props();
</script>
```

### Derived State

```svelte
<script lang="ts">
    let name: string = $state('');
    let creating: boolean = $state(false);
    
    const canSubmit: boolean = $derived(name.trim().length > 0 && !creating);
</script>
```

### Component Exports (barrel files)

```typescript
// $lib/components/channels/index.ts
export { default as CreateChannelSheet } from './CreateChannelSheet.svelte';
export { default as EditChannelSheet } from './EditChannelSheet.svelte';
export { default as ChannelRow } from './ChannelRow.svelte';
```

---

## Layouts

### Public Layout
Provides: `AppBackground`, centering, `animate-fade-in`

```svelte
<!-- routes/(public)/login/+page.svelte -->
<div class="max-w-md mx-auto">
    <div class="mb-8 text-center">
        <AppLogo size="medium" subtitle="Description" />
    </div>
    
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
        <!-- content -->
    </Card>
</div>
```

### Authenticated Layout
Provides: `AppBackground`, `AppSidebar`, session validation

```svelte
<!-- routes/(authenticated)/dashboard/+page.svelte -->
<main class="container mx-auto px-4 py-8">
    <div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
        <!-- content -->
    </div>
</main>
```

---

## Styling Patterns

### Cards (glassmorphism)
```svelte
<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
```

### Inputs
```svelte
<Input class="bg-background/50 border-border/50" />
```

### Buttons
```svelte
<!-- Primary -->
<Button class="w-full">Submit</Button>

<!-- With loading -->
<Button disabled={isSubmitting}>
    {#if isSubmitting}
        <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
        Loading...
    {:else}
        Submit
    {/if}
</Button>
```

### Alerts
```svelte
<Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
    <CircleAlert class="h-4 w-4" />
    <AlertDescription>{message}</AlertDescription>
</Alert>
```

### Card Headers (always with icon)
```svelte
<CardTitle class="text-2xl flex items-center gap-2">
    <Shield class="w-5 h-5 text-primary" />
    Title
</CardTitle>
```

---

## DataTable

For server-side paginated lists with sorting/filtering.

```svelte
<script lang="ts">
    import { onMount } from 'svelte';
    import { DataTable, createDataTableService } from '$lib/components/data-table';
    import type { DataTableColumn } from '$lib/components/data-table/types';
    import { searchOrganizations } from '$lib/api/organization/OrganizationController';
    import type { OrganizationResponse } from '$lib/api/models';

    const columns: DataTableColumn<OrganizationResponse>[] = [
        { key: 'name', label: 'Name', sortable: true, filterable: true, 
          filterType: 'FUZZY_TEXT', colSpan: 2 },
        { key: 'status', label: 'Status', sortable: true, filterable: true,
          filterType: 'MULTI_ENUM', filterOptions: [
            { value: 'ACTIVE', label: 'Active' },
            { value: 'SUSPENDED', label: 'Suspended' }
          ], colSpan: 2 },
        { key: 'createdAt', label: 'Created', sortable: true, colSpan: 1 }
    ];

    const service = createDataTableService<OrganizationResponse>({
        fetchFn: searchOrganizations,
        pageSize: 10,
        defaultSort: [{ name: 'name', direction: 'ASC' }]
    });

    onMount(() => service.load());
</script>

<DataTable {columns} {service} title="Organizations" icon={Building2}>
    {#snippet row(org: OrganizationResponse)}
        <div class="grid grid-cols-5 gap-4 p-4 hover:bg-muted/30">
            <div class="col-span-2">{org.name}</div>
            <div class="col-span-2"><Badge>{org.status}</Badge></div>
            <div class="col-span-1">{formatDate(org.createdAt)}</div>
        </div>
    {/snippet}
</DataTable>
```

**Filter types:** `TEXT`, `FUZZY_TEXT`, `ENUM`, `MULTI_ENUM`, `BOOLEAN`, `NUMERIC`, `DATE`

---

## Sheet Pattern

```svelte
<script lang="ts">
    import * as Sheet from '$lib/components/ui/sheet';
    import { Radio, LoaderCircle } from '@lucide/svelte';

    interface Props {
        open: boolean;
        creating: boolean;
        onOpenChange: (open: boolean) => void;
        onSubmit: (name: string) => void;
    }

    let { open, creating, onOpenChange, onSubmit }: Props = $props();
    let name: string = $state('');

    function handleSubmit(): void {
        if (!name.trim()) return;
        onSubmit(name.trim());
        name = '';
    }
</script>

<Sheet.Root {open} onOpenChange={onOpenChange}>
    <Sheet.Content class="bg-card/95 backdrop-blur-xl border-border/50 flex flex-col sm:max-w-lg">
        <Sheet.Header class="pt-6">
            <Sheet.Title class="flex items-center gap-2">
                <Radio class="h-4 w-4 text-primary" />
                Create Item
            </Sheet.Title>
        </Sheet.Header>

        <div class="flex-1 py-6 space-y-6">
            <!-- Form fields -->
        </div>

        <Sheet.Footer class="flex-row gap-3 pb-6">
            <Button variant="outline" onclick={() => onOpenChange(false)}>Cancel</Button>
            <Button onclick={handleSubmit} disabled={creating}>
                {#if creating}
                    <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
                {/if}
                Create
            </Button>
        </Sheet.Footer>
    </Sheet.Content>
</Sheet.Root>
```

---

## Reusable Components

### Before Writing New Code

1. **Check existing components** in `$lib/components/` for reusable patterns
2. **Check the dev-playbook** at `/dev-playbook` for documented UI components
3. **Search the codebase** for similar implementations before creating new ones

### Component Locations

| Location | Purpose |
|----------|---------|
| `$lib/components/ui/` | shadcn-svelte base components |
| `$lib/components/layout/` | AppBackground, AppSidebar, AppLogo, AppNavbar |
| `$lib/components/data-table/` | Paginated table with filters |
| `$lib/components/[feature]/` | Feature-specific (channels, members, api-keys, etc.) |

### When Creating New Components

- **Extract if used 2+ times** - Don't duplicate code across pages
- **Keep components focused** - Single responsibility, minimal props
- **Add to barrel file** - Export from `index.ts` in the component folder
- **Consider dev-playbook** - If the component is generic and reusable across features, add it to `/dev-playbook` for documentation

### Dev Playbook Candidates

Add to dev-playbook if the component is:
- Generic (not tied to specific business logic)
- Reusable across multiple features
- Has configurable props/variants
- Would benefit other developers as a reference

Examples: StatusIndicator, InfoField, SectionHeader, EditableField, RoleBadge

---

## Utility Functions

### Date Formatting
```typescript
import { formatDate, formatDateTime, formatRelativeDate } from '$lib/utils/date';

formatDate('2024-01-15');        // "Jan 15, 2024"
formatDateTime('2024-01-15');    // "Jan 15, 2024, 2:30 PM"
formatRelativeDate('2024-01-15'); // "2 days ago"
```

### Error Handling
```typescript
import { handleError, getValidationErrorMap } from '$lib/utils/error-handler';
```

---

## Commands

```bash
bun run dev          # Dev server
bun run check        # Type check (must pass)
bun run build        # Production build
bun run sync:api     # Regenerate API types from backend
bun run test         # Playwright tests
```

---

## Checklist

Before completing any frontend work:

**Code Quality:**
- [ ] Explicit types on all variables and functions
- [ ] `CLIENT_ROUTES` used (no hardcoded paths)
- [ ] OpenAPI types from `$lib/api/models`
- [ ] No custom types in `$lib/api/models` only OpenAPI types
- [ ] Error handling with `handleError()` + `toast`
- [ ] Loading states on async operations
- [ ] `bun run check` passes

**Reusability:**
- [ ] Checked `$lib/components/` for existing reusable components
- [ ] Checked `/dev-playbook` for documented patterns
- [ ] Code is concise - no duplication, extracted shared logic
- [ ] New generic components added to dev-playbook (if applicable)
