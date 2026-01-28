import type { TimeRange } from '$lib/api/models';

// ============ Types ============

export interface MonitorFilters {
	timeRange: TimeRange;
	onlyCritical: boolean;
	sortBySeverity: boolean;
	groupedView: boolean;
	customStartTime: string;
	customEndTime: string;
}

export interface MonitorSession extends MonitorFilters {
	watchlistId: number | null;
}

export const DEFAULT_FILTERS: MonitorFilters = {
	timeRange: '24h',
	onlyCritical: false,
	sortBySeverity: false,
	groupedView: false,
	customStartTime: '',
	customEndTime: ''
};

export function createDefaultFilters(): MonitorFilters {
	return { ...DEFAULT_FILTERS };
}

export function createDefaultSession(): MonitorSession {
	return {
		watchlistId: null,
		...DEFAULT_FILTERS
	};
}

// ============ Session Key Helpers ============

const DEFAULT_SESSION_KEY = 'monitor_session';

export function getMonitorSessionKey(orgId?: number): string {
	return orgId ? `monitor_session_org_${orgId}` : DEFAULT_SESSION_KEY;
}

// ============ URL Query Params ============

export function parseMonitorQueryParams(url: URL): { watchlistId: number; filters: Partial<MonitorFilters> } | null {
	const id = url.searchParams.get('id');
	if (!id) return null;

	const filters: Partial<MonitorFilters> = {};

	const tr = url.searchParams.get('timeRange');
	if (tr && ['2h', '4h', '12h', '24h', '7d', '30d', 'custom'].includes(tr)) {
		filters.timeRange = tr as TimeRange;
	}

	const oc = url.searchParams.get('onlyCritical');
	if (oc !== null) filters.onlyCritical = oc === 'true';

	const sbs = url.searchParams.get('sortBySeverity');
	if (sbs !== null) filters.sortBySeverity = sbs === 'true';

	const gv = url.searchParams.get('groupedView');
	if (gv !== null) filters.groupedView = gv === 'true';

	const st = url.searchParams.get('startTime');
	const et = url.searchParams.get('endTime');
	if (st) filters.customStartTime = st;
	if (et) filters.customEndTime = et;

	return { watchlistId: parseInt(id, 10), filters };
}

export function buildMonitorShareUrl(basePath: string, watchlistId: number, filters: MonitorFilters): string {
	const url = new URL(window.location.origin + basePath);
	url.searchParams.set('id', watchlistId.toString());
	url.searchParams.set('timeRange', filters.timeRange);
	url.searchParams.set('onlyCritical', filters.onlyCritical.toString());
	url.searchParams.set('sortBySeverity', filters.sortBySeverity.toString());
	url.searchParams.set('groupedView', filters.groupedView.toString());
	
	if (filters.timeRange === 'custom' && filters.customStartTime && filters.customEndTime) {
		url.searchParams.set('startTime', filters.customStartTime);
		url.searchParams.set('endTime', filters.customEndTime);
	}
	return url.toString();
}

// ============ Auto-Refresh Management ============

export function createAutoRefresh(
	callback: () => void,
	intervalMs: number = 60000
): { start: () => void; stop: () => void } {
	let intervalId: ReturnType<typeof setInterval> | null = null;
	
	return {
		start: () => {
			if (intervalId) clearInterval(intervalId);
			intervalId = setInterval(callback, intervalMs);
		},
		stop: () => {
			if (intervalId) {
				clearInterval(intervalId);
				intervalId = null;
			}
		}
	};
}
