<script lang="ts">
	import { onMount } from 'svelte';
	import { getWatchlist, searchWatchlists } from '$lib/api/watchlist/UserWatchlistController';
	import { getUserMonitor } from '$lib/api/monitor/UserMonitorController';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { createMonitorPageService } from '$lib/api/monitor/service/MonitorPageService.svelte';
	import { WatchlistSelector, MonitorCanvas } from '$lib/components/monitor';

	// Create monitor service with user-specific configuration
	const service = createMonitorPageService({
		getWatchlist: (watchlistId: number) => getWatchlist(watchlistId),
		searchWatchlists: (params: any) => searchWatchlists(params),
		getMonitor: (request: any) => getUserMonitor(request),
		buildEditRoute: (watchlistId: number) => `${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlistId}`,
		buildWatchlistsRoute: () => CLIENT_ROUTES.WATCHLISTS_PAGE.path,
		buildMonitorRoute: () => CLIENT_ROUTES.WATCHLISTS_MONITOR_PAGE.path
	});

	// Initial load
	onMount(() => {
		service.initializePage();
		return () => service.cleanup();
	});
</script>

<svelte:head>
	<title>{service.watchlist?.name ?? 'Monitor'} - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="flex items-center justify-between mb-4">
			<div>
				<h1 class="text-3xl font-bold text-primary">Monitor</h1>
				<p class="text-muted-foreground mt-1">
					Real-time monitoring with timeline visualization
				</p>
			</div>
		</div>

		<MonitorCanvas {service}>
			{#snippet selector()}
				{#if service.watchlist}
					<WatchlistSelector
						currentWatchlistId={service.watchlistId ?? 0}
						currentWatchlistName={service.watchlist.name ?? ''}
						onSelect={service.handleWatchlistChange}
					/>
				{:else if service.noWatchlistSelected}
					<WatchlistSelector
						currentWatchlistId={0}
						currentWatchlistName="Select a watchlist..."
						onSelect={service.handleWatchlistChange}
					/>
				{/if}
			{/snippet}
		</MonitorCanvas>
	</div>
</main>
