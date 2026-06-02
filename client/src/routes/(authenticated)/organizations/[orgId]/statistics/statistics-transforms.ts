import type { OrgTimelineBucketResponse, OrgErrorRateBucketResponse } from '$lib/api/models';

export type ChartPoint = { date: Date; count: number };

export function buildThroughputChartData(
	timeline: OrgTimelineBucketResponse[]
): ChartPoint[] {
	return timeline.map(
		(b: OrgTimelineBucketResponse): ChartPoint => ({
			date: new Date(b.bucket),
			count: b.eventCount
		})
	);
}

export function buildErrorChartData(
	errorTimeline: OrgErrorRateBucketResponse[] | null | undefined
): ChartPoint[] {
	if (!errorTimeline || errorTimeline.length === 0) return [];
	return errorTimeline.map(
		(b: OrgErrorRateBucketResponse): ChartPoint => ({
			date: new Date(b.bucket),
			count: b.errorRate
		})
	);
}
