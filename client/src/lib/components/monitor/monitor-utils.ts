import type { TimelineDuration, Severity } from '$lib/api/models';
import type { SegmentStyle, TimeTick } from './types';

export const BUCKET_INFO: Record<string, { label: string; zoomWindowHours: number }> = {
	PT4H: { label: '4h resolution', zoomWindowHours: 24 },
	PT2H: { label: '2h resolution', zoomWindowHours: 12 },
	PT30M: { label: '30m resolution', zoomWindowHours: 3 }
};

/**
 * Get color classes for severity level
 */
export function getSeverityColors(severity: Severity): {
	bg: string;
	text: string;
	border: string;
} {
	switch (severity) {
		case 'CRITICAL':
			return { bg: 'bg-red-500', text: 'text-red-500', border: 'border-red-500' };
		case 'WARNING':
			return { bg: 'bg-amber-500', text: 'text-amber-500', border: 'border-amber-500' };
		case 'OK':
			return { bg: 'bg-green-500', text: 'text-green-500', border: 'border-green-500' };
		case 'NO_DATA':
			return { bg: 'bg-gray-400', text: 'text-gray-400', border: 'border-gray-400' };
	}
}

/**
 * Calculate all segment styles with cumulative left positioning to eliminate
 * sub-pixel floating-point gaps between adjacent segments.
 */
export function calculateCumulativeSegmentStyles(
	durations: TimelineDuration[],
	rangeStart: Date,
	rangeEnd: Date
): SegmentStyle[] {
	const totalMs: number = rangeEnd.getTime() - rangeStart.getTime();
	const rangeStartMs: number = rangeStart.getTime();
	const styles: SegmentStyle[] = [];

	for (const duration of durations) {
		const startMs: number = new Date(duration.startTime).getTime();
		const endMs: number = duration.endTime
			? new Date(duration.endTime).getTime()
			: rangeEnd.getTime();
		const left: number = ((startMs - rangeStartMs) / totalMs) * 100;
		const right: number = ((endMs - rangeStartMs) / totalMs) * 100;

		styles.push({ left, width: right - left });
	}

	return styles;
}

/**
 * Calculate time axis ticks based on range
 */
export function calculateTimeTicks(rangeStart: Date, rangeEnd: Date): TimeTick[] {
	const ticks: TimeTick[] = [];
	const totalMs: number = rangeEnd.getTime() - rangeStart.getTime();
	const rangeHours: number = totalMs / (1000 * 60 * 60);

	let intervalMs: number;
	let formatOptions: Intl.DateTimeFormatOptions;

	if (rangeHours <= 2) {
		intervalMs = 30 * 60 * 1000;
		formatOptions = { hour: '2-digit', minute: '2-digit' };
	} else if (rangeHours <= 12) {
		intervalMs = 2 * 60 * 60 * 1000;
		formatOptions = { hour: '2-digit', minute: '2-digit' };
	} else if (rangeHours <= 24) {
		intervalMs = 4 * 60 * 60 * 1000;
		formatOptions = { hour: '2-digit', minute: '2-digit' };
	} else if (rangeHours <= 168) {
		intervalMs = 24 * 60 * 60 * 1000;
		formatOptions = { day: 'numeric', month: 'short' };
	} else {
		intervalMs = 5 * 24 * 60 * 60 * 1000;
		formatOptions = { day: 'numeric', month: 'short' };
	}

	let currentTime: number = rangeStart.getTime();
	currentTime = Math.ceil(currentTime / intervalMs) * intervalMs;

	while (currentTime <= rangeEnd.getTime()) {
		const timestamp: Date = new Date(currentTime);
		const position: number = ((currentTime - rangeStart.getTime()) / totalMs) * 100;

		ticks.push({
			timestamp,
			label: timestamp.toLocaleString('en-GB', formatOptions),
			position
		});

		currentTime += intervalMs;
	}

	return ticks;
}

/**
 * Format a date range as a human-readable zoom label
 */
export function formatZoomRangeLabel(start: Date, end: Date): string {
	const rangeHours: number = (end.getTime() - start.getTime()) / (1000 * 60 * 60);
	if (rangeHours <= 24) {
		return `${start.toLocaleDateString('en-GB', { month: 'short', day: 'numeric' })} ${start.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}-${end.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}`;
	}
	return `${start.toLocaleDateString('en-GB', { month: 'short', day: 'numeric' })} - ${end.toLocaleDateString('en-GB', { month: 'short', day: 'numeric' })}`;
}

/**
 * Merge consecutive same-severity durations that are sub-pixel wide
 */
export function mergeSubPixelDurations(
	durations: TimelineDuration[],
	rangeStart: Date,
	rangeEnd: Date,
	containerWidth: number
): TimelineDuration[] {
	if (containerWidth <= 0 || durations.length === 0) return durations;

	const totalMs: number = rangeEnd.getTime() - rangeStart.getTime();
	const result: TimelineDuration[] = [];
	let current: TimelineDuration | null = null;

	for (const d of durations) {
		const endMs: number = d.endTime ? new Date(d.endTime).getTime() : rangeEnd.getTime();
		const widthPx: number =
			((endMs - new Date(d.startTime).getTime()) / totalMs) * containerWidth;

		if (current && current.severity === d.severity && widthPx < 1) {
			current = { ...current, endTime: d.endTime } as TimelineDuration;
		} else {
			if (current) result.push(current);
			current = { ...d } as TimelineDuration;
		}
	}
	if (current) result.push(current);
	return result;
}

/**
 * Get current severity from timeline (last segment's severity)
 */
export function getCurrentSeverityFromTimeline(
	timeline: import('$lib/api/models').Timeline
): Severity | null {
	if (!timeline.durations || timeline.durations.length === 0) {
		return null;
	}
	return timeline.durations[timeline.durations.length - 1].severity;
}
