<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchWatchlists, deleteWatchlist } from '$lib/api/watchlist/UserWatchlistController';
	import type { WatchlistDetailsResponse } from '$lib/api/models';
	import Button from '$lib/components/ui/button/button.svelte';
	import { ClipboardList, Edit, Trash2, Plus } from '@lucide/svelte';
	import { formatDate } from '$lib/utils/date';
	import { toast } from 'svelte-sonner';
	import { CLIENT_ROUTES } from '$lib/config/routes';

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

	// Helper functions
	function truncateDescription(description: string | undefined | null, maxLength: number = 80): string {
		if (!description) return 'No description';
		if (description.length <= maxLength) return description;
		return `${description.substring(0, maxLength)}...`;
	}

	function handleEditWatchlist(watchlist: WatchlistDetailsResponse): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlist.id}`);
	}

	function handleNewWatchlist(): void {
		goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/new`);
	}

	async function handleDeleteWatchlist(watchlist: WatchlistDetailsResponse): Promise<void> {
		if (!confirm(`Are you sure you want to delete "${watchlist.name}"? This action cannot be undone.`)) {
			return;
		}

		try {
			await deleteWatchlist(watchlist.id ?? 0);
			toast.success('Watchlist deleted');
			dataTableService.load();
		} catch (error) {
			toast.error('Failed to delete watchlist');
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
		<div class="flex items-center justify-between mb-8">
			<div>
				<h1 class="text-3xl font-bold bg-gradient-to-r from-primary via-accent to-primary bg-clip-text text-transparent">
					Watchlists
				</h1>
				<p class="text-muted-foreground mt-2">
					Monitor channels and track important events
				</p>
			</div>
			<Button
				onclick={handleNewWatchlist}
				class="bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all shadow-lg shadow-primary/20"
			>
				<Plus class="mr-2 h-4 w-4" />
				New Watchlist
			</Button>
		</div>

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="All Watchlists" icon={ClipboardList}>
			{#snippet row(watchlist: WatchlistDetailsResponse)}
				<div
					class="grid grid-cols-1 md:grid-cols-12 items-center gap-4 px-4 py-3 rounded-lg border border-border/50 bg-card/50 backdrop-blur-sm hover:bg-card/70 hover:border-border transition-all text-left w-full"
				>
					<!-- Watchlist Name -->
					<div class="col-span-1 md:col-span-3">
						<div class="flex items-center gap-3">
							<div class="h-10 w-10 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center flex-shrink-0">
								<ClipboardList class="h-5 w-5 text-primary" />
							</div>
							<div class="min-w-0">
								<div class="font-medium truncate">{watchlist.name}</div>
								<div class="text-sm text-muted-foreground truncate md:hidden">
									{truncateDescription(watchlist.description, 40)}
								</div>
							</div>
						</div>
					</div>

					<!-- Description (desktop only) -->
					<div class="hidden md:flex md:col-span-6 items-center">
						<span class="text-sm text-muted-foreground truncate">
							{truncateDescription(watchlist.description, 120)}
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
							class="h-8 w-8 hover:bg-primary/10 hover:text-primary"
							onclick={() => handleEditWatchlist(watchlist)}
							aria-label="Edit watchlist"
						>
							<Edit class="h-4 w-4" />
						</Button>
						<Button
							variant="ghost"
							size="icon"
							class="h-8 w-8 hover:bg-destructive/10 hover:text-destructive"
							onclick={() => handleDeleteWatchlist(watchlist)}
							aria-label="Delete watchlist"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>
				</div>
			{/snippet}
		</DataTable>
	</div>
</main>
