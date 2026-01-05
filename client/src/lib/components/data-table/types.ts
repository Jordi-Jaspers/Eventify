import type {
	SortDirection,
	SearchInput,
	SortablePageInput,
	SortableColumn,
	PageResource
} from '$lib/api/models';
import type { Snippet, Component } from 'svelte';

// Filter types matching jframe SearchType
export type FilterType =
	| 'TEXT'
	| 'FUZZY_TEXT'
	| 'ENUM'
	| 'MULTI_ENUM'
	| 'BOOLEAN'
	| 'NUMERIC'
	| 'DATE';

export interface FilterOption {
	value: string;
	label: string;
}

export interface DateRange {
	from?: string;
	to?: string;
}

export type FilterValue = string | string[] | number | boolean | DateRange | null;

// Column definition
export interface DataTableColumn<T> {
	key: string; // Field name (maps to backend)
	label: string; // Display label
	colSpan?: number; // Grid columns (default: 1)
	sortable?: boolean; // Can sort by this column
	filterable?: boolean; // Show filter for this column
	filterType?: FilterType; // How to filter (required if filterable)
	filterOptions?: FilterOption[]; // For ENUM/MULTI_ENUM
	filterPlaceholder?: string; // Placeholder text
	format?: (value: unknown, item: T) => string; // Format for display
}

// Service configuration
export interface DataTableConfig<T> {
	fetchFn: (input: SortablePageInput) => Promise<PageResource<T>>;
	pageSize?: number;
	defaultSort?: SortableColumn[];
}

// Service interface (what createDataTableService returns)
export interface DataTableService<T> {
	// Data (reactive getters)
	readonly items: T[];
	readonly loading: boolean;
	readonly error: string | null;

	// Pagination
	readonly currentPage: number;
	readonly totalPages: number;
	readonly totalElements: number;
	readonly pageSize: number;
	readonly showingRange: string;
	readonly hasPreviousPage: boolean;
	readonly hasNextPage: boolean;

	// Sort
	readonly sortKey: string | null;
	readonly sortDirection: SortDirection;

	// Filters
	readonly filters: Record<string, FilterValue>;

	// Actions
	load(): Promise<void>;
	setPage(page: number): void;
	nextPage(): void;
	previousPage(): void;
	setSort(key: string): void;
	setFilter(fieldName: string, value: FilterValue): void;
	clearFilter(fieldName: string): void;
	clearAllFilters(): void;
	reset(): void;
	refresh(): void;
}
