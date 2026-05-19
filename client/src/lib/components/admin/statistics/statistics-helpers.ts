import type { GrowthDataPoint } from '$lib/api/models.ts';
import type { ChartConfig } from '$lib/components/ui/chart/types';

// ── Chart configs ──────────────────────────────────────────────────────────────

export const usersOrgsChartConfig: ChartConfig = {
	totalOrganizations: { label: 'Organizations', color: 'hsl(280 80% 60%)' },
	totalUsers: { label: 'Users', color: 'hsl(210 80% 60%)' }
};

export const eventsChartConfig: ChartConfig = {
	newEvents: { label: 'Events Ingested', color: 'hsl(150 70% 50%)' }
};

export const ingestionChartConfig: ChartConfig = {
	count: { label: 'Events Ingested', color: 'hsl(210 80% 60%)' }
};

// ── Types ──────────────────────────────────────────────────────────────────────

export interface ChartDataPoint {
	date: Date;
	totalOrganizations: number;
	totalUsers: number;
	newEvents: number;
	dateStr: string;
	newUsersGrowthPercentage?: number | null;
	newOrganizationsGrowthPercentage?: number | null;
}

export interface IngestionChartPoint {
	date: Date;
	count: number;
}

// ── Formatters ─────────────────────────────────────────────────────────────────

export function formatChartData(growthData: GrowthDataPoint[]): ChartDataPoint[] {
	return growthData.map((point: GrowthDataPoint): ChartDataPoint => {
		const dateStr: string = point.date ?? '';
		const date: Date = new Date(dateStr);
		return {
			date,
			totalOrganizations: point.totalOrganizations ?? 0,
			totalUsers: point.totalUsers ?? 0,
			newEvents: point.newEvents ?? 0,
			dateStr: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
			newUsersGrowthPercentage: point.newUsersGrowthPercentage,
			newOrganizationsGrowthPercentage: point.newOrganizationsGrowthPercentage
		};
	});
}

export function formatXAxisDate(date: Date): string {
	return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export function formatTooltipDate(date: Date): string {
	return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
}

export function formatPercentage(value: number | null | undefined): string {
	if (value === null || value === undefined) return '0%';
	const sign: string = value > 0 ? '+' : '';
	return `${sign}${value.toFixed(1)}%`;
}

export function getBadgeVariant(value: number | null | undefined): 'default' | 'success' | 'destructive' {
	if (value === null || value === undefined || value === 0) return 'default';
	return value > 0 ? 'success' : 'destructive';
}

export function formatBestDayDate(point: GrowthDataPoint | null | undefined): string {
	if (!point?.date) return '';
	const date: Date = new Date(point.date);
	return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export function formatBestDayCount(
	point: GrowthDataPoint | null | undefined,
	field: 'newUsers' | 'newOrganizations' | 'newEvents'
): string {
	if (!point?.date) return '—';
	const count: number = (point[field] as number | undefined) ?? 0;
	return `${count.toLocaleString()} on ${formatBestDayDate(point)}`;
}

export function formatIngestionData(
	data: { date: string; eventCount: number }[],
	days: number
): IngestionChartPoint[] {
	const dataMap: Map<string, number> = new Map(data.map((p) => [p.date, p.eventCount]));
	const today: Date = new Date();
	today.setHours(0, 0, 0, 0);
	const points: IngestionChartPoint[] = [];
	for (let i: number = days - 1; i >= 0; i--) {
		const d: Date = new Date(today);
		d.setDate(d.getDate() - i);
		const key: string = d.toISOString().slice(0, 10);
		points.push({ date: d, count: dataMap.get(key) ?? 0 });
	}
	return points;
}
