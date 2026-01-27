<script lang="ts">
	import { page } from '$app/state';
	import { goto, replaceState } from '$app/navigation';
	import { onMount } from 'svelte';
	import { getWatchlist, searchWatchlists } from '$lib/api/watchlist/UserWatchlistController';
	import { getUserMonitor } from '$lib/api/monitor/UserMonitorController';
	import type { MonitorRequest, MonitorResponse, WatchlistDetailsResponse, SearchInput } from '$lib/api/models';
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';
	import { AlertCircle, Edit, Share2, LayoutList } from '@lucide/svelte';
	import { toast } from 'svelte-sonner';
	import { handleError } from '$lib/utils/error-handler';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import MonitorRow from '$lib/components/monitor/MonitorRow.svelte';
	import MonitorGroup from '$lib/components/monitor/MonitorGroup.svelte';
	import WatchlistSelector from '$lib/components/monitor/WatchlistSelector.svelte';
	import ConfigurePopover from '$lib/components/monitor/ConfigurePopover.svelte';
	import {
		calculateTimeTicks,
		getCurrentSeverityFromTimeline
	} from '$lib/components/monitor/types';

	type TimeRange = '2h' | '4h' | '12h' | '24h' | '7d' | '30d' | 'custom';

	// Session storage key for monitor state
	const MONITOR_SESSION_KEY = 'monitor_session';

	interface MonitorSession {
		watchlistId: number;
		timeRange: TimeRange;
		onlyCritical: boolean;
		sortBySeverity: boolean;
		groupedView: boolean;
		startTime?: string; // ISO string for custom range
		endTime?: string; // ISO string for custom range
	}

	// State
	let watchlistId = $state<number | null>(null);
	let watchlist = $state<WatchlistDetailsResponse | null>(null);
	let monitorData = $state<MonitorResponse | null>(null);
	let loading = $state(true);
	let loadingMonitor = $state(false);
	let noWatchlistSelected = $state(false);

	// Filter state
	let timeRange: TimeRange = $state('24h');
	let onlyCritical: boolean = $state(false);
	let sortBySeverity: boolean = $state(false);
	let groupedView: boolean = $state(false);
	let customStartTime: string = $state(''); // ISO string for custom range
	let customEndTime: string = $state(''); // ISO string for custom range

	// Auto-refresh for live mode
	let refreshInterval: ReturnType<typeof setInterval> | null = null;
	let lastUpdated: Date | null = $state(null);

	// Derived state
	const rangeStart: Date | null = $derived(
		monitorData ? new Date(monitorData.rangeStart) : null
	);
	const rangeEnd: Date | null = $derived(monitorData ? new Date(monitorData.rangeEnd) : null);
	const isLive: boolean = $derived(monitorData?.live ?? false);
	const dashboardSeverity = $derived(
		monitorData?.dashboard?.timeline ? getCurrentSeverityFromTimeline(monitorData.dashboard.timeline) : null
	);
	const timeTicks = $derived(
		rangeStart && rangeEnd ? calculateTimeTicks(rangeStart, rangeEnd) : []
	);

	function saveSession(): void {
		if (watchlistId === null) return;
		const session: MonitorSession = {
			watchlistId,
			timeRange,
			onlyCritical,
			sortBySeverity,
			groupedView,
			startTime: customStartTime || undefined,
			endTime: customEndTime || undefined
		};
		try {
			sessionStorage.setItem(MONITOR_SESSION_KEY, JSON.stringify(session));
		} catch (err) {
			console.error('Failed to save monitor session:', err);
		}
	}

	function loadSession(): MonitorSession | null {
		try {
			const stored = sessionStorage.getItem(MONITOR_SESSION_KEY);
			if (stored) {
				return JSON.parse(stored) as MonitorSession;
			}
		} catch (err) {
			console.error('Failed to load monitor session:', err);
		}
		return null;
	}

	function parseQueryParams(): Partial<MonitorSession> | null {
		const url = new URL(window.location.href);
		const id = url.searchParams.get('id');
		
		if (!id) return null;

		const params: Partial<MonitorSession> = {
			watchlistId: parseInt(id, 10)
		};

		const tr = url.searchParams.get('timeRange');
		if (tr && ['2h', '4h', '12h', '24h', '7d', '30d', 'custom'].includes(tr)) {
			params.timeRange = tr as TimeRange;
		}

		const oc = url.searchParams.get('onlyCritical');
		if (oc !== null) {
			params.onlyCritical = oc === 'true';
		}

		const sbs = url.searchParams.get('sortBySeverity');
		if (sbs !== null) {
			params.sortBySeverity = sbs === 'true';
		}

		const gv = url.searchParams.get('groupedView');
		if (gv !== null) {
			params.groupedView = gv === 'true';
		}

		// Parse custom time range
		const st = url.searchParams.get('startTime');
		const et = url.searchParams.get('endTime');
		if (st && et) {
			params.startTime = st;
			params.endTime = et;
		}

		return params;
	}

	function clearQueryParams(): void {
		// Replace URL without query params
		replaceState(CLIENT_ROUTES.WATCHLISTS_MONITOR_PAGE.path, {});
	}

	function getShareUrl(): string {
		const url = new URL(window.location.origin + CLIENT_ROUTES.WATCHLISTS_MONITOR_PAGE.path);
		if (watchlistId !== null) {
			url.searchParams.set('id', watchlistId.toString());
		}
		// Always include all filters for shareable links
		url.searchParams.set('timeRange', timeRange);
		url.searchParams.set('onlyCritical', onlyCritical.toString());
		url.searchParams.set('sortBySeverity', sortBySeverity.toString());
		url.searchParams.set('groupedView', groupedView.toString());
		
		// Include custom time range dates if applicable
		if (timeRange === 'custom' && customStartTime && customEndTime) {
			url.searchParams.set('startTime', customStartTime);
			url.searchParams.set('endTime', customEndTime);
		}
		return url.toString();
	}

	async function handleShare(): Promise<void> {
		const url = getShareUrl();
		try {
			await navigator.clipboard.writeText(url);
			toast.success('Link copied to clipboard');
		} catch (err) {
			toast.error('Failed to copy link');
		}
	}

	async function fetchFirstWatchlistId(): Promise<number | null> {
		try {
			const sortOrder = [{ name: 'name', direction: 'ASC' }];
			const searchInputs: SearchInput[] = [];
			
			const result = await searchWatchlists({
				pageNumber: 0,
				pageSize: 1,
				sortOrder,
				searchInputs
			});
			
			if (result.content && result.content.length > 0) {
				return result.content[0].id ?? null;
			}
		} catch (err: unknown) {
			console.error('Failed to fetch first watchlist:', err);
		}
		return null;
	}

	async function loadWatchlist(): Promise<void> {
		if (watchlistId === null) return;
		
		try {
			watchlist = await getWatchlist(watchlistId);
		} catch (err: unknown) {
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
					timeRange,
					onlyCritical,
					sortBySeverity,
					groupedView,
					// Include custom time range if applicable
					...(timeRange === 'custom' && customStartTime && customEndTime
						? { startTime: customStartTime, endTime: customEndTime }
						: {})
				}
			};

			monitorData = await getUserMonitor(request);
			lastUpdated = new Date();

			// Save session after successful load
			saveSession();

			// Setup auto-refresh if live mode
			setupAutoRefresh(monitorData.live);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load monitor data');
			toast.error(message);
		} finally {
			loadingMonitor = false;
		}
	}

	async function initializePage(): Promise<void> {
		loading = true;
		
		// Check query params first
		const queryParams = parseQueryParams();
		
		if (queryParams?.watchlistId) {
			// Apply query params
			watchlistId = queryParams.watchlistId;
			if (queryParams.timeRange) timeRange = queryParams.timeRange;
			if (queryParams.onlyCritical !== undefined) onlyCritical = queryParams.onlyCritical;
			if (queryParams.sortBySeverity !== undefined) sortBySeverity = queryParams.sortBySeverity;
			if (queryParams.groupedView !== undefined) groupedView = queryParams.groupedView;
			if (queryParams.startTime) customStartTime = queryParams.startTime;
			if (queryParams.endTime) customEndTime = queryParams.endTime;
			
			// Clear query params from URL
			clearQueryParams();
		} else {
			// Try to load from session storage
			const session = loadSession();
			if (session) {
				watchlistId = session.watchlistId;
				timeRange = session.timeRange;
				onlyCritical = session.onlyCritical;
				sortBySeverity = session.sortBySeverity;
				groupedView = session.groupedView;
				if (session.startTime) customStartTime = session.startTime;
				if (session.endTime) customEndTime = session.endTime;
			}
		}

		// If still no watchlist, try to fetch the first one
		if (watchlistId === null) {
			watchlistId = await fetchFirstWatchlistId();
		}

		if (watchlistId === null) {
			// No watchlists available at all
			noWatchlistSelected = true;
			loading = false;
			return;
		}

		noWatchlistSelected = false;
		await loadWatchlist();
		await loadMonitorData();
		loading = false;
	}

	function setupAutoRefresh(isLive: boolean): void {
		// Clear existing interval
		if (refreshInterval !== null) {
			clearInterval(refreshInterval);
			refreshInterval = null;
		}

		// Setup new interval if live mode and preset range
		if (isLive && timeRange !== 'custom') {
			refreshInterval = setInterval(() => {
				loadMonitorData();
			}, 60000); // 60 seconds
		}
	}

	function handleTimeRangeChange(newRange: TimeRange): void {
		timeRange = newRange;
		// Clear custom times if switching away from custom
		if (newRange !== 'custom') {
			customStartTime = '';
			customEndTime = '';
			loadMonitorData();
		}
		// For custom, don't load yet - wait for both times to be set
	}

	function handleCustomStartTimeChange(newStartTime: string): void {
		customStartTime = newStartTime;
		// Only load if both times are set
		if (customStartTime && customEndTime) {
			loadMonitorData();
		}
	}

	function handleCustomEndTimeChange(newEndTime: string): void {
		customEndTime = newEndTime;
		// Only load if both times are set
		if (customStartTime && customEndTime) {
			loadMonitorData();
		}
	}

	function handleToggleOnlyCritical(): void {
		onlyCritical = !onlyCritical;
		loadMonitorData();
	}

	function handleToggleSortBySeverity(): void {
		sortBySeverity = !sortBySeverity;
		loadMonitorData();
	}

	function handleToggleGroupedView(): void {
		groupedView = !groupedView;
		loadMonitorData();
	}

	function handleEdit(): void {
		if (watchlistId !== null) {
			goto(`${CLIENT_ROUTES.WATCHLISTS_PAGE.path}/${watchlistId}`);
		}
	}

	function handleWatchlistChange(newWatchlistId: number): void {
		watchlistId = newWatchlistId;
		// Reset to defaults for new watchlist
		timeRange = '24h';
		onlyCritical = false;
		sortBySeverity = false;
		groupedView = false;
		customStartTime = '';
		customEndTime = '';
		initializeAfterWatchlistChange();
	}

	async function initializeAfterWatchlistChange(): Promise<void> {
		loading = true;
		noWatchlistSelected = false;
		await loadWatchlist();
		await loadMonitorData();
		loading = false;
	}

	// Initial load
	onMount(() => {
		initializePage();

		// Cleanup on unmount
		return () => {
			if (refreshInterval !== null) {
				clearInterval(refreshInterval);
			}
		};
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
					<WatchlistSelector
						currentWatchlistId={watchlistId ?? 0}
						currentWatchlistName={watchlist.name ?? ''}
						onSelect={handleWatchlistChange}
					/>
				{:else if loading}
					<div class="h-9 w-[300px] bg-muted/20 rounded animate-pulse"></div>
				{:else if noWatchlistSelected}
					<WatchlistSelector
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
						{timeRange}
						{onlyCritical}
						{sortBySeverity}
						{groupedView}
						{isLive}
						{customStartTime}
						{customEndTime}
						onTimeRangeChange={handleTimeRangeChange}
						onToggleOnlyCritical={handleToggleOnlyCritical}
						onToggleSortBySeverity={handleToggleSortBySeverity}
						onToggleGroupedView={handleToggleGroupedView}
						onCustomStartTimeChange={handleCustomStartTimeChange}
						onCustomEndTimeChange={handleCustomEndTimeChange}
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
			<!-- No Watchlist Selected State -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
				<CardContent class="p-12">
					<div class="flex flex-col items-center justify-center text-center">
						<div class="h-20 w-20 rounded-full bg-primary/10 flex items-center justify-center mb-6">
							<LayoutList class="h-10 w-10 text-primary" />
						</div>
						<h3 class="text-xl font-semibold mb-2">No Watchlist Selected</h3>
						<p class="text-muted-foreground max-w-md mb-6">
							Select a watchlist from the dropdown above to start monitoring your channels.
						</p>
						<Button onclick={() => goto(CLIENT_ROUTES.WATCHLISTS_PAGE.path)} variant="outline">
							View All Watchlists
						</Button>
					</div>
				</CardContent>
			</Card>
		{:else if !watchlist}
			<!-- Error State -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
				<CardContent class="p-12">
					<div class="flex flex-col items-center justify-center text-center">
						<div class="h-20 w-20 rounded-full bg-destructive/10 flex items-center justify-center mb-6">
							<AlertCircle class="h-10 w-10 text-destructive" />
						</div>
						<h3 class="text-xl font-semibold mb-2">Watchlist Not Found</h3>
						<p class="text-muted-foreground max-w-md">
							The watchlist you're looking for doesn't exist or you don't have access to it.
						</p>
					</div>
				</CardContent>
			</Card>
		{:else if watchlist.configuration?.channelIds?.length === 0 && (!watchlist.configuration?.groups || watchlist.configuration.groups.length === 0)}
			<!-- Empty Watchlist State -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
				<CardContent class="p-12">
					<div class="flex flex-col items-center justify-center text-center">
						<div class="h-20 w-20 rounded-full bg-primary/10 flex items-center justify-center mb-6">
							<AlertCircle class="h-10 w-10 text-primary" />
						</div>
						<h3 class="text-xl font-semibold mb-2">No Channels Configured</h3>
						<p class="text-muted-foreground max-w-md mb-6">
							This watchlist has no channels to monitor. Add channels to start tracking their status
							over time.
						</p>
						<Button onclick={handleEdit} class="gap-2 bg-gradient-to-r from-primary to-accent">
							<Edit class="h-4 w-4" />
							Configure Watchlist
						</Button>
					</div>
				</CardContent>
			</Card>
		{:else if monitorData && rangeStart && rangeEnd}
			<!-- Main Monitor Card -->
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg overflow-hidden pt-2">
				<CardContent class="p-0">
					<!-- Time Axis Header -->
					<div class="grid grid-cols-[32px_minmax(120px,180px)_1fr_32px] gap-3 items-end px-4 py-1 bg-muted/20 border-b border-border/50">
						<!-- Columns 1+2: Live/Updated status (spans icon + name columns) -->
						<div class="col-span-2 flex flex-col pb-0.5 pl-4">
							{#if isLive}
								<span class="text-[11px] font-medium text-green-500 flex items-center gap-1">
									<span class="relative flex h-1.5 w-1.5">
										<span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
										<span class="relative inline-flex rounded-full h-1.5 w-1.5 bg-green-500"></span>
									</span>
									Live
								</span>
							{/if}
							{#if lastUpdated}
								<span class="text-[11px] text-muted-foreground">
									Last updated {lastUpdated.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
								</span>
							{/if}
						</div>

						<!-- Column 3: Time Axis (aligns with timeline) -->
						<div class="min-w-0 relative h-5">
							<!-- Axis line -->
							<div class="absolute bottom-0 left-0 right-0 h-px bg-border"></div>

							<!-- Start time -->
							<div class="absolute left-0 bottom-0 flex flex-col items-start">
								<span class="text-[10px] text-muted-foreground mb-0.5">
									{rangeStart.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
								</span>
								<div class="h-1 w-px bg-border"></div>
							</div>

							<!-- Intermediate ticks -->
							{#each timeTicks as tick, index (`tick-${index}-${tick.timestamp.getTime()}`)}
								{#if tick.position > 12 && tick.position < 88}
									<div
										class="absolute bottom-0 flex flex-col items-center transform -translate-x-1/2"
										style="left: {tick.position}%;"
									>
										<span class="text-[10px] text-muted-foreground mb-0.5">{tick.label}</span>
										<div class="h-1 w-px bg-border"></div>
									</div>
								{/if}
							{/each}

							<!-- End time / Now -->
							<div class="absolute right-0 bottom-0 flex flex-col items-end">
								<span class="text-[10px] text-muted-foreground mb-0.5">
									{#if isLive}
										Now
									{:else}
										{rangeEnd.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
									{/if}
								</span>
								<div class="h-1 w-px bg-border"></div>
							</div>
						</div>

						<!-- Column 4: Status placeholder -->
						<div></div>
					</div>

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
						{#if groupedView && monitorData.dashboard.groups && monitorData.dashboard.groups.length > 0}
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
						{#if monitorData.dashboard.channels && monitorData.dashboard.channels.length > 0}
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
						{#if (!monitorData.dashboard.channels || monitorData.dashboard.channels.length === 0) && (!monitorData.dashboard.groups || monitorData.dashboard.groups.length === 0)}
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
