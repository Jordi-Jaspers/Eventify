<script lang="ts">
	import { TrendingUp, AlertTriangle } from '@lucide/svelte';

	interface Props {
		used: number;
		limit: number;
	}

	let { used, limit }: Props = $props();

	const percentage: number = $derived(Math.min((used / limit) * 100, 100));
	const remaining: number = $derived(Math.max(limit - used, 0));

	// Color thresholds: green → amber → red
	const barColor: string = $derived(
		percentage >= 90
			? 'bg-red-500'
			: percentage >= 75
				? 'bg-amber-500'
				: percentage >= 50
					? 'bg-yellow-500'
					: 'bg-primary'
	);

	const textColor: string = $derived(
		percentage >= 90
			? 'text-red-500'
			: percentage >= 75
				? 'text-amber-500'
				: 'text-muted-foreground'
	);

	const showWarning: boolean = $derived(percentage >= 75);
</script>

<div class="p-4 rounded-lg bg-background/50 border border-border/50 space-y-2">
	<div class="flex items-center justify-between">
		<div class="flex items-center gap-2">
			<TrendingUp class="w-4 h-4 text-primary" />
			<span class="text-sm font-medium">Monthly Quota</span>
		</div>
		<span class="text-sm font-mono {textColor}">
			{used.toLocaleString()} / {limit.toLocaleString()}
		</span>
	</div>

	<!-- Custom progress bar with dynamic color -->
	<div class="h-2 w-full rounded-full bg-muted/50 overflow-hidden">
		<div
			class="h-full rounded-full transition-all duration-500 {barColor}"
			style="width: {percentage}%"
		></div>
	</div>

	<!-- Contextual message -->
	{#if showWarning}
		<div class="flex items-center gap-1.5 {textColor}">
			<AlertTriangle class="w-3.5 h-3.5" />
			<p class="text-xs font-medium">
				{remaining.toLocaleString()} events remaining this month
			</p>
		</div>
	{:else}
		<p class="text-xs text-muted-foreground">
			{percentage.toFixed(1)}% of monthly event quota used
		</p>
	{/if}
</div>
