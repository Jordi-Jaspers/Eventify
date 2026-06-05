<script lang="ts">
	import { Pencil, Trash2, Bell } from '@lucide/svelte';
	import Button from '$lib/components/ui/button/button.svelte';
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

	const adapterCount: number = $derived(subscription.adapterConfigIds.length);
	const adapterLabel: string = $derived(adapterCount === 1 ? '1 adapter' : `${adapterCount} adapters`);

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
</script>

<div class="grid grid-cols-1 md:grid-cols-12 gap-2 md:gap-4 px-4 py-3 hover:bg-muted/30 transition-all">
	<!-- Watchlist: 4 cols -->
	<div class="col-span-1 md:col-span-4 flex flex-col gap-0.5">
		<p class="font-medium text-sm">{subscription.watchlistName ?? `Watchlist #${subscription.watchlistId}`}</p>
		<p class="text-xs text-muted-foreground">#{subscription.watchlistId}</p>
	</div>

	<!-- Severities: 3 cols -->
	<div class="col-span-1 md:col-span-3 flex flex-wrap items-center gap-1.5">
		{#each subscription.targetSeverities as severity}
			<Tooltip.Root>
				<Tooltip.Trigger class="rounded-full focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring">
					<div class="h-2.5 w-2.5 rounded-full {severityDotColors[severity] ?? 'bg-gray-400'}" role="img" aria-label={severity}></div>
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

	<!-- Created: 2 cols -->
	<div class="col-span-1 md:col-span-2 flex items-center">
		<p class="text-sm text-muted-foreground">{createdAtFormatted}</p>
	</div>

	<!-- Actions: 1 col -->
	<div class="col-span-1 md:col-span-1 flex items-center justify-end gap-1">
		<Button
			variant="ghost"
			size="icon"
			class="h-8 w-8 text-muted-foreground hover:text-foreground"
			onclick={() => onEdit(subscription)}
			aria-label="Edit subscription"
		>
			<Pencil class="h-4 w-4" />
		</Button>
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
