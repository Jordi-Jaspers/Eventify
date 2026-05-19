<script lang="ts">
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { Key, TrendingUp, Activity, Calendar, Database, Radio } from '@lucide/svelte';
	import type { AdminApiKeyStatsResponse, TableSizeEntry, AdminCountsResponse } from '$lib/api/models';

	interface Props {
		counts: AdminCountsResponse | null;
		apiKeyStats: AdminApiKeyStatsResponse | null;
		storageStats: TableSizeEntry[];
		countsLoading: boolean;
		infraLoading: boolean;
		loadingSkeleton: import('svelte').Snippet<[number, string?]>;
	}

	let { counts, apiKeyStats, storageStats, countsLoading, infraLoading, loadingSkeleton }: Props = $props();
</script>

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
		value={counts?.activeChannels?.toLocaleString() ?? '0'}
		icon={Activity}
		variant="green"
		loading={countsLoading}
	/>
	<StatCard
		title="Paused"
		value={counts?.pausedChannels?.toLocaleString() ?? '0'}
		icon={Radio}
		variant="yellow"
		loading={countsLoading}
	/>
	<StatCard
		title="Stale"
		value={counts?.staleChannels?.toLocaleString() ?? '0'}
		icon={Radio}
		variant="orange"
		loading={countsLoading}
	/>
	<StatCard
		title="Pending Deletion"
		value={counts?.pendingDeletionChannels?.toLocaleString() ?? '0'}
		icon={Radio}
		variant="red"
		loading={countsLoading}
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
