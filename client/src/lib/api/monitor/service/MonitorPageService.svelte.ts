import { replaceState } from '$app/navigation';
import { toast } from 'svelte-sonner';
import { handleError } from '$lib/utils/error-handler';
import { Sessionstorage } from '$lib/utils/sessionstorage.svelte';
import type { 
	MonitorRequest, 
	MonitorResponse, 
	WatchlistDetailsResponse, 
	TimeRange,
	TimelineDuration,
	Severity
} from '$lib/api/models';
import {
	type MonitorFilters,
	type MonitorSession,
	createDefaultFilters,
	createDefaultSession,
	getMonitorSessionKey,
	parseMonitorQueryParams,
	buildMonitorShareUrl,
	createAutoRefresh
} from '../monitor.service';
import { type ZoomEntry, type ZoomBreadcrumb, BUCKET_INFO, formatZoomRangeLabel } from '$lib/components/monitor/types';

// ============ Types ============

export interface MonitorPageConfig {
	orgId?: number;
	getWatchlist: (watchlistId: number) => Promise<WatchlistDetailsResponse>;
	searchWatchlists: (params: any) => Promise<{ content?: Array<{ id?: number }> }>;
	getMonitor: (request: MonitorRequest) => Promise<MonitorResponse>;
	buildEditRoute: (watchlistId: number) => string;
	buildWatchlistsRoute: () => string;
	buildMonitorRoute: () => string;
}

// ============ Service Factory ============

export function createMonitorPageService(config: MonitorPageConfig) {
	const { orgId, getWatchlist, searchWatchlists, getMonitor, buildEditRoute, buildWatchlistsRoute, buildMonitorRoute } = config;

	// Session storage for persisting monitor state
	const session: Sessionstorage<MonitorSession> = new Sessionstorage<MonitorSession>(
		getMonitorSessionKey(orgId),
		createDefaultSession()
	);

	// Core state
	let watchlist: WatchlistDetailsResponse | null = $state(null);
	let monitorData: MonitorResponse | null = $state(null);
	let loading: boolean = $state(true);
	let loadingMonitor: boolean = $state(false);
	let noWatchlistSelected: boolean = $state(false);
	let lastUpdated: Date | null = $state(null);
	// Tracks the watchlistId of the last successfully loaded watchlist (for filter defaults)
	let lastLoadedWatchlistId: number | null = null;

	// Modal state
	let modalOpen: boolean = $state(false);
	let selectedChannelId: number | undefined = $state(undefined);
	let selectedChannelName: string = $state('');
	let selectedSeverity: Severity | null = $state(null);
	let selectedDuration: TimelineDuration | null = $state(null);
	let selectedTimelineDurations: TimelineDuration[] = $state([]);

	// Zoom state
	let zoomStack: ZoomEntry[] = $state([]);

	// Request counter for race condition protection
	let loadMonitorRequestId: number = 0;

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
	const rangeStart: Date | null = $derived.by(() => {
		if (!monitorData) return null;
		return new Date(monitorData.rangeStart);
	});
	const rangeEnd: Date | null = $derived.by(() => {
		if (!monitorData) return null;
		return new Date(monitorData.rangeEnd);
	});
	const isLive: boolean = $derived.by(() => monitorData?.live ?? false);
	const hasChannels: boolean = $derived.by(() =>
		(watchlist?.configuration?.channelIds?.length ?? 0) > 0 ||
		(watchlist?.configuration?.groups?.length ?? 0) > 0
	);
	const hasMonitorData: boolean = $derived.by(() =>
		(monitorData?.dashboard?.channels?.length ?? 0) > 0 ||
		(monitorData?.dashboard?.groups?.length ?? 0) > 0
	);

	// Check if current filters match watchlist defaults
	const filtersMatchDefaults = $derived.by(() => {
		if (!watchlist?.filters) return true; // No saved defaults = always match
		const defaults = watchlist.filters;
		return (
			filters.timeRange === ((defaults.timeRange as TimeRange) ?? '24h') &&
			filters.onlyCritical === (defaults.onlyCritical ?? false) &&
			filters.sortBySeverity === (defaults.sortBySeverity ?? false) &&
			filters.groupedView === (defaults.groupedView ?? false)
		);
	});

	// Derived zoom state
	const currentZoomLevel: number = $derived(zoomStack.length);
	const isAggregated: boolean = $derived.by(() => monitorData?.bucketSize != null);
	const bucketSizeLabel: string | null = $derived.by(() => {
		if (!monitorData?.bucketSize) return null;
		return BUCKET_INFO[monitorData.bucketSize]?.label ?? null;
	});
	const canZoomIn: boolean = $derived(isAggregated && currentZoomLevel < 2);
	const zoomBreadcrumbs: ZoomBreadcrumb[] = $derived.by(() => {
		if (currentZoomLevel === 0) return [];
		return zoomStack.map((entry: ZoomEntry, index: number): ZoomBreadcrumb => ({
			level: index,
			label: entry.label
		}));
	});

	// ============ Filter Handlers ============

	function updateFilter<K extends keyof MonitorFilters>(key: K, value: MonitorFilters[K]): void {
		const updates: Partial<MonitorSession> = { [key]: value };
		
		// Reset zoom when time range changes
		if (key === 'timeRange' || key === 'customStartTime' || key === 'customEndTime') {
			zoomStack = [];
		}
		
		// Clear custom times when switching away from custom
		if (key === 'timeRange' && value !== 'custom') {
			updates.customStartTime = '';
			updates.customEndTime = '';
		}
		
		session.update(updates);
		
		// For custom range, only reload when both times are set
		if (key === 'customStartTime' || key === 'customEndTime') {
			const currentSession: MonitorSession = session.value;
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
			const result = await searchWatchlists({
				pageNumber: 0,
				pageSize: 1,
				sortOrder: [{ name: 'name', direction: 'ASC' }],
				searchInputs: []
			});
			return result.content?.[0]?.id ?? null;
		} catch (err: unknown) {
			console.error('Failed to fetch first watchlist:', err);
			return null;
		}
	}

	async function loadWatchlist(): Promise<void> {
		if (watchlistId === null) return;
		
		try {
			watchlist = await getWatchlist(watchlistId);
			
			// Apply watchlist's saved filters if this is a fresh load or watchlist switch.
			// Compare against lastLoadedWatchlistId (set AFTER successful load) to correctly
			// detect a watchlist switch — session.value.watchlistId is already the new ID.
			if (watchlist?.filters) {
				const isFirstLoad: boolean = lastLoadedWatchlistId === null;
				const isWatchlistSwitch: boolean =
					lastLoadedWatchlistId !== null && lastLoadedWatchlistId !== watchlistId;
				
				if (isFirstLoad || isWatchlistSwitch) {
					session.update({
						timeRange: (watchlist.filters.timeRange as TimeRange) ?? '24h',
						onlyCritical: watchlist.filters.onlyCritical ?? false,
						sortBySeverity: watchlist.filters.sortBySeverity ?? false,
						groupedView: watchlist.filters.groupedView ?? false,
						// Clear custom times when loading watchlist defaults
						customStartTime: '',
						customEndTime: ''
					});
				}
			}
			lastLoadedWatchlistId = watchlistId;
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load watchlist');
			toast.error(message);
			watchlist = null;
		}
	}

	async function loadMonitorData(): Promise<void> {
		if (watchlistId === null) return;
		
		const requestId: number = ++loadMonitorRequestId;
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

			const data: MonitorResponse = await getMonitor(request);

			// Discard stale response if a newer request has been fired
			if (requestId !== loadMonitorRequestId) return;

			monitorData = data;
			lastUpdated = new Date();

			// Setup auto-refresh if live mode
			autoRefresh.stop();
			if (monitorData.live && (filters.timeRange !== 'custom' || currentZoomLevel > 0)) {
				autoRefresh.start();
			}
		} catch (err: unknown) {
			if (requestId !== loadMonitorRequestId) return;
			const { message } = handleError(err, 'Failed to load monitor data');
			toast.error(message);
		} finally {
			if (requestId === loadMonitorRequestId) {
				loadingMonitor = false;
			}
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
			replaceState(buildMonitorRoute(), {});
		}

		// If still no watchlist, try to fetch the first one
		if (session.value.watchlistId === null) {
			const firstId: number | null = await fetchFirstWatchlistId();
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
		
		const url: string = buildMonitorShareUrl(buildMonitorRoute(), watchlistId, filters);
		try {
			await navigator.clipboard.writeText(url);
			toast.success('Link copied to clipboard');
		} catch {
			toast.error('Failed to copy link');
		}
	}

	function handleEdit(): string {
		if (watchlistId !== null) {
			return buildEditRoute(watchlistId);
		}
		return '';
	}

	async function handleWatchlistChange(newWatchlistId: number): Promise<void> {
		// Just update watchlistId, let loadWatchlist apply the filters
		session.update({ watchlistId: newWatchlistId });
		
		loading = true;
		noWatchlistSelected = false;
		await loadWatchlist();
		await loadMonitorData();
		loading = false;
	}

	function openDetailsModal(
		channelId: number, 
		name: string, 
		severity: Severity | null, 
		duration: TimelineDuration, 
		timeline: TimelineDuration[]
	): void {
		selectedChannelId = channelId;
		selectedChannelName = name;
		selectedSeverity = severity;
		selectedDuration = duration;
		selectedTimelineDurations = timeline;
		modalOpen = true;
	}

	function resetToDefaults(): void {
		if (watchlist?.filters) {
			session.update({
				timeRange: (watchlist.filters.timeRange as TimeRange) ?? '24h',
				onlyCritical: watchlist.filters.onlyCritical ?? false,
				sortBySeverity: watchlist.filters.sortBySeverity ?? false,
				groupedView: watchlist.filters.groupedView ?? false,
				customStartTime: '',
				customEndTime: ''
			});
			loadMonitorData();
		}
	}

	function cleanup(): void {
		autoRefresh.stop();
	}

	// ============ Zoom Methods ============

	function zoomIn(duration: TimelineDuration): void {
		if (!isAggregated || !monitorData?.bucketSize || !rangeStart || !rangeEnd) return;
		if (currentZoomLevel >= 2) return;

		const bucketInfo = BUCKET_INFO[monitorData.bucketSize];
		if (!bucketInfo) return;

		const label: string =
			currentZoomLevel === 0
				? `${filters.timeRange} overview`
				: formatZoomRangeLabel(rangeStart, rangeEnd);

		zoomStack = [
			...zoomStack,
			{
				timeRange: filters.timeRange,
				customStartTime: filters.customStartTime,
				customEndTime: filters.customEndTime,
				label
			}
		];

		const midTime: number =
			(new Date(duration.startTime).getTime() + new Date(duration.endTime).getTime()) / 2;
		const halfWindowMs: number = (bucketInfo.zoomWindowHours * 60 * 60 * 1000) / 2;
		const zoomStart: Date = new Date(midTime - halfWindowMs);
		const zoomEnd: Date = new Date(midTime + halfWindowMs);

		session.update({
			timeRange: 'custom',
			customStartTime: zoomStart.toISOString(),
			customEndTime: zoomEnd.toISOString()
		});
		loadMonitorData();
	}

	function zoomOut(level: number): void {
		if (level < 0 || level >= zoomStack.length) return;

		const entry: ZoomEntry = zoomStack[level];
		zoomStack = zoomStack.slice(0, level);

		session.update({
			timeRange: entry.timeRange,
			customStartTime: entry.customStartTime,
			customEndTime: entry.customEndTime
		});

		if (entry.timeRange !== 'custom') {
			session.update({
				customStartTime: '',
				customEndTime: ''
			});
		}

		loadMonitorData();
	}

	function resetZoom(): void {
		if (zoomStack.length === 0) return;
		zoomOut(0);
	}

	// ============ Return Service Interface ============

	return {
		// State getters
		get watchlist(): WatchlistDetailsResponse | null { return watchlist; },
		get monitorData(): MonitorResponse | null { return monitorData; },
		get loading(): boolean { return loading; },
		get loadingMonitor(): boolean { return loadingMonitor; },
		get noWatchlistSelected(): boolean { return noWatchlistSelected; },
		get lastUpdated(): Date | null { return lastUpdated; },
		
		// Modal state
		get modalOpen(): boolean { return modalOpen; },
		set modalOpen(value: boolean) { modalOpen = value; },
		get selectedChannelId(): number | undefined { return selectedChannelId; },
		get selectedChannelName(): string { return selectedChannelName; },
		get selectedSeverity(): Severity | null { return selectedSeverity; },
		get selectedDuration(): TimelineDuration | null { return selectedDuration; },
		set selectedDuration(value: TimelineDuration | null) { selectedDuration = value; },
		get selectedTimelineDurations(): TimelineDuration[] { return selectedTimelineDurations; },
		
		// Derived state
		get watchlistId(): number | null { return watchlistId; },
		get filters(): MonitorFilters { return filters; },
		get rangeStart(): Date | null { return rangeStart; },
		get rangeEnd(): Date | null { return rangeEnd; },
		get isLive(): boolean { return isLive; },
		get hasChannels(): boolean { return hasChannels; },
		get hasMonitorData(): boolean { return hasMonitorData; },
		get filtersMatchDefaults(): boolean { return filtersMatchDefaults; },
		
		// Methods
		updateFilter,
		initializePage,
		handleShare,
		handleEdit,
		handleWatchlistChange,
		openDetailsModal,
		resetToDefaults,
		cleanup,
		buildWatchlistsRoute,

		// Zoom state
		get currentZoomLevel(): number { return currentZoomLevel; },
		get isAggregated(): boolean { return isAggregated; },
		get bucketSizeLabel(): string | null { return bucketSizeLabel; },
		get canZoomIn(): boolean { return canZoomIn; },
		get zoomBreadcrumbs(): ZoomBreadcrumb[] { return zoomBreadcrumbs; },

		// Zoom methods
		zoomIn,
		zoomOut,
		resetZoom
	};
}

export type MonitorPageService = ReturnType<typeof createMonitorPageService>;
