<script lang="ts">
	import type { AuditLogStatsResponse } from '$lib/api/models';
	import { AreaChartCard } from '$lib/components/ui/chart';
	import type { ChartConfig } from '$lib/components/ui/chart/types';
	import { scaleUtc } from 'd3-scale';

	interface Props {
		stats: AuditLogStatsResponse | null;
		statsLoading: boolean;
		onHourClick: (from: string, to: string) => void;
	}

	let { stats, statsLoading, onHourClick }: Props = $props();

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

	function formatHour(v: Date): string {
		return v.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
	}
</script>

<AreaChartCard
	title="Hourly Traffic"
	data={chartData}
	xScale={scaleUtc()}
	{series}
	config={chartConfig}
	heightClass="h-36"
	xAxisFormat={(v) => formatHour(v as Date)}
	loading={statsLoading}
>
	{#snippet tooltip({ data })}
		<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3">
			<div class="font-semibold text-sm mb-1.5">{formatHour(data.date as Date)}</div>
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
