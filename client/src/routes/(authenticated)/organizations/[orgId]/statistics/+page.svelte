<script lang="ts">
	import { page } from '$app/state';
	import { goto } from '$app/navigation';
	import { AreaChartCard } from '$lib/components/ui/chart';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { PillToggle } from '$lib/components/ui/pill-toggle';
	import { TimeRangePopover } from '$lib/components/ui/time-range-popover';
	import ApiKeyStatsSection from '$lib/components/organization/api-key-stats-section.svelte';
	import { scaleTime } from 'd3-scale';
	import { Calendar, TrendingUp, AlertTriangle, Radio } from '@lucide/svelte';

	import {
		getOrganizationTimeline,
		getOrganizationSummary,
		getOrganizationApiKeyStats
	} from '$lib/api/organization/OrganizationStatisticsController';
	import type { OrgStatsRequest, OrgTimelineResponse, OrgSummaryResponse, OrgApiKeyStatsResponse } from '$lib/api/models';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { formatChartXAxis, formatChartTooltipDate } from '$lib/utils/chart-date-format';
	import {
		buildThroughputChartData,
		buildErrorChartData,
		type ChartPoint
	} from './statistics-transforms';

	const CHART_CONFIG = {
		throughput: { label: 'Events', color: 'hsl(150 70% 50%)' },
		errorRate: { label: 'Error Rate', color: 'hsl(0 70% 55%)' }
	} as const;

	const orgId: number = Number(page.params.orgId);

	let selectedDays: string = $state(page.url.searchParams.get('days') ?? '30');
	let isCustomRange: boolean = $state(
		page.url.searchParams.has('start') && page.url.searchParams.has('end')
	);
	let customStart: string = $state(page.url.searchParams.get('start') ?? '');
	let customEnd: string = $state(page.url.searchParams.get('end') ?? '');

	let activeChart: 'throughput' | 'errorRate' = $state('throughput');

	let timeline: OrgTimelineResponse | null = $state<OrgTimelineResponse | null>(null);
	let summary: OrgSummaryResponse | null = $state<OrgSummaryResponse | null>(null);
	let apiKeyStats: OrgApiKeyStatsResponse | null = $state<OrgApiKeyStatsResponse | null>(null);

	let timelineLoading: boolean = $state(false);
	let summaryLoading: boolean = $state(false);
	let apiKeyLoading: boolean = $state(false);

	const throughputChartData: ChartPoint[] = $derived(
		timeline != null ? buildThroughputChartData(timeline.timeline) : []
	);

	const errorChartData: ChartPoint[] = $derived(
		buildErrorChartData(timeline?.errorTimeline)
	);

	const activeChartData: ChartPoint[] = $derived(
		activeChart === 'throughput' ? throughputChartData : errorChartData
	);

	const daysNum: number = $derived(Number(selectedDays));

	function buildStatsRequest(): OrgStatsRequest {
		if (isCustomRange && customStart && customEnd) {
			return { startDate: customStart, endDate: customEnd };
		}
		return { days: Number(selectedDays) };
	}

	function formatXAxis(v: unknown): string {
		return formatChartXAxis(v as Date, daysNum);
	}

	function formatTooltipDate(d: Date): string {
		return formatChartTooltipDate(d, daysNum);
	}

	async function loadTimeline(): Promise<void> {
		timelineLoading = true;
		try {
			timeline = await getOrganizationTimeline(orgId, buildStatsRequest());
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load timeline');
			toast.error(message);
		} finally {
			timelineLoading = false;
		}
	}

	async function loadSummary(): Promise<void> {
		summaryLoading = true;
		try {
			summary = await getOrganizationSummary(orgId, buildStatsRequest());
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load summary');
			toast.error(message);
		} finally {
			summaryLoading = false;
		}
	}

	async function loadApiKeyStats(): Promise<void> {
		apiKeyLoading = true;
		try {
			apiKeyStats = await getOrganizationApiKeyStats(orgId);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load API key stats');
			toast.error(message);
		} finally {
			apiKeyLoading = false;
		}
	}

	function handleQuickRangeSelect(days: string): void {
		selectedDays = days;
		isCustomRange = false;
		customStart = '';
		customEnd = '';
		goto(`?days=${days}`, { replaceState: true });
	}

	function handleCustomRangeApply(start: string, end: string): void {
		customStart = start;
		customEnd = end;
		isCustomRange = true;
		goto(`?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`, { replaceState: true });
	}

	function handleCustomRangeToggle(): void {
		isCustomRange = true;
	}

	$effect(() => {
		const _days = selectedDays;
		const _custom = isCustomRange;
		const _start = customStart;
		const _end = customEnd;
		loadTimeline();
		loadSummary();
	});

	$effect(() => {
		loadApiKeyStats();
	});
</script>

<svelte:head>
	<title>Statistics - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-8 animate-fade-in">
		<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-2">
			<div>
				<h1 class="text-3xl font-bold text-primary">Statistics</h1>
				<p class="text-muted-foreground mt-1">Organization analytics and insights</p>
			</div>
			<TimeRangePopover
				{selectedDays}
				{isCustomRange}
				{customStart}
				{customEnd}
				onQuickRangeSelect={handleQuickRangeSelect}
				onCustomRangeApply={handleCustomRangeApply}
				onCustomRangeToggle={handleCustomRangeToggle}
			/>
		</div>

		<div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
			<StatCard
				title="Total Events"
				value={summary?.totalEvents?.toLocaleString() ?? '0'}
				icon={Calendar}
				variant="blue"
				loading={summaryLoading}
			/>
			<StatCard
				title="Avg Daily Volume"
				value={summary?.avgDailyVolume != null ? Math.round(summary.avgDailyVolume).toLocaleString() : '0'}
				icon={TrendingUp}
				variant="green"
				loading={summaryLoading}
			/>
			<StatCard
				title="Error Rate"
				value={summary?.currentErrorRate != null ? `${summary.currentErrorRate.toFixed(2)}%` : '0%'}
				icon={AlertTriangle}
				variant="red"
				loading={summaryLoading}
			/>
			<StatCard
				title="Active Channels"
				value={summary?.totalChannels?.toLocaleString() ?? '0'}
				icon={Radio}
				variant="purple"
				loading={summaryLoading}
			/>
		</div>

		<div class="space-y-3">
			<PillToggle
				items={[{ value: 'throughput', label: 'Event Volume' }, { value: 'errorRate', label: 'Error Rate' }]}
				active={activeChart}
				onSelect={(v) => { activeChart = v as 'throughput' | 'errorRate'; }}
			/>

			{#if activeChart === 'errorRate' && !timelineLoading && errorChartData.length === 0}
				<div class="rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl p-12 text-center">
					<AlertTriangle class="size-10 text-muted-foreground/40 mx-auto mb-3" />
					<p class="text-muted-foreground text-sm">No error data in this period.</p>
				</div>
			{:else}
				<AreaChartCard
					title={activeChart === 'throughput' ? 'Event Volume' : 'Error Rate'}
					data={activeChartData}
					xScale={scaleTime()}
					series={[{ key: 'count', label: CHART_CONFIG[activeChart].label, color: CHART_CONFIG[activeChart].color }]}
					config={{ count: { label: CHART_CONFIG[activeChart].label, color: CHART_CONFIG[activeChart].color } }}
					heightClass="h-80"
					showYAxis={true}
					xAxisFormat={formatXAxis}
					loading={timelineLoading}
				>
					{#snippet tooltip({ data })}
						<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3 min-w-40">
							<div class="font-semibold text-sm mb-2">
								{formatTooltipDate(data.date as Date)}
							</div>
							<div class="flex items-center gap-2">
								<div
									class="h-2.5 w-2.5 rounded-full flex-shrink-0"
									style="background-color: {CHART_CONFIG[activeChart].color};"
								></div>
								<span class="text-xs text-muted-foreground">
									{CHART_CONFIG[activeChart].label}:
								</span>
								<span class="text-sm font-medium ml-auto">
									{activeChart === 'throughput'
										? ((data.count as number) ?? 0).toLocaleString()
										: `${((data.count as number) ?? 0).toFixed(2)}%`}
								</span>
							</div>
						</div>
					{/snippet}
				</AreaChartCard>
			{/if}
		</div>

		{#if apiKeyStats != null}
			<ApiKeyStatsSection apiKeyStats={apiKeyStats} loading={apiKeyLoading} />
		{/if}
	</div>
</main>
