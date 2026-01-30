# DataTable Component - Generic Server-Side Table

## Date: 2026-01-04
## Epic: UI Components

---

## Summary

Built a generic, reusable DataTable component that integrates with jframe's `SortablePageInput`/`PageResource<T>` pattern for server-side pagination, sorting, and filtering. Refactored Organizations admin page to use it.

---

## Features

### DataTable Component Suite
- **17 files** in `client/src/lib/components/data-table/`
- Generic TypeScript support (`<T>`)
- Svelte 5 runes (`$state`, `$derived`, `$props`)
- Glassmorphism styling

### Filter Types Supported
| Type | UI | Backend Mapping |
|------|-----|-----------------|
| TEXT | Text input | `textValue` |
| FUZZY_TEXT | Text input with search icon, debounced | `textValue` |
| ENUM | Button group (single select) | `textValue` |
| MULTI_ENUM | Pill buttons (multi-select) | `textValueList` |
| BOOLEAN | 3-state toggle (All/Yes/No) | `textValue` |
| NUMERIC | Number input | `textValueAsInteger` |
| DATE | Date range picker | `fromDateValue`, `toDateValue` |

### Components Created
- `DataTable.svelte` - Main wrapper with Card, error handling
- `DataTableHeader.svelte` - Sortable column headers
- `DataTableFilters.svelte` - Smart filter bar layout
- `DataTablePagination.svelte` - Prev/next controls
- `DataTableSkeleton.svelte` - Shimmer animation skeleton
- `DataTableEmpty.svelte` - Empty state with icon
- `filters/TextFilter.svelte` - Text/fuzzy search
- `filters/EnumFilter.svelte` - Single select
- `filters/MultiEnumFilter.svelte` - Multi-select pills
- `filters/BooleanFilter.svelte` - 3-state toggle
- `filters/NumericFilter.svelte` - Number input
- `filters/DateFilter.svelte` - Date range
- `service.svelte.ts` - `createDataTableService<T>()` factory
- `types.ts` - TypeScript definitions
- `index.ts` - Exports

---

## Usage Pattern

```typescript
// 1. Define columns
const columns: DataTableColumn<OrganizationResponse>[] = [
  { 
    key: 'name', 
    label: 'Name', 
    sortable: true, 
    filterable: true, 
    filterType: 'FUZZY_TEXT',
    colSpan: 2 
  },
  { 
    key: 'status', 
    label: 'Status', 
    filterable: true,
    filterType: 'MULTI_ENUM',
    filterOptions: [
      { value: 'ACTIVE', label: 'Active' },
      { value: 'TRIAL', label: 'Trial' }
    ]
  }
];

// 2. Create service with controller function
const service = createDataTableService<OrganizationResponse>({
  fetchFn: searchOrganizations,  // From controller
  pageSize: 10
});

// 3. Load on mount
onMount(() => service.load());
```

```svelte
<!-- 4. Render DataTable -->
<DataTable {columns} {service} title="Organizations" icon={Building2}>
  {#snippet row(org)}
    <div class="grid grid-cols-11 ...">
      <!-- Custom row content -->
    </div>
  {/snippet}
</DataTable>
```

---

## Organizations Page Refactor

### Before
- 297 lines
- Custom `OrganizationListService.svelte.ts`
- Inline table, filters, pagination, skeleton
- Direct `client.POST` calls

### After
- 198 lines (-33%)
- Uses `DataTable` component
- Uses `searchOrganizations` from controller
- Clean column configuration
- Custom row snippet

---

## Files Changed

### Created (17 files)
```
client/src/lib/components/data-table/
├── index.ts
├── types.ts
├── service.svelte.ts
├── DataTable.svelte
├── DataTableHeader.svelte
├── DataTableFilters.svelte
├── DataTablePagination.svelte
├── DataTableSkeleton.svelte
├── DataTableEmpty.svelte
├── README.md
└── filters/
    ├── index.ts
    ├── TextFilter.svelte
    ├── EnumFilter.svelte
    ├── MultiEnumFilter.svelte
    ├── BooleanFilter.svelte
    ├── NumericFilter.svelte
    └── DateFilter.svelte
```

### Modified
- `client/src/routes/(authenticated)/admin/organizations/+page.svelte` - Refactored to use DataTable
- `client/src/lib/api/organization/OrganizationController.ts` - Simplified to accept `SortablePageInput`

### Deleted
- `client/src/lib/api/organization/OrganizationListService.svelte.ts` - No longer needed

---

## Filter Bar Layout

Smart layout that matches original design:
- **Row 1:** Text search (flex-1) + Pill filters (inline) + Clear button
- **Row 2:** Numeric/Date filters (if present)

Responsive: stacks vertically on mobile.

---

## Skeleton Animation

Custom shimmer effect instead of basic `animate-pulse`:
- Gradient sweep animation (1.5s)
- Varied widths for natural look
- Realistic layout: avatar + text, middle content, action button

---

## Quality Metrics

- ✅ `bun run check`: 0 errors, 0 warnings
- ✅ TypeScript strict mode
- ✅ Explicit types everywhere
- ✅ Svelte 5 runes
- ✅ Glassmorphism styling
- ✅ Responsive design

---

## Next Steps

Story created for members table integration:
- `.opencode/jira/refined/DATATABLE-members-search-integration.md`
- Requires backend `searchMembers` endpoint first
