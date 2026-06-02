import type { TimelineDuration } from '$lib/api/models';
import { type ZoomEntry, type ZoomBreadcrumb, BUCKET_INFO, formatZoomRangeLabel } from '$lib/components/monitor/types';
import type { MonitorFilters } from '../monitor.service';

export interface ZoomServiceCallbacks {
	getFilters: () => MonitorFilters;
	getRangeStart: () => Date | null;
	getRangeEnd: () => Date | null;
	getIsAggregated: () => boolean;
	getBucketSize: () => string | null | undefined;
	onZoomChange: (timeRange: string, customStartTime: string, customEndTime: string) => void;
	onReload: () => void;
}

export function createMonitorZoomService(callbacks: ZoomServiceCallbacks) {
	let zoomStack: ZoomEntry[] = $state([]);

	const currentZoomLevel: number = $derived(zoomStack.length);
	const canZoomIn: boolean = $derived(callbacks.getIsAggregated() && currentZoomLevel < 2);
	const zoomBreadcrumbs: ZoomBreadcrumb[] = $derived.by(() => {
		if (currentZoomLevel === 0) return [];
		return zoomStack.map((entry: ZoomEntry, index: number): ZoomBreadcrumb => ({
			level: index,
			label: entry.label
		}));
	});

	function resetStack(): void {
		zoomStack = [];
	}

	function zoomIn(duration: TimelineDuration): void {
		const bucketSize: string | null | undefined = callbacks.getBucketSize();
		const rangeStart: Date | null = callbacks.getRangeStart();
		const rangeEnd: Date | null = callbacks.getRangeEnd();

		if (!callbacks.getIsAggregated() || !bucketSize || !rangeStart || !rangeEnd) return;
		if (currentZoomLevel >= 2) return;

		const bucketInfo = BUCKET_INFO[bucketSize];
		if (!bucketInfo) return;

		const filters: MonitorFilters = callbacks.getFilters();
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
		let zoomEndMs: number = midTime + halfWindowMs;
		let zoomStartMs: number = midTime - halfWindowMs;
		const now: number = Date.now();
		if (zoomEndMs > now) {
			zoomStartMs -= zoomEndMs - now;
			zoomEndMs = now;
		}

		callbacks.onZoomChange(
			'custom',
			new Date(zoomStartMs).toISOString(),
			new Date(zoomEndMs).toISOString()
		);
		callbacks.onReload();
	}

	function zoomOut(level: number): void {
		if (level < 0 || level >= zoomStack.length) return;

		const entry: ZoomEntry = zoomStack[level];
		zoomStack = zoomStack.slice(0, level);

		const customStart: string = entry.timeRange !== 'custom' ? '' : entry.customStartTime;
		const customEnd: string = entry.timeRange !== 'custom' ? '' : entry.customEndTime;

		callbacks.onZoomChange(entry.timeRange, customStart, customEnd);
		callbacks.onReload();
	}

	function resetZoom(): void {
		if (zoomStack.length === 0) return;
		zoomOut(0);
	}

	return {
		get currentZoomLevel(): number { return currentZoomLevel; },
		get canZoomIn(): boolean { return canZoomIn; },
		get zoomBreadcrumbs(): ZoomBreadcrumb[] { return zoomBreadcrumbs; },
		resetStack,
		zoomIn,
		zoomOut,
		resetZoom
	};
}

export type MonitorZoomService = ReturnType<typeof createMonitorZoomService>;
