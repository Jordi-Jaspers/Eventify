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
	type MonitorTab,
	createDefaultFilters,
	createDefaultSession,
	getMonitorSessionKey,
	parseMonitorQueryParams,
	buildMonitorShareUrl,
	createAutoRefresh
} from '../monitor.service';
import { BUCKET_INFO } from '$lib/components/monitor/types';
import { createMonitorZoomService } from './MonitorZoomService.svelte';

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
	let lastLoadedWatchlistId: number | null = null;

	// Modal state
	let modalOpen: boolean = $state(false);
	let selectedChannelId: number | undefined = $state(undefined);
	let selectedChannelName: string = $state('');
	let selectedSeverity: Severity | null = $state(null);
	let selectedDuration: TimelineDuration | null = $state(null);
	let selectedTimelineDurations: TimelineDuration[] = $state([]);

	// Tab state
	let activeTab: MonitorTab = $state('timeline');

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
	let eventFeedLive: boolean = $state(false);
	const hasChannels: boolean = $derived.by(() =>
		(watchlist?.configuration?.channelIds?.length ?? 0) > 0 ||
		(watchlist?.configuration?.groups?.length ?? 0) > 0
	);
	const hasMonitorData: boolean = $derived.by(() =>
		(monitorData?.dashboard?.channels?.length ?? 0) > 0 ||
		(monitorData?.dashboard?.groups?.length ?? 0) > 0
	);

	const filtersMatchDefaults = $derived.by(() => {
		if (!watchlist?.filters) return true;
		const defaults = watchlist.filters;
		return (
			filters.timeRange === ((defaults.timeRange as TimeRange) ?? '24h') &&
			filters.onlyCritical === (defaults.onlyCritical ?? false) &&
			filters.sortBySeverity === (defaults.sortBySeverity ?? false) &&
			filters.groupedView === (defaults.groupedView ?? false)
		);
	});

	const isAggregated: boolean = $derived.by(() => monitorData?.bucketSize != null);
	const bucketSizeLabel: string | null = $derived.by(() => {
		if (!monitorData?.bucketSize) return null;
		return BUCKET_INFO[monitorData.bucketSize]?.label ?? null;
	});

	// Zoom service
	const zoom = createMonitorZoomService({
		getFilters: () => filters,
		getRangeStart: () => rangeStart,
		getRangeEnd: () => rangeEnd,
		getIsAggregated: () => isAggregated,
		getBucketSize: () => monitorData?.bucketSize,
		onZoomChange: (timeRange: string, customStartTime: string, customEndTime: string) => {
			session.update({ timeRange: timeRange as TimeRange, customStartTime, customEndTime });
		},
		onReload: () => loadMonitorData()
	});

	// ============ Filter Handlers ============

	function updateFilter<K extends keyof MonitorFilters>(key: K, value: MonitorFilters[K]): void {
		const updates: Partial<MonitorSession> = { [key]: value };
		
		if (key === 'timeRange' || key === 'customStartTime' || key === 'customEndTime') {
			zoom.resetStack();
		}
		
		if (key === 'timeRange' && value !== 'custom') {
			updates.customStartTime = '';
			updates.customEndTime = '';
		}
		
		session.update(updates);
		
		if (key === 'customStartTime' || key === 'customEndTime') {
			const currentSession: MonitorSession = session.value;
			if (currentSession.customStartTime && currentSession.customEndTime) {
				loadMonitorData();
			}
		} else if (key !== 'timeRange' || value !== 'custom') {
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

			if (requestId !== loadMonitorRequestId) return;

			monitorData = data;
			lastUpdated = new Date();

			autoRefresh.stop();
			if (monitorData.live && (filters.timeRange !== 'custom' || zoom.currentZoomLevel > 0)) {
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
		
		const queryParams = parseMonitorQueryParams(new URL(window.location.href));
		
		if (queryParams) {
			session.value = {
				watchlistId: queryParams.watchlistId,
				...createDefaultFilters(),
				...queryParams.filters
			};
			if (queryParams.tab) {
				activeTab = queryParams.tab;
			}
			replaceState(buildMonitorRoute(), {});
		} else {
			const savedTab = sessionStorage.getItem(getMonitorSessionKey(orgId) + '_tab');
			if (savedTab === 'timeline' || savedTab === 'events') {
				activeTab = savedTab;
			}
		}

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
		
		const url: string = buildMonitorShareUrl(buildMonitorRoute(), watchlistId, filters, activeTab);
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
		get eventFeedLive(): boolean { return eventFeedLive; },
		set eventFeedLive(v: boolean) { eventFeedLive = v; },
		get activeTab(): MonitorTab { return activeTab; },
		set activeTab(v: MonitorTab) {
			activeTab = v;
			sessionStorage.setItem(getMonitorSessionKey(orgId) + '_tab', v);
		},
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

		// Zoom state (delegated)
		get currentZoomLevel(): number { return zoom.currentZoomLevel; },
		get isAggregated(): boolean { return isAggregated; },
		get bucketSizeLabel(): string | null { return bucketSizeLabel; },
		get canZoomIn(): boolean { return zoom.canZoomIn; },
		get zoomBreadcrumbs() { return zoom.zoomBreadcrumbs; },

		// Zoom methods (delegated)
		zoomIn: (duration: TimelineDuration) => zoom.zoomIn(duration),
		zoomOut: (level: number) => zoom.zoomOut(level),
		resetZoom: () => zoom.resetZoom()
	};
}

export type MonitorPageService = ReturnType<typeof createMonitorPageService>;
