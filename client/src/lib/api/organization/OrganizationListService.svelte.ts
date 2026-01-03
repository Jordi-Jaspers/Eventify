import { toast } from 'svelte-sonner';
import { searchOrganizations } from './OrganizationController';
import { handleError } from '$lib/utils/error-handler';
import type { OrganizationResponse, OrganizationStatus, SortDirection } from '$lib/api/models';

/**
 * Service for managing organization search and listing.
 * Encapsulates search, filtering, pagination, sorting, and state management.
 */
export function createOrganizationListService(pageSize: number = 10) {
	// Core state
	let organizations: OrganizationResponse[] = $state([]);
	let loading: boolean = $state(true);
	let error: string | null = $state(null);

	// Search & filter state
	let searchQuery: string = $state('');
	let selectedStatus: OrganizationStatus | undefined = $state(undefined);

	// Sort state
	let sortKey: string | null = $state(null);
	let sortDirection: SortDirection = $state('ASC');

	// Pagination state
	let currentPage: number = $state(0);
	let totalPages: number = $state(0);
	let totalElements: number = $state(0);

	// Debounce timer
	let debounceTimer: ReturnType<typeof setTimeout> | null = null;

	async function load(): Promise<void> {
		loading = true;
		error = null;

		try {
			const response = await searchOrganizations({
				page: currentPage,
				size: pageSize,
				search: searchQuery || undefined,
				status: selectedStatus,
				sortKey: sortKey || undefined,
				sortDirection: sortDirection
			});

			organizations = response.content ?? [];
			totalPages = response.totalPages ?? 0;
			totalElements = response.totalElements ?? 0;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load organizations');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	function setSearchQuery(query: string): void {
		searchQuery = query;

		if (debounceTimer) clearTimeout(debounceTimer);
		debounceTimer = setTimeout(() => {
			currentPage = 0;
			load();
		}, 300);
	}

	function setStatusFilter(status: OrganizationStatus | undefined): void {
		selectedStatus = status;
		currentPage = 0;
		load();
	}

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

	function previousPage(): void {
		if (currentPage > 0) {
			currentPage--;
			load();
		}
	}

	function nextPage(): void {
		if (currentPage < totalPages - 1) {
			currentPage++;
			load();
		}
	}

	function getShowingRange(): string {
		if (totalElements === 0) return 'Showing 0 organizations';

		const start: number = currentPage * pageSize + 1;
		const end: number = Math.min((currentPage + 1) * pageSize, totalElements);
		return `Showing ${start}-${end} of ${totalElements} organizations`;
	}

	function getStatusBadgeVariant(
		status: OrganizationStatus | undefined
	): 'default' | 'success' | 'destructive' {
		switch (status) {
			case 'ACTIVE':
				return 'success';
			case 'SUSPENDED':
				return 'destructive';
			case 'TRIAL':
			default:
				return 'default';
		}
	}

	return {
		// Getters (reactive)
		get organizations() { return organizations; },
		get loading() { return loading; },
		get error() { return error; },
		get searchQuery() { return searchQuery; },
		get selectedStatus() { return selectedStatus; },
		get sortKey() { return sortKey; },
		get sortDirection() { return sortDirection; },
		get currentPage() { return currentPage; },
		get totalPages() { return totalPages; },
		get totalElements() { return totalElements; },

		// Computed
		get showingRange() { return getShowingRange(); },
		get hasPreviousPage() { return currentPage > 0; },
		get hasNextPage() { return currentPage < totalPages - 1; },

		// Actions
		load,
		setSearchQuery,
		setStatusFilter,
		setSort,
		previousPage,
		nextPage,
		getStatusBadgeVariant
	};
}

export type OrganizationListService = ReturnType<typeof createOrganizationListService>;
