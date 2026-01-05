# DataTable Component Suite

Generic, reusable DataTable components for Eventify that integrate with jframe's `SortablePageInput`/`PageResource<T>` pattern.

## Features

- ✅ **Server-side pagination** - Efficient handling of large datasets
- ✅ **Multi-column sorting** - Click headers to sort ASC/DESC
- ✅ **7 filter types** - TEXT, FUZZY_TEXT, ENUM, MULTI_ENUM, BOOLEAN, NUMERIC, DATE
- ✅ **Responsive design** - Mobile-optimized with collapsible headers
- ✅ **Glassmorphism styling** - Matches Eventify design standards
- ✅ **Type-safe** - Full TypeScript with generics
- ✅ **Loading states** - Skeleton loaders during fetch
- ✅ **Empty states** - Customizable empty state messages
- ✅ **Error handling** - Toast notifications and retry buttons

## Files Structure

```
client/src/lib/components/data-table/
├── index.ts                      # Main exports
├── types.ts                      # TypeScript definitions
├── service.svelte.ts             # createDataTableService<T>() factory
├── DataTable.svelte              # Main wrapper component
├── DataTableHeader.svelte        # Sortable column headers
├── DataTableFilters.svelte       # Filter bar (auto-rendered)
├── DataTablePagination.svelte    # Pagination controls
├── DataTableSkeleton.svelte      # Loading skeleton
├── DataTableEmpty.svelte         # Empty state
└── filters/
    ├── index.ts
    ├── TextFilter.svelte         # TEXT, FUZZY_TEXT
    ├── EnumFilter.svelte         # ENUM (single select buttons)
    ├── MultiEnumFilter.svelte    # MULTI_ENUM (multi-select buttons)
    ├── BooleanFilter.svelte      # BOOLEAN (3-state toggle)
    ├── NumericFilter.svelte      # NUMERIC (number input)
    └── DateFilter.svelte         # DATE (from/to date picker)
```

## Quick Start

### 1. Define Column Configuration

```typescript
import type { DataTableColumn, OrganizationResponse } from '$lib/components/data-table';

const columns: DataTableColumn<OrganizationResponse>[] = [
  {
    key: 'name',
    label: 'Name',
    sortable: true,
    filterable: true,
    filterType: 'FUZZY_TEXT',
    filterPlaceholder: 'Search organizations...',
    colSpan: 2
  },
  {
    key: 'status',
    label: 'Status',
    sortable: true,
    filterable: true,
    filterType: 'MULTI_ENUM',
    filterOptions: [
      { value: 'TRIAL', label: 'Trial' },
      { value: 'ACTIVE', label: 'Active' },
      { value: 'SUSPENDED', label: 'Suspended' }
    ]
  },
  {
    key: 'createdAt',
    label: 'Created',
    sortable: true,
    filterable: true,
    filterType: 'DATE',
    colSpan: 2,
    format: (value) => new Date(value as string).toLocaleDateString()
  }
];
```

### 2. Create Service

```typescript
import { createDataTableService } from '$lib/components/data-table';
import { client } from '$lib/api/client';
import type { SortablePageInput, PageResourceOrganizationResponse } from '$lib/api/models';

async function fetchOrganizations(input: SortablePageInput): Promise<PageResourceOrganizationResponse> {
  const { data, error } = await client.POST('/v1/admin/organization/search', { body: input });
  if (error) throw error;
  return data;
}

const service = createDataTableService<OrganizationResponse>({
  fetchFn: fetchOrganizations,
  pageSize: 10,
  defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
});
```

### 3. Use DataTable Component

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { DataTable } from '$lib/components/data-table';
  import { Badge } from '$lib/components/ui/badge';
  import { Building2 } from '@lucide/svelte';

  onMount(() => service.load());
</script>

<DataTable {columns} {service} title="Organizations" icon={Building2}>
  {#snippet row(org)}
    <div
      class="grid grid-cols-1 md:grid-cols-11 gap-2 md:gap-4 p-4 rounded-lg border border-border/50 bg-card/30 hover:bg-accent/5 transition-colors"
    >
      <!-- Name (2 cols) -->
      <div class="col-span-1 md:col-span-2">
        <div class="font-medium">{org.name}</div>
      </div>

      <!-- Status (1 col) -->
      <div class="col-span-1 md:col-span-1">
        <Badge>{org.status}</Badge>
      </div>

      <!-- Created (2 cols) -->
      <div class="col-span-1 md:col-span-2">
        <div class="text-sm text-muted-foreground">
          {new Date(org.createdAt).toLocaleDateString()}
        </div>
      </div>
    </div>
  {/snippet}
</DataTable>
```

## API Reference

### `createDataTableService<T>(config: DataTableConfig<T>)`

Creates a reactive service for managing table state.

**Config:**
- `fetchFn: (input: SortablePageInput) => Promise<PageResource<T>>` - API fetch function
- `pageSize?: number` - Items per page (default: 10)
- `defaultSort?: SortableColumn[]` - Initial sort order (supports multi-column)

**Returns:** `DataTableService<T>` with:

**State (reactive getters):**
- `items: T[]` - Current page items
- `loading: boolean` - Loading state
- `error: string | null` - Error message
- `currentPage: number` - Current page (0-based)
- `totalPages: number` - Total pages
- `totalElements: number` - Total items
- `pageSize: number` - Items per page
- `showingRange: string` - "Showing 1-10 of 50"
- `hasPreviousPage: boolean` - Can go back
- `hasNextPage: boolean` - Can go forward
- `sortKey: string | null` - Current sort column
- `sortDirection: SortDirection` - Current sort direction
- `filters: Record<string, FilterValue>` - Active filters

**Actions:**
- `load(): Promise<void>` - Load data from API
- `setPage(page: number): void` - Go to specific page
- `nextPage(): void` - Go to next page
- `previousPage(): void` - Go to previous page
- `setSort(key: string): void` - Sort by column (toggles direction)
- `setFilter(fieldName: string, value: FilterValue): void` - Set filter
- `clearFilter(fieldName: string): void` - Remove filter
- `clearAllFilters(): void` - Remove all filters
- `reset(): void` - Reset all filters and sort to defaults
- `refresh(): void` - Reload current page

### `DataTable` Component Props

```typescript
interface Props<T> {
  columns: DataTableColumn<T>[];
  service: DataTableService<T>;
  row: Snippet<[T]>;              // Custom row rendering
  empty?: Snippet;                // Custom empty state (optional)
  skeletonRows?: number;          // Skeleton rows count (default: 5)
  title?: string;                 // Card title
  description?: string;           // Card description
  icon?: Component;               // Title icon (Lucide component)
}
```

### `DataTableColumn<T>` Interface

```typescript
interface DataTableColumn<T> {
  key: string;                      // Field name (maps to backend)
  label: string;                    // Display label
  colSpan?: number;                 // Grid columns (default: 1)
  sortable?: boolean;               // Enable sorting
  filterable?: boolean;             // Show filter
  filterType?: FilterType;          // Filter UI type
  filterOptions?: FilterOption[];   // Options for ENUM/MULTI_ENUM
  filterPlaceholder?: string;       // Filter placeholder text
  format?: (value: unknown, item: T) => string;  // Display formatter
}
```

### Filter Types

| FilterType | UI Component | Backend Mapping |
|------------|--------------|-----------------|
| `TEXT` | Text input (no debounce) | `textValue` |
| `FUZZY_TEXT` | Text input with search icon (debounced 300ms) | `textValue` |
| `ENUM` | Single-select button group | `textValue` |
| `MULTI_ENUM` | Multi-select pill buttons | `textValueList` |
| `BOOLEAN` | 3-state toggle (All/Yes/No) | `textValue` ("true"/"false") |
| `NUMERIC` | Number input | `textValueAsInteger` |
| `DATE` | Date range picker (from/to) | `fromDateValue`, `toDateValue` |

## Advanced Usage

### Custom Empty State

```svelte
<DataTable {columns} {service}>
  {#snippet row(item)}
    <!-- Row content -->
  {/snippet}

  {#snippet empty()}
    <div class="py-12 text-center">
      <Building2 class="w-16 h-16 mx-auto text-primary/50" />
      <h3 class="mt-4 text-lg font-semibold">No organizations yet</h3>
      <p class="mt-2 text-muted-foreground">Create your first organization to get started</p>
      <Button class="mt-4">Create Organization</Button>
    </div>
  {/snippet}
</DataTable>
```

### Custom Format Function

```typescript
{
  key: 'createdAt',
  label: 'Created',
  format: (value, item) => {
    const date = new Date(value as string);
    const now = new Date();
    const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    return date.toLocaleDateString();
  }
}
```

### Programmatic Control

```typescript
// Load data on mount
onMount(() => service.load());

// Refresh data
function handleRefresh() {
  service.refresh();
}

// Apply preset filter
function filterActiveOnly() {
  service.setFilter('status', 'ACTIVE');
}

// Reset all filters and sort to defaults
function resetAll() {
  service.reset();
}

// Go to specific page
function goToPage(page: number) {
  service.setPage(page);
}
```

## Design Patterns

### Grid Layout

Columns use CSS Grid with `colSpan` to control width:

```typescript
// Total columns = sum of all colSpan values
const columns = [
  { key: 'name', colSpan: 3 },     // Takes 3 grid units
  { key: 'status', colSpan: 1 },   // Takes 1 grid unit
  { key: 'date', colSpan: 2 }      // Takes 2 grid units
];
// Total: 6 grid columns
```

Row rendering must match column spans:

```svelte
{#snippet row(item)}
  <div class="grid grid-cols-6 gap-4">
    <div class="col-span-3">{item.name}</div>
    <div class="col-span-1">{item.status}</div>
    <div class="col-span-2">{item.date}</div>
  </div>
{/snippet}
```

### Mobile Responsiveness

- **Desktop:** Full grid layout with all columns
- **Mobile:** Stack vertically, hide desktop-only columns

```svelte
{#snippet row(item)}
  <div class="grid grid-cols-1 md:grid-cols-6 gap-2 md:gap-4 p-4">
    <!-- Name: Full width on mobile, 3 cols on desktop -->
    <div class="col-span-1 md:col-span-3">
      <span class="md:hidden font-medium">Name: </span>
      {item.name}
    </div>
    
    <!-- Status: Inline on mobile, 1 col on desktop -->
    <div class="col-span-1 md:col-span-1">
      <span class="md:hidden">Status: </span>
      <Badge>{item.status}</Badge>
    </div>
    
    <!-- Date: Hidden on mobile, 2 cols on desktop -->
    <div class="hidden md:block md:col-span-2">
      {item.date}
    </div>
  </div>
{/snippet}
```

## Styling

### Glassmorphism Theme

All components use consistent glassmorphism styling:

```css
/* Cards */
border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl

/* Rows */
border-border/50 bg-card/30 hover:bg-accent/5 transition-colors

/* Inputs */
bg-background/50 border-border focus:border-primary focus:ring-2 focus:ring-primary/20

/* Active filters/buttons */
bg-gradient-to-r from-primary to-accent
```

### Custom Row Styling

Add hover effects, badges, icons as needed:

```svelte
{#snippet row(org)}
  <div class="grid grid-cols-11 gap-4 p-4 rounded-lg border border-border/50 
              bg-card/30 hover:bg-accent/5 transition-colors group">
    <div class="col-span-3 flex items-center gap-2">
      <Building2 class="h-4 w-4 text-primary group-hover:scale-110 transition-transform" />
      <span class="font-medium">{org.name}</span>
    </div>
    <!-- More columns -->
  </div>
{/snippet}
```

## Performance

- **Server-side pagination** - Only loads current page data
- **Debounced text filters** - 300ms delay for FUZZY_TEXT
- **Optimized re-renders** - Svelte 5 runes for fine-grained reactivity
- **Skeleton loaders** - Immediate visual feedback during loading

## Type Safety

All types are imported from OpenAPI-generated schemas:

```typescript
import type {
  SortablePageInput,
  PageResource,
  SearchInput,
  SortableColumn,
  SortDirection
} from '$lib/api/models';

// Use OpenAPI types for response data
import type { OrganizationResponse } from '$lib/api/models';

const service = createDataTableService<OrganizationResponse>({
  fetchFn: async (input: SortablePageInput) => {
    // TypeScript validates input/output
    const { data } = await client.POST('/v1/admin/organization/search', { body: input });
    return data; // PageResource<OrganizationResponse>
  }
});
```

## Error Handling

- **Toast notifications** - Automatic error toasts via `svelte-sonner`
- **Error alerts** - In-component error display with retry button
- **Type-safe errors** - Uses `handleError()` utility for consistent formatting

## Troubleshooting

### Filters not working

Ensure `filterable: true` and `filterType` is set:

```typescript
{
  key: 'status',
  label: 'Status',
  filterable: true,        // ✅ Required
  filterType: 'ENUM',      // ✅ Required
  filterOptions: [...]     // ✅ Required for ENUM/MULTI_ENUM
}
```

### Sort not working

Ensure `sortable: true`:

```typescript
{
  key: 'createdAt',
  label: 'Created',
  sortable: true  // ✅ Required
}
```

### Grid layout broken

Check that row grid columns match sum of `colSpan` values:

```typescript
// Columns define 11 total columns
const columns = [
  { key: 'name', colSpan: 2 },
  { key: 'slug', colSpan: 2 },
  { key: 'status', colSpan: 1 },
  { key: 'owner', colSpan: 2 },
  { key: 'members', colSpan: 1 },
  { key: 'created', colSpan: 2 },
  { key: 'actions', colSpan: 1 }
];
// Sum = 11 ✅

// Row must use grid-cols-11
{#snippet row(org)}
  <div class="grid grid-cols-11 gap-4">
    <!-- ... -->
  </div>
{/snippet}
```

## Future Enhancements

Potential additions for future versions:

- [ ] Column visibility toggle
- [ ] Export to CSV/Excel
- [ ] Bulk selection with checkboxes
- [ ] Column resizing
- [ ] Saved filter presets
- [ ] Advanced filter builder UI
- [ ] Virtual scrolling for very large datasets
- [ ] Row expansion (detail view)

## Migration from Old Patterns

If migrating from custom table implementations:

**Before:**
```typescript
// Custom service with manual pagination/filter logic
const service = createOrganizationListService(10);
```

**After:**
```typescript
// Generic DataTable service
const service = createDataTableService<OrganizationResponse>({
  fetchFn: fetchOrganizations,
  pageSize: 10
});
```

**Before:**
```svelte
<!-- Manual table markup -->
<Table>
  <TableHeader>...</TableHeader>
  <TableBody>...</TableBody>
</Table>
```

**After:**
```svelte
<!-- Declarative DataTable -->
<DataTable {columns} {service}>
  {#snippet row(item)}
    <!-- Custom row rendering -->
  {/snippet}
</DataTable>
```

---

Built with ❤️ for Eventify using SvelteKit + Svelte 5 + shadcn-svelte
