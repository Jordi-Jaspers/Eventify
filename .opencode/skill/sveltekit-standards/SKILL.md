---
name: sveltekit-standards
description: Project-specific conventions for the eventify frontend. Claude already knows SvelteKit/Svelte 5 fundamentals.
metadata:
  skill-type: frontend
  language: svelte
  framework: sveltekit
  build-tool: vite, bun
---

# SvelteKit Standards

Project-specific conventions for the eventify frontend. Claude already knows SvelteKit/Svelte 5 fundamentals.

## Tech Stack

```yaml
Framework: SvelteKit 2.x with Svelte 5
Language: TypeScript (strict mode)
Runtime: Bun 1.3.0
UI: shadcn-svelte (copy-paste, you own it)
Styling: TailwindCSS v4
Icons: @lucide/svelte
API: OpenAPI with type generation
State: Svelte stores & runes ($state, $derived)
```

## TypeScript Standards (Non-Negotiable)

**Explicit type annotations on ALL variables and functions:**

```typescript
// CORRECT
const user: UserResponse = await fetchUser();
const items: EventItem[] = [];
const loading: boolean = false;
function handleSubmit(event: SubmitEvent): Promise<void> { /* ... */ }

// WRONG - never use type inference
const user = await fetchUser();
const items = [];
let loading = false;
```

**Use OpenAPI generated types (never duplicate):**

```typescript
// Types are in $lib/api/models.ts
import type { UserResponse, CreateUserRequest } from '$lib/api/models';
```

## Route Architecture

**Routes are adapters - keep them slim:**

```typescript
// routes/events/[id]/+page.server.ts
import { EventService } from '$lib/services/events';
import { error } from '@sveltejs/kit';

export async function load({ params }): Promise<{ event: Event }> {
  const event: Event | null = await EventService.getById(params.id);
  if (!event) throw error(404, 'Event not found');
  return { event };
}
```

**Business logic goes in services, not routes.**

**Never hardcode paths:**

```typescript
// CORRECT
import { CLIENT_ROUTES } from '$lib/config/routes';
goto(CLIENT_ROUTES.LOGIN);

// WRONG
goto('/login');
```

## Design System

### Visual Principles
- **Dark & Sophisticated** - Dark themes with rich gradients
- **Glassmorphism** - Semi-transparent cards with backdrop blur
- **Gradient Accents** - Strategic use for emphasis
- **Futuristic Feel** - Clean lines, smooth animations

### Required Classes by Element Type

**Cards (always glassmorphism):**
```svelte
<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
```

**Inputs (semi-transparent):**
```svelte
<Input class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20" />
```

**Primary Buttons (gradient):**
```svelte
<Button class="w-full bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all shadow-lg hover:shadow-primary/50">
```

**Alerts (backdrop blur):**
```svelte
<Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
```

**Tables/Lists (border styling):**
```svelte
<div class="rounded-lg border border-border/50 bg-card/50 backdrop-blur-sm">
```

### Icon Rules

**Always use icons with actions, never text-only buttons:**

```typescript
// Common icons from @lucide/svelte
Activity       // Logo/branding
Shield         // Security/authentication
UserPlus       // Registration
LogOut         // Logout
User           // Profile
Clock          // Time
Check / X      // Success/error
Upload         // Uploads
Database       // Data
```

**Card headers always have icons:**
```svelte
<CardTitle class="text-2xl flex items-center gap-2">
    <Shield class="w-5 h-5 text-primary" />
    Title Here
</CardTitle>
```

### Animation Rules

**DO USE:**
- Page-level entrance (`animate-fade-in` on container)
- Hover transitions on interactive elements
- Focus ring transitions

**DO NOT USE:**
- Stagger delays on form fields
- Individual field entrance animations
- Overly complex sequences

## Reusable Components

### Layout Components (already exist)
| Component | Usage |
|-----------|-------|
| `<AppBackground>` | Animated grid + gradient orbs (layout provides) |
| `<AppLogo size="..." subtitle="..." />` | Branding |
| `<AppSidebar currentPath="..." />` | Authenticated pages navigation |

### Auth Components
| Component | Usage |
|-----------|-------|
| `<OAuthButtons disabled={...} />` | Google/GitHub buttons with separator |

**Rule:** Use component if it exists. Create new if pattern repeats 3+ times.

## Page Patterns

### Public Pages (login, register, etc.)

Layout provides: background, centering, fade-in. You only need content:

```svelte
<div class="max-w-md mx-auto">
    <div class="mb-8">
        <AppLogo size="medium" subtitle="Context description" />
    </div>
    
    <Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
        <!-- Card content -->
    </Card>
    
    <p class="text-center text-xs text-muted-foreground mt-6">
        By [action], you agree to our Terms of Service and Privacy Policy
    </p>
</div>
```

### Authenticated Pages (dashboard, etc.)

Use `AppSidebar` with `SidebarProvider`. See `routes/(authenticated)/+layout.svelte` for pattern.

### Form Pattern

```svelte
<script lang="ts">
  import { goto } from '$app/navigation';
  import { Button, Input, Label } from '$lib/components/ui';
  import { Card, CardHeader, CardTitle, CardContent } from '$lib/components/ui/card';
  import { Alert, AlertDescription } from '$lib/components/ui/alert';
  import { LoaderCircle, Shield, CircleAlert } from '@lucide/svelte';
  import { toast } from 'svelte-sonner';
  import { CLIENT_ROUTES } from '$lib/config/routes';
  
  let email: string = $state('');
  let isSubmitting: boolean = $state(false);
  let errors: Record<string, string> = $state({});
  
  async function handleSubmit(event: SubmitEvent): Promise<void> {
    event.preventDefault();
    isSubmitting = true;
    errors = {};
    
    try {
      // API call
      goto(CLIENT_ROUTES.DASHBOARD);
    } catch (error) {
      toast.error('Error message');
    } finally {
      isSubmitting = false;
    }
  }
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl">
  <CardHeader>
    <CardTitle class="text-2xl flex items-center gap-2">
      <Shield class="w-5 h-5 text-primary" />
      Form Title
    </CardTitle>
  </CardHeader>
  
  <CardContent>
    {#if errors.general}
      <Alert variant="destructive" class="mb-4 bg-destructive/10 border-destructive/50 backdrop-blur-sm">
        <CircleAlert class="h-4 w-4" />
        <AlertDescription>{errors.general}</AlertDescription>
      </Alert>
    {/if}

    <form onsubmit={handleSubmit} class="space-y-4">
      <div class="space-y-2">
        <Label for="email">Email</Label>
        <Input
          id="email"
          type="email"
          bind:value={email}
          disabled={isSubmitting}
          class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
          aria-invalid={!!errors.email}
        />
      </div>
      
      <Button 
        type="submit" 
        class="w-full bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all shadow-lg"
        disabled={isSubmitting}
      >
        {#if isSubmitting}
          <LoaderCircle class="mr-2 h-4 w-4 animate-spin" />
          Loading...
        {:else}
          Submit
        {/if}
      </Button>
    </form>
  </CardContent>
</Card>
```

### Password Field with Toggle

```svelte
<script lang="ts">
  import { Eye, EyeOff } from '@lucide/svelte';
  let showPassword: boolean = $state(false);
</script>

<div class="relative">
  <Input
    type={showPassword ? 'text' : 'password'}
    class="pr-10 bg-background/50 border-border transition-all"
  />
  <button
    type="button"
    onclick={() => showPassword = !showPassword}
    class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-primary"
    aria-label={showPassword ? 'Hide password' : 'Show password'}
  >
    {#if showPassword}<EyeOff class="h-4 w-4" />{:else}<Eye class="h-4 w-4" />{/if}
  </button>
</div>
```

## DataTable (Server-Side Pagination)

Use for: paginated lists, server-side search/sort/filter.

**Location:** `$lib/components/data-table/`

**Key components:**
- `DataTable` - Main wrapper
- `createDataTableService<T>()` - Service factory

**Filter types:** `TEXT`, `FUZZY_TEXT`, `ENUM`, `MULTI_ENUM`, `BOOLEAN`, `NUMERIC`, `DATE`

**Quick reference:**
```svelte
<script lang="ts">
  import { DataTable, createDataTableService } from '$lib/components/data-table';
  import type { DataTableColumn } from '$lib/components/data-table/types';
  
  const columns: DataTableColumn<Item>[] = [
    { key: 'name', label: 'Name', sortable: true, filterable: true, filterType: 'FUZZY_TEXT' },
    { key: 'status', label: 'Status', filterable: true, filterType: 'MULTI_ENUM', filterOptions: [...] }
  ];

  const service = createDataTableService<Item>({ fetchFn: searchItems, pageSize: 10 });
  onMount(() => service.load());
</script>

<DataTable {columns} {service} title="Items" icon={Box}>
  {#snippet row(item: Item)}
    <div class="grid grid-cols-3 gap-4 p-4"><!-- row content --></div>
  {/snippet}
</DataTable>
```

**Reference implementation:** `routes/(authenticated)/admin/organizations/+page.svelte`

## State Management

### Svelte 5 Runes Pattern

```typescript
// lib/stores/app-state.svelte.ts
class AppState {
  items = $state<Item[]>([]);
  filter = $state<FilterOptions>({ status: 'all' });
  
  filteredItems = $derived(
    this.items.filter((item: Item) => 
      this.filter.status === 'all' || item.status === this.filter.status
    )
  );
  
  async loadItems(): Promise<void> {
    this.items = await fetchItems();
  }
}

export const appState: AppState = new AppState();
```

## Accessibility (Required)

```svelte
<form
  onsubmit={handleSubmit}
  aria-busy={submitting}
>
  <Label for="email">
    Email <span class="text-destructive" aria-label="required">*</span>
  </Label>
  <Input
    id="email"
    required
    aria-invalid={!!errors.email}
    aria-describedby={errors.email ? 'email-error' : undefined}
  />
  {#if errors.email}
    <p id="email-error" class="text-sm text-destructive">{errors.email}</p>
  {/if}
</form>
```

**Every component must be:**
- Keyboard navigable
- Screen-reader friendly
- WCAG AA compliant (contrast)

## Loading & Empty States

### Skeleton Loading
```svelte
<div class="space-y-4">
  {#each Array(3) as _}
    <div class="rounded-lg border border-border/50 bg-card/50 p-4">
      <div class="h-4 bg-muted/50 rounded animate-pulse w-3/4" />
    </div>
  {/each}
</div>
```

### Empty State
```svelte
<div class="flex flex-col items-center justify-center py-12">
  <div class="p-6 rounded-2xl bg-gradient-to-br from-primary/10 to-accent/10 border border-border/50">
    <Database class="w-12 h-12 text-primary" />
  </div>
  <h3 class="mt-6 text-lg font-semibold">No data available</h3>
  <p class="mt-2 text-sm text-muted-foreground">Description</p>
  <Button class="mt-6 bg-gradient-to-r from-primary to-accent">
    <Upload class="mr-2 h-4 w-4" />
    Action
  </Button>
</div>
```

## Development Commands

```bash
# From client/ directory
bun run dev              # Dev server
bun run check            # Type check (MUST pass)
bun run build            # Production build
bun run download:api     # Download OpenAPI spec
bun run generate:api     # Generate types from spec
bun run test:components  # Playwright screenshot tests
```

**OpenAPI workflow:**
1. Ensure backend is running
2. `bun run download:api` 
3. `bun run generate:api`

## Quality Checklist

Before completion:

**Visual:**
- [ ] Glassmorphism on cards
- [ ] Gradient on primary buttons
- [ ] Icons in card headers
- [ ] Consistent spacing

**Code:**
- [ ] Explicit types on ALL variables
- [ ] CLIENT_ROUTES used (no hardcoded paths)
- [ ] OpenAPI types used
- [ ] `bun run check` passes

**UX:**
- [ ] Loading states with skeletons
- [ ] Error states with guidance
- [ ] Empty states
- [ ] Keyboard navigation works

## Update Protocol

**If you create/modify reusable components or design patterns:**
1. Update this skill file
2. Document in agent's "Available Components" section

This ensures future implementations stay consistent.
