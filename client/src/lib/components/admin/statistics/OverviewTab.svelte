<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { AreaChartCard } from '$lib/components/ui/chart';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { scaleTime } from 'd3-scale';
	import { Users, Building2, Radio, Calendar, Trophy } from '@lucide/svelte';
	import type { AdminStatsResponse } from '$lib/api/models.ts';
	import {
		formatChartData,
		formatXAxisDate,
		formatTooltipDate,
		formatPercentage,
		getBadgeVariant,
		formatBestDayCount,
		usersOrgsChartConfig,
		eventsChartConfig
	} from './statistics-helpers.js';

	interface Props {
		stats: AdminStatsResponse | null;
		loading: boolean;
		loadingSkeleton: import('svelte').Snippet<[number, string?]>;
	}

	let { stats, loading, loadingSkeleton }: Props = $props();

	type ChartView = 'users-orgs' | 'events';
	let chartView: ChartView = $state('users-orgs');

	function getActiveUserPercentage(): number {
		if (!stats?.totalUsers || stats.totalUsers === 0) return 0;
		return Math.round(((stats.activeUsers ?? 0) / stats.totalUsers) * 100);
	}
</script>

<!-- 4 Stat Cards -->
<div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
	<StatCard
		title="Users"
		value={stats?.totalUsers?.toLocaleString() ?? '0'}
		icon={Users}
		variant="blue"
		{loading}
	>
		{#snippet trailing()}
			<Badge variant="secondary" class="mb-1.5 text-xs">
				{getActiveUserPercentage()}% active
			</Badge>
		{/snippet}
	</StatCard>

	<StatCard
		title="Organizations"
		value={stats?.totalOrganizations?.toLocaleString() ?? '0'}
		icon={Building2}
		variant="purple"
		{loading}
	/>

	<StatCard
		title="Channels"
		value={stats?.totalChannels?.toLocaleString() ?? '0'}
		icon={Radio}
		variant="green"
		subtitle="{stats?.activeChannels ?? 0} active · {stats?.pausedChannels ?? 0} paused · {stats?.staleChannels ?? 0} stale"
		{loading}
	/>

	<StatCard
		title="Events"
		value={stats?.totalEventsInPeriod?.toLocaleString() ?? '0'}
		icon={Calendar}
		variant="orange"
		subtitle="in selected period"
		{loading}
	/>
</div>

<!-- Growth Chart with toggle -->
<div class="space-y-3">
	<div class="flex items-center justify-between">
		<h2 class="text-lg font-semibold">Growth Trends</h2>
		<div class="flex items-center gap-1 bg-muted/40 rounded-full p-1 border border-border/50">
			{#each ([{ value: 'users-orgs', label: 'Users & Orgs' }, { value: 'events', label: 'Events' }] as { value: string; label: string }[]) as item (item.value)}
				<button
					class="px-3 py-1 rounded-full text-xs font-medium transition-all {chartView === item.value
						? 'bg-primary text-primary-foreground shadow-sm'
						: 'text-muted-foreground hover:text-foreground'}"
					onclick={() => (chartView = item.value as ChartView)}
				>
					{item.label}
				</button>
			{/each}
		</div>
	</div>

	{#if chartView === 'users-orgs'}
		<AreaChartCard
			title=""
			data={formatChartData(stats?.growthData ?? [])}
			xScale={scaleTime()}
			series={[
				{ key: 'totalUsers', label: 'Users', color: usersOrgsChartConfig.totalUsers.color },
				{ key: 'totalOrganizations', label: 'Organizations', color: usersOrgsChartConfig.totalOrganizations.color }
			]}
			config={usersOrgsChartConfig}
			heightClass="h-80"
			showYAxis={true}
			xAxisFormat={(v) => formatXAxisDate(v as Date)}
			{loading}
		>
			{#snippet tooltip({ data })}
				<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3 min-w-48">
					<div class="font-semibold text-sm mb-2">{formatTooltipDate(data.date as Date)}</div>
					<div class="space-y-1.5">
						<div class="flex items-center gap-2">
							<div class="h-2.5 w-2.5 rounded-full flex-shrink-0" style="background-color: {usersOrgsChartConfig.totalUsers.color}"></div>
							<span class="text-xs text-muted-foreground">Users:</span>
							<span class="text-sm font-medium ml-auto">{((data.totalUsers as number) ?? 0).toLocaleString()}</span>
							{#if data.newUsersGrowthPercentage !== null && data.newUsersGrowthPercentage !== undefined}
								<Badge variant={getBadgeVariant(data.newUsersGrowthPercentage as number)} class="text-xs px-1.5 py-0">
									{formatPercentage(data.newUsersGrowthPercentage as number)}
								</Badge>
							{/if}
						</div>
						<div class="flex items-center gap-2">
							<div class="h-2.5 w-2.5 rounded-full flex-shrink-0" style="background-color: {usersOrgsChartConfig.totalOrganizations.color}"></div>
							<span class="text-xs text-muted-foreground">Orgs:</span>
							<span class="text-sm font-medium ml-auto">{((data.totalOrganizations as number) ?? 0).toLocaleString()}</span>
							{#if data.newOrganizationsGrowthPercentage !== null && data.newOrganizationsGrowthPercentage !== undefined}
								<Badge variant={getBadgeVariant(data.newOrganizationsGrowthPercentage as number)} class="text-xs px-1.5 py-0">
									{formatPercentage(data.newOrganizationsGrowthPercentage as number)}
								</Badge>
							{/if}
						</div>
					</div>
				</div>
			{/snippet}
		</AreaChartCard>
	{:else}
		<AreaChartCard
			title=""
			data={formatChartData(stats?.growthData ?? [])}
			xScale={scaleTime()}
			series={[
				{ key: 'newEvents', label: 'Events Ingested', color: eventsChartConfig.newEvents.color }
			]}
			config={eventsChartConfig}
			heightClass="h-80"
			showYAxis={true}
			xAxisFormat={(v) => formatXAxisDate(v as Date)}
			{loading}
		>
			{#snippet tooltip({ data })}
				<div class="rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-xl p-3 min-w-40">
					<div class="font-semibold text-sm mb-2">{formatTooltipDate(data.date as Date)}</div>
					<div class="flex items-center gap-2">
						<div class="h-2.5 w-2.5 rounded-full flex-shrink-0" style="background-color: {eventsChartConfig.newEvents.color}"></div>
						<span class="text-xs text-muted-foreground">Events:</span>
						<span class="text-sm font-medium ml-auto">{((data.newEvents as number) ?? 0).toLocaleString()}</span>
					</div>
				</div>
			{/snippet}
		</AreaChartCard>
	{/if}
</div>

<!-- Records section -->
<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
	<CardHeader class="flex flex-row items-center gap-2 pb-3">
		<Trophy class="h-5 w-5 text-primary" />
		<CardTitle class="text-base">Records</CardTitle>
	</CardHeader>
	<CardContent>
		{#if loading}
			{@render loadingSkeleton(3)}
		{:else}
			<div class="divide-y divide-border/50">
				<div class="flex items-center justify-between py-2.5">
					<div class="flex items-center gap-2">
						<Users class="h-4 w-4 text-blue-400" />
						<span class="text-sm text-muted-foreground">Most users in a day</span>
					</div>
					<span class="text-sm font-semibold tabular-nums">{formatBestDayCount(stats?.bestGrowthDayUsers, 'newUsers')}</span>
				</div>
				<div class="flex items-center justify-between py-2.5">
					<div class="flex items-center gap-2">
						<Building2 class="h-4 w-4 text-purple-400" />
						<span class="text-sm text-muted-foreground">Most orgs in a day</span>
					</div>
					<span class="text-sm font-semibold tabular-nums">{formatBestDayCount(stats?.bestGrowthDayOrganizations, 'newOrganizations')}</span>
				</div>
				<div class="flex items-center justify-between py-2.5">
					<div class="flex items-center gap-2">
						<Calendar class="h-4 w-4 text-green-400" />
						<span class="text-sm text-muted-foreground">Most events in a day</span>
					</div>
					<span class="text-sm font-semibold tabular-nums">{formatBestDayCount(stats?.bestGrowthDayEvents, 'newEvents')}</span>
				</div>
			</div>
		{/if}
	</CardContent>
</Card>
