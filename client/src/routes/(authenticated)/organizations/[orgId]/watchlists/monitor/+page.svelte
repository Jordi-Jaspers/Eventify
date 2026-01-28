<script lang="ts">
	import { page } from '$app/state';
	import { goto, replaceState } from '$app/navigation';
	import { onMount, untrack } from 'svelte';
	import { getWatchlist, searchWatchlists } from '$lib/api/watchlist/OrganizationWatchlistController';
	import { getOrganizationMonitor } from '$lib/api/monitor/OrganizationMonitorController';
	import type { MonitorRequest, MonitorResponse, WatchlistDetailsResponse, SearchInput, TimeRange } from '$lib/api/models';
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { AlertCircle, Edit, Share2, LayoutList } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { Sessionstorage } from '$lib/utils/sessionstorage.svelte';
	import { 
		type MonitorFilters,
		type MonitorSession,
		createDefaultFilters,
		createDefaultSession,
		getMonitorSessionKey,
		parseMonitorQueryParams,
		buildMonitorShareUrl,
		createAutoRefresh
	} from '$lib/api/monitor/monitor.service';
	import {
		MonitorRow,
		MonitorGroup,
		MonitorEmptyState,
		OrganizationWatchlistSelector,
		ConfigurePopover,
		TimeAxisHeader,
		calculateTimeTicks,
		getCurrentSeverityFromTimeline
	} from '$lib/components/monitor';

	// Get orgId from route params
	const orgId: number = $derived(parseInt(page.params.orgId ?? '0', 10));

	// Session storage for persisting monitor state (org-specific key)
	// Use untrack to read orgId at initialization without creating reactive dependency
	const session = new Sessionstorage<MonitorSession>(
		getMonitorSessionKey(untrack(() => orgId)),
		createDefaultSession()
	);

	// Core state
	let watchlist = $state<WatchlistDetailsResponse | null>(null);
	let monitorData = $state<MonitorResponse | null>(null);
	let loading = $state(true);
	let loadingMonitor = $state(false);
	let noWatchlistSelected = $state(false);
	let lastUpdated: Date | null = $state(null);

	// Auto-refresh for live mode
	const autoRefresh = createAutoRefresh(() => loadMonitorData());

	// Derived state from session
	const watchlistId = $derived(session.value.watchlistId);
	const filters = $derived<MonitorFilters>({
		timeRange: session.value.timeRange,
		onlyCritical: session.value.onlyCritical,
		sortBySeverity: session.value.sortBySeverity,
		groupedView: session.value.groupedView,
		customStartTime: session.value.customStartTime,
		customEndTime: session.value.customEndTime
	});

	// Derived state from monitor data
	const rangeStart = $derived(monitorData ? new Date(monitorData.rangeStart) : null);
	const rangeEnd = $derived(monitorData ? new Date(monitorData.rangeEnd) : null);
	const isLive = $derived(monitorData?.live ?? false);
	const dashboardSeverity = $derived(
		monitorData?.dashboard?.timeline ? getCurrentSeverityFromTimeline(monitorData.dashboard.timeline) : null
	);
	const timeTicks = $derived(
		rangeStart && rangeEnd ? calculateTimeTicks(rangeStart, rangeEnd) : []
	);
	const hasChannels = $derived(
		(watchlist?.configuration?.channelIds?.length ?? 0) > 0 ||
		(watchlist?.configuration?.groups?.length ?? 0) > 0
	);
	const hasMonitorData = $derived(
		(monitorData?.dashboard?.channels?.length ?? 0) > 0 ||
		(monitorData?.dashboard?.groups?.length ?? 0) > 0
	);

	// ============ Filter Handlers ============

	function updateFilter<K extends keyof MonitorFilters>(key: K, value: MonitorFilters[K]): void {
		const updates: Partial<MonitorSession> = { [key]: value };
		
		// Clear custom times when switching away from custom
		if (key === 'timeRange' && value !== 'custom') {
			updates.customStartTime = '';
			updates.customEndTime = '';
		}
		
		session.update(updates);
		
		// For custom range, only reload when both times are set
		if (key === 'customStartTime' || key === 'customEndTime') {
			const currentSession = session.value;
			if (currentSession.customStartTime && currentSession.customEndTime) {
				loadMonitorData();
			}
		} else if (key !== 'timeRange' || value !== 'custom') {
			// For other filters or non-custom timeRange, reload immediately
			loadMonitorData();
		}
	}

	// ============ API Functions ============

	async function fetchFirstWatchlistId(): Promise<number | null> {
		try {
			const result = await searchWatchlists(orgId, {
				pageNumber: 0,
				pageSize: 1,
				sortOrder: [{ name: 'name', direction: 'ASC' }],
				searchInputs: []
			});
			return result.content?.[0]?.id ?? null;
		} catch (err) {
			console.error('Failed to fetch first watchlist:', err);
			return null;
		}
	}

	async function loadWatchlist(): Promise<void> {
		if (watchlistId === null) return;
		
		try {
			watchlist = await getWatchlist(orgId, watchlistId);
		} catch (err) {
			const { message } = handleError(err, 'Failed to load watchlist');
			toast.error(message);
			watchlist = null;
		}
	}

	async function loadMonitorData(): Promise<void> {
		if (watchlistId === null) return;
		
		loadingMonitor = true;
		try {
			const request: MonitorRequest = {
				watchlistId,
				filters: {
					timeRange: filters.timeRange,
					onlyCritical: filters.onlyCritical,
					sortBySeverity: filters.sortBySeverity,
					groupedView: filters.groupedView,
					...(filters.timeRange === 'custom' && filters.customStartTime && filters.customEndTime
						? { startTime: filters.customStartTime, endTime: filters.customEndTime }
						: {})
				}
			};

			monitorData = await getOrganizationMonitor(orgId, request);
			lastUpdated = new Date();

			// Setup auto-refresh if live mode
			autoRefresh.stop();
			if (monitorData.live && filters.timeRange !== 'custom') {
				autoRefresh.start();
			}
		} catch (err) {
			const { message } = handleError(err, 'Failed to load monitor data');
			toast.error(message);
		} finally {
			loadingMonitor = false;
		}
	}

	// ============ Initialization ============

	async function initializePage(): Promise<void> {
		loading = true;
		
		// Try query params first, then use session storage (already loaded)
		const queryParams = parseMonitorQueryParams(new URL(window.location.href));
		
		if (queryParams) {
			// Update session with query params
			session.value = {
				watchlistId: queryParams.watchlistId,
				...createDefaultFilters(),
				...queryParams.filters
			};
			replaceState(CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(orgId).path, {});
		}

		// If still no watchlist, try to fetch the first one
		if (session.value.watchlistId === null) {
			const firstId = await fetchFirstWatchlistId();
			if (firstId) {
				session.update({ watchlistId: firstId });
			}
		}

		if (session.value.watchlistId === null) {
			noWatchlistSelected = true;
			loading = false;
			return;
		}

		noWatchlistSelected = false;
		await loadWatchlist();
		await loadMonitorData();
		loading = false;
	}

	// ============ Event Handlers ============

	async function handleShare(): Promise<void> {
		if (watchlistId === null) return;
		
		const url = buildMonitorShareUrl(CLIENT_ROUTES.ORGANIZATION_MONITOR_PAGE(orgId).path, watchlistId, filters);
		try {
			await navigator.clipboard.writeText(url);
			toast.success('Link copied to clipboard');
		} catch {
			toast.error('Failed to copy link');
		}
	}

	function handleEdit(): void {
		if (watchlistId !== null) {
			goto(`${CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path}/${watchlistId}`);
		}
	}

	async function handleWatchlistChange(newWatchlistId: number): Promise<void> {
		session.value = { ...createDefaultSession(), watchlistId: newWatchlistId };
		
		loading = true;
		noWatchlistSelected = false;
		await loadWatchlist();
		await loadMonitorData();
		loading = false;
	}

	// Initial load
	onMount(() => {
		initializePage();
		return () => autoRefresh.stop();
	});
</script>

<svelte:head>
	<title>{watchlist?.name ?? 'Monitor'} - Eventify</title>
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

		<!-- Watchlist Selector Row with Buttons -->
		<div class="flex items-center justify-between">
			<div class="flex items-center gap-4">
				{#if watchlist}
					<OrganizationWatchlistSelector
						{orgId}
						currentWatchlistId={watchlistId ?? 0}
						currentWatchlistName={watchlist.name ?? ''}
						onSelect={handleWatchlistChange}
					/>
				{:else if loading}
					<div class="h-9 w-[300px] bg-muted/20 rounded animate-pulse"></div>
				{:else if noWatchlistSelected}
					<OrganizationWatchlistSelector
						{orgId}
						currentWatchlistId={0}
						currentWatchlistName="Select a watchlist..."
						onSelect={handleWatchlistChange}
					/>
				{/if}
			</div>
			<div class="flex items-center gap-2">
				{#if watchlist}
					<Button variant="outline" size="icon" onclick={handleShare} title="Share">
						<Share2 class="h-4 w-4" />
					</Button>
					<Button variant="outline" onclick={handleEdit}>
						<Edit class="mr-2 h-4 w-4" />
						Edit Watchlist
					</Button>
					<ConfigurePopover
						timeRange={filters.timeRange}
						onlyCritical={filters.onlyCritical}
						sortBySeverity={filters.sortBySeverity}
						groupedView={filters.groupedView}
						customStartTime={filters.customStartTime}
						customEndTime={filters.customEndTime}
						{isLive}
						onTimeRangeChange={(v) => updateFilter('timeRange', v)}
						onToggleOnlyCritical={() => updateFilter('onlyCritical', !filters.onlyCritical)}
						onToggleSortBySeverity={() => updateFilter('sortBySeverity', !filters.sortBySeverity)}
						onToggleGroupedView={() => updateFilter('groupedView', !filters.groupedView)}
						onCustomStartTimeChange={(v) => updateFilter('customStartTime', v)}
						onCustomEndTimeChange={(v) => updateFilter('customEndTime', v)}
					/>
				{/if}
			</div>
		</div>

		<!-- Loading State -->
		{#if loading}
			<div class="flex items-center justify-center py-12">
				<div class="text-muted-foreground">Loading...</div>
			</div>
		{:else if noWatchlistSelected}
			<MonitorEmptyState
				icon={LayoutList}
				title="No Watchlist Selected"
				description="Select a watchlist from the dropdown above to start monitoring your channels."
				actionLabel="View All Watchlists"
				onAction={() => goto(CLIENT_ROUTES.ORGANIZATION_WATCHLISTS_PAGE(orgId).path)}
			/>
		{:else if !watchlist}
			<MonitorEmptyState
				icon={AlertCircle}
				iconVariant="destructive"
				title="Watchlist Not Found"
				description="The watchlist you're looking for doesn't exist or you don't have access to it."
			/>
		{:else if !hasChannels}
			<MonitorEmptyState
				icon={AlertCircle}
				title="No Channels Configured"
				description="This watchlist has no channels to monitor. Add channels to start tracking their status over time."
				actionLabel="Configure Watchlist"
				onAction={handleEdit}
			/>
		{:else if monitorData && rangeStart && rangeEnd}
			<!-- Main Monitor Card -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg overflow-hidden pt-2">
				<CardContent class="p-0">
					<TimeAxisHeader {rangeStart} {rangeEnd} {timeTicks} {isLive} {lastUpdated} />

					<div class="divide-y divide-border/30">
						<!-- Dashboard Timeline Row -->
						<MonitorRow
							type="dashboard"
							name={monitorData.watchlistName}
							timeline={monitorData.dashboard.timeline}
							currentSeverity={dashboardSeverity}
							status={null}
							{rangeStart}
							{rangeEnd}
						/>

						<!-- Grouped View -->
						{#if filters.groupedView && monitorData.dashboard.groups?.length}
							{#each monitorData.dashboard.groups as group, idx (`group-${idx}-${group.id}`)}
								{#if group.timeline && group.channels}
									<MonitorGroup
										name={group.name ?? 'Unnamed Group'}
										timeline={group.timeline}
										channels={group.channels}
										{rangeStart}
										{rangeEnd}
									/>
								{/if}
							{/each}
						{/if}

						<!-- Standalone Channels or Flat View -->
						{#if monitorData.dashboard.channels?.length}
							{#each monitorData.dashboard.channels as channel, idx (`channel-${idx}-${channel.channelId}`)}
								{#if channel.timeline}
									<MonitorRow
										type="channel"
										name={channel.channelName ?? 'Unnamed Channel'}
										timeline={channel.timeline}
										currentSeverity={channel.currentSeverity ?? null}
										status={channel.status ?? null}
										{rangeStart}
										{rangeEnd}
									/>
								{/if}
							{/each}
						{/if}

						<!-- No Data State -->
						{#if !hasMonitorData}
							<div class="p-8 text-center text-muted-foreground">
								No data available for the selected filters
							</div>
						{/if}
					</div>
				</CardContent>
			</Card>
		{/if}
	</div>
</main>
