<script lang="ts">
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { CheckCircle2, AlertTriangle, ListChecks, ChevronRight } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import type { WatchlistHealthResponse } from '$lib/api/models';

	interface Props {
		watchlistHealth: WatchlistHealthResponse[];
	}

	let { watchlistHealth }: Props = $props();

	function getSeverityClass(severity: string): string {
		return severity === 'CRITICAL'
			? 'bg-destructive/20 text-destructive border-destructive/50'
			: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50';
	}
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
	<CardHeader class="flex-row items-center justify-between space-y-0 pb-3">
		<CardTitle class="text-lg flex items-center gap-2">
			<ListChecks class="w-5 h-5 text-primary" />
			Watchlist Health
		</CardTitle>
		<a
			href={CLIENT_ROUTES.MONITOR_PAGE.path}
			class="flex items-center gap-0.5 text-xs text-muted-foreground hover:text-primary transition-colors"
		>
			View all <ChevronRight class="w-3 h-3" />
		</a>
	</CardHeader>
	<CardContent>
		{#if watchlistHealth.length === 0}
			<div class="flex items-center gap-3 py-4 text-green-400">
				<CheckCircle2 class="w-5 h-5 shrink-0" />
				<span class="text-sm font-medium">All watchlists are healthy</span>
			</div>
		{:else}
			<ul class="space-y-2">
				{#each watchlistHealth as item (item.id)}
					<li class="flex items-center justify-between p-3 rounded-lg bg-background/50 border border-border/50">
						<div class="flex items-center gap-2 min-w-0">
							<AlertTriangle class="w-4 h-4 shrink-0 {item.severity === 'CRITICAL' ? 'text-destructive' : 'text-yellow-400'}" />
							<span class="text-sm font-medium truncate">{item.name}</span>
						</div>
						<div class="flex items-center gap-2 shrink-0 ml-2">
							<span class="text-xs text-muted-foreground">{item.channelsInAlert} ch</span>
							<Badge class="text-xs border {getSeverityClass(item.severity)}">
								{item.severity}
							</Badge>
						</div>
					</li>
				{/each}
			</ul>
		{/if}
	</CardContent>
</Card>
