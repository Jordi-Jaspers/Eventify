import type { EventSearchResponse } from '$lib/api/models';

// ============ Event Feed Types ============

export type EventFeedSeverity = NonNullable<EventSearchResponse['severity']>;

export interface EventFeedChannel {
	id: number;
	name: string;
}

// ============ Zoom Types ============

export interface ZoomEntry {
	timeRange: import('$lib/api/models').TimeRange;
	customStartTime: string;
	customEndTime: string;
	label: string;
}

export interface ZoomBreadcrumb {
	level: number;
	label: string;
}

/**
 * Segment position and width as percentage of time range
 */
export interface SegmentStyle {
	left: number;
	width: number;
}

/**
 * Time tick for axis rendering
 */
export interface TimeTick {
	timestamp: Date;
	label: string;
	position: number;
}

// Re-export utilities for backward compatibility
export {
	BUCKET_INFO,
	getSeverityColors,
	calculateCumulativeSegmentStyles,
	calculateTimeTicks,
	formatZoomRangeLabel,
	mergeSubPixelDurations,
	getCurrentSeverityFromTimeline
} from './monitor-utils';
