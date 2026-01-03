import { toast } from 'svelte-sonner';
import { handleError } from '$lib/utils/error-handler';
import type {
	SortablePageInput,
	SearchInput,
	SortableColumn,
	PageResource
} from '$lib/api/models';
import type { DataTableConfig, DataTableService, FilterValue, DateRange } from './types';

/**
 * Creates a reactive DataTable service that manages data fetching, sorting, filtering, and pagination.
 * Uses Svelte 5 runes for reactive state management.
 */
export function createDataTableService<T>(config: DataTableConfig<T>): DataTableService<T> {
	const { fetchFn, pageSize = 10, defaultSortKey = null, defaultSortDirection = 'ASC' } = config;

	// Core state
	let items: T[] = $state([]);
	let loading: boolean = $state(false);
	let error: string | null = $state(null);

	// Sort state
	let sortKey: string | null = $state(defaultSortKey);
	let sortDirection: typeof defaultSortDirection = $state(defaultSortDirection);

	// Filter state
	let filters: Record<string, FilterValue> = $state({});

	// Pagination state
	let currentPage: number = $state(0);
	let totalPages: number = $state(0);
	let totalElements: number = $state(0);

	// Debounce timer
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;

	/**
	 * Converts filter values to SearchInput[] for API consumption
	 */
	function buildSearchInputs(): SearchInput[] {
		const searchInputs: SearchInput[] = [];

		Object.entries(filters).forEach(([fieldName, value]: [string, FilterValue]) => {
			if (value === null || value === undefined || value === '') return;

			const searchInput: Partial<SearchInput> = {
				fieldName,
				operator: 'AND'
			};

			// Handle different filter value types
			if (typeof value === 'string') {
				searchInput.textValue = value;
			} else if (typeof value === 'number') {
				searchInput.textValueAsInteger = value;
			} else if (typeof value === 'boolean') {
				searchInput.textValue = value.toString();
			} else if (Array.isArray(value)) {
				if (value.length === 0) return; // Skip empty arrays
				searchInput.textValueList = value;
			} else if (typeof value === 'object' && 'from' in value) {
				// DateRange
				const dateRange: DateRange = value as DateRange;
				if (dateRange.from) {
					searchInput.fromDateValue = dateRange.from;
				}
				if (dateRange.to) {
					searchInput.toDateValue = dateRange.to;
				}
				// Skip if no date values provided
				if (!searchInput.fromDateValue && !searchInput.toDateValue) return;
			}

			searchInputs.push(searchInput as SearchInput);
		});

		return searchInputs;
	}

	/**
	 * Builds SortablePageInput from current state
	 */
	function buildPageInput(): SortablePageInput {
		const input: SortablePageInput = {
			pageNumber: currentPage,
			pageSize,
			searchInputs: buildSearchInputs()
		};

		if (sortKey) {
			const sortColumn: SortableColumn = {
				name: sortKey,
				direction: sortDirection
			};
			input.sortOrder = [sortColumn];
		}

		return input;
	}

	/**
	 * Loads data from API
	 */
	async function load(): Promise<void> {
		loading = true;
		error = null;

		try {
			const response: PageResource<T> = await fetchFn(buildPageInput());

			items = response.content ?? [];
			totalPages = response.totalPages;
			totalElements = response.totalElements;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load data');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	/**
	 * Sets current page and reloads
	 */
	function setPage(page: number): void {
		if (page >= 0 && page < totalPages) {
			currentPage = page;
			load();
		}
	}

	/**
	 * Goes to next page
	 */
	function nextPage(): void {
		if (currentPage < totalPages - 1) {
			currentPage++;
			load();
		}
	}

	/**
	 * Goes to previous page
	 */
	function previousPage(): void {
		if (currentPage > 0) {
			currentPage--;
			load();
		}
	}

	/**
	 * Sets sort key and direction, then reloads
	 */
	function setSort(key: string): void {
		if (sortKey === key) {
			// Toggle direction if same key
			sortDirection = sortDirection === 'ASC' ? 'DESC' : 'ASC';
		} else {
			// New key, default to ASC
			sortKey = key;
			sortDirection = 'ASC';
		}
		currentPage = 0;
		load();
	}

	/**
	 * Sets a filter value with optional debounce for text filters
	 */
	function setFilter(fieldName: string, value: FilterValue, debounce: boolean = false): void {
		filters[fieldName] = value;

		if (debounceTimer) {
			clearTimeout(debounceTimer);
		}

		if (debounce) {
			debounceTimer = setTimeout(() => {
				currentPage = 0;
				load();
			}, 300);
		} else {
			currentPage = 0;
			load();
		}
	}

	/**
	 * Clears a specific filter
	 */
	function clearFilter(fieldName: string): void {
		delete filters[fieldName];
		currentPage = 0;
		load();
	}

	/**
	 * Clears all filters
	 */
	function clearAllFilters(): void {
		filters = {};
		currentPage = 0;
		load();
	}

	/**
	 * Refreshes data (reloads current page)
	 */
	function refresh(): void {
		load();
	}

	/**
	 * Computes showing range string
	 */
	const showingRange: string = $derived.by(() => {
		if (totalElements === 0) return 'Showing 0 results';

		const start: number = currentPage * pageSize + 1;
		const end: number = Math.min((currentPage + 1) * pageSize, totalElements);
		return `Showing ${start}-${end} of ${totalElements}`;
	});

	const hasPreviousPage: boolean = $derived(currentPage > 0);
	const hasNextPage: boolean = $derived(currentPage < totalPages - 1);

	return {
		// Getters (reactive)
		get items() {
			return items;
		},
		get loading() {
			return loading;
		},
		get error() {
			return error;
		},
		get currentPage() {
			return currentPage;
		},
		get totalPages() {
			return totalPages;
		},
		get totalElements() {
			return totalElements;
		},
		get pageSize() {
			return pageSize;
		},
		get showingRange() {
			return showingRange;
		},
		get hasPreviousPage() {
			return hasPreviousPage;
		},
		get hasNextPage() {
			return hasNextPage;
		},
		get sortKey() {
			return sortKey;
		},
		get sortDirection() {
			return sortDirection;
		},
		get filters() {
			return filters;
		},

		// Actions
		load,
		setPage,
		nextPage,
		previousPage,
		setSort,
		setFilter,
		clearFilter,
		clearAllFilters,
		refresh
	};
}
