<script lang="ts">
	import { Card, CardContent } from '$lib/components/ui/card';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { Key, AlertTriangle, Clock } from '@lucide/svelte';
	import type { OrgApiKeyStatsResponse } from '$lib/api/models';

	interface Props {
		apiKeyStats: OrgApiKeyStatsResponse;
		loading?: boolean;
	}

	let { apiKeyStats, loading = false }: Props = $props();
</script>

<div class="space-y-4">
	<h2 class="text-lg font-semibold flex items-center gap-2">
		<Key class="h-5 w-5 text-primary" />
		API Key Stats
	</h2>

	<div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
		<StatCard
			title="Revoked This Month"
			value={apiKeyStats.revokedThisMonth?.toLocaleString() ?? '0'}
			icon={AlertTriangle}
			variant="red"
			{loading}
		/>
		<StatCard
			title="Expiring This Month"
			value={apiKeyStats.expiringThisMonth?.toLocaleString() ?? '0'}
			icon={Clock}
			variant="orange"
			{loading}
		/>
		<StatCard
			title="Never Used"
			value={apiKeyStats.neverUsed?.toLocaleString() ?? '0'}
			icon={Key}
			variant="yellow"
			{loading}
		/>
	</div>

	{#if apiKeyStats.topKeys != null && apiKeyStats.topKeys.length > 0}
		<div>
			<h3 class="text-sm font-semibold text-muted-foreground mb-2 uppercase tracking-wide">
				Top API Keys
			</h3>
			<Card class="border-border/50 bg-card/50 backdrop-blur-xl">
				<CardContent class="pt-4">
					<div class="divide-y divide-border/50">
						<div
							class="grid grid-cols-3 pb-2 text-xs font-medium text-muted-foreground uppercase tracking-wide"
						>
							<span>Name</span>
							<span>Suffix</span>
							<span class="text-right">Requests</span>
						</div>
						{#each apiKeyStats.topKeys as key (key.suffix)}
							<div class="grid grid-cols-3 py-2.5 text-sm">
								<span class="font-medium truncate pr-4">{key.name}</span>
								<span class="text-muted-foreground font-mono text-xs truncate pr-4">...{key.suffix}</span>
								<span class="text-right tabular-nums">{key.totalRequests.toLocaleString()}</span>
							</div>
						{/each}
					</div>
				</CardContent>
			</Card>
		</div>
	{/if}
</div>
