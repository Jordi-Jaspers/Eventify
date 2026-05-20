<script lang="ts">
	import type { AuditLogStatsResponse } from '$lib/api/models';
	import { AreaChartCard } from '$lib/components/ui/chart';
	import type { ChartConfig } from '$lib/components/ui/chart/types';
	import { scaleUtc } from 'd3-scale';

	interface Props {
		stats: AuditLogStatsResponse | null;
		statsLoading: boolean;
		dateFrom?: string;
		dateTo?: string;
		onHourClick: (from: string, to: string) => void;
	}

	let { stats, statsLoading, dateFrom, dateTo, onHourClick }: Props = $props();

	const xDomain = $derived.by((): [Date, Date] | undefined => {
		if (!dateFrom || !dateTo) return undefined;
		return [new Date(dateFrom), new Date(dateTo)];
	});

	const chartData = $derived(
		(stats?.hourlyBuckets ?? []).map((b) => ({
			date: new Date(b.hour),
			total: b.total,
			errors: b.errors
		}))
	);

	const chartConfig: ChartConfig = {
		total: { label: 'Requests', color: 'hsl(221 83% 53%)' },
		errors: { label: 'Errors', color: 'hsl(0 84% 60%)' }
	};

	const series = [
		{ key: 'errors', label: chartConfig.errors.label, color: chartConfig.errors.color },
		{ key: 'total', label: chartConfig.total.label, color: chartConfig.total.color }
	];

	const isMultiDay = $derived.by((): boolean => {
		if (chartData.length < 2) return false;
		const first: Date = chartData[0].date;
		const last: Date = chartData[chartData.length - 1].date;
		return last.getTime() - first.getTime() > 24 * 60 * 60 * 1000;
	});

	function formatHour(v: Date): string {
		return v.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
	}

	function formatDay(v: Date): string {
		return v.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	}
</script>

<AreaChartCard
	title={isMultiDay ? 'Daily Traffic' : 'Hourly Traffic'}
	data={chartData}
	xScale={scaleUtc()}
	{xDomain}
	{series}
	config={chartConfig}
	heightClass={isMultiDay ? 'h-48' : 'h-36'}
	xAxisFormat={(v) => isMultiDay ? formatDay(v as Date) : formatHour(v as Date)}
	loading={statsLoading}
>
	{#snippet tooltip({ data })}
		<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3">
			<div class="font-semibold text-sm mb-1.5">{isMultiDay ? formatDay(data.date as Date) : formatHour(data.date as Date)}</div>
			<div class="grid gap-1 text-xs">
				{#each series as s (s.key)}
					<div class="flex items-center gap-2">
						<span class="w-2 h-2 rounded-full" style="background-color: {s.color}"></span>
						<span class="text-muted-foreground">{s.label}:</span>
						<span class="font-medium">{data[s.key]}</span>
					</div>
				{/each}
			</div>
		</div>
	{/snippet}
</AreaChartCard>
