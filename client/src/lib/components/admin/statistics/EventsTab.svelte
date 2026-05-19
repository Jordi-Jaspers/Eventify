<script lang="ts">
	import { Card, CardContent } from '$lib/components/ui/card';
	import { AreaChartCard } from '$lib/components/ui/chart';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { scaleTime } from 'd3-scale';
	import {
		Users,
		Radio,
		ShieldAlert,
		AlertTriangle,
		CheckCircle,
		Gauge,
		BarChart3
	} from '@lucide/svelte';
	import type { AdminEventStatsResponse } from '$lib/api/admin/AdminController';
	import {
		formatIngestionData,
		formatXAxisDate,
		formatTooltipDate,
		ingestionChartConfig
	} from './statistics-helpers.js';

	interface Props {
		eventStats: AdminEventStatsResponse | null;
		selectedDays: number;
		eventsLoading: boolean;
		loadingSkeleton: import('svelte').Snippet<[number, string?]>;
	}

	let { eventStats, selectedDays, eventsLoading, loadingSkeleton }: Props = $props();
</script>

<!-- Daily Ingestion Chart -->
<div class="space-y-3">
	<h2 class="text-lg font-semibold flex items-center gap-2">
		<BarChart3 class="h-5 w-5 text-primary" />
		Daily Ingestion
	</h2>
	<AreaChartCard
		title=""
		data={formatIngestionData(eventStats?.dailyIngestion ?? [], selectedDays)}
		xScale={scaleTime()}
		series={[{ key: 'count', label: 'Events Ingested', color: ingestionChartConfig.count.color }]}
		config={ingestionChartConfig}
		heightClass="h-80"
		showYAxis={true}
		xAxisFormat={(v) => formatXAxisDate(v as Date)}
		loading={eventsLoading}
	>
		{#snippet tooltip({ data })}
			<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3 min-w-40">
				<div class="font-semibold text-sm mb-2">{formatTooltipDate(data.date as Date)}</div>
				<div class="flex items-center gap-2">
					<div class="h-2.5 w-2.5 rounded-full flex-shrink-0" style="background-color: {ingestionChartConfig.count.color}"></div>
					<span class="text-xs text-muted-foreground">Events:</span>
					<span class="text-sm font-medium ml-auto">{((data.count as number) ?? 0).toLocaleString()}</span>
				</div>
			</div>
		{/snippet}
	</AreaChartCard>
</div>

<!-- Severity Breakdown -->
<div>
	<h2 class="text-lg font-semibold flex items-center gap-2 mb-3">
		<ShieldAlert class="h-5 w-5 text-primary" />
		Severity Breakdown
	</h2>
	<div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
		<StatCard
			title="Critical"
			value={eventStats?.severityBreakdown?.critical?.toLocaleString() ?? '0'}
			icon={ShieldAlert}
			variant="red"
			loading={eventsLoading}
		/>
		<StatCard
			title="Warning"
			value={eventStats?.severityBreakdown?.warning?.toLocaleString() ?? '0'}
			icon={AlertTriangle}
			variant="yellow"
			loading={eventsLoading}
		/>
		<StatCard
			title="OK"
			value={eventStats?.severityBreakdown?.ok?.toLocaleString() ?? '0'}
			icon={CheckCircle}
			variant="green"
			loading={eventsLoading}
		/>
	</div>
</div>

<!-- Quota Stats -->
<div>
	<h2 class="text-lg font-semibold flex items-center gap-2 mb-3">
		<Gauge class="h-5 w-5 text-primary" />
		Quota Stats
	</h2>
	<div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
		<StatCard
			title="Users Near Limit"
			value={eventStats?.quotaStats?.usersNearLimit?.toLocaleString() ?? '0'}
			icon={Users}
			variant="orange"
			loading={eventsLoading}
		/>
		<StatCard
			title="Users At Limit"
			value={eventStats?.quotaStats?.usersAtLimit?.toLocaleString() ?? '0'}
			icon={Users}
			variant="red"
			loading={eventsLoading}
		/>
		<StatCard
			title="Avg Utilization"
			value="{(eventStats?.quotaStats?.averageUtilization ?? 0).toFixed(1)}%"
			icon={Gauge}
			variant="blue"
			loading={eventsLoading}
		/>
	</div>
</div>

<!-- Top Channels -->
<div>
	<h2 class="text-lg font-semibold flex items-center gap-2 mb-3">
		<Radio class="h-5 w-5 text-primary" />
		Top Channels by Volume
	</h2>
	<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
		<CardContent class="pt-4">
			{#if eventsLoading}
				{@render loadingSkeleton(5, 'h-8')}
			{:else if !eventStats?.topChannels?.length}
				<p class="text-sm text-muted-foreground">No channel data available.</p>
			{:else}
				<div class="divide-y divide-border/50">
					<div class="grid grid-cols-3 pb-2 text-xs font-medium text-muted-foreground uppercase tracking-wide">
						<span>Channel</span>
						<span>Owner</span>
						<span class="text-right">Events</span>
					</div>
					{#each eventStats.topChannels as ch (ch.channelName)}
						<div class="grid grid-cols-3 py-2.5 text-sm">
							<span class="font-medium truncate pr-4">{ch.channelName}</span>
							<span class="text-muted-foreground truncate pr-4">{ch.ownerName}</span>
							<span class="text-right tabular-nums">{ch.eventCount.toLocaleString()}</span>
						</div>
					{/each}
				</div>
			{/if}
		</CardContent>
	</Card>
</div>
