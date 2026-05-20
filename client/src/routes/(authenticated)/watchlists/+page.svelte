<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchWatchlists, deleteWatchlist } from '$lib/api/watchlist/UserWatchlistController';
	import type { WatchlistDetailsResponse } from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { ClipboardList, Plus } from '@lucide/svelte';
	import { PageHeader } from '$lib/components/ui/page-header';
	import { toast } from 'svelte-sonner';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { WatchlistTableRow } from '$lib/components/watchlist';
	import ConfirmDialog from '$lib/components/ui/confirm-dialog/confirm-dialog.svelte';

	let confirmOpen: boolean = $state(false);
	let watchlistToDelete: WatchlistDetailsResponse | null = $state(null);

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

	// DataTable service
	const dataTableService = createDataTableService<WatchlistDetailsResponse>({
		fetchFn: searchWatchlists,
		pageSize: 10,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});

	function handleEditWatchlist(watchlist: WatchlistDetailsResponse): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlist.id}`);
	}

	function handleMonitorWatchlist(watchlist: WatchlistDetailsResponse): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_MONITOR_PAGE.path}?id=${watchlist.id}`);
	}

	function handleNewWatchlist(): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/new`);
	}

	function handleDeleteWatchlist(watchlist: WatchlistDetailsResponse): void {
		watchlistToDelete = watchlist;
		confirmOpen = true;
	}

	async function confirmDelete(): Promise<void> {
		if (!watchlistToDelete) return;
		try {
			await deleteWatchlist(watchlistToDelete.id ?? 0);
			toast.success('Watchlist deleted');
			dataTableService.load();
		} catch {
			toast.error('Failed to delete watchlist');
		} finally {
			confirmOpen = false;
			watchlistToDelete = null;
		}
	}

	onMount(() => dataTableService.load());
</script>

<svelte:head>
	<title>Watchlists - Eventify</title>
</svelte:head>

<!-- Main Content -->
<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<PageHeader title="Watchlists" description="Monitor channels and track important events">
			{#snippet actions()}
				<Button onclick={handleNewWatchlist}>
					<Plus class="mr-2 h-4 w-4" />
					New Watchlist
				</Button>
			{/snippet}
		</PageHeader>

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="All Watchlists" icon={ClipboardList}>
		{#snippet row(watchlist: WatchlistDetailsResponse)}
			<WatchlistTableRow {watchlist} onMonitor={handleMonitorWatchlist} onEdit={handleEditWatchlist} onDelete={handleDeleteWatchlist} />
		{/snippet}
		</DataTable>
	</div>
</main>

<ConfirmDialog
	open={confirmOpen}
	title="Delete Watchlist"
	confirmLabel="Delete"
	destructive={true}
	onOpenChange={(o) => (confirmOpen = o)}
	onConfirm={confirmDelete}
>
	{#snippet description()}
		Are you sure you want to delete "{watchlistToDelete?.name}"? This action cannot be undone.
	{/snippet}
</ConfirmDialog>
