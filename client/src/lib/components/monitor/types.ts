import type {
	TimelineDuration,
	Severity,
	TimeRange,
	MonitorFilters,
	Timeline
} from '$lib/api/models';

/**
 * Segment position and width as percentage of time range
 */
export interface SegmentStyle {
	left: number;
	width: number;
}

/**
 * Get current severity from timeline (last segment's severity)
 */
export function getCurrentSeverityFromTimeline(timeline: Timeline): Severity | null {
	if (!timeline.durations || timeline.durations.length === 0) {
		return null;
	}
	// Return the severity of the last (most recent) duration segment
	return timeline.durations[timeline.durations.length - 1].severity;
}

/**
 * Calculate segment position for timeline rendering
 */
export function calculateSegmentStyle(
	duration: TimelineDuration,
	rangeStart: Date,
	rangeEnd: Date
): SegmentStyle {
	const totalMs: number = rangeEnd.getTime() - rangeStart.getTime();
	const startOffset: number = new Date(duration.startTime).getTime() - rangeStart.getTime();
	const segmentDuration: number =
		new Date(duration.endTime).getTime() - new Date(duration.startTime).getTime();

	return {
		left: (startOffset / totalMs) * 100,
		width: (segmentDuration / totalMs) * 100
	};
}

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
 * Format timestamp for display (European format)
 */
export function formatTimestamp(isoString: string): string {
	const date: Date = new Date(isoString);
	return date.toLocaleString('en-GB', {
		month: 'short',
		day: 'numeric',
		hour: '2-digit',
		minute: '2-digit'
	});
}

/**
 * Session storage key for monitor state
 */
export interface MonitorSessionState {
	timeRange: TimeRange;
	startTime?: string;
	endTime?: string;
	onlyCritical: boolean;
	sortBySeverity: boolean;
	groupedView: boolean;
}

const SESSION_KEY_PREFIX = 'monitor_state_';

export function getSessionKey(watchlistId: number): string {
	return `${SESSION_KEY_PREFIX}${watchlistId}`;
}

export function saveMonitorState(watchlistId: number, state: MonitorSessionState): void {
	try {
		sessionStorage.setItem(getSessionKey(watchlistId), JSON.stringify(state));
	} catch (err: unknown) {
		console.error('Failed to save monitor state:', err);
	}
}

export function loadMonitorState(watchlistId: number): MonitorSessionState | null {
	try {
		const stored: string | null = sessionStorage.getItem(getSessionKey(watchlistId));
		if (stored) {
			return JSON.parse(stored) as MonitorSessionState;
		}
	} catch (err: unknown) {
		console.error('Failed to load monitor state:', err);
	}
	return null;
}

/**
 * Calculate time axis ticks based on range
 */
export interface TimeTick {
	timestamp: Date;
	label: string;
	position: number;
}

export function calculateTimeTicks(rangeStart: Date, rangeEnd: Date): TimeTick[] {
	const ticks: TimeTick[] = [];
	const totalMs: number = rangeEnd.getTime() - rangeStart.getTime();
	const rangeHours: number = totalMs / (1000 * 60 * 60);

	let intervalMs: number;
	let formatOptions: Intl.DateTimeFormatOptions;

	if (rangeHours <= 2) {
		// Every 30 minutes
		intervalMs = 30 * 60 * 1000;
		formatOptions = { hour: '2-digit', minute: '2-digit' };
	} else if (rangeHours <= 12) {
		// Every 2 hours
		intervalMs = 2 * 60 * 60 * 1000;
		formatOptions = { hour: '2-digit', minute: '2-digit' };
	} else if (rangeHours <= 24) {
		// Every 4 hours
		intervalMs = 4 * 60 * 60 * 1000;
		formatOptions = { hour: '2-digit', minute: '2-digit' };
	} else if (rangeHours <= 168) {
		// Every day (7d range)
		intervalMs = 24 * 60 * 60 * 1000;
		formatOptions = { day: 'numeric', month: 'short' };
	} else {
		// Every 5 days (30d range)
		intervalMs = 5 * 24 * 60 * 60 * 1000;
		formatOptions = { day: 'numeric', month: 'short' };
	}

	let currentTime: number = rangeStart.getTime();
	// Round to next interval
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
