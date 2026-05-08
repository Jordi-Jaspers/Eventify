<script lang="ts">
	import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { ShieldAlert } from '@lucide/svelte';
	import type { AdminApiKeyAuditResponse } from '$lib/api/models';
	import { formatDate } from '$lib/utils/date';
	import { getScopeBadgeClass, formatNumber } from './utils';

	interface Props {
		revocations: AdminApiKeyAuditResponse[];
		loading: boolean;
	}

	const { revocations, loading }: Props = $props();
</script>

<Card
	class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden ring-1 ring-white/5"
>
	<div
		class="absolute inset-0 bg-gradient-to-br from-destructive/5 via-transparent to-destructive/5 opacity-30"
	></div>
	<CardHeader class="relative z-10">
		<div class="flex items-center gap-2">
			<ShieldAlert class="w-5 h-5 text-destructive" />
			<CardTitle class="text-xl">Recent Revocations</CardTitle>
		</div>
		<CardDescription>Recently revoked API keys and audit information</CardDescription>
	</CardHeader>
	<CardContent class="relative z-10">
		{#if loading}
			<div class="space-y-4">
				{#each Array(3) as _}
					<div class="rounded-lg border border-border/50 bg-card/50 p-4">
						<div class="h-4 bg-muted/50 rounded animate-pulse w-3/4 mb-2"></div>
						<div class="h-3 bg-muted/50 rounded animate-pulse w-1/2"></div>
					</div>
				{/each}
			</div>
		{:else if revocations.length > 0}
			<div class="space-y-3">
				{#each revocations as audit}
					<div
						class="rounded-lg border border-border/50 bg-card/30 p-4 hover:bg-accent/5 transition-colors group"
					>
						<div class="flex items-start justify-between gap-4">
							<div class="flex-1 min-w-0">
								<div class="flex items-center gap-2 mb-2">
									<span class="font-medium truncate">{audit.keyName}</span>
									<Badge
										class="font-mono text-xs group-hover:border-primary/30 transition-colors"
										variant="outline">{audit.keyPrefix}</Badge
									>
									<Badge class={getScopeBadgeClass(audit.scope)}>{audit.scope}</Badge>
								</div>
								<div class="text-sm text-muted-foreground space-y-1">
									<div>
										Owner: {audit.ownerName}
										{#if audit.ownerEmail}
											<span class="text-xs">({audit.ownerEmail})</span>
										{/if}
									</div>
									<div class="flex items-center gap-4 flex-wrap">
										<span>
											Revoked by: {audit.revokedBy?.firstName}
											{audit.revokedBy?.lastName}
											{#if audit.revokedBy?.email}
												<span class="text-xs">({audit.revokedBy.email})</span>
											{/if}
										</span>
										<span>•</span>
										<span>{formatDate(audit.revokedAt ?? '')}</span>
										<span>•</span>
										<span>{formatNumber(audit.totalRequestsAtRevocation)} lifetime requests</span>
									</div>
								</div>
							</div>
						</div>
					</div>
				{/each}
			</div>
		{:else}
			<div class="text-center py-12">
				<div class="inline-flex p-4 rounded-full bg-muted/50 mb-4">
					<ShieldAlert class="h-12 w-12 text-muted-foreground/50" />
				</div>
				<p class="text-muted-foreground font-medium">No recent revocations</p>
				<p class="text-sm text-muted-foreground/70 mt-1">Revoked API keys will appear here</p>
			</div>
		{/if}
	</CardContent>
</Card>
