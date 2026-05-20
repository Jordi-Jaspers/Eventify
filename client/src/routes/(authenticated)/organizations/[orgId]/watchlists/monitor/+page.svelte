<script lang="ts">
	import { page } from '$app/state';
	import { onMount, untrack } from 'svelte';
	import { getWatchlist, searchWatchlists } from '$lib/api/watchlist/OrganizationWatchlistController';
	import { getOrganizationMonitor } from '$lib/api/monitor/OrganizationMonitorController';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { createMonitorPageService } from '$lib/api/monitor/service/MonitorPageService.svelte';
	import { OrganizationWatchlistSelector, MonitorCanvas } from '$lib/components/monitor';

	// Get orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0', 10));

	// Create monitor service with org-specific configuration
	// Use untrack to read orgId at initialization without creating reactive dependency
	const service = createMonitorPageService({
		orgId: untrack(() => orgId),
		getWatchlist: (watchlistId: number) => getWatchlist(untrack(() => orgId), watchlistId),
		searchWatchlists: (params: any) => searchWatchlists(untrack(() => orgId), params),
		getMonitor: (request: any) => getOrganizationMonitor(untrack(() => orgId), request),
		buildEditRoute: (watchlistId: number) => `${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(untrack(() => orgId)).path}/${watchlistId}`,
		buildWatchlistsRoute: () => CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(untrack(() => orgId)).path,
		buildMonitorRoute: () => CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(untrack(() => orgId)).path
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
		<div class="flex items-center justify-between mb-8">
			<div>
				<h1 class="text-3xl font-bold text-primary">Monitor</h1>
				<p class="text-muted-foreground mt-2">
					Real-time monitoring with timeline visualization
				</p>
			</div>
		</div>

		<MonitorCanvas {service} {orgId}>
			{#snippet selector()}
				{#if service.watchlist}
					<OrganizationWatchlistSelector
						{orgId}
						currentWatchlistId={service.watchlistId ?? 0}
						currentWatchlistName={service.watchlist.name ?? ''}
						onSelect={service.handleWatchlistChange}
					/>
				{:else if service.noWatchlistSelected}
					<OrganizationWatchlistSelector
						{orgId}
						currentWatchlistId={0}
						currentWatchlistName="Select a watchlist..."
						onSelect={service.handleWatchlistChange}
					/>
				{/if}
			{/snippet}
		</MonitorCanvas>
	</div>
</main>
