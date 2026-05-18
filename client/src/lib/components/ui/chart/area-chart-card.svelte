<script lang="ts">
	import type { Snippet } from 'svelte';
	import * as Chart from '$lib/components/ui/chart';
	import type { ChartConfig } from '$lib/components/ui/chart/types';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { curveNatural } from 'd3-shape';
	import { AreaChart, Tooltip } from 'layerchart';

	interface SeriesConfig {
		key: string;
		label: string;
		value?: string;
		color: string;
	}

	interface Props {
		title: string;
		data: any[];
		x?: string;
		xScale: any;
		series: SeriesConfig[];
		config: ChartConfig;
		seriesLayout?: 'overlap' | 'stack';
		heightClass?: string;
		xAxisFormat?: (v: any) => string;
		showYAxis?: boolean;
		loading?: boolean;
		tooltip?: Snippet<[{ data: any }]>;
	}

	let {
		title,
		data,
		x = 'date',
		xScale,
		series,
		config,
		seriesLayout = 'overlap',
		heightClass = 'h-48',
		xAxisFormat,
		showYAxis = false,
		loading = false,
		tooltip
	}: Props = $props();
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden">
	<CardHeader>
		<div class="flex items-center justify-between">
			<CardTitle class="text-sm font-medium text-muted-foreground">{title}</CardTitle>
			<div class="flex items-center gap-4 text-xs text-muted-foreground">
				{#each series as s (s.key)}
					<span class="flex items-center gap-1.5">
						<span class="w-2.5 h-2.5 rounded-sm" style="background-color: {s.color}"></span>
						{s.label}
					</span>
				{/each}
			</div>
		</div>
	</CardHeader>
	<CardContent>
		{#if loading}
			<div class="{heightClass} w-full bg-muted/50 animate-pulse rounded"></div>
		{:else if data.length > 0}
			<Chart.Container {config} class="{heightClass} w-full">
				<AreaChart
					{data}
					{x}
					{xScale}
					series={series.map((s) => ({ key: s.key, label: s.label, value: s.value ?? s.key, color: s.color }))}
					{seriesLayout}
					padding={{ top: 10, bottom: 30, left: showYAxis ? 40 : 10, right: 10 }}
					props={{
						area: { fillOpacity: 0.2, curve: curveNatural },
						xAxis: { format: xAxisFormat, rule: true },
						...(showYAxis ? { yAxis: { rule: true } } : {})
					}}
				>
					{#snippet tooltip()}
						<Tooltip.Root variant="none">
							{#snippet children(ctx)}
								{@const d = (ctx as any).data}
								{#if tooltip}
									{@render (tooltip as any)({ data: d })}
								{:else}
									<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3">
										<div class="grid gap-1 text-xs">
											{#each series as s (s.key)}
												<div class="flex items-center gap-2">
													<span class="w-2 h-2 rounded-full" style="background-color: {s.color}"></span>
													<span class="text-muted-foreground">{s.label}:</span>
													<span class="font-medium">{d[s.key]}</span>
												</div>
											{/each}
										</div>
									</div>
								{/if}
							{/snippet}
						</Tooltip.Root>
					{/snippet}
				</AreaChart>
			</Chart.Container>
		{:else}
			<div class="{heightClass} flex items-center justify-center text-sm text-muted-foreground">
				No data available
			</div>
		{/if}
	</CardContent>
</Card>
