<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { getWatchlist, searchWatchlists } from '$lib/api/watchlist/UserWatchlistController';
	import { getUserMonitor } from '$lib/api/monitor/UserMonitorController';
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { AlertCircle, Edit, Share2, LayoutList, LoaderCircle, SearchX } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { createMonitorPageService } from '$lib/api/monitor/service/MonitorPageService.svelte';
	import type { TimelineDuration, Severity } from '$lib/api/models';
	import {
		MonitorRow,
		MonitorGroup,
		MonitorEmptyState,
		WatchlistSelector,
		ConfigurePopover,
		TimeAxisHeader,
		DurationDetailsModal,
		ZoomBreadcrumb,
		calculateTimeTicks,
		getCurrentSeverityFromTimeline,
		formatZoomRangeLabel
	} from '$lib/components/monitor';

	// Create monitor service with user-specific configuration
	const service = createMonitorPageService({
		getWatchlist: (watchlistId: number) => getWatchlist(watchlistId),
		searchWatchlists: (params: any) => searchWatchlists(params),
		getMonitor: (request: any) => getUserMonitor(request),
		buildEditRoute: (watchlistId: number) => `${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlistId}`,
		buildWatchlistsRoute: () => CLIENT_ROUTES.WATCHLISTS_PAGE.path,
		buildMonitorRoute: () => CLIENT_ROUTES.WATCHLISTS_MONITOR_PAGE.path
	});

	// Derived state from monitor data
	const dashboardSeverity = $derived(
		service.monitorData?.dashboard?.timeline ? getCurrentSeverityFromTimeline(service.monitorData.dashboard.timeline) : null
	);
	const timeTicks = $derived(
		service.rangeStart && service.rangeEnd ? calculateTimeTicks(service.rangeStart, service.rangeEnd) : []
	);

	// Current zoom label for breadcrumb display
	const currentZoomLabel: string = $derived(
		service.rangeStart && service.rangeEnd
			? formatZoomRangeLabel(service.rangeStart, service.rangeEnd)
			: 'Current view'
	);

	// Segment click handler — zoom in if aggregated, otherwise open modal
	function handleSegmentClick(
		channelId: number,
		channelName: string,
		severity: Severity | null,
		duration: TimelineDuration,
		timeline: TimelineDuration[]
	): void {
		if (service.canZoomIn) {
			service.zoomIn(duration);
		} else {
			service.openDetailsModal(channelId, channelName, severity, duration, timeline);
		}
	}

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

		<!-- Watchlist Selector Row with Buttons -->
		<div class="flex items-center justify-between">
			<div class="flex items-center gap-4">
				{#if service.watchlist}
					<WatchlistSelector
						currentWatchlistId={service.watchlistId ?? 0}
						currentWatchlistName={service.watchlist.name ?? ''}
						onSelect={service.handleWatchlistChange}
					/>
				{:else if service.loading}
					<div class="h-9 w-[300px] bg-muted/20 rounded animate-pulse"></div>
				{:else if service.noWatchlistSelected}
					<WatchlistSelector
						currentWatchlistId={0}
						currentWatchlistName="Select a watchlist..."
						onSelect={service.handleWatchlistChange}
					/>
				{/if}
			</div>
			<div class="flex items-center gap-2">
				{#if service.watchlist}
					<Button variant="outline" size="icon" onclick={service.handleShare} title="Share">
						<Share2 class="h-4 w-4" />
					</Button>
					<Button variant="outline" onclick={() => goto(service.handleEdit())}>
						<Edit class="mr-2 h-4 w-4" />
						Edit Watchlist
					</Button>
					<ConfigurePopover
						timeRange={service.filters.timeRange}
						onlyCritical={service.filters.onlyCritical}
						sortBySeverity={service.filters.sortBySeverity}
						groupedView={service.filters.groupedView}
						customStartTime={service.filters.customStartTime}
						customEndTime={service.filters.customEndTime}
						isLive={service.isLive}
						showResetButton={!service.filtersMatchDefaults}
						showModifiedIndicator={!service.filtersMatchDefaults}
						onResetToDefaults={service.resetToDefaults}
						onTimeRangeChange={(v) => service.updateFilter('timeRange', v)}
						onToggleOnlyCritical={() => service.updateFilter('onlyCritical', !service.filters.onlyCritical)}
						onToggleSortBySeverity={() => service.updateFilter('sortBySeverity', !service.filters.sortBySeverity)}
						onToggleGroupedView={() => service.updateFilter('groupedView', !service.filters.groupedView)}
						onCustomStartTimeChange={(v) => service.updateFilter('customStartTime', v)}
						onCustomEndTimeChange={(v) => service.updateFilter('customEndTime', v)}
					/>
				{/if}
			</div>
		</div>

		<!-- Zoom Breadcrumb -->
		{#if service.currentZoomLevel > 0}
			<ZoomBreadcrumb
				breadcrumbs={service.zoomBreadcrumbs}
				currentLabel={currentZoomLabel}
				onNavigate={(level) => service.zoomOut(level)}
			/>
		{/if}

		<!-- Loading State -->
		{#if service.loading}
			<div class="flex items-center justify-center py-12">
				<LoaderCircle class="h-6 w-6 text-muted-foreground animate-spin mr-2" />
				<div class="text-muted-foreground">Loading...</div>
			</div>
		{:else if service.noWatchlistSelected}
			<MonitorEmptyState
				icon={LayoutList}
				title="No Watchlist Selected"
				description="Select a watchlist from the dropdown above to start monitoring your channels."
				actionLabel="View All Watchlists"
				onAction={() => goto(service.buildWatchlistsRoute())}
			/>
		{:else if !service.watchlist}
			<MonitorEmptyState
				icon={AlertCircle}
				iconVariant="destructive"
				title="Watchlist Not Found"
				description="The watchlist you're looking for doesn't exist or you don't have access to it."
			/>
		{:else if !service.hasChannels}
			<MonitorEmptyState
				icon={AlertCircle}
				title="No Channels Configured"
				description="This watchlist has no channels to monitor. Add channels to start tracking their status over time."
				actionLabel="Configure Watchlist"
				onAction={() => goto(service.handleEdit())}
			/>
		{:else if service.monitorData && service.rangeStart && service.rangeEnd}
			<!-- Main Monitor Card -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg overflow-hidden pt-2">
				<CardContent class="p-0">
					<TimeAxisHeader 
						rangeStart={service.rangeStart} 
						rangeEnd={service.rangeEnd} 
						{timeTicks} 
						isLive={service.isLive} 
						lastUpdated={service.lastUpdated}
						bucketSizeLabel={service.bucketSizeLabel}
					/>

					<div class="divide-y divide-border/30">
						<!-- Dashboard Timeline Row -->
						<MonitorRow
							type="dashboard"
							name={service.monitorData.watchlistName}
							timeline={service.monitorData.dashboard.timeline}
							currentSeverity={dashboardSeverity}
							status={null}
							rangeStart={service.rangeStart}
							rangeEnd={service.rangeEnd}
							isAggregated={service.isAggregated}
						/>

						<!-- Grouped View -->
						{#if service.filters.groupedView && service.monitorData.dashboard.groups?.length}
							{#each service.monitorData.dashboard.groups as group, idx (`group-${idx}-${group.id}`)}
								{#if group.timeline && group.channels}
									<MonitorGroup
										name={group.name ?? 'Unnamed Group'}
										timeline={group.timeline}
										channels={group.channels}
										rangeStart={service.rangeStart}
										rangeEnd={service.rangeEnd}
										isAggregated={service.isAggregated}
										onSegmentClick={(channelId, name, severity, d, timeline) =>
											handleSegmentClick(channelId, name, severity, d, timeline)}
									/>
								{/if}
							{/each}
						{/if}

						<!-- Standalone Channels or Flat View -->
						{#if service.monitorData.dashboard.channels?.length}
							{#each service.monitorData.dashboard.channels as channel, idx (`channel-${idx}-${channel.channelId}`)}
								{#if channel.timeline}
									<MonitorRow
										type="channel"
										name={channel.channelName ?? 'Unnamed Channel'}
										timeline={channel.timeline}
										currentSeverity={channel.currentSeverity ?? null}
										status={channel.status ?? null}
										rangeStart={service.rangeStart}
										rangeEnd={service.rangeEnd}
										isAggregated={service.isAggregated}
										onSegmentClick={(d) => handleSegmentClick(channel.channelId!, channel.channelName!, channel.currentSeverity ?? null, d, channel.timeline!.durations!)}
									/>
								{/if}
							{/each}
						{/if}

						<!-- No Data State -->
						{#if !service.hasMonitorData}
							<div class="p-8 text-center flex flex-col items-center gap-2 text-muted-foreground">
								<SearchX class="h-8 w-8 opacity-40" />
								<span class="text-sm">No data available for the selected filters</span>
							</div>
						{/if}
					</div>
				</CardContent>
			</Card>
		{/if}
	</div>

	<!-- Details Modal -->
	<DurationDetailsModal
		bind:open={service.modalOpen}
		onOpenChange={(v) => service.modalOpen = v}
		channelName={service.selectedChannelName}
		currentSeverity={service.selectedSeverity}
		bind:selectedDuration={service.selectedDuration}
		channelId={service.selectedChannelId}
	/>
</main>
