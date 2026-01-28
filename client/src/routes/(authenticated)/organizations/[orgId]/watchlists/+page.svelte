<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchWatchlists, deleteWatchlist } from '$lib/api/watchlist/OrganizationWatchlistController';
	import type { WatchlistDetailsResponse, SortablePageInput, PageResourceWatchlistDetailsResponse } from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { ClipboardList, Edit, Trash2, Plus, Eye } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { truncateText } from '$lib/utils/string';
	import { toast } from 'svelte-sonner';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { organizationStore } from '$lib/stores/organization.svelte';

	// Get orgId from URL params
	const orgId: number = $derived(Number($page.params.orgId));

	// Role-based visibility: OWNER/ADMIN can create/edit/delete
	const canManage: boolean = $derived(
		organizationStore.currentRole === 'OWNER' || organizationStore.currentRole === 'ADMIN'
	);

	// Columns configuration
	const columns: DataTableColumn<WatchlistDetailsResponse>[] = [
		{
			key: 'search',
			label: 'Watchlist',
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search watchlists...',
			colSpan: 3
		},
		{
			key: 'description',
			label: 'Description',
			colSpan: 6
		},
		{
			key: 'createdAt',
			label: 'Created',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'actions',
			colSpan: 1
		}
	];

	// Wrap API call to include orgId
	async function fetchWatchlists(input: SortablePageInput): Promise<PageResourceWatchlistDetailsResponse> {
		return searchWatchlists(orgId, input);
	}

	// DataTable service
	const dataTableService = createDataTableService<WatchlistDetailsResponse>({
		fetchFn: fetchWatchlists,
		pageSize: 10,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});

	function handleEditWatchlist(watchlist: WatchlistDetailsResponse): void {
		goto(`${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path}/${watchlist.id}`);
	}

	function handleMonitorWatchlist(watchlist: WatchlistDetailsResponse): void {
		goto(`${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path}/monitor?id=${watchlist.id}`);
	}

	function handleNewWatchlist(): void {
		goto(`${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path}/new`);
	}

	async function handleDeleteWatchlist(watchlist: WatchlistDetailsResponse): Promise<void> {
		if (!confirm(`Are you sure you want to delete "${watchlist.name}"? This action cannot be undone.`)) {
			return;
		}

		try {
			await deleteWatchlist(orgId, watchlist.id ?? 0);
			toast.success('Watchlist deleted');
			dataTableService.load();
		} catch (error) {
			toast.error('Failed to delete watchlist');
		}
	}

	onMount(() => dataTableService.load());
</script>

<svelte:head>
	<title>Organization Watchlists - Eventify</title>
</svelte:head>

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center justify-between mb-8">
			<div>
				<h1 class="text-3xl font-bold text-primary">
					Organization Watchlists
				</h1>
				<p class="text-muted-foreground mt-2">
					Monitor channels and track important events for your organization
				</p>
			</div>
			{#if canManage}
				<Button onclick={handleNewWatchlist}>
					<Plus class="mr-2 h-4 w-4" />
					New Watchlist
				</Button>
			{/if}
		</div>

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="All Watchlists" icon={ClipboardList}>
			{#snippet row(watchlist: WatchlistDetailsResponse)}
				<div
					class="grid grid-cols-1 md:grid-cols-12 items-center gap-4 px-4 py-3 hover:bg-muted/30 transition-all text-left w-full"
				>
					<!-- Watchlist Name -->
					<div class="col-span-1 md:col-span-3">
						<div class="flex items-center gap-3">
							<ClipboardList class="h-5 w-5 text-primary shrink-0" />
							<div class="min-w-0">
								<div class="font-medium truncate">{watchlist.name}</div>
								<div class="text-sm text-muted-foreground truncate md:hidden">
									{truncateText(watchlist.description, 40, 'No description')}
								</div>
							</div>
						</div>
					</div>

					<!-- Description (desktop only) -->
					<div class="hidden md:flex md:col-span-6 items-center">
						<span class="text-sm text-muted-foreground truncate">
							{truncateText(watchlist.description, 120, 'No description')}
						</span>
					</div>

					<!-- Created -->
					<div class="col-span-1 md:col-span-2 flex items-center">
						<span class="text-sm text-muted-foreground whitespace-nowrap">
							<span class="md:hidden">Created: </span>
							{formatDate(watchlist.createdAt ?? '')}
						</span>
					</div>

					<!-- Actions -->
					<div class="col-span-1 md:col-span-1 flex items-center justify-end gap-1">
						<Button
							variant="ghost"
							size="icon"
							class="h-8 w-8 text-muted-foreground hover:text-primary"
							onclick={() => handleMonitorWatchlist(watchlist)}
							aria-label="Monitor watchlist"
						>
							<Eye class="h-4 w-4" />
						</Button>
						{#if canManage}
							<Button
								variant="ghost"
								size="icon"
								class="h-8 w-8 text-muted-foreground hover:text-primary"
								onclick={() => handleEditWatchlist(watchlist)}
								aria-label="Edit watchlist"
							>
								<Edit class="h-4 w-4" />
							</Button>
							<Button
								variant="ghost"
								size="icon"
								class="h-8 w-8 text-muted-foreground hover:text-destructive"
								onclick={() => handleDeleteWatchlist(watchlist)}
								aria-label="Delete watchlist"
							>
								<Trash2 class="h-4 w-4" />
							</Button>
						{/if}
					</div>
				</div>
			{/snippet}
		</DataTable>
	</div>
</main>
