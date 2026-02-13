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

	// Modal state
	let modalOpen: boolean = $state(false);
	let selectedChannelId: number | undefined = $state(undefined);
	let selectedChannelName: string = $state('');
	let selectedSeverity: Severity | null = $state(null);
	let selectedDuration: TimelineDuration | null = $state(null);
	let selectedTimelineDurations: TimelineDuration[] = $state([]);

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
	const rangeStart: Date | null = $derived(
		monitorData ? new Date((monitorData as MonitorResponse).rangeStart) : null
	);
	const rangeEnd: Date | null = $derived(
		monitorData ? new Date((monitorData as MonitorResponse).rangeEnd) : null
	);
	const isLive: boolean = $derived((monitorData as MonitorResponse | null)?.live ?? false);
	const hasChannels: boolean = $derived(
		((watchlist as WatchlistDetailsResponse | null)?.configuration?.channelIds?.length ?? 0) > 0 ||
		((watchlist as WatchlistDetailsResponse | null)?.configuration?.groups?.length ?? 0) > 0
	);
	const hasMonitorData: boolean = $derived(
		((monitorData as MonitorResponse | null)?.dashboard?.channels?.length ?? 0) > 0 ||
		((monitorData as MonitorResponse | null)?.dashboard?.groups?.length ?? 0) > 0
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
			
			// Apply watchlist's saved filters if this is a fresh load or watchlist switch
			if (watchlist?.filters) {
				const currentFilters: MonitorSession = session.value;
				const isFirstLoad: boolean = !currentFilters.timeRange || currentFilters.timeRange === '24h'; // default
				const isWatchlistSwitch: boolean = currentFilters.watchlistId !== watchlistId;
				
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
					timeRange: filters.timeRange,
					onlyCritical: filters.onlyCritical,
					sortBySeverity: filters.sortBySeverity,
					groupedView: filters.groupedView,
					...(filters.timeRange === 'custom' && filters.customStartTime && filters.customEndTime
						? { startTime: filters.customStartTime, endTime: filters.customEndTime }
						: {})
				}
			};

			monitorData = await getMonitor(request);
			lastUpdated = new Date();

			// Setup auto-refresh if live mode
			autoRefresh.stop();
			if (monitorData.live && filters.timeRange !== 'custom') {
				autoRefresh.start();
			}
		} catch (err: unknown) {
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
		buildWatchlistsRoute
	};
}

export type MonitorPageService = ReturnType<typeof createMonitorPageService>;
