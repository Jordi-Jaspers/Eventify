<script lang="ts">
	import { Pencil, Trash2, Bell, Lock } from '@lucide/svelte';
	import Button from '$lib/components/ui/button/button.svelte';
	import Badge from '$lib/components/ui/badge/badge.svelte';
	import * as Tooltip from '$lib/components/ui/tooltip';
	import type { SubscriptionResponse } from '$lib/api/models';

	interface Props {
		subscription: SubscriptionResponse;
		onEdit: (sub: SubscriptionResponse) => void;
		onDelete: (sub: SubscriptionResponse) => void;
	}

	const { subscription, onEdit, onDelete }: Props = $props();

	const severityDotColors: Record<string, string> = {
		CRITICAL: 'bg-red-500',
		WARNING: 'bg-amber-500',
		OK: 'bg-green-500',
		NO_DATA: 'bg-gray-400'
	};

	const adapterLabel = $derived(subscription.adapterConfigIds.length === 1 ? '1 adapter' : `${subscription.adapterConfigIds.length} adapters`);
	const isBlocked = $derived(!!subscription.blocked);

	function formatDate(dateStr: string): string {
		const date = new Date(dateStr);
		const diff = Date.now() - date.getTime();
		const days = Math.floor(diff / 86_400_000);
		if (days === 0) return 'Today';
		if (days === 1) return 'Yesterday';
		if (days < 7) return `${days}d ago`;
		if (days < 30) return `${Math.floor(days / 7)}w ago`;
		return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: '2-digit' });
	}

	const createdAtFormatted: string = $derived(formatDate(subscription.createdAt));

	const BLOCKED_TOOLTIP = 'Organization suspended — notifications paused';
</script>

<div class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 px-4 py-3 hover:bg-muted/30 transition-all">
	<!-- Watchlist: 4 cols -->
	<div class="col-span-1 md:col-span-4 flex flex-col gap-0.5 min-w-0">
		<p class="font-medium text-sm truncate">{subscription.watchlistName ?? `Watchlist #${subscription.watchlistId}`}</p>
		<p class="text-xs text-muted-foreground">#{subscription.watchlistId}</p>
	</div>

	<!-- Status: 2 cols -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		{#if isBlocked}
			<Tooltip.Root>
				<Tooltip.Trigger class="rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
					<Badge class="flex items-center gap-1 bg-amber-500/10 border-amber-500/50 text-amber-600">
						<Lock class="h-3 w-3 shrink-0" />
						Blocked
					</Badge>
				</Tooltip.Trigger>
				<Tooltip.Content>{BLOCKED_TOOLTIP}</Tooltip.Content>
			</Tooltip.Root>
		{:else}
			<Badge class="bg-green-500/10 border-green-500/50 text-green-600">Active</Badge>
		{/if}
	</div>

	<!-- Severities: 2 cols -->
	<div class="col-span-1 md:col-span-2 flex flex-wrap items-center gap-1.5">
		{#each subscription.targetSeverities as severity}
			<Tooltip.Root>
				<Tooltip.Trigger class="rounded-full focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" aria-label={severity}>
					<div class="h-2.5 w-2.5 rounded-full {severityDotColors[severity] ?? 'bg-gray-400'}" aria-hidden="true"></div>
				</Tooltip.Trigger>
				<Tooltip.Content>{severity}</Tooltip.Content>
			</Tooltip.Root>
		{/each}
	</div>

	<!-- Adapters: 2 cols -->
	<div class="col-span-1 md:col-span-2 flex items-center gap-1.5">
		<Bell class="h-3.5 w-3.5 text-muted-foreground shrink-0" />
		<p class="text-sm text-muted-foreground">{adapterLabel}</p>
	</div>

	<!-- Created: 1 col -->
	<div class="col-span-1 md:col-span-1 flex items-center">
		<p class="text-sm text-muted-foreground">{createdAtFormatted}</p>
	</div>

	<!-- Actions: 1 col -->
	<div class="col-span-1 md:col-span-1 flex items-center justify-end gap-1">
		<Tooltip.Root>
			<Tooltip.Trigger class="rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
				<Button
					variant="ghost"
					size="icon"
					class="h-8 w-8 text-muted-foreground hover:text-foreground {isBlocked ? 'opacity-50 pointer-events-none' : ''}"
					onclick={() => onEdit(subscription)}
					aria-label="Edit subscription"
					aria-disabled={isBlocked}
				>
					<Pencil class="h-4 w-4" />
				</Button>
			</Tooltip.Trigger>
			{#if isBlocked}
				<Tooltip.Content>{BLOCKED_TOOLTIP}</Tooltip.Content>
			{/if}
		</Tooltip.Root>
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-destructive"
			onclick={() => onDelete(subscription)}
			aria-label="Delete subscription"
		>
			<Trash2 class="h-4 w-4" />
		</Button>
	</div>
</div>
