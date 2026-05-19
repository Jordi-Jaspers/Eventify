<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import { Badge } from '$lib/components/ui/badge';
	import Button from '$lib/components/ui/button/button.svelte';
	import { AreaChartCard } from '$lib/components/ui/chart';
	import { scaleTime } from 'd3-scale';
	import { getAdminStats, getStorageStats } from '$lib/api/admin/AdminController';
	import { getAdminApiKeyStats } from '$lib/api/admin/AdminApiKeyController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import {
		Users,
		Building2,
		Radio,
		Calendar,
		Key,
		Database,
		Activity,
		TrendingUp,
		Trophy,
		CircleAlert
	} from '@lucide/svelte';
	import type {
		AdminStatsResponse,
		GrowthDataPoint,
		AdminApiKeyStatsResponse,
		TableSizeEntry
	} from '$lib/api/models.ts';
	import type { ChartConfig } from '$lib/components/ui/chart/types';
	import { StatCard } from '$lib/components/ui/stat-card';

	// ── State ──────────────────────────────────────────────────────────────────
	let stats: AdminStatsResponse | null = $state(null);
	let apiKeyStats: AdminApiKeyStatsResponse | null = $state(null);
	let storageStats: TableSizeEntry[] = $state([]);
	let loading: boolean = $state(true);
	let infraLoading: boolean = $state(false);
	let error: string | null = $state(null);

	// ── URL params ─────────────────────────────────────────────────────────────
	type Tab = 'overview' | 'infrastructure';
	type Days = 7 | 30 | 90;

	const activeTab: Tab = $derived(
		($page.url.searchParams.get('tab') as Tab) === 'infrastructure' ? 'infrastructure' : 'overview'
	);
	const selectedDays: Days = $derived(
		((): Days => {
			const d: number = Number($page.url.searchParams.get('days') ?? '30');
			return (d === 7 || d === 90 ? d : 30) as Days;
		})()
	);

	function setTab(tab: Tab): void {
		const url: URL = new URL($page.url);
		url.searchParams.set('tab', tab);
		goto(url.toString(), { replaceState: true });
		if (tab === 'infrastructure' && !apiKeyStats) {
			loadInfra();
		}
	}

	function setDays(days: Days): void {
		const url: URL = new URL($page.url);
		url.searchParams.set('days', String(days));
		goto(url.toString(), { replaceState: true });
		loadStats(days);
	}

	// ── Data loading ───────────────────────────────────────────────────────────
	async function loadStats(days?: Days): Promise<void> {
		loading = true;
		error = null;
		try {
			stats = await getAdminStats(days ?? selectedDays);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load admin statistics');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function loadInfra(): Promise<void> {
		infraLoading = true;
		try {
			const [keys, storage]: [AdminApiKeyStatsResponse, TableSizeEntry[]] = await Promise.all([
				getAdminApiKeyStats(),
				getStorageStats()
			]);
			apiKeyStats = keys;
			storageStats = storage;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load infrastructure stats');
			toast.error(message);
		} finally {
			infraLoading = false;
		}
	}

	onMount((): void => {
		loadStats();
		if (activeTab === 'infrastructure') {
			loadInfra();
		}
	});

	// ── Chart helpers ──────────────────────────────────────────────────────────
	type ChartView = 'users-orgs' | 'events';
	let chartView: ChartView = $state('users-orgs');

	interface ChartDataPoint {
		date: Date;
		totalOrganizations: number;
		totalUsers: number;
		newEvents: number;
		dateStr: string;
		newUsersGrowthPercentage?: number | null;
		newOrganizationsGrowthPercentage?: number | null;
	}

	const usersOrgsChartConfig: ChartConfig = {
		totalOrganizations: { label: 'Organizations', color: 'hsl(280 80% 60%)' },
		totalUsers: { label: 'Users', color: 'hsl(210 80% 60%)' }
	};

	const eventsChartConfig: ChartConfig = {
		newEvents: { label: 'Events Ingested', color: 'hsl(150 70% 50%)' }
	};

	function formatChartData(growthData: GrowthDataPoint[]): ChartDataPoint[] {
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

	function formatXAxisDate(date: Date): string {
		return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	}

	function formatTooltipDate(date: Date): string {
		return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
	}

	function formatPercentage(value: number | null | undefined): string {
		if (value === null || value === undefined) return '0%';
		const sign: string = value > 0 ? '+' : '';
		return `${sign}${value.toFixed(1)}%`;
	}

	function getBadgeVariant(value: number | null | undefined): 'default' | 'success' | 'destructive' {
		if (value === null || value === undefined || value === 0) return 'default';
		return value > 0 ? 'success' : 'destructive';
	}

	function getActiveUserPercentage(): number {
		if (!stats?.totalUsers || stats.totalUsers === 0) return 0;
		return Math.round(((stats.activeUsers ?? 0) / stats.totalUsers) * 100);
	}

	function formatBestDayDate(point: GrowthDataPoint | null | undefined): string {
		if (!point?.date) return '';
		const date: Date = new Date(point.date);
		return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
	}

	function formatBestDayCount(point: GrowthDataPoint | null | undefined, field: 'newUsers' | 'newOrganizations' | 'newEvents'): string {
		if (!point?.date) return '—';
		const count: number = (point[field] as number | undefined) ?? 0;
		return `${count.toLocaleString()} on ${formatBestDayDate(point)}`;
	}
</script>

{#snippet pillToggle(items: { value: string; label: string }[], active: string, onSelect: (v: string) => void, size?: 'sm' | 'md')}
	{@const px = size === 'sm' ? 'px-3 py-1' : 'px-4 py-1.5'}
	{@const text = size === 'sm' ? 'text-xs' : 'text-sm'}
	<div class="flex items-center gap-1 bg-muted/40 rounded-full p-1 border border-border/50">
		{#each items as item (item.value)}
			<button
				class="{px} rounded-full {text} font-medium transition-all {active === item.value
					? 'bg-primary text-primary-foreground shadow-sm'
					: 'text-muted-foreground hover:text-foreground'}"
				onclick={() => onSelect(item.value)}
			>
				{item.label}
			</button>
		{/each}
	</div>
{/snippet}

{#snippet loadingSkeleton(rows: number, height?: string)}
	{@const h = height ?? 'h-6'}
	<div class="space-y-3">
		{#each Array(rows) as _, i (i)}
			<div class="{h} bg-muted animate-pulse rounded"></div>
		{/each}
	</div>
{/snippet}

<svelte:head>
	<title>Admin Dashboard - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">

		<!-- Header -->
		<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-2">
			<div>
				<h1 class="text-3xl font-bold text-primary">Admin Dashboard</h1>
				<p class="text-muted-foreground mt-1">Platform statistics and infrastructure overview</p>
			</div>

			<!-- Time range selector -->
			{@render pillToggle(
				([7, 30, 90] as Days[]).map((d) => ({ value: String(d), label: `${d}d` })),
				String(selectedDays),
				(v) => setDays(Number(v) as Days)
			)}
		</div>

		<!-- Tab navigation -->
		{@render pillToggle(
			[{ value: 'overview', label: 'Overview' }, { value: 'infrastructure', label: 'Infrastructure' }],
			activeTab,
			(v) => setTab(v as Tab),
			'md'
		)}

		<!-- Error Alert -->
		{#if error && !loading}
			<Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{error}
					<Button variant="outline" size="sm" class="ml-4" onclick={() => loadStats()}>Retry</Button>
				</AlertDescription>
			</Alert>
		{/if}

		<!-- ═══════════════════════════════════════════════════════════════════ -->
		<!-- OVERVIEW TAB                                                        -->
		<!-- ═══════════════════════════════════════════════════════════════════ -->
		{#if activeTab === 'overview'}

			<!-- 4 Stat Cards -->
			<div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
				<!-- Users -->
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

				<!-- Organizations -->
				<StatCard
					title="Organizations"
					value={stats?.totalOrganizations?.toLocaleString() ?? '0'}
					icon={Building2}
					variant="purple"
					{loading}
				/>

				<!-- Channels -->
				<StatCard
					title="Channels"
					value={stats?.totalChannels?.toLocaleString() ?? '0'}
					icon={Radio}
					variant="green"
					subtitle="{stats?.activeChannels ?? 0} active · {stats?.pausedChannels ?? 0} paused · {stats?.staleChannels ?? 0} stale"
					{loading}
				/>

				<!-- Events -->
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
					{@render pillToggle(
						[{ value: 'users-orgs', label: 'Users & Orgs' }, { value: 'events', label: 'Events' }],
						chartView,
						(v) => (chartView = v as ChartView),
						'sm'
					)}
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

		<!-- ═══════════════════════════════════════════════════════════════════ -->
		<!-- INFRASTRUCTURE TAB                                                  -->
		<!-- ═══════════════════════════════════════════════════════════════════ -->
		{:else if activeTab === 'infrastructure'}

			<!-- API Key Stats -->
			<div>
				<h2 class="text-lg font-semibold flex items-center gap-2 mb-3">
					<Key class="h-5 w-5 text-primary" />
					API Key Stats
				</h2>
				<div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mb-4">
					<StatCard
						title="Total Keys"
						value={apiKeyStats?.totalKeys?.toLocaleString() ?? '0'}
						icon={Key}
						variant="blue"
						subtitle="{apiKeyStats?.userKeys ?? 0} user · {apiKeyStats?.organizationKeys ?? 0} org"
						loading={infraLoading}
					/>
					<StatCard
						title="Created This Week"
						value={apiKeyStats?.createdThisWeek?.toLocaleString() ?? '0'}
						icon={TrendingUp}
						variant="green"
						loading={infraLoading}
					/>
					<StatCard
						title="Created This Month"
						value={apiKeyStats?.createdThisMonth?.toLocaleString() ?? '0'}
						icon={TrendingUp}
						variant="purple"
						loading={infraLoading}
					/>
					<StatCard
						title="Revoked This Month"
						value={apiKeyStats?.revokedThisMonth?.toLocaleString() ?? '0'}
						icon={Activity}
						variant="red"
						loading={infraLoading}
					/>
				</div>
				<div class="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
					<StatCard
						title="Expiring Next 30 Days"
						value={apiKeyStats?.expiringNext30Days?.toLocaleString() ?? '0'}
						icon={Calendar}
						variant="yellow"
						loading={infraLoading}
					/>
					<StatCard
						title="Never Used Keys"
						value={apiKeyStats?.neverUsedKeys?.toLocaleString() ?? '0'}
						icon={Key}
						variant="orange"
						loading={infraLoading}
					/>
				</div>

				<!-- Top Keys mini-table -->
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
					<CardHeader class="flex flex-row items-center gap-2">
						<TrendingUp class="h-5 w-5 text-primary" />
						<CardTitle class="text-base">Top Keys by Usage</CardTitle>
					</CardHeader>
					<CardContent>
						{#if infraLoading}
							{@render loadingSkeleton(3, 'h-8')}
						{:else if !apiKeyStats?.topKeysByUsage?.length}
							<p class="text-sm text-muted-foreground">No data available.</p>
						{:else}
							<div class="divide-y divide-border/50">
								<div class="grid grid-cols-2 pb-2 text-xs font-medium text-muted-foreground uppercase tracking-wide">
									<span>Name</span>
									<span class="text-right">Requests</span>
								</div>
								{#each apiKeyStats.topKeysByUsage as key (key.id)}
									<div class="grid grid-cols-2 py-2.5 text-sm">
										<span class="font-medium truncate pr-4">{key.name}</span>
										<span class="text-right tabular-nums">{key.totalRequests.toLocaleString()}</span>
									</div>
								{/each}
							</div>
						{/if}
					</CardContent>
				</Card>
			</div>

			<!-- Channel Health -->
			<div>
				<h2 class="text-lg font-semibold flex items-center gap-2 mb-3">
					<Radio class="h-5 w-5 text-primary" />
					Channel Health
				</h2>
				<div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
					<StatCard
						title="Active"
						value={stats?.activeChannels?.toLocaleString() ?? '0'}
						icon={Activity}
						variant="green"
						{loading}
					/>
					<StatCard
						title="Paused"
						value={stats?.pausedChannels?.toLocaleString() ?? '0'}
						icon={Radio}
						variant="yellow"
						{loading}
					/>
					<StatCard
						title="Stale"
						value={stats?.staleChannels?.toLocaleString() ?? '0'}
						icon={Radio}
						variant="orange"
						{loading}
					/>
					<StatCard
						title="Pending Deletion"
						value={stats?.pendingDeletionChannels?.toLocaleString() ?? '0'}
						icon={Radio}
						variant="red"
						{loading}
					/>
				</div>
			</div>

			<!-- Storage -->
			<div>
				<h2 class="text-lg font-semibold flex items-center gap-2 mb-3">
					<Database class="h-5 w-5 text-primary" />
					Storage
				</h2>
				<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
					<CardContent class="pt-4">
						{#if infraLoading}
							{@render loadingSkeleton(5, 'h-8')}
						{:else if !storageStats.length}
							<p class="text-sm text-muted-foreground">No storage data available.</p>
						{:else}
							<div class="divide-y divide-border/50">
								<div class="grid grid-cols-2 pb-2 text-xs font-medium text-muted-foreground uppercase tracking-wide">
									<span>Table</span>
									<span class="text-right">Size</span>
								</div>
								{#each storageStats as entry (entry.tableName)}
									<div class="grid grid-cols-2 py-2.5 text-sm">
										<span class="font-mono text-xs">{entry.tableName}</span>
										<span class="text-right tabular-nums text-muted-foreground">{entry.sizeFormatted}</span>
									</div>
								{/each}
							</div>
						{/if}
					</CardContent>
				</Card>
			</div>

		{/if}
	</div>
</main>
